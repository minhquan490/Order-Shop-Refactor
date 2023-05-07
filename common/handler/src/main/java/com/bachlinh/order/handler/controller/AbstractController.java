package com.bachlinh.order.handler.controller;

import org.springframework.http.ResponseEntity;
import com.bachlinh.order.annotation.RouteProvider;
import com.bachlinh.order.core.NativeMethodHandleRequestMetadataReader;
import com.bachlinh.order.core.http.NativeRequest;
import com.bachlinh.order.core.http.NativeResponse;
import com.bachlinh.order.core.http.Payload;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.service.container.ContainerWrapper;
import com.bachlinh.order.service.container.DependenciesContainerResolver;
import com.bachlinh.order.utils.map.LinkedMultiValueMap;

import java.lang.reflect.Method;

public abstract class AbstractController<T, U> implements Controller<T, U> {
    private NativeRequest<U> request;
    private NativeResponse<T> response;
    private DependenciesContainerResolver containerResolver;
    private Environment environment;

    public boolean isController() {
        return this.getClass().isAnnotationPresent(RouteProvider.class);
    }

    public void setWrapper(ContainerWrapper wrapper, String profile) {
        this.containerResolver = DependenciesContainerResolver.buildResolver(wrapper.unwrap(), profile);
        this.environment = Environment.getInstance(profile);
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.getName().equals("internalHandler")) {
                NativeMethodHandleRequestMetadataReader.getInstance().defineMetadata(getPath(), method);
            }
        }
    }

    @Override
    public void setNativeRequest(NativeRequest<U> request) {
        this.request = request;
    }

    @Override
    public void setNativeResponse(NativeResponse<T> response) {
        this.response = response;
    }

    @Override
    public NativeRequest<U> getNativeRequest() {
        return request;
    }

    @Override
    public NativeResponse<T> getNativeResponse() {
        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final NativeResponse<T> handle(NativeRequest<U> request) {
        inject();
        T returnValue = internalHandler(request.getBody());
        if (ResponseEntity.class.isAssignableFrom(returnValue.getClass())) {
            ResponseEntity<T> res = (ResponseEntity<T>) returnValue;
            return getNativeResponse().merge((NativeResponse<T>) NativeResponse
                    .builder()
                    .statusCode(res.getStatusCode().value())
                    .body(res.getBody())
                    .headers(new LinkedMultiValueMap<>(res.getHeaders()))
                    .build());
        }
        if (NativeResponse.class.isAssignableFrom(returnValue.getClass())) {
            return getNativeResponse().merge((NativeResponse<T>) returnValue);
        }
        return getNativeResponse().merge((NativeResponse<T>) NativeResponse
                .builder()
                .activePushBuilder(getNativeResponse().isActivePushBuilder())
                .statusCode(getNativeResponse().getStatusCode() <= 0 ? 200 : getNativeResponse().getStatusCode())
                .body(returnValue)
                .build());
    }

    protected abstract T internalHandler(Payload<U> request);

    protected abstract void inject();

    protected DependenciesContainerResolver getContainerResolver() {
        return containerResolver;
    }

    protected Environment getEnvironment() {
        return environment;
    }
}
