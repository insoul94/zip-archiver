package com.javarush.task.task31.task3110;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    public static String readString() throws IOException{
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    public static int readInt() throws IOException{
        return Integer.parseInt(readString());
    }
}
