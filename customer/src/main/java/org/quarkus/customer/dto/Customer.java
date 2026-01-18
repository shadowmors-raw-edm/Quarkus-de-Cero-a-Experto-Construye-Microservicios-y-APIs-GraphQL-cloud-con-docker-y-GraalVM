package org.quarkus.customer.dto;

import java.util.List;

public class Customer {

    private Long id;
    private String code;
    private String accountNumber;
    private String names;
    private String surnames;
    private String phone;
    private String address;
    private List<Product> product;
}
