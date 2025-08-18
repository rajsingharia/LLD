package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueProcessor {

    private KeyValue keyValue;

    public KeyValueProcessor() {
        keyValue = new KeyValue();
    }

    // true- processed
    // false - illegal

    // T1 - put(key, value1);
    // T2 - put(key, value2);

    public Boolean processCommand(String command) {
        List<String> commandList = List.of(command.split(" "));

        if(commandList.size() == 0) return false;

        switch (commandList.get(0)) {

            case "get" -> {
                if(commandList.size() <= 1) return false;
                String key = commandList.get(1);
                Attributes commandOutput = keyValue.get(key);
                if(commandOutput == null) {
                    System.out.println("Illegal");
                }
                System.out.println(commandOutput);
            }

            case "put" -> {
                if(commandList.size() < 3) return false;
                String key = commandList.get(1);
                Map<String, String> att = new HashMap<>();
                for(int i = 2;i< commandList.size(); i += 2) {
                    att.put(commandList.get(i), commandList.get(i + 1));
                }
                keyValue.put(key, att);
            }

            case "delete" -> {
                if(commandList.size() <= 1) return false;
                String key = commandList.get(1);
                keyValue.delete(key);
            }

            case "search" -> {
                if(commandList.size() <= 2) return false;
                String keyAtt = commandList.get(1);
                String keyAttValue = commandList.get(2);
                List<String>searchKeys = keyValue.search(keyAtt, keyAttValue);
                System.out.println(searchKeys);
            }

            default -> {
                //exit
            }

        }

        return true;

    }

}
