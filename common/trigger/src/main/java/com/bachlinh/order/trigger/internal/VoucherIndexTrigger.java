package com.bachlinh.order.trigger.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.entity.EntityFactory;
import com.bachlinh.order.entity.enums.TriggerExecution;
import com.bachlinh.order.entity.enums.TriggerMode;
import com.bachlinh.order.entity.model.Voucher;
import com.bachlinh.order.service.container.DependenciesResolver;
import com.bachlinh.order.trigger.spi.AbstractTrigger;

@ActiveReflection
public class VoucherIndexTrigger extends AbstractTrigger<Voucher> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private EntityFactory entityFactory;

    @ActiveReflection
    public VoucherIndexTrigger(DependenciesResolver dependenciesResolver) {
        super(dependenciesResolver);
    }

    @Override
    public TriggerMode getMode() {
        return TriggerMode.AFTER;
    }

    @Override
    public TriggerExecution[] getExecuteOn() {
        return new TriggerExecution[]{TriggerExecution.ON_INSERT, TriggerExecution.ON_UPDATE};
    }

    @Override
    protected void doExecute(Voucher entity) {
        if (log.isDebugEnabled()) {
            log.debug("BEGIN: Index Voucher with name [{}]", entity.getName());
        }
        entityFactory.getEntityContext(Voucher.class).analyze(entity);
        if (log.isDebugEnabled()) {
            log.debug("END: Index Voucher with name [{}] done", entity.getName());
        }
    }

    @Override
    protected void inject() {
        if (entityFactory == null) {
            entityFactory = getDependenciesResolver().resolveDependencies(EntityFactory.class);
        }
    }

    @Override
    protected String getTriggerName() {
        return "voucherIndexTrigger";
    }
}
