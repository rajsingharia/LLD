package org.example;

import java.util.ArrayList;
import java.util.HashMap;

public class SplitWiseController {

    private static SplitWiseController instance;
    private ArrayList<Group> groups;
    private HashMap<String, Group> groupMapping;

    private SplitWiseController() {
        instance = null;
        groups = new ArrayList<>();
        groupMapping = new HashMap<>();
    }

    public static SplitWiseController getInstance() {
        if(instance == null) {
            instance = new SplitWiseController();
        }
        return instance;
    }

    public void addNewGroup(Group newGroup) {
        groups.add(newGroup);
        groupMapping.put(newGroup.getName(), newGroup);
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void addNewExpense(String groupName, Expense expense) {
        Group currentGroup = groupMapping.get(groupName);
        if(currentGroup != null) {

            //check if expense should be equally divided
            Double amountPaid = expense.getAmount();
            User payer = expense.getPayer();

            if(amountPaid != null && payer != null) {

                ArrayList<Split> newSplits = new ArrayList<>();
                Double amountToSettle = amountPaid / currentGroup.getUsers().size();

                currentGroup.getUsers().forEach( user -> {
                    if(user != payer) {
                        Double currentAmountDue = payer.getDue(user.getName());
                        Double currentAmountToSettle = user.getDue(payer.getName());
                        if(currentAmountDue == 0.0) {
                            user.insertDue(payer.getName(), currentAmountToSettle + amountToSettle);
                        } else {
                            if(currentAmountDue > currentAmountToSettle) {
                                payer.insertDue(user.getName(), currentAmountDue - currentAmountToSettle);
                            } else {
                                payer.insertDue(user.getName(), 0.0);
                                user.insertDue(payer.getName(), currentAmountToSettle - amountToSettle);
                            }
                        }
                        newSplits.add(new Split(user, amountToSettle));
                    } else {
                        newSplits.add(new Split(user, 0.0));
                    }
                });

                expense.setSplits(newSplits);
                currentGroup.addNewExpense(expense);
            }
        }
    }

    public Group getGroupDetail(String groupName) {
        return groupMapping.get(groupName);
    }
}
