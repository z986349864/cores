package com.zd.core.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocaleMessageSource {

    @Autowired
    private MessageSource messageSource;

    /*public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }*/

    /**
     * @param code ：对应messages配置的key.
     * @return
     */
    public String getMessage(String code) {
        return this.getMessage(code, new Object[]{});
    }

    public String getMessage(String code, String defaultMessage) {
        return this.getMessage(code, null, defaultMessage);
    }

    public String getMessage(String code, String defaultMessage, Locale locale) {
        return this.getMessage(code, null, defaultMessage, locale);
    }

    public String getMessage(String code, Locale locale) {
        return this.getMessage(code, null, code, locale);
    }

    /**
     * @param code ：对应messages配置的key.
     * @param args : 数组参数.
     * @return
     */
    public String getMessage(String code, Object[] args) {
        return this.getMessage(code, args, code);
    }

    public String getMessage(String code, Object[] args, Locale locale) {
        return this.getMessage(code, args, code, locale);
    }

    /**
     * @param code           ：对应messages配置的key.
     * @param args           : 数组参数.
     * @param defaultMessage : 没有设置key的时候的默认值.
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return this.getMessage(code, args, defaultMessage, locale);
    }

    /**
     * 指定语言.
     *
     * @param code
     * @param args
     * @param defaultMessage
     * @param locale
     * @return
     */
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}