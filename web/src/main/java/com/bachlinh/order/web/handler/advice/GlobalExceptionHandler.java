package com.bachlinh.order.web.handler.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.RouteExceptionHandler;
import com.bachlinh.order.core.http.handler.ExceptionHandler;

@RouteExceptionHandler
@ActiveReflection
public class GlobalExceptionHandler extends ExceptionHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @ActiveReflection
    public GlobalExceptionHandler() {
    }

    @Override
    protected int status() {
        return HttpStatus.SERVICE_UNAVAILABLE.value();
    }

    @Override
    protected String[] message(Exception exception) {
        return new String[]{"Server under maintenance please try again"};
    }

    @Override
    protected void doOnException(Exception exception) {
        log.error("Problem when handle request !", exception);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Exception>[] activeOnTypes() {
        return new Class[]{Exception.class};
    }

    @Override
    public boolean isErrorHandler() {
        return false;
    }
}
