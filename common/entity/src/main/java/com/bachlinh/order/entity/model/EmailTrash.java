package com.bachlinh.order.entity.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.bachlinh.order.annotation.ActiveReflection;

import java.util.HashSet;
import java.util.Set;

@ActiveReflection
@Entity
@Table(name = "EMAIL_TRASH")
@Getter
@NoArgsConstructor(access = AccessLevel.NONE, onConstructor = @__({@ActiveReflection}))
public class EmailTrash extends AbstractEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false, unique = true)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "trash")
    private Set<Email> emails = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false, unique = true, updatable = false)
    private Customer customer;

    @ActiveReflection
    public void setEmails(Set<Email> emails) {
        this.emails = emails;
    }

    @ActiveReflection
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    @ActiveReflection
    public void setId(Object id) {
        if (id instanceof Integer casted) {
            this.id = casted;
        } else {
            throw new PersistenceException("Id of EmailTrash must be int");
        }
    }
}
