package com.nahwasa.study.howtosolveconcurrencyissues.domain;

import jakarta.persistence.*;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    @Version
    private Long version;

    public Stock() {
    }

    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getId() {
        return id;
    }

    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0)
            throw new RuntimeException("Not enough stock");

        this.quantity -= quantity;
    }

}
