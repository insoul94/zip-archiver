package com.javarush.task.task31.task3110;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final Path rootPath;
    private final List<Path> relativePaths;

    public FileManager(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        this.relativePaths = new ArrayList<>();
        collectRelativePaths(rootPath);
    }

    public List<Path> getRelativePaths() {
        return relativePaths;
    }

    private void collectRelativePaths(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            relativePaths.add(rootPath.relativize(path));
        }
        else if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path entry : directoryStream) {
                    collectRelativePaths(entry);
                }
            }
        }
    }
}
