package org.example;

import java.util.ArrayList;

public class Group {
    private String name;
    private ArrayList<User> users;
    private ArrayList<Expense> expenses;

    public Group() {
        this.users = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    public Group(String name, ArrayList<User> users) {
        this.name = name;
        this.users = users;
        this.expenses = new ArrayList<>();
    }

    public Group(String name, ArrayList<User> users, ArrayList<Expense> expenses) {
        this.name = name;
        this.users = users;
        this.expenses = expenses;
    }

    public void addNewExpense(Expense newExpense) {
        expenses.add(newExpense);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }
}
