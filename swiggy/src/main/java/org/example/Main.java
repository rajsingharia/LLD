package org.example;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        KeyValueProcessor keyValueProcessor = new KeyValueProcessor();
        Scanner scanner = new Scanner(System.in);
        for(int i= 0 ; i< 20; i++) {
            String command = scanner.nextLine();
            keyValueProcessor.processCommand(command);
        }
    }
}