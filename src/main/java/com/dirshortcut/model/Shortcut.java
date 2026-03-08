package com.dirshortcut.model;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Shortcut {

    private String path;
    private String label; // nullable — if null, use folder name from path

    public Shortcut(String path, String label) {
        this.path = path;
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /** Display name: custom label if set, otherwise last segment of the path. */
    public String getDisplayName() {
        if (label != null && !label.isBlank()) {
            return label;
        }
        Path p = Paths.get(path);
        Path fileName = p.getFileName();
        return fileName != null ? fileName.toString() : path;
    }
}
