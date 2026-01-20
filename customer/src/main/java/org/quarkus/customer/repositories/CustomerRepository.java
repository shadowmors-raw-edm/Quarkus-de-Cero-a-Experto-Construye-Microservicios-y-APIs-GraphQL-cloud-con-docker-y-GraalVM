package org.quarkus.customer.repositories;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.quarkus.customer.entities.Customer;

import java.util.List;

@ApplicationScoped
public class CustomerRepository implements PanacheRepositoryBase<Customer, Long> {


}
