package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class FileSystemNode {
    private final String name;
    private final boolean isFile;
    private final Map<String, FileSystemNode> children = new ConcurrentHashMap<>();
    private final String createdAt;
    private final String modifiedAt;

    FileSystemNode(String name, boolean isFile, String createdAt, String modifiedAt) {
        this.name = name;
        this.isFile = isFile;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public void addFileSystemNode(String name, FileSystemNode fileSystemNode) {
        if(!this.children.containsKey(name)) {
            this.children.put(name, fileSystemNode);
        }
    }

    public FileSystemNode getFileSystemNode(String name) {
        return this.children.get(name);
    }

    public boolean isValidFileSystemNode(String name) {
        return this.children.containsKey(name);
    }

    public String getName() {
        return name;
    }

    public boolean isFile() {
        return isFile;
    }

    public Map<String, FileSystemNode> getChildren() {
        return children;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }
}

class File extends FileSystemNode {

    private final String content;
    private final String extendion;

    File(String name, String createdAt, String modifiedAt, String content, String extendion) {
        super(name, true, createdAt, modifiedAt);
        this.content = content;
        this.extendion = extendion;
    }

}

class Directory extends FileSystemNode {

    Directory(String name, String createdAt, String modifiedAt) {
        super(name, false, createdAt, modifiedAt);
    }

    public void addDirectory(FileSystemNode node) {
        this.addFileSystemNode(node.getName(), node);
    }

}

class FileSystem {
    private final FileSystemNode root;

    FileSystem(FileSystemNode root) {
        this.root = root;
    }

    public void createFile(String path) {
        List<DecodedPath> decodedPaths = this.decodePath(path);

        FileSystemNode iterator = root;

        for (DecodedPath decodedPath: decodedPaths) {
            if(!decodedPath.isFile) {
                iterator.addFileSystemNode(
                        decodedPath.name,
                        new Directory(
                                decodedPath.name,
                                String.valueOf(System.currentTimeMillis()),
                                String.valueOf(System.currentTimeMillis())
                        )
                );
            } else {
                iterator.addFileSystemNode(
                        decodedPath.name,
                        new File(
                                decodedPath.name,
                                String.valueOf(System.currentTimeMillis()),
                                String.valueOf(System.currentTimeMillis()),
                                "",
                                decodedPath.name.split("\\.")[1]
                        )
                );
            }

            iterator = iterator.getFileSystemNode(decodedPath.name);

        }
    }

    public boolean isValidPath(String path) {
        List<DecodedPath> decodedPaths = this.decodePath(path);

        FileSystemNode iterator = root;

        for (int i = 0; i < decodedPaths.size(); i++) {
            DecodedPath decodedPath = decodedPaths.get(i);

            if (!iterator.isValidFileSystemNode(decodedPath.name)) {
                return false;
            }

            iterator = iterator.getFileSystemNode(decodedPath.name);

            if (i == decodedPaths.size() - 1 && decodedPath.isFile != iterator.isFile()) {
                return false;
            }
        }

        return true;
    }

    public void print() {
        printIterator(root, 0);
    }

    private void printIterator(FileSystemNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + (node.isFile() ? "- " : " ") + node.getName());

        for (FileSystemNode child : node.getChildren().values()) {
            printIterator(child, depth + 1);
        }
    }


    private List<DecodedPath> decodePath(String path) {
        List<DecodedPath> decodedPaths = new ArrayList<>();

        String[] splitPath = path.split("/");

        for (String s : splitPath) {
            // can also validate
            decodedPaths.add(new DecodedPath(s, s.contains(".")));
        }

        return decodedPaths;

    }

    record DecodedPath(String name, boolean isFile) {}

}

// command design pattern -> (mkdir, ls, ..)

public class Main {
    public static void main(String[] args) {
        Directory directory = new Directory(
                "/",
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(System.currentTimeMillis()));
        FileSystem fileSystem = new FileSystem(directory);

        fileSystem.createFile("document/priyal.java");
        fileSystem.createFile("document/raj.java");
        fileSystem.print();
    }
}