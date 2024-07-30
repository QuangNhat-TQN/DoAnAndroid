package com.example.onlineshopapp.Model;

import java.io.Serializable;
import java.util.Map;

public class Order implements Serializable {
    private String id;
    private String address;
    private String approval_date;
    private String cancellation_date;
    private String completion_date;
    private String customer_name;
    private String delivery_date;
    private String order_date;
    private String payment_method;
    private String phone;
    private Map<String, CartItem> products;
    private int total_price;
    private String user_id;

    public Order() {
        // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    }

    public Order(String address, String approval_date, String cancellation_date, String completion_date,
                 String customer_name, String delivery_date, String order_date, String payment_method,
                 String phone, Map<String, CartItem> products, int total_price, String user_id) {
        this.address = address;
        this.approval_date = approval_date;
        this.cancellation_date = cancellation_date;
        this.completion_date = completion_date;
        this.customer_name = customer_name;
        this.delivery_date = delivery_date;
        this.order_date = order_date;
        this.payment_method = payment_method;
        this.phone = phone;
        this.products = products;
        this.total_price = total_price;
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApproval_date() {
        return approval_date;
    }

    public void setApproval_date(String approval_date) {
        this.approval_date = approval_date;
    }

    public String getCancellation_date() {
        return cancellation_date;
    }

    public void setCancellation_date(String cancellation_date) {
        this.cancellation_date = cancellation_date;
    }

    public String getCompletion_date() {
        return completion_date;
    }

    public void setCompletion_date(String completion_date) {
        this.completion_date = completion_date;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getDelivery_date() {
        return delivery_date;
    }

    public void setDelivery_date(String delivery_date) {
        this.delivery_date = delivery_date;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Map<String, CartItem> getProducts() {
        return products;
    }

    public void setProducts(Map<String, CartItem> products) {
        this.products = products;
    }

    public int getTotal_price() {
        return total_price;
    }

    public void setTotal_price(int total_price) {
        this.total_price = total_price;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
