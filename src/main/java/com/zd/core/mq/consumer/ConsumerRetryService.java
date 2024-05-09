package com.zd.core.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.zd.core.constant.ApplicationConstants;
import com.zd.core.exception.BusinessException;
import com.zd.core.mq.failretry.entity.FailRetryEntity;
import com.zd.core.mq.failretry.entity.IRetryResultProcessStrategy;
import com.zd.core.mq.failretry.service.IFailRetryService;
import com.zd.core.utils.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zd
 * @Title: ConsumerRetryService
 * @Description: 消息消费失败重试
 */

@Service
public class ConsumerRetryService implements IConsumerRetryService, ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(ConsumerRetryService.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    private IFailRetryService failRetryService;
    /**
     * 所有的消费者
     * K failRetryType
     * V 当前消费者的class
     */
    private static final ConcurrentHashMap<Integer, Class<? extends IConsumer>> businessConsumerContainer = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<Integer, Class<? extends IConsumer>> getBusinessConsumerContainer() {
        return businessConsumerContainer;
    }

    /**
     * 消费失败重试
     *
     * @param lastSendTime
     * @param thisTime
     * @param businessType
     * @return void
     * @author clf
     * @date 2020/6/11
     */
    @Override
    public void retry(Long lastSendTime, Long thisTime, Integer businessType) {

        List<FailRetryEntity> failRetryEntities = failRetryService.listNeedRetry(businessType, lastSendTime, thisTime);
        if (CollectionUtils.isEmpty(failRetryEntities)) {
            return;
        }
        IRetryResultProcessStrategy iRetryResultProcessStrategy = null;
        IConsumer iConsumer = null;
        logger.info("consumer fail retry start businessType:{},lastSendTime:{},startTime:{}", businessType, lastSendTime, thisTime);
        for (FailRetryEntity failRetryEntity : failRetryEntities) {
            try {
                iConsumer = resolveConsumer(businessType);
                //获取当前消费者的已注册的 重试策略
                iRetryResultProcessStrategy = iConsumer.registeredRetryResultProcessStrategy();

                ResolvableType resolvableType = ResolvableType.forType(ResolvableType.forClass(iConsumer.getClass()).getSuperType().getType());
                Class<?> clazz = Objects.equals(resolvableType.resolve(), AbstractBaseConsumer.class) ?
                        resolvableType.resolveGeneric(0)
                        : resolvableType.getSuperType().resolveGeneric(0);
                if (clazz == null) {
                    throw new RuntimeException("can't be find this consumer generic type");
                }
                if (Collection.class.isAssignableFrom(clazz)) {
                    Class<?> actualClazz = Objects.equals(resolvableType.resolve(), AbstractBaseConsumer.class) ?
                            resolvableType.resolveGeneric(0, 0)
                            : resolvableType.getSuperType().resolveGeneric(0, 0);
                    if (actualClazz == null) {
                        throw new BusinessException("this message generic type not support");
                    }
                    Object listData = JSON.parseArray(failRetryEntity.getParams(), actualClazz);
                    iConsumer.consumer(listData);
                } else {
                    Object data = JSON.parseObject(failRetryEntity.getParams(), clazz);
                    iConsumer.consumer(data);
                }
                failRetryEntity.setStatus(ApplicationConstants.SUCCESS);
                failRetryEntity.setRetryTimeStrategy(iConsumer.getNextRetryTimeStrategy());
                failRetryService.processResult(failRetryEntity, iRetryResultProcessStrategy);
            } catch (Exception e) {
                logger.error("consumer retry failed businessType:" + businessType, e);
                failRetryEntity.setStatus(ApplicationConstants.FAIL);
                failRetryEntity.setFailReason(StringUtils.isBlank(e.getMessage()) ? ExceptionUtils.getStack(e, 4000) : e.getMessage());
                if (iConsumer != null) {
                    failRetryEntity.setRetryTimeStrategy(iConsumer.getNextRetryTimeStrategy());
                }
                failRetryService.processResult(failRetryEntity, iRetryResultProcessStrategy);
            }
        }
    }

    /**
     * 找到当前的消费类型
     *
     * @param businessType
     * @return
     */
    public IConsumer resolveConsumer(Integer businessType) {
        return this.applicationContext.getBean(businessConsumerContainer.get(businessType));
    }
}
