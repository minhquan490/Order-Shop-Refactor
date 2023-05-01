package com.bachlinh.order.repository.implementer;

import com.bachlinh.order.entity.model.EmailTemplate;
import com.bachlinh.order.entity.model.EmailTemplate_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.EmailTemplateRepository;
import com.bachlinh.order.service.container.DependenciesContainerResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
class EmailTemplateRepositoryImpl extends AbstractRepository<EmailTemplate, String> implements EmailTemplateRepository {

    @Autowired
    EmailTemplateRepositoryImpl(DependenciesContainerResolver containerResolver) {
        super(EmailTemplate.class, containerResolver.getDependenciesResolver());
    }

    @Override
    public boolean isEmailTemplateTitleExisted(String title) {
        Specification<EmailTemplate> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(EmailTemplate_.title), title));
        return findOne(spec).isPresent();
    }

    @Override
    @PersistenceContext
    protected void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
