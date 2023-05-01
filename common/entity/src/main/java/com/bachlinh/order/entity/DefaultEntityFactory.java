package com.bachlinh.order.entity;

import com.bachlinh.order.annotation.EnableFullTextSearch;
import com.bachlinh.order.core.scanner.ApplicationScanner;
import com.bachlinh.order.entity.context.internal.DefaultEntityContext;
import com.bachlinh.order.entity.context.spi.EntityContext;
import com.bachlinh.order.entity.index.internal.InternalProvider;
import com.bachlinh.order.entity.index.spi.SearchManager;
import com.bachlinh.order.entity.index.spi.SearchManagerFactory;
import com.bachlinh.order.entity.model.BaseEntity;
import com.bachlinh.order.entity.transaction.internal.DefaultTransactionManager;
import com.bachlinh.order.entity.transaction.spi.EntityTransactionManager;
import com.bachlinh.order.environment.Environment;
import com.bachlinh.order.service.container.ContainerWrapper;
import com.bachlinh.order.service.container.DependenciesContainerResolver;
import com.bachlinh.order.service.container.DependenciesResolver;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public final class DefaultEntityFactory implements EntityFactory {

    private final Map<Class<?>, EntityContext> entityContext;
    private final DependenciesResolver dependenciesResolver;
    private final EntityTransactionManager transactionManager;
    private final String activeProfile;

    private DefaultEntityFactory(Map<Class<?>, EntityContext> entityContext, DependenciesResolver dependenciesResolver, String activeProfile) {
        this.entityContext = entityContext;
        this.dependenciesResolver = dependenciesResolver;
        this.transactionManager = new DefaultTransactionManager();
        this.activeProfile = activeProfile;
    }

    @Override
    public <T> boolean containsEntity(Class<T> entityType) {
        return entityContext.containsKey(entityType);
    }

    @Override
    public <T> T getEntity(Class<T> entityType) {
        if (containsEntity(entityType)) {
            return entityType.cast(entityContext.get(entityType).getEntity());
        }
        return null;
    }

    @Override
    public EntityContext getEntityContext(Class<?> entityType) {
        return entityContext.get(entityType);
    }

    @Override
    public EntityTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public DependenciesResolver getResolver() {
        return dependenciesResolver;
    }

    @Override
    public String activeProfile() {
        return activeProfile;
    }

    public static class DefaultEntityFactoryBuilder implements EntityFactoryBuilder {
        private String activeProfile;
        private ContainerWrapper containerWrapper;

        @Override
        public EntityFactoryBuilder profile(String profile) {
            this.activeProfile = profile;
            return this;
        }

        @Override
        public EntityFactoryBuilder container(ContainerWrapper wrapper) {
            this.containerWrapper = wrapper;
            return this;
        }

        @Override
        public EntityFactory build() throws IOException {
            DependenciesContainerResolver containerResolver = DependenciesContainerResolver.buildResolver(containerWrapper.unwrap(), activeProfile);
            Map<Class<?>, EntityContext> entityContext = new HashMap<>();
            Collection<Class<?>> entities = new ApplicationScanner()
                    .findComponents()
                    .stream()
                    .filter(BaseEntity.class::isAssignableFrom)
                    .toList();
            SearchManager manager = buildSearchManager(entities, containerWrapper, activeProfile);
            entities.forEach(entity -> entityContext.put(entity, new DefaultEntityContext(entity, containerResolver.getDependenciesResolver(), manager)));
            return new DefaultEntityFactory(entityContext, containerResolver.getDependenciesResolver(), activeProfile);
        }

        private SearchManager buildSearchManager(Collection<Class<?>> entities, ContainerWrapper wrapper, String activeProfile) throws IOException {
            DependenciesContainerResolver containerResolver = DependenciesContainerResolver.buildResolver(wrapper.unwrap(), activeProfile);
            Environment environment = Environment.getInstance(activeProfile);
            SearchManagerFactory.Builder builder = InternalProvider.useDefaultSearchManagerFactoryBuilder();
            builder.indexFilePath(environment.getProperty("server.index.path"));
            builder.stopWordFilePath(environment.getProperty("server.stop-word.path"));
            List<Class<?>> entitiesForIndex = entities.stream()
                    .filter(entity -> entity.isAnnotationPresent(EnableFullTextSearch.class))
                    .toList();
            builder.entities(entitiesForIndex.toArray(new Class[0]));
            builder.indexNames(entitiesForIndex.stream()
                    .map(clazz -> clazz.getSimpleName().toLowerCase())
                    .toList()
                    .toArray(new String[0]));
            builder.threadPool(containerResolver.getDependenciesResolver().resolveDependencies(ThreadPoolTaskExecutor.class));
            return builder.build().obtainManager();
        }
    }
}
