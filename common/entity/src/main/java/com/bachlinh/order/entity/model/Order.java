package com.bachlinh.order.entity.model;

import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.Label;
import com.bachlinh.order.annotation.Trigger;
import com.bachlinh.order.annotation.Validator;
import com.google.common.base.Objects;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "ORDERS", indexes = @Index(name = "idx_order_customer", columnList = "CUSTOMER_ID"))
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "order")
@Label("ODR-")
@Validator(validators = "com.bachlinh.order.core.entity.validator.internal.OrderValidator")
@Trigger(triggers = {"com.bachlinh.order.core.entity.trigger.internal.OrderHistoryTrigger", "com.bachlinh.order.core.entity.trigger.internal.NewOrderPushingTrigger"})
@ActiveReflection
public class Order extends AbstractEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false, columnDefinition = "varchar(32)")
    private String id;

    @Column(name = "DEPOSITED", nullable = false, columnDefinition = "bit")
    private boolean deposited;

    @Column(name = "TOTAL_DEPOSIT", nullable = false)
    private int totalDeposit;

    @Column(name = "ORDER_TIME", nullable = false)
    private Timestamp timeOrder;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "ORDER_STATUS_ID", unique = true, nullable = false, updatable = false)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, updatable = false)
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "order", orphanRemoval = true)
    private Collection<OrderDetail> orderDetails;

    @ActiveReflection
    Order() {
    }

    @Override
    @ActiveReflection
    public void setId(Object id) {
        if (!(id instanceof String)) {
            throw new PersistenceException("Id of order must be string");
        }
        this.id = (String) id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equal(getId(), order.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @ActiveReflection
    public String getId() {
        return this.id;
    }

    @ActiveReflection
    public boolean isDeposited() {
        return this.deposited;
    }

    @ActiveReflection
    public int getTotalDeposit() {
        return this.totalDeposit;
    }

    @ActiveReflection
    public Timestamp getTimeOrder() {
        return this.timeOrder;
    }

    @ActiveReflection
    public OrderStatus getOrderStatus() {
        return this.orderStatus;
    }

    @ActiveReflection
    public Customer getCustomer() {
        return this.customer;
    }

    @ActiveReflection
    public Collection<OrderDetail> getOrderDetails() {
        return this.orderDetails;
    }

    @ActiveReflection
    public void setDeposited(boolean deposited) {
        this.deposited = deposited;
    }

    @ActiveReflection
    public void setTotalDeposit(int totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    @ActiveReflection
    public void setTimeOrder(Timestamp timeOrder) {
        this.timeOrder = timeOrder;
    }

    @ActiveReflection
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    @ActiveReflection
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @ActiveReflection
    public void setOrderDetails(Collection<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
}