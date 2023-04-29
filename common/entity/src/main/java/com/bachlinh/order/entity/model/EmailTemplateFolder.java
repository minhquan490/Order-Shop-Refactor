package com.bachlinh.order.entity.model;

import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.Label;
import com.bachlinh.order.annotation.Validator;
import com.google.common.base.Objects;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;

import java.util.Set;

@Label("ETF-")
@Entity
@Table(name = "EMAIL_TEMPLATE_FOLDER", indexes = @Index(name = "idx_email_template_folder_owner", columnList = "OWNER_ID"))
@Validator(validators = "com.bachlinh.order.core.entity.validator.internal.EmailTemplateFolderValidator")
@ActiveReflection
public class EmailTemplateFolder extends AbstractEntity {

    @Id
    @Column(name = "ID", nullable = false, updatable = false, unique = true, columnDefinition = "varchar(32)")
    private String id;

    @Column(name = "NAME", columnDefinition = "nvarchar(300)", unique = true)
    private String name;

    @Column(name = "CLEAR_EMAIL_TEMPLATE_POLICY")
    private Integer clearTemplatePolicy = -1;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinColumn(name = "OWNER_ID", nullable = false, updatable = false)
    private Customer owner;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "folder")
    private Set<EmailTemplate> emailTemplates;

    @ActiveReflection
    EmailTemplateFolder() {
    }

    @Override
    @ActiveReflection
    public void setId(Object id) {
        if (id instanceof String casted) {
            this.id = casted;
        }
        throw new PersistenceException("Id of email template folder must be string");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailTemplateFolder that = (EmailTemplateFolder) o;
        return Objects.equal(getId(), that.getId());
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
    public String getName() {
        return this.name;
    }

    @ActiveReflection
    public Integer getClearTemplatePolicy() {
        return this.clearTemplatePolicy;
    }

    @ActiveReflection
    public Customer getOwner() {
        return this.owner;
    }

    @ActiveReflection
    public Set<EmailTemplate> getEmailTemplates() {
        return this.emailTemplates;
    }

    @ActiveReflection
    public void setName(String name) {
        this.name = name;
    }

    @ActiveReflection
    public void setClearTemplatePolicy(Integer clearTemplatePolicy) {
        this.clearTemplatePolicy = clearTemplatePolicy;
    }

    @ActiveReflection
    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    @ActiveReflection
    public void setEmailTemplates(Set<EmailTemplate> emailTemplates) {
        this.emailTemplates = emailTemplates;
    }
}