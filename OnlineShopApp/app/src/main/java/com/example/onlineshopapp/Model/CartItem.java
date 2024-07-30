package com.example.onlineshopapp.Model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private int quantity;

    public CartItem() {
    }

    public CartItem(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
