package com.haolin.haotool.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 国际化工具类
 */
public class MessageUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);

    /**
     * 国际化
     */
    public static String getMessage(String result, Object[] params, MessageSource messageSource) {
        String message = "";
        try {
            Locale locale = LocaleContextHolder.getLocale();
            message = messageSource.getMessage(result, params, locale);
        } catch (Exception e) {
            LOGGER.error("parse message error! ", e);
        }
        return message;
    }

    /**
     * 国际化
     * 注：通过@Autowired private MessageSource messageSource无法获取
     */
    public static String getMessage(String result, Object... params) {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setCacheSeconds(-1);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setBasenames("/i18n/messages");

        String message = "";
        try {
            Locale locale = LocaleContextHolder.getLocale();
            message = messageSource.getMessage(result, params, locale);
        } catch (Exception e) {
            LOGGER.error("parse message error! ", e);
        }
        return message;
    }

}