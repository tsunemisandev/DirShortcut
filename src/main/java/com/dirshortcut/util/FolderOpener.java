package com.dirshortcut.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;

public class FolderOpener {

    public static boolean open(String path) {
        File folder = resolve(path);
        if (folder == null) return false;
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                // Use 'open' directly to avoid Desktop API Unicode issues on macOS
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
     * Resolves the path to an existing directory, trying NFC and NFD normalization
     * to handle macOS APFS Unicode (NFD) vs pasted text (NFC) differences.
     */
    private static File resolve(String path) {
        File f = new File(path);
        if (f.exists() && f.isDirectory()) return f;

        // Try NFD (macOS native normalization)
        String nfd = Normalizer.normalize(path, Normalizer.Form.NFD);
        File fNfd = new File(nfd);
        if (fNfd.exists() && fNfd.isDirectory()) return fNfd;

        // Try NFC
        String nfc = Normalizer.normalize(path, Normalizer.Form.NFC);
        File fNfc = new File(nfc);
        if (fNfc.exists() && fNfc.isDirectory()) return fNfc;

        return null;
    }
}
