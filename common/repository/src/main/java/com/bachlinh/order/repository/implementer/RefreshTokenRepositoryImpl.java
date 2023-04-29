package com.bachlinh.order.repository.implementer;

import com.bachlinh.order.entity.model.RefreshToken;
import com.bachlinh.order.entity.model.RefreshToken_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.RefreshTokenRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Repository
@Primary
class RefreshTokenRepositoryImpl extends AbstractRepository<RefreshToken, String> implements RefreshTokenRepository {

    @Autowired
    RefreshTokenRepositoryImpl(ApplicationContext applicationContext) {
        super(RefreshToken.class, applicationContext);
    }

    @Override
    public RefreshToken getRefreshToken(String token) {
        Specification<RefreshToken> spec = Specification.where(((root, query, criteriaBuilder) -> {
            query.multiselect(root.get(RefreshToken_.REFRESH_TOKEN_VALUE), root.get(RefreshToken_.CUSTOMER), root.get(RefreshToken_.TIME_EXPIRED));
            root.join("customer", JoinType.INNER);
            return criteriaBuilder.equal(root.get(RefreshToken_.REFRESH_TOKEN_VALUE), token);
        }));
        return get(spec);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public RefreshToken saveRefreshToken(RefreshToken token) {
        return this.save(token);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public RefreshToken updateRefreshToken(RefreshToken refreshToken) {
        return saveRefreshToken(refreshToken);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public boolean deleteRefreshToken(RefreshToken refreshToken) {
        if (StringUtils.hasText((CharSequence) refreshToken.getId())) {
            long numRowDeleted = this.delete(Specification.where((root, query, builder) -> builder.equal(root.get("id"), refreshToken.getId())));
            return numRowDeleted != 0;
        } else {
            return false;
        }
    }

    @Override
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}