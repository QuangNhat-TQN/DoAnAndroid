package com.example.onlineshopapp.Model;

public class Address {
    private String address_detail;
    private String full_name;
    private String phone;
    private String id;

    public Address() {
    }

    public Address(String address_detail, String full_name, String phone) {
        this.address_detail = address_detail;
        this.full_name = full_name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress_detail() {
        return address_detail;
    }

    public void setAddress_detail(String address_detail) {
        this.address_detail = address_detail;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
