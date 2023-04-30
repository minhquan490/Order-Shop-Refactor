package com.bachlinh.order.web.dto.form;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Objects;

@JsonRootName("cart")
public class CartForm {

    @JsonAlias("products")
    private ProductForm[] productForms;

    public void setProductForms(ProductForm[] productForms) {
        this.productForms = productForms;
    }

    public ProductForm[] getProductForms() {
        return productForms;
    }

    @JsonRootName("product")
    public record ProductForm(String id, String name, String amount) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ProductForm that)) return false;
            return Objects.equals(id, that.id) && Objects.equals(amount, that.amount);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, amount);
        }
    }
}