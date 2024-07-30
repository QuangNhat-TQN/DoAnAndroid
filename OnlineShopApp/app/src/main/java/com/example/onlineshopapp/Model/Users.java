package com.example.onlineshopapp.Model;

import java.util.HashMap;

public class Users {
    private String id;
    private String email;
    private String full_name;
    private String phone;
    private String image_url;
    private String role;
    private HashMap<String, Address> address;

    public Users() {
    }

    public Users(HashMap<String, Address> address, String email, String full_name, String phone, String image_url, String role) {
        this.email = email;
        this.full_name = full_name;
        this.phone = phone;
        this.address = address;
        this.image_url = image_url;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public HashMap<String, Address> getAddress() {
        return address;
    }

    public void setAddress(HashMap<String, Address> address) {
        this.address = address;
    }
}
