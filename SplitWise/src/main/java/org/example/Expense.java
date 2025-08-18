package org.example;

import java.util.ArrayList;

public class Expense {
    private ArrayList<Split> splits;
    private User payer;
    private Double amount;


    public Expense() {
        splits = new ArrayList<>();
        payer = null;
        amount = 0.0;
    }


    public Expense(User payer, Double amount) {
        this.splits = new ArrayList<>();
        this.payer = payer;
        this.amount = amount;
    }

    public Expense(ArrayList<Split> splits, User payer, Double amount) {
        this.splits = splits;
        this.payer = payer;
        this.amount = amount;
    }

    public ArrayList<Split> getSplits() {
        return splits;
    }

    public void setSplits(ArrayList<Split> splits) {
        this.splits = splits;
    }

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
