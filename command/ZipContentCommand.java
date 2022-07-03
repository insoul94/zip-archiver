package com.javarush.task.task31.task3110.command;

import com.javarush.task.task31.task3110.ConsoleHelper;
import com.javarush.task.task31.task3110.FileProperties;
import com.javarush.task.task31.task3110.ZipFileManager;

import java.util.List;

public class ZipContentCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        ConsoleHelper.writeMessage("Viewing contents of ZIP file...");

        ZipFileManager zipFileManager = getZipFileManager();

        ConsoleHelper.writeMessage("Content:");

        List<FileProperties> filePropertiesList = zipFileManager.getFilePropertiesList();
        for (FileProperties fileProperties : filePropertiesList) {
            ConsoleHelper.writeMessage(fileProperties.toString());
        }

        ConsoleHelper.writeMessage("ZIP file contents read!");
    }
}
