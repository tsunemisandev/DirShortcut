package com.dirshortcut.model;

import java.util.ArrayList;
import java.util.List;

public class Group {

    private String name;
    private List<Shortcut> shortcuts;

    public Group(String name) {
        this.name = name;
        this.shortcuts = new ArrayList<>();
    }

    public Group(String name, List<Shortcut> shortcuts) {
        this.name = name;
        this.shortcuts = new ArrayList<>(shortcuts);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Shortcut> getShortcuts() {
        return shortcuts;
    }

    public void addShortcut(Shortcut shortcut) {
        shortcuts.add(shortcut);
    }

    public void removeShortcut(Shortcut shortcut) {
        shortcuts.remove(shortcut);
    }

    public void moveShortcut(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= shortcuts.size()) return;
        if (toIndex < 0 || toIndex >= shortcuts.size()) return;
        Shortcut s = shortcuts.remove(fromIndex);
        shortcuts.add(toIndex, s);
    }
}
