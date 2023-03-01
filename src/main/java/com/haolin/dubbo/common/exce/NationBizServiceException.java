package com.haolin.dubbo.common.exce;

import com.haolin.haotool.i18n.Messages;

/**
 * 国际化通用异常
 */
public class NationBizServiceException extends RuntimeException {
    public NationBizServiceException(String errorCode, Object... args) {
        super(Messages.getMessage(errorCode, args));
    }


    public NationBizServiceException(Throwable throwable, String errorCode, Object... args) {
        super(Messages.getMessage(errorCode, args),throwable);
    }


}