package org.quarkus.customer.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.quarkus.customer.entities.CustomerEntity;

import java.util.List;

@ApplicationScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    @Transactional
    public void createCustomer(CustomerEntity customerEntity) {
        em.persist(customerEntity);
    }

    @Transactional
    public List<CustomerEntity> listCustomers() {
        List<CustomerEntity> customerEntities = em.createQuery("select c from CustomerEntity c", CustomerEntity.class).getResultList();
        return customerEntities;
    }

    @Transactional
    public CustomerEntity findCustomerById(Long id) {
        return em.find(CustomerEntity.class, id);
    }

    @Transactional
    public void update(CustomerEntity customerEntity) {
        em.merge(customerEntity);
    }

    @Transactional
    public void delete(Long id) {
        em.remove(id);
    }
}
