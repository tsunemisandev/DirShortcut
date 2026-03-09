package com.dirshortcut.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;

public class FolderOpener {

    public static boolean isNetworkPath(String path) {
        if (path == null) return false;
        String p = path.trim();
        // Windows UNC: \\server\share
        if (p.startsWith("\\\\") || p.startsWith("//")) return true;
        // macOS network mounts
        if (p.startsWith("/Volumes/") || p.startsWith("/net/") || p.startsWith("/mnt/")) return true;
        return false;
    }

    public static boolean open(String path) {
        if (path == null || path.isBlank()) return false;

        String os = System.getProperty("os.name").toLowerCase();

        // ── Windows UNC path (\\server\share\...) ────────────────────────────
        // File.exists() often returns false for UNC before the shell resolves it.
        // Delegate directly to explorer.exe.
        String trimmed = path.trim();
        if (os.contains("win") && (trimmed.startsWith("\\\\") || trimmed.startsWith("//"))) {
            try {
                new ProcessBuilder("explorer.exe", trimmed.replace('/', '\\')).start();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // ── Resolve local / mounted path ─────────────────────────────────────
        File folder = resolve(trimmed);
        if (folder == null) return false;

        try {
            if (os.contains("mac")) {
                new ProcessBuilder("open", folder.getAbsolutePath()).start();
                return true;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
                return true;
            }
            if (os.contains("linux")) {
                new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Resolves a path to an existing directory.
     * Tries NFC and NFD normalization to handle macOS APFS (NFD) vs pasted text (NFC).
     */
    private static File resolve(String path) {
        File f = new File(path);
        if (f.exists() && f.isDirectory()) return f;

        String nfd = Normalizer.normalize(path, Normalizer.Form.NFD);
        File fNfd = new File(nfd);
        if (fNfd.exists() && fNfd.isDirectory()) return fNfd;

        String nfc = Normalizer.normalize(path, Normalizer.Form.NFC);
        File fNfc = new File(nfc);
        if (fNfc.exists() && fNfc.isDirectory()) return fNfc;

        return null;
    }
}
