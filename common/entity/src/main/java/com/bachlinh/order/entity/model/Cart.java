package com.bachlinh.order.entity.model;

import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.Label;
import com.google.common.base.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Set;

@Entity
@Table(name = "CART", indexes = @Index(name = "idx_cart_customer", columnList = "CUSTOMER_ID"))
@Label("CRT-")
@ActiveReflection
public class Cart extends AbstractEntity {

    @Id
    @Column(name = "ID", nullable = false, updatable = false, columnDefinition = "varchar(32)")
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMER_ID", unique = true, nullable = false, updatable = false)
    private Customer customer;

    @ManyToMany
    @JoinTable(
            name = "PRODUCT_CART",
            joinColumns = @JoinColumn(name = "CART_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "PRODUCT_ID", referencedColumnName = "ID"),
            indexes = @Index(name = "idx_product_cart", columnList = "CART_ID, PRODUCT_ID"),
            uniqueConstraints = @UniqueConstraint(name = "udx", columnNames = "CART_ID")
    )
    private Set<Product> products;

    @ActiveReflection
    Cart() {
    }

    @Override
    @ActiveReflection
    public void setId(Object id) {
        if (!(id instanceof String)) {
            throw new PersistenceException("Id of cart must be string");
        }
        this.id = (String) id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cart cart)) return false;
        return Objects.equal(getId(), cart.getId());
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
    public Customer getCustomer() {
        return this.customer;
    }

    @ActiveReflection
    public Set<Product> getProducts() {
        return this.products;
    }

    @ActiveReflection
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @ActiveReflection
    public void setProducts(Set<Product> products) {
        this.products = products;
    }
}