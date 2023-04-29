package com.bachlinh.order.repository.implementer;

import com.bachlinh.order.entity.model.Product;
import com.bachlinh.order.entity.model.Product_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Repository
@Primary
class ProductRepositoryImpl extends AbstractRepository<Product, String> implements ProductRepository {
    private static final String LIKE_PATTERN = "%{0}%";

    @Autowired
    ProductRepositoryImpl(ApplicationContext applicationContext) {
        super(Product.class, applicationContext);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Product saveProduct(Product product) {
        return this.save(product);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, isolation = READ_COMMITTED)
    public Product updateProduct(Product product) {
        return this.saveProduct(product);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, isolation = READ_COMMITTED)
    public boolean deleteProduct(Product product) {
        if (product == null) {
            return false;
        }
        if (existsById(product.getId())) {
            delete(product);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean productNameExist(Product product) {
        Specification<Product> spec = Specification.where(((root, query, criteriaBuilder) -> {
            query.select(root.get(Product_.ID));
            return criteriaBuilder.equal(root.get(Product_.NAME), product.getName());
        }));
        return get(spec) != null;
    }

    @Override
    public long countProduct() {
        return count();
    }

    @Override
    public Product getProductByCondition(Map<String, Object> conditions) {
        Specification<Product> spec = specWithCondition(conditions);
        return get(spec);
    }

    @Override
    public Page<Product> getProductsByCondition(Map<String, Object> conditions, Pageable pageable) {
        Specification<Product> spec = specWithCondition(conditions);
        return findAll(spec, pageable);
    }

    @Override
    public Page<Product> getProductsWithUnion(Collection<String> ids, Map<String, Object> conditions, Pageable pageable) {
        Specification<Product> spec = specWithCondition(conditions);
        return unionQueryWithId(ids, spec, pageable);
    }

    @Override
    public Collection<Product> getAllProducts() {
        return findAll();
    }

    private Specification<Product> specWithCondition(Map<String, Object> conditions) {
        Map<String, Object> copyConditions = conditions.entrySet()
                .stream()
                .filter(stringObjectEntry -> stringObjectEntry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Specification.where(((root, query, criteriaBuilder) -> {
            query.multiselect(
                    root.get(Product_.id),
                    root.get(Product_.carts),
                    root.get(Product_.size),
                    root.get(Product_.name),
                    root.get(Product_.color),
                    root.get(Product_.price),
                    root.get(Product_.taobaoUrl),
                    root.get(Product_.description),
                    root.get(Product_.categories),
                    root.get(Product_.medias)
            );
            root.join(Product_.categories, JoinType.INNER);
            root.join(Product_.medias, JoinType.LEFT);
            AtomicReference<Predicate> predicateWrapper = new AtomicReference<>();
            copyConditions.forEach((key, value) -> {
                Predicate predicate = switch (key) {
                    case Product_.PRICE -> criteriaBuilder.lessThanOrEqualTo(root.get(key), (int) value);
                    case Product_.NAME ->
                            criteriaBuilder.like(root.get(Product_.NAME), MessageFormat.format(LIKE_PATTERN, value));
//                    case "IDS" -> criteriaBuilder.in(root.get(Product_.ID)).in(value);
                    case Product_.CATEGORIES -> criteriaBuilder.in(root.get(Product_.CATEGORIES)).in(value);
                    default -> criteriaBuilder.equal(root.get(key), value);
                };
                predicateWrapper.set(criteriaBuilder.and(predicate));
            });
            return predicateWrapper.get();
        }));
    }

    @Override
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}