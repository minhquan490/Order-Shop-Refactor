package com.bachlinh.order.dto.proxy;

public interface DtoProxyFactory {
    <T, U> T createProxy(U source, Class<T> receiverType);

    boolean canCreate(Class<?> type);

    void registerProxy(Class<?> type, Proxy<?, ?> proxy);

    static DtoProxyFactory defaultInstance() {
        return new DefaultDtoProxyFactory();
    }
}
