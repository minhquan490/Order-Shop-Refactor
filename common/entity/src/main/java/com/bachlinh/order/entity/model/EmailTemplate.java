package com.bachlinh.order.entity.model;

import com.google.common.base.Objects;
import jakarta.persistence.CascadeType;
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
import com.bachlinh.order.annotation.EnableFullTextSearch;
import com.bachlinh.order.annotation.FullTextField;
import com.bachlinh.order.annotation.Label;
import com.bachlinh.order.annotation.Trigger;
import com.bachlinh.order.annotation.Validator;

@Label("ETE-")
@Entity
@Table(
        name = "EMAIL_TEMPLATE",
        indexes = {
                @Index(name = "idx_email_template_owner", columnList = "OWNER_ID"),
                @Index(name = "idx_email_template_folder", columnList = "FOLDER_ID"),
                @Index(name = "idx_email_template_title", columnList = "TITLE"),
                @Index(name = "idx_email_template_name", columnList = "NAME")
        }
)
@Validator(validators = "com.bachlinh.order.validator.internal.EmailTemplateValidator")
@ActiveReflection
@EnableFullTextSearch
@Trigger(triggers = {"com.bachlinh.order.trigger.internal.EmailTemplateIndexTrigger"})
public class EmailTemplate extends AbstractEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false, columnDefinition = "varchar(32)")
    private String id;

    @Column(name = "NAME", columnDefinition = "nvarchar(100)")
    @FullTextField
    @ActiveReflection
    private String name;

    @Column(name = "TITLE", nullable = false)
    @FullTextField
    @ActiveReflection
    private String title;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "nvarchar(700)")
    @FullTextField
    @ActiveReflection
    private String content;

    @Column(name = "EXPIRY_POLICY", nullable = false)
    private Integer expiryPolicy = -1;

    @Column(name = "TOTAL_ARGUMENT", nullable = false)
    private Integer totalArgument;

    @Column(name = "PARAMS", nullable = false)
    private String params;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "OWNER_ID", nullable = false, updatable = false)
    private Customer owner;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "FOLDER_ID", nullable = false)
    private EmailTemplateFolder folder;

    @ActiveReflection
    EmailTemplate() {
    }

    @ActiveReflection
    @Override
    public void setId(Object id) {
        if (id instanceof String casted) {
            this.id = casted;
            return;
        }
        throw new PersistenceException("Id of email template must be string");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailTemplate that = (EmailTemplate) o;
        return Objects.equal(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public Integer getExpiryPolicy() {
        return this.expiryPolicy;
    }

    public Customer getOwner() {
        return this.owner;
    }

    public EmailTemplateFolder getFolder() {
        return this.folder;
    }

    public String getParams() {
        return params;
    }

    @ActiveReflection
    public void setTitle(String title) {
        this.title = title;
    }

    @ActiveReflection
    public void setContent(String content) {
        this.content = content;
    }

    @ActiveReflection
    public void setExpiryPolicy(Integer expiryPolicy) {
        this.expiryPolicy = expiryPolicy;
    }

    @ActiveReflection
    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    @ActiveReflection
    public void setFolder(EmailTemplateFolder folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public Integer getTotalArgument() {
        return totalArgument;
    }

    @ActiveReflection
    public void setName(String name) {
        this.name = name;
    }

    @ActiveReflection
    public void setTotalArgument(Integer totalArgument) {
        this.totalArgument = totalArgument;
    }

    @ActiveReflection
    public void setParams(String params) {
        this.params = params;
    }
}