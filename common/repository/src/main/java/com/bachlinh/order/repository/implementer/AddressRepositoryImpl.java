package com.bachlinh.order.repository.implementer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.annotation.RepositoryComponent;
import com.bachlinh.order.entity.model.Address;
import com.bachlinh.order.entity.model.Address_;
import com.bachlinh.order.repository.AbstractRepository;
import com.bachlinh.order.repository.AddressRepository;
import com.bachlinh.order.repository.CustomerRepository;
import com.bachlinh.order.service.container.DependenciesContainerResolver;

import java.util.Optional;

@RepositoryComponent
@ActiveReflection
public class AddressRepositoryImpl extends AbstractRepository<Address, String> implements AddressRepository {

    @DependenciesInitialize
    @ActiveReflection
    public AddressRepositoryImpl(@NonNull DependenciesContainerResolver containerResolver) {
        super(Address.class, containerResolver.getDependenciesResolver());
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public Address composeSave(@NonNull Address address, @NonNull CustomerRepository customerRepository) {
        String customerId = address.getCustomer().getId();
        if (customerRepository.existById(customerId)) {
            customerRepository.saveCustomer(address.getCustomer());
        }
        return Optional.of(this.save(address)).orElse(null);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public Address updateAddress(Address address) {
        return this.save(address);
    }

    @Override
    @Transactional(propagation = MANDATORY, isolation = READ_COMMITTED)
    public boolean deleteAddress(Address address) {
        long numRowDeleted = this.delete(Specification.where(((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Address_.ID), address.getId()))));
        return numRowDeleted == 0;
    }

    @Override
    @PersistenceContext
    @ActiveReflection
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
    }
}
