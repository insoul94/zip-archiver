package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.exception.WrongZipFileException;

import java.io.IOException;

public class Archiver {
    public static void main(String[] args) throws Exception {
        Operation operation = null;
        do {
            try {
                operation = askOperation();
                CommandExecutor.execute(operation);
            } catch (WrongZipFileException e) {
                ConsoleHelper.writeMessage("You have chosen wrong ZIP file.");
            } catch (Exception e) {
                ConsoleHelper.writeMessage("ERROR. Check input data." + e.getMessage());
            }
        } while (operation != Operation.EXIT);
    }

    public static Operation askOperation() throws IOException {
        ConsoleHelper.writeMessage("HI! This is ZIP-archiver. A program which manipulates ZIP archives.");
        ConsoleHelper.writeMessage(String.format("\nChoose operation:\n" +
                        "\t %d - archive files\n" +
                        "\t %d - add file into archive\n" +
                        "\t %d - remove file from archive\n" +
                        "\t %d - extract files\n" +
                        "\t %d - view content of archive\n" +
                        "\t %d - exit\n",
                Operation.CREATE.ordinal(),
                Operation.ADD.ordinal(),
                Operation.REMOVE.ordinal(),
                Operation.EXTRACT.ordinal(),
                Operation.CONTENT.ordinal(),
                Operation.EXIT.ordinal()));
        return Operation.values()[ConsoleHelper.readInt()];
    }
}
