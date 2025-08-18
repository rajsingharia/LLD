package org.example;

import java.util.*;

// Product representation
class Item {
    String name;
    int price;
    int quantity;

    Item(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    boolean isAvailable() {
        return quantity > 0;
    }

    void reduceQuantity() {
        quantity--;
    }
}

// State Interface
interface VendingState {
    void selectItem(String code);
    void insertMoney(int amount);
    void dispense();
    void cancel();
}

// Context (Machine)
class VendingMachine {
    private final Map<String, Item> inventory = new HashMap<>();
    private final VendingState idleState;
    private final VendingState hasMoneyState;
    private final VendingState dispenseState;
    private final VendingState soldOutState;

    private VendingState currentState;
    private Item selectedItem;
    private int currentMoney;

    public VendingMachine() {
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        dispenseState = new DispenseState(this);
        soldOutState = new SoldOutState(this);
        currentState = idleState;

        // Sample inventory
        inventory.put("A1", new Item("Coke", 25, 5));
        inventory.put("B1", new Item("Pepsi", 35, 0)); // Sold Out
        inventory.put("C1", new Item("Water", 15, 10));
    }

    // State transitions
    public void setState(VendingState state) {
        this.currentState = state;
    }

    public VendingState getIdleState() {
        return idleState;
    }

    public VendingState getHasMoneyState() {
        return hasMoneyState;
    }

    public VendingState getDispenseState() {
        return dispenseState;
    }

    public VendingState getSoldOutState() {
        return soldOutState;
    }

    public void selectItem(String code) {
        currentState.selectItem(code);
    }

    public void insertMoney(int amount) {
        currentState.insertMoney(amount);
    }

    public void dispense() {
        currentState.dispense();
    }

    public void cancel() {
        currentState.cancel();
    }

    public Item getItem(String code) {
        return inventory.get(code);
    }

    public void setSelectedItem(Item item) {
        this.selectedItem = item;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setCurrentMoney(int money) {
        this.currentMoney = money;
    }

    public int getCurrentMoney() {
        return currentMoney;
    }

    public void reset() {
        selectedItem = null;
        currentMoney = 0;
    }
}

// Concrete States
class IdleState implements VendingState {
    private final VendingMachine machine;

    public IdleState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String code) {
        Item item = machine.getItem(code);
        if (item == null) {
            System.out.println("Invalid selection.");
        } else if (!item.isAvailable()) {
            System.out.println("Item is sold out.");
            machine.setState(machine.getSoldOutState());
        } else {
            machine.setSelectedItem(item);
            System.out.println("Selected: " + item.name + " | Price: " + item.price);
            machine.setState(machine.getHasMoneyState());
        }
    }

    public void insertMoney(int amount) {
        System.out.println("Select an item first.");
    }

    public void dispense() {
        System.out.println("Select item and insert money first.");
    }

    public void cancel() {
        System.out.println("Nothing to cancel.");
    }
}

class HasMoneyState implements VendingState {
    private final VendingMachine machine;

    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String code) {
        System.out.println("Item already selected.");
    }

    public void insertMoney(int amount) {
        machine.setCurrentMoney(machine.getCurrentMoney() + amount);
        Item item = machine.getSelectedItem();
        System.out.println("Inserted: " + amount + " | Total: " + machine.getCurrentMoney());

        if (machine.getCurrentMoney() >= item.price) {
            machine.setState(machine.getDispenseState());
            machine.dispense();
        } else {
            System.out.println("Insert " + (item.price - machine.getCurrentMoney()) + " more.");
        }
    }

    public void dispense() {
        System.out.println("Insert full amount first.");
    }

    public void cancel() {
        System.out.println("Cancelled. Returning: " + machine.getCurrentMoney());
        machine.reset();
        machine.setState(machine.getIdleState());
    }
}

class DispenseState implements VendingState  {
    private final VendingMachine machine;

    public DispenseState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String code) {
        System.out.println("Dispensing in progress...");
    }

    public void insertMoney(int amount) {
        System.out.println("Already dispensing.");
    }

    public void dispense() {
        Item item = machine.getSelectedItem();
        int change = machine.getCurrentMoney() - item.price;

        System.out.println("Dispensing: " + item.name);
        item.reduceQuantity();

        if (change > 0) {
            System.out.println("Returning change: " + change);
        }

        machine.reset();
        machine.setState(machine.getIdleState());
    }

    public void cancel() {
        System.out.println("Cannot cancel now.");
    }
}

class SoldOutState implements VendingState {
    private final VendingMachine machine;

    public SoldOutState(VendingMachine machine) {
        this.machine = machine;
    }

    public void selectItem(String code) {
        System.out.println("Item sold out. Choose another.");
        machine.setState(machine.getIdleState());
    }

    public void insertMoney(int amount) {
        System.out.println("Cannot insert money. Item is sold out.");
    }

    public void dispense() {
        System.out.println("Nothing to dispense.");
    }

    public void cancel() {
        machine.reset();
        machine.setState(machine.getIdleState());
    }
}

// --- Main Tester ---
public class Main {
    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine();

        // Test case: normal purchase
        vm.selectItem("A1");
        vm.insertMoney(10);
        vm.insertMoney(10);
        vm.insertMoney(5); // Total 25

        // Test case: sold out
        vm.selectItem("B1");

        // Test case: cancel
        vm.selectItem("C1");
        vm.insertMoney(5);
        vm.cancel();
    }
}
