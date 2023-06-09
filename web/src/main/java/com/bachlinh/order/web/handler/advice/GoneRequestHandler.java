package com.bachlinh.order.web.handler.advice;

import org.springframework.http.HttpStatus;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.RouteExceptionHandler;
import com.bachlinh.order.core.http.handler.ExceptionHandler;
import com.bachlinh.order.exception.http.TemporaryTokenExpiredException;

@RouteExceptionHandler
@ActiveReflection
public class GoneRequestHandler extends ExceptionHandler {

    @ActiveReflection
    public GoneRequestHandler() {
        // Do nothing
    }

    @Override
    protected int status() {
        return HttpStatus.GONE.value();
    }

    @Override
    protected String[] message(Exception exception) {
        return new String[]{exception.getMessage()};
    }

    @Override
    protected void doOnException(Exception exception) {
        //Do nothing
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Exception>[] activeOnTypes() {
        return new Class[]{TemporaryTokenExpiredException.class};
    }

    @Override
    public boolean isErrorHandler() {
        return false;
    }
}
