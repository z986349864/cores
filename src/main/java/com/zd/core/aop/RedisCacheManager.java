package com.zd.core.aop;


import com.alibaba.fastjson.JSON;
import com.zd.core.annotation.RedisCache;
import com.zd.core.exception.BusinessException;
import com.zd.core.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis缓存切面
 */

@Aspect
@Component
@Slf4j
public class RedisCacheManager {

    @Autowired
    private RedisUtil redisUtils;

    /**
     * 切入点：@RedisCache
     */
    @Pointcut("@annotation(com.zd.core.annotation.RedisCache)")
    public void annotationPointCut() {
    }

    /**
     * 环绕通知
     *
     * @param joinPoint
     * @return
     */
    @Around("annotationPointCut();")
    public Object cache(ProceedingJoinPoint joinPoint) {
        try {
            Method method = getMethod(joinPoint);
            RedisCache redisCacheAnnotation = method.getAnnotation(RedisCache.class);
            if (redisCacheAnnotation != null && redisCacheAnnotation.use()) {
                /**
                 * 获取@RedisCache key属性，没有设置使用类型+方法名+参数作为缓存key
                 */
                String key = generateKey(redisCacheAnnotation.key(), method, joinPoint);
                log.info("generate cache key:" + key);
                /**
                 * 缓存删除条件表达式 flush属性
                 */
                String flushExpression = redisCacheAnnotation.flush();
                boolean isEvict = false;
                if (StringUtils.isNotBlank(flushExpression)) {
                    isEvict = (boolean) spelParse(flushExpression, method, joinPoint.getArgs());
                }
                if (isEvict) {
                    log.info("flush by spel :" + flushExpression);
                    redisUtils.del(key);
                }
                Object value = redisUtils.get(key, Object.class);
                if (value == null) {
                    log.info("缓存没有命中...");
                    log.info("执行方法，重新设置缓存...");
                    Object result = joinPoint.proceed();
                    //TODO put cache
                    if (result != null) {
                        setCache(redisCacheAnnotation, key, result);
                    }

                    return result;
                } else {
                    log.info("缓存命中返回数据..." + JSON.toJSONString(value));
                    Type type = method.getReturnType();
                    if (value instanceof List) {
                        type = method.getGenericReturnType();
                    }
                    return JSON.parseObject(value.toString(), type);
                }
            }
            log.info("不使用缓存...");
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new BusinessException(throwable.getMessage());
        }
    }


    private boolean setCache(RedisCache redisCacheAnnotation, String key, Object value) {
        try {
            TimeUnit timeUnit = redisCacheAnnotation.timeUnit();
            int time = (int) timeUnit.toSeconds(redisCacheAnnotation.time());
            log.info(String.format("放入缓存 : %1$s \t 缓存时间 ： %2$s", JSON.toJSONString(value), time));
            redisUtils.set(key, value, time);
        } catch (Exception e) {
            log.error(String.format("缓存设置失败:%s", e.getMessage()));
            return false;
        }
        return true;
    }

    private String generateKey(String key, Method method, ProceedingJoinPoint joinPoint) {
        if (StringUtils.isBlank(key)) {
            StringBuilder generationKey = new StringBuilder();
            generationKey.append(joinPoint.getTarget().getClass().getName()).append(":").append(method.getName());
            for (Object args : joinPoint.getArgs()) {
                generationKey.append(":").append(String.valueOf(args));
            }
            key = generationKey.toString();
        } else {
            key = (String) spelParse(key, method, joinPoint.getArgs());
        }
        return key;
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    private Object spelParse(String spel, Method method, Object[] args) {
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        spel = StringUtils.replace(spel, "#methodName", "'" + method.getName() + "'");
        spel = StringUtils.replace(spel, "#simpleClassName", "'" + method.getDeclaringClass().getSimpleName() + "'");
        if (StringUtils.contains(spel, "#")) {
            LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
            String[] parameterNames = discoverer.getParameterNames(method);
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return parser.parseExpression(spel).getValue(context);
    }

}