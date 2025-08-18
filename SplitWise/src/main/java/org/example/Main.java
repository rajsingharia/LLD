package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        SplitWiseController controller = SplitWiseController.getInstance();

        User user1 = new User("User1");
        User user2 = new User("User2");
        User user3 = new User("User3");

        String groupName = "Group:User1&User2";

        Group group = new Group(groupName, new ArrayList<>(List.of(user1, user2)));
        controller.addNewGroup(group);

        Expense newExpense = new Expense(user1, 1000.0);
        Expense newExpense2 = new Expense(user2, 2000.0);
        Expense newExpense3 = new Expense(user2, 500.0);
        controller.addNewExpense(groupName, newExpense);
        controller.addNewExpense(groupName, newExpense2);
        controller.addNewExpense(groupName, newExpense3);


        System.out.println(user1.getDue("User2"));
        System.out.println(user2.getDue("User1"));

    }
}