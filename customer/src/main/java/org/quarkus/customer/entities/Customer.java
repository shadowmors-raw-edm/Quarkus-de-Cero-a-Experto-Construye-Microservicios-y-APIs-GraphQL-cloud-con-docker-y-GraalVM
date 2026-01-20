package org.quarkus.customer.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
@Setter
public class Customer extends PanacheEntity {

    private String code;
    private String accountNumber;
    private String names;
    private String surnames;
    private String phone;
    private String address;


    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Product> productEntities = new ArrayList<>();

}
