package com.javarush.task.task31.task3110;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Archiver {
    public static void main(String[] args) {
        BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.println("Enter archive full path:");
            ZipFileManager zipFileManager = new ZipFileManager(Paths.get(bis.readLine()));

            System.out.println("Enter full path of file to be archived:");
            zipFileManager.createZip(Paths.get(bis.readLine()));

        } catch (IOException e) {
            System.out.println("Exception while reading file path: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Exception creating ZIP: " + e.getMessage());
        }

    }
}
