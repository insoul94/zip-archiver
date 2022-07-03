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
    // ZIP file full path
    private final Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void createZip(Path source) throws Exception {
        // Create a directory for archive.
        createDirectories(zipFile.getParent());

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFile))) {

            if (Files.isDirectory(source)) {
                // If a directory to be archived then get all files relative paths.
                FileManager fileManager = new FileManager(source);
                List<Path> relativeFilePaths = fileManager.getRelativePaths();

                // Add each file into archive.
                for (Path relativeFilePath : relativeFilePaths) {
                    addNewZipEntry(zipOut, source, relativeFilePath);
                }

            } else if (Files.isRegularFile(source)) {
                // If a file to be archived then get its name and parent folder.
                addNewZipEntry(zipOut, source.getParent(), source.getFileName());
            } else {
                throw new PathIsNotFoundException();
            }
        }
    }

    public void extractAll(Path outputFolder) throws Exception {
        checkZipFileExists();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {
            // Create output directory if not exists.
            createDirectories(outputFolder);

            // Iterate through all ZIP entries in ZIP file.
            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {

                Path fileFullName = outputFolder.resolve(zipEntry.getName());

                // Create directory for each file if needed.
                createDirectories(fileFullName.getParent());

                // Extract file data.
                try (OutputStream outputStream = Files.newOutputStream(fileFullName)) {
                    copyData(zipIn, outputStream);
                }
            }
        }
    }

    /* Single file removal. */
    public void removeFile(Path path) throws Exception {
        removeFiles(Collections.singletonList(path));
    }

    public void removeFiles(List<Path> pathList) throws Exception {
        checkZipFileExists();

        // Create temporary archive.
        Path tempZipFile = Files.createTempFile(null, null);

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipFile));
            ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {

                // Get relative file path inside the archive.
                Path archivedFile = Paths.get(zipEntry.getName());

                // Check if the file is in removal list.
                if (!pathList.contains(archivedFile)) {
                    // Transfer ZIP entry from old archive to temp archive.
                    transferZipEntry(zipIn, zipOut, zipEntry);
                } else {
                    ConsoleHelper.writeMessage(String.format("Removed: '%s'", archivedFile));
                }
            }
        }

        // Replace old ZIP with temp ZIP.
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /* Adding a single file */
    public void addFile(Path absolutePath) throws Exception {
        addFiles(Collections.singletonList(absolutePath));
    }

    public void addFiles(List<Path> absolutePaths) throws Exception {
        checkZipFileExists();

        // Create a temporary ZIP file.
        Path tempZipFile = Files.createTempFile(null, null);
        // File already in the archive.
        List<Path> archivedFiles = new ArrayList<>();

        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {
            try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

                ZipEntry zipEntry;
                while ((zipEntry = zipIn.getNextEntry()) != null) {

                    // Transfer ZIP entry from old archive to temp archive.
                    transferZipEntry(zipIn, zipOut, zipEntry);
                    archivedFiles.add(Paths.get(zipEntry.getName()));
                }
            }

            // Archiving new files.
            //TODO: add directory into archive
            for (Path filePath : absolutePaths) {

                if (Files.isRegularFile(filePath)) {

                    if (archivedFiles.contains(filePath.getFileName())) {
                        ConsoleHelper.writeMessage(String.format("File '%s' already exists in the archive.", filePath));
                    } else {
                        addNewZipEntry(zipOut, filePath.getParent(), filePath.getFileName());
                        ConsoleHelper.writeMessage(String.format("Added: '%s'", filePath));
                    }

                } else {
                    throw new PathIsNotFoundException();
                }
            }
        }

        // Replace old ZIP with temp ZIP.
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public List<FileProperties> getFilePropertiesList() throws Exception {
        checkZipFileExists();

        List<FileProperties> filePropertiesList = new ArrayList<>();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {

            ZipEntry zipEntry;
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                // Properties SIZE and COMPRESSED_SIZE are unknown until the file is read.
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

    /* Check if directory exists and create if not, as well as all parent directories. */
    private void createDirectories(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }
}
