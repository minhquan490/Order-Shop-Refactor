package com.bachlinh.order.entity.model;

import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.Trigger;

import java.sql.Timestamp;

@Entity
@Table(
        name = "DIRECT_MESSAGE",
        indexes = {
                @Index(name = "idx_direct_message_from_customer", columnList = "FROM_CUSTOMER_ID"),
                @Index(name = "idx_direct_message_to_customer", columnList = "TO_CUSTOMER_ID")
        }
)
@ActiveReflection
@Trigger(triggers = {"com.bachlinh.order.trigger.internal.DirectMessageIndexTrigger"})
public class DirectMessage extends AbstractEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "nvarchar(500)")
    private String content;

    @Column(name = "SENT_TIME", nullable = false, updatable = false)
    private Timestamp timeSent;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "FROM_CUSTOMER_ID", nullable = false, updatable = false)
    private Customer fromCustomer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "TO_CUSTOMER_ID", nullable = false, updatable = false)
    private Customer toCustomer;

    @ActiveReflection
    DirectMessage() {
    }

    @Override
    @ActiveReflection
    public void setId(Object id) {
        if (id instanceof Integer casted) {
            this.id = casted;
            return;
        }
        throw new PersistenceException("Id of direct message must be integer");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectMessage that = (DirectMessage) o;
        return Objects.equal(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public Integer getId() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }

    public Timestamp getTimeSent() {
        return this.timeSent;
    }

    public Customer getFromCustomer() {
        return this.fromCustomer;
    }

    public Customer getToCustomer() {
        return this.toCustomer;
    }

    @ActiveReflection
    public void setContent(String content) {
        this.content = content;
    }

    @ActiveReflection
    public void setTimeSent(Timestamp timeSent) {
        this.timeSent = timeSent;
    }

    @ActiveReflection
    public void setFromCustomer(Customer fromCustomer) {
        this.fromCustomer = fromCustomer;
    }

    @ActiveReflection
    public void setToCustomer(Customer toCustomer) {
        this.toCustomer = toCustomer;
    }
}