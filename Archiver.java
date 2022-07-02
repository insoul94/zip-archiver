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
                ConsoleHelper.writeMessage("Вы не выбрали файл архива или выбрали неверный файл.");
            } catch (Exception e) {
                ConsoleHelper.writeMessage("Произошла ошибка. Проверьте введенные данные." + e.getMessage());
            }
        } while (operation != Operation.EXIT);
    }

    public static Operation askOperation() throws IOException {
        ConsoleHelper.writeMessage(String.format("\nВыберите операцию\n" +
                        "\t %d - упаковать файлы в архив\n" +
                        "\t %d - добавить файл в архив\n" +
                        "\t %d - удалить файл из архива\n" +
                        "\t %d - распаковать архив\n" +
                        "\t %d - просмотреть содержимое архива\n" +
                        "\t %d - выход\n",
                Operation.CREATE.ordinal(),
                Operation.ADD.ordinal(),
                Operation.REMOVE.ordinal(),
                Operation.EXTRACT.ordinal(),
                Operation.CONTENT.ordinal(),
                Operation.EXIT.ordinal()));
        return Operation.values()[ConsoleHelper.readInt()];
    }
}
