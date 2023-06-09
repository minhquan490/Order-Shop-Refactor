package com.bachlinh.order.repository.implementer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.annotation.RepositoryComponent;
import com.bachlinh.order.entity.model.Category;
import com.bachlinh.order.entity.model.Category_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.CategoryRepository;
import com.bachlinh.order.repository.query.Join;
import com.bachlinh.order.repository.query.QueryExtractor;
import com.bachlinh.order.repository.query.Where;
import com.bachlinh.order.service.container.DependenciesContainerResolver;

@RepositoryComponent
@ActiveReflection
public class CategoryRepositoryImpl extends AbstractRepository<Category, String> implements CategoryRepository {

    @DependenciesInitialize
    @ActiveReflection
    public CategoryRepositoryImpl(DependenciesContainerResolver containerResolver) {
        super(Category.class, containerResolver.getDependenciesResolver());
    }

    @Override
    public Category getCategoryByName(String categoryName) {
        Specification<Category> spec = Specification.where((root, query, criteriaBuilder) -> {
            root.join(Category_.PRODUCTS, JoinType.INNER);
            var join = Join.builder().attribute(Category_.PRODUCTS).type(JoinType.INNER).build();
            var where = Where.builder().attribute(Category_.NAME).value(categoryName).build();
            var extractor = new QueryExtractor(criteriaBuilder, query, root);
            extractor.join(join);
            extractor.where(where);
            return extractor.extract();
        });
        return this.get(spec);
    }

    @Override
    public Category getCategoryById(String categoryId) {
        Specification<Category> spec = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Category_.ID), categoryId));
        return this.get(spec);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public Category saveCategory(Category category) {
        return this.save(category);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public Category updateCategory(Category category) {
        return saveCategory(category);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public boolean deleteCategory(Category category) {
        if (category == null) {
            return false;
        }
        if (existsById(category.getId())) {
            delete(category);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isExits(String id) {
        return existsById(id);
    }

    @Override
    public Page<Category> getCategories() {
        return this.findAll(Pageable.unpaged());
    }

    @Override
    @PersistenceContext
    @ActiveReflection
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
