package com.bachlinh.order.service;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.service.container.ContainerWrapper;
import com.bachlinh.order.service.container.DependenciesContainerResolver;

import java.util.Objects;
import java.util.concurrent.Executor;

public abstract class AbstractService<T, U> implements BaseService<T, U> {

    private final Executor executor;
    private final DependenciesContainerResolver containerResolver;
    private final Environment environment;

    protected AbstractService(Executor executor, ContainerWrapper wrapper, String profile) {
        this.executor = executor;
        this.containerResolver = DependenciesContainerResolver.buildResolver(wrapper.unwrap(), profile);
        this.environment = Environment.getInstance(profile);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Result<T> save(Form<U> form) {
        inject();
        return new InternalResult<>(doSave(form.get()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Result<T> delete(Form<U> form) {
        inject();
        return new InternalResult<>(doDelete(form.get()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Result<T> update(Form<U> form) {
        inject();
        return new InternalResult<>(doUpdate(form.get()));
    }

    @Override
    public Result<T> getOne(Form<U> form) {
        inject();
        return new InternalResult<>(doGetOne(form.get()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X extends Iterable<T>> Result<X> getList(Form<U> form) {
        inject();
        return new InternalResult<>(doGetList(form.get()));
    }

    protected DependenciesContainerResolver getContainerResolver() {
        return containerResolver;
    }

    protected Environment getEnvironment() {
        return environment;
    }

    protected Executor getExecutor() {
        return executor;
    }

    protected abstract T doSave(U param);

    protected abstract T doUpdate(U param);

    protected abstract T doDelete(U param);

    protected abstract T doGetOne(U param);

    protected abstract <K, X extends Iterable<K>> X doGetList(U param);

    protected abstract void inject();

    private record InternalResult<T>(T wappedResult) implements Result<T> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InternalResult<?> that)) return false;
            return Objects.equals(wappedResult, that.wappedResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wappedResult);
        }

        @Override
        public T get() {
            return wappedResult;
        }
    }
}
