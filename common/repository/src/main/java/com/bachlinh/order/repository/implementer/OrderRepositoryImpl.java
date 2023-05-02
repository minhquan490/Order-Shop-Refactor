package com.bachlinh.order.repository.implementer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.annotation.RepositoryComponent;
import com.bachlinh.order.entity.model.Customer;
import com.bachlinh.order.entity.model.Order;
import com.bachlinh.order.entity.model.OrderDetail_;
import com.bachlinh.order.entity.model.Order_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.OrderRepository;
import com.bachlinh.order.service.container.DependenciesContainerResolver;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RepositoryComponent
@ActiveReflection
public class OrderRepositoryImpl extends AbstractRepository<Order, String> implements OrderRepository {

    @DependenciesInitialize
    @ActiveReflection
    public OrderRepositoryImpl(DependenciesContainerResolver containerResolver) {
        super(Order.class, containerResolver.getDependenciesResolver());
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public Order saveOrder(Order order) {
        return Optional.of(this.save(order)).orElse(null);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public Order updateOrder(Order order) {
        return saveOrder(order);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public boolean deleteOrder(Order order) {
        if (order == null) {
            return false;
        }
        if (existsById(order.getId())) {
            delete(order);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Order getOrder(String orderId) {
        Specification<Order> spec = Specification.where((root, query, criteriaBuilder) -> {
            root.join(Order_.orderStatus, JoinType.INNER);
            root.join(Order_.orderDetails, JoinType.INNER).join(OrderDetail_.product, JoinType.INNER);
            return criteriaBuilder.equal(root.get(Order_.id), orderId);
        });
        return findOne(spec).orElse(null);
    }

    @Override
    public List<Order> getOrderOfCustomer(Customer customer) {
        Specification<Order> spec = Specification.where((root, query, criteriaBuilder) -> {
            root.join(Order_.orderStatus, JoinType.INNER);
            root.join(Order_.orderDetails, JoinType.INNER).join(OrderDetail_.product, JoinType.INNER);
            return criteriaBuilder.equal(root.get(Order_.customer), customer);
        });
        return findAll(spec);
    }

    @Override
    public List<Order> getNewOrdersInDate() {
        LocalDate now = LocalDate.now();
        Specification<Order> spec = Specification.where((root, query, criteriaBuilder) -> {
            Predicate firstStatement = criteriaBuilder.greaterThanOrEqualTo(root.get(Order_.timeOrder), Timestamp.valueOf(LocalDateTime.of(now, LocalTime.of(0, 0, 0))));
            Predicate secondStatement = criteriaBuilder.lessThanOrEqualTo(root.get(Order_.timeOrder), Timestamp.valueOf(LocalDateTime.of(now, LocalTime.of(23, 59, 59))));
            return criteriaBuilder.and(firstStatement, secondStatement);
        });
        return findAll(spec, Sort.unsorted());
    }

    @Override
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
