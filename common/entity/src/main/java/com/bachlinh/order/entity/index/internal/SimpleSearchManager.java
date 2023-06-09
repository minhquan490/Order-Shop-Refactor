package com.bachlinh.order.entity.index.internal;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.bachlinh.order.annotation.EnableFullTextSearch;
import com.bachlinh.order.entity.EntityProxyFactory;
import com.bachlinh.order.entity.index.spi.EntityIndexer;
import com.bachlinh.order.entity.index.spi.EntitySearcher;
import com.bachlinh.order.entity.index.spi.MetadataFactory;
import com.bachlinh.order.entity.index.spi.SearchManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class SimpleSearchManager implements SearchManager {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<Class<?>, EntityIndexer> indexerMap;
    private final ThreadPoolTaskExecutor executor;
    private final EntityProxyFactory entityProxyFactory;

    SimpleSearchManager(Map<Class<?>, DirectoryHolder> directoryMap, IndexWriterConfig indexWriterConfig, ThreadPoolTaskExecutor executor, EntityProxyFactory entityProxyFactory) {
        this.executor = executor;
        this.entityProxyFactory = entityProxyFactory;
        indexerMap = buildIndexer(directoryMap, indexWriterConfig);
    }

    @Override
    public Directory getDirectory(Class<?> entity) {
        try {
            return ((EntityOperation) indexerMap.get(entity)).open();
        } catch (IOException e) {
            log.warn("Can not open directory for entity [{}]", entity.getName());
            return null;
        }
    }

    @Override
    public void analyze(Object entity) {
        if (!entity.getClass().isAnnotationPresent(EnableFullTextSearch.class)) {
            return;
        }
        indexerMap.get(entity.getClass()).index(entity);
    }

    @Override
    public void analyze(Collection<Object> entities) {
        executor.execute(() -> entities.forEach(this::analyze));
    }

    @Override
    public Collection<String> search(Class<?> entity, String keyword) {
        if (!entity.isAnnotationPresent(EnableFullTextSearch.class)) {
            return Collections.emptyList();
        }
        return ((EntitySearcher) indexerMap.get(entity)).search(keyword);
    }

    private Map<Class<?>, EntityIndexer> buildIndexer(Map<Class<?>, DirectoryHolder> directoryMap, IndexWriterConfig indexWriterConfig) {
        Map<Class<?>, EntityIndexer> result = new HashMap<>();
        MetadataFactory metadataFactory = new ProxyMetadataFactory(entityProxyFactory);
        Map<String, IndexWriter> sharedIndexWriter = new HashMap<>();
        directoryMap.forEach((entity, directoryHolder) -> {
            EntityOperation entityOperation = new EntityOperation(metadataFactory, indexWriterConfig, directoryHolder, new SimpleFieldDescriptor(entity));
            if (sharedIndexWriter.containsKey(directoryHolder.getDirectoryPath())) {
                entityOperation.setIndexWriter(sharedIndexWriter.get(directoryHolder.getDirectoryPath()));
            } else {
                try {
                    IndexWriter indexWriter = entityOperation.buildIndexWriter();
                    sharedIndexWriter.put(directoryHolder.getDirectoryPath(), indexWriter);
                    entityOperation.setIndexWriter(indexWriter);
                } catch (IOException e) {
                    log.warn("Create index writer failure");
                }
            }
            result.put(entity, entityOperation);
        });
        return result;
    }
}
