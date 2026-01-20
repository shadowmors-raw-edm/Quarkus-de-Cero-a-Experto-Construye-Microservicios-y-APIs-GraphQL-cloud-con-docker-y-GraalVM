package org.quarkus.customer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Table(
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"customer", "product"})
)
@JsonIgnoreProperties({"customer"})
@NoArgsConstructor
@Getter
@Setter
public class Product extends PanacheEntity {

    @Transient
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
    private Customer customer;

}
