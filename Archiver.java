package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.command.ExitCommand;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Archiver {
    public static void main(String[] args) throws Exception {
        BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter new archive full path:");
        ZipFileManager zipFileManager = new ZipFileManager(Paths.get(bis.readLine()));

        System.out.println("Enter full path of the file to be archived:");
        zipFileManager.createZip(Paths.get(bis.readLine()));

        new ExitCommand().execute();
    }
}
