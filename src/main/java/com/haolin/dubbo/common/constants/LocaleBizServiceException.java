package com.haolin.dubbo.common.constants;

import com.haolin.haotool.i18n.Messages;

public class LocaleBizServiceException extends RuntimeException {
    public LocaleBizServiceException(String errorCode, Object... args) {
        super(Messages.getMessage(errorCode, args));
    }


    public LocaleBizServiceException(Throwable throwable, String errorCode, Object... args) {
        super(Messages.getMessage(errorCode, args),throwable);
    }


}