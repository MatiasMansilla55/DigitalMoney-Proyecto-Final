package com.example.checkout.model;

import java.util.List;

public class Checkout {
    private String id;
    private String url;
    private String totalAmount;
    private List<String> availableMethods;


    public Checkout(String id, String url, String totalAmount, List<String> availableMethods) {
        super();
        this.id = id;
        this.url = url;
        this.totalAmount = totalAmount;
        this.availableMethods = availableMethods;
    }

    public Checkout(String id) {

        this.id = id;

    }


    public String getId() {
        return id;
    }
    public String getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getAvailableMethods() {
        return availableMethods;
    }
    public void setAvailableMethods(List<String> availableMethods) {
        this.availableMethods = availableMethods;
    }
}
