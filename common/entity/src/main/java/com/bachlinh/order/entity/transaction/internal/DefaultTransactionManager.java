package com.bachlinh.order.entity.transaction.internal;

import com.bachlinh.order.entity.transaction.spi.AbstractTransactionManager;
import com.bachlinh.order.entity.transaction.spi.EntitySavePointManager;

public class DefaultTransactionManager extends AbstractTransactionManager {
    private final EntitySavePointManager savePointManager;

    public DefaultTransactionManager() {
        this.savePointManager = new DefaultSavePointManager();
    }

    @Override
    public EntitySavePointManager getSavePointManager() {
        return savePointManager;
    }

    @Override
    public boolean hasSavePoint() {
        return savePointManager.hasSavePoint();
    }
}
