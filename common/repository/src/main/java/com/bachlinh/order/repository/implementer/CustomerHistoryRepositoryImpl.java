package com.bachlinh.order.repository.implementer;

import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.entity.model.CustomerHistory;
import com.bachlinh.order.entity.model.CustomerHistory_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.CustomerHistoryRepository;
import com.bachlinh.order.service.container.DependenciesContainerResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Collection;

@Repository
@ActiveReflection
class CustomerHistoryRepositoryImpl extends AbstractRepository<CustomerHistory, Integer> implements CustomerHistoryRepository {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CustomerHistoryRepositoryImpl.class);

    @Autowired
    @ActiveReflection
    CustomerHistoryRepositoryImpl(DependenciesContainerResolver containerResolver) {
        super(CustomerHistory.class, containerResolver.getDependenciesResolver());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public CustomerHistory saveCustomerHistory(CustomerHistory customerHistory) {
        return this.save(customerHistory);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.MANDATORY)
    public boolean deleteCustomerHistory(CustomerHistory customerHistory) {
        if (customerHistory == null) {
            return false;
        }
        if (existsById(customerHistory.getId())) {
            delete(customerHistory);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.READ_COMMITTED)
    public boolean deleteAll(Collection<CustomerHistory> histories) {
        try {
            this.deleteAllInBatch(histories);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Collection<CustomerHistory> getHistories(Customer customer) {
        Specification<CustomerHistory> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(CustomerHistory_.customer), customer));
        return findAll(spec);
    }

    @Override
    public Collection<CustomerHistory> getHistoriesExpireNow(Date now) {
        Specification<CustomerHistory> spec = Specification.where(((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(CustomerHistory_.removeTime), now)));
        return findAll(spec);
    }

    @Override
    @PersistenceContext
    @ActiveReflection
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
