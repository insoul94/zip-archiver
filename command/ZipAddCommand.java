package com.javarush.task.task31.task3110.command;

import com.javarush.task.task31.task3110.ConsoleHelper;
import com.javarush.task.task31.task3110.ZipFileManager;
import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipAddCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        try {
            ConsoleHelper.writeMessage("Adding new file into the archive...");

            ConsoleHelper.writeMessage("Enter full path of the file to be added:");
            Path sourcePath = Paths.get(ConsoleHelper.readString());

            ZipFileManager zipFileManager = getZipFileManager();
            zipFileManager.addFile(sourcePath);

            ConsoleHelper.writeMessage("Adding file completed!");

        } catch (PathIsNotFoundException e) {
            ConsoleHelper.writeMessage("File not found.");
        }
    }
}
