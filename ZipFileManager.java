package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;
import com.javarush.task.task31.task3110.exception.WrongZipFileException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileManager {
    // Полный путь zip файла
    private final Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void createZip(Path source) throws Exception {
        // Проверяем, существует ли директория, где будет создаваться архив
        // При необходимости создаем ее
        createDirectories(zipFile.getParent());

        // Создаем zip поток
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {

            if (Files.isDirectory(source)) {
                // Если архивируем директорию, то нужно получить список файлов в ней
                FileManager fileManager = new FileManager(source);
                List<Path> relativeFilePaths = fileManager.getRelativePaths();

                // Добавляем каждый файл в архив
                for (Path relativeFilePath : relativeFilePaths)
                    addNewZipEntry(zipOut, source, relativeFilePath);

            } else if (Files.isRegularFile(source)) {

                // Если архивируем отдельный файл, то нужно получить его директорию и имя
                addNewZipEntry(zipOut, source.getParent(), source.getFileName());
            } else {

                // Если переданный source не директория и не файл, бросаем исключение
                throw new PathIsNotFoundException();
            }
        }
    }

    public void extractAll(Path outputFolder) throws Exception {
        checkZipFileExists();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {
            // Создаем директорию вывода, если она не существует
            createDirectories(outputFolder);

            // Проходимся по содержимому zip потока (файла)
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {

                Path fileFullName = outputFolder.resolve(zipEntry.getName());

                // Создаем необходимые директории
                createDirectories(fileFullName.getParent());

                try (OutputStream outputStream = Files.newOutputStream(fileFullName)) {
                    copyData(zipIn, outputStream);
                }
            }
        }
    }

    public void removeFile(Path path) throws Exception {
        removeFiles(Collections.singletonList(path));
    }

    public void removeFiles(List<Path> pathList) throws Exception {
        checkZipFileExists();

        // Создаем временный файл
        Path tempZipFile = Files.createTempFile(null, null);

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipFile));
            ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {

                Path archivedFile = Paths.get(zipEntry.getName());

                if (!pathList.contains(archivedFile)) {
                    transferZipEntry(zipIn, zipOut, zipEntry);
                } else {
                    ConsoleHelper.writeMessage(String.format("Файл '%s' удален из архива.", archivedFile));
                }
            }
        }

        // Перемещаем временный файл на место оригинального
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public void addFile(Path absolutePath) throws Exception {
        addFiles(Collections.singletonList(absolutePath));
    }

    public void addFiles(List<Path> absolutePaths) throws Exception {
        checkZipFileExists();

        // Создаем временный файл
        Path tempZipFile = Files.createTempFile(null, null);
        List<Path> archiveFiles = new ArrayList<>();

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {
            try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

                ZipEntry zipEntry;
                while ((zipEntry = zipIn.getNextEntry()) != null) {

                    transferZipEntry(zipIn, zipOut, zipEntry);
                    archiveFiles.add(Paths.get(zipEntry.getName()));
                }
            }

            // Архивируем новые файлы
            for (Path filePath : absolutePaths) {

                if (Files.isRegularFile(filePath)) {

                    if (archiveFiles.contains(filePath.getFileName())) {
                        ConsoleHelper.writeMessage(String.format("Файл '%s' уже существует в архиве.", filePath));
                    } else {
                        addNewZipEntry(zipOut, filePath.getParent(), filePath.getFileName());
                        ConsoleHelper.writeMessage(String.format("Файл '%s' добавлен в архиве.", filePath));
                    }

                } else
                    throw new PathIsNotFoundException();
            }
        }

        // Перемещаем временный файл на место оригинального
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public List<FileProperties> getFilePropertiesList() throws Exception {
        checkZipFileExists();
        List<FileProperties> filePropertiesList = new ArrayList<>();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                // Поля "размер" и "сжатый размер" не известны, пока элемент не будет прочитан
                // Давайте вычитаем его в какой-то выходной поток
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copyData(zipIn, baos);

                FileProperties file = new FileProperties(
                        zipEntry.getName(),
                        zipEntry.getSize(),
                        zipEntry.getCompressedSize(),
                        zipEntry.getMethod());

                filePropertiesList.add(file);
            }
        }

        return filePropertiesList;
    }

    private void addNewZipEntry(ZipOutputStream zipOutputStream, Path dirPath, Path relativeFilePath) throws Exception {
        Path fullPath = dirPath.resolve(relativeFilePath);

        try (InputStream inputStream = Files.newInputStream(fullPath)) {

            ZipEntry entry = new ZipEntry(relativeFilePath.toString());
            zipOutputStream.putNextEntry(entry);

            copyData(inputStream, zipOutputStream);
            zipOutputStream.closeEntry();
        }
    }

    private void transferZipEntry(ZipInputStream zis, ZipOutputStream zos, ZipEntry entry) throws Exception{
        zos.putNextEntry(new ZipEntry(entry.getName()));
        copyData(zis, zos);

        zis.closeEntry();
        zos.closeEntry();
    }

    private void copyData(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    private void checkZipFileExists() throws WrongZipFileException {
        if (!Files.isRegularFile(zipFile) && zipFile.endsWith(".zip")) {
            throw new WrongZipFileException();
        }
    }

    private void createDirectories(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}
