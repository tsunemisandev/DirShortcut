package com.dirshortcut.persistence;

import com.dirshortcut.model.Group;
import com.dirshortcut.model.Shortcut;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hand-rolled JSON persistence — no third-party libraries.
 * Saves/loads groups from a data.json file next to the jar (or in the working directory).
 */
public class Storage {

    private static final Path DATA_FILE = Paths.get(
            System.getProperty("user.home"), ".dirshortcut", "data.json"
    );

    public static List<Group> load() {
        if (!Files.exists(DATA_FILE)) return new ArrayList<>();
        try {
            String json = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
            return parseGroups(json);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void save(List<Group> groups) {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            String json = toJson(groups);
            Files.writeString(DATA_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Serialization ──────────────────────────────────────────────────────────

    private static String toJson(List<Group> groups) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"groups\": [\n");
        for (int i = 0; i < groups.size(); i++) {
            Group g = groups.get(i);
            sb.append("    {\n");
            sb.append("      \"name\": ").append(jsonString(g.getName())).append(",\n");
            sb.append("      \"shortcuts\": [\n");
            List<Shortcut> shortcuts = g.getShortcuts();
            for (int j = 0; j < shortcuts.size(); j++) {
                Shortcut s = shortcuts.get(j);
                sb.append("        {\n");
                sb.append("          \"path\": ").append(jsonString(s.getPath())).append(",\n");
                sb.append("          \"label\": ").append(jsonString(s.getLabel())).append("\n");
                sb.append("        }");
                if (j < shortcuts.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("      ]\n");
            sb.append("    }");
            if (i < groups.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private static String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    // ── Deserialization (minimal hand-rolled parser) ───────────────────────────

    private static List<Group> parseGroups(String json) {
        List<Group> groups = new ArrayList<>();
        int groupsStart = json.indexOf("\"groups\"");
        if (groupsStart < 0) return groups;
        int arrayStart = json.indexOf('[', groupsStart);
        if (arrayStart < 0) return groups;

        // Split into group objects
        List<String> groupObjects = extractObjects(json, arrayStart);
        for (String obj : groupObjects) {
            String name = extractStringValue(obj, "name");
            List<Shortcut> shortcuts = new ArrayList<>();

            int shortcutsStart = obj.indexOf("\"shortcuts\"");
            if (shortcutsStart >= 0) {
                int sArrayStart = obj.indexOf('[', shortcutsStart);
                if (sArrayStart >= 0) {
                    List<String> shortcutObjects = extractObjects(obj, sArrayStart);
                    for (String so : shortcutObjects) {
                        String path = extractStringValue(so, "path");
                        String label = extractStringValue(so, "label");
                        if (path != null) {
                            shortcuts.add(new Shortcut(path, label));
                        }
                    }
                }
            }

            if (name != null) {
                groups.add(new Group(name, shortcuts));
            }
        }
        return groups;
    }

    /** Extract top-level JSON objects { ... } from inside an array starting at arrayStart. */
    private static List<String> extractObjects(String json, int arrayStart) {
        List<String> objects = new ArrayList<>();
        int i = arrayStart + 1;
        int len = json.length();
        while (i < len) {
            // skip whitespace and commas
            char c = json.charAt(i);
            if (c == ']') break;
            if (c == '{') {
                int end = findMatchingBrace(json, i);
                if (end < 0) break;
                objects.add(json.substring(i, end + 1));
                i = end + 1;
            } else {
                i++;
            }
        }
        return objects;
    }

    private static int findMatchingBrace(String json, int start) {
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && inString) { i++; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    /** Extract the string value for a given key. Returns null if key missing or value is JSON null. */
    private static String extractStringValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx < 0) return null;
        int colon = json.indexOf(':', keyIdx + search.length());
        if (colon < 0) return null;
        int valStart = colon + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) valStart++;
        if (valStart >= json.length()) return null;
        if (json.startsWith("null", valStart)) return null;
        if (json.charAt(valStart) != '"') return null;
        // read string
        StringBuilder sb = new StringBuilder();
        int i = valStart + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= json.length()) break;
                char esc = json.charAt(i);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(esc);
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
            i++;
        }
        return sb.toString();
    }
}
