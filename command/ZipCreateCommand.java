package com.javarush.task.task31.task3110.command;

import com.javarush.task.task31.task3110.ConsoleHelper;
import com.javarush.task.task31.task3110.ZipFileManager;
import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipCreateCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        try {
            ConsoleHelper.writeMessage("Creating new ZIP file...");

            ConsoleHelper.writeMessage("Enter full path of file or directory to be archived:");
            Path sourcePath = Paths.get(ConsoleHelper.readString());

            ZipFileManager zipFileManager = getZipFileManager();
            zipFileManager.createZip(sourcePath);

            ConsoleHelper.writeMessage("ZIP file created!");

        } catch (PathIsNotFoundException e) {
            ConsoleHelper.writeMessage("Full path of file or directory is incorrect.");
        }
    }
}
