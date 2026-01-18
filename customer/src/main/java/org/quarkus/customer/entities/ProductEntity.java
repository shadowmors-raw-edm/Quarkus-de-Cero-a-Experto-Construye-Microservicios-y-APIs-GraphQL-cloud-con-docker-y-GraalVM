package org.quarkus.customer.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"customer", "product"})
)
@JsonIgnoreProperties({"customer"})
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long product;

    @Transient
    private String name;

    @Transient
    private String code;

    @Transient
    private String description;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

}
