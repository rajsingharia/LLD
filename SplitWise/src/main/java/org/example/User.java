package org.example;

import java.util.HashMap;

public class User {
    private String name;
    private HashMap<String, Double> balanceSheet;

    public User() {
        this.balanceSheet = new HashMap<>();
        this.name = null;
    }

    public User(String name) {
        this.name = name;
        this.balanceSheet = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Double> getBalanceSheet() {
        return balanceSheet;
    }

    public void setBalanceSheet(HashMap<String, Double> balanceSheet) {
        this.balanceSheet = balanceSheet;
    }

    public void insertDue(String key, Double amount) {
        balanceSheet.put(key, amount);
    }

    public Double getDue(String key) {
        Double amount = balanceSheet.get(key);
        if(amount == null) return  0.0;
        return amount;
    }
}
