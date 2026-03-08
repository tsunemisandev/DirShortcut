package com.dirshortcut.ui;

import com.dirshortcut.model.Group;
import com.dirshortcut.model.Shortcut;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GroupPanel extends JPanel {

    private final Group group;
    private final Runnable onChanged;
    private final List<ShortcutItem> shortcutItems = new ArrayList<>();

    private final JPanel header;
    private final JLabel titleLabel;
    private final JPanel body;
    private boolean collapsed = false;

    private static final Color HEADER_BG = new Color(60, 80, 180);
    private static final Color HEADER_FG = Color.WHITE;

    public GroupPanel(Group group, Runnable onChanged) {
        this.group     = group;
        this.onChanged = onChanged;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 8, 0));

        // ── Header ───────────────────────────────────────────────────────────
        header = new JPanel(new BorderLayout(6, 0));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(8, 12, 8, 8));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        titleLabel = new JLabel("▼ " + group.getName());
        titleLabel.setForeground(HEADER_FG);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerActions.setOpaque(false);

        JButton addBtn    = headerBtn("+ 追加");
        JButton renameBtn = headerBtn("✏");
        JButton deleteBtn = headerBtn("✕");
        headerActions.add(addBtn);
        headerActions.add(renameBtn);
        headerActions.add(deleteBtn);

        header.add(titleLabel,     BorderLayout.CENTER);
        header.add(headerActions,  BorderLayout.EAST);

        // ── Body ─────────────────────────────────────────────────────────────
        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(250, 251, 255));
        body.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, HEADER_BG));

        add(header);
        add(body);

        // Populate shortcuts
        rebuildShortcutItems();

        // ── Events ───────────────────────────────────────────────────────────
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // Only toggle collapse if not clicking a button
                if (!(e.getSource() instanceof JButton)) {
                    toggleCollapse();
                }
            }
        });
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { toggleCollapse(); }
        });

        addBtn.addActionListener(e -> showAddDialog());
        renameBtn.addActionListener(e -> showRenameDialog());
        deleteBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "グループ「" + group.getName() + "」とすべてのショートカットを削除しますか？",
                    "削除の確認", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Signal parent to remove this group — handled in MainWindow
                firePropertyChange("groupDeleted", null, group);
            }
        });
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public Group getGroup() { return group; }

    /** Filter visible shortcuts by query. Returns true if any shortcut is visible. */
    public boolean applyFilter(String query) {
        boolean anyVisible = false;
        for (ShortcutItem item : shortcutItems) {
            boolean visible = item.matches(query);
            item.setVisible(visible);
            item.updateDisplay(query);
            if (visible) anyVisible = true;
        }
        setVisible(query == null || query.isBlank() || anyVisible);
        return anyVisible;
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private void rebuildShortcutItems() {
        body.removeAll();
        shortcutItems.clear();
        for (Shortcut s : group.getShortcuts()) {
            addShortcutItem(s);
        }
        body.revalidate();
        body.repaint();
    }

    private void addShortcutItem(Shortcut s) {
        ShortcutItem item = new ShortcutItem(
                s,
                () -> {
                    group.removeShortcut(s);
                    rebuildShortcutItems();
                    onChanged.run();
                },
                () -> showEditDialog(s)
        );
        shortcutItems.add(item);
        body.add(item);
    }

    private void toggleCollapse() {
        collapsed = !collapsed;
        body.setVisible(!collapsed);
        titleLabel.setText((collapsed ? "▶ " : "▼ ") + group.getName());
    }

    private void showAddDialog() {
        JTextField pathField  = new JTextField(30);
        JTextField labelField = new JTextField(30);

        JPanel panel = new JPanel(new GridLayout(4, 1, 4, 4));
        panel.add(new JLabel("フォルダパス："));
        panel.add(pathField);
        panel.add(new JLabel("ラベル（任意）："));
        panel.add(labelField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "ショートカットを追加", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String path  = pathField.getText().trim();
            String label = labelField.getText().trim();
            if (!path.isEmpty()) {
                Shortcut s = new Shortcut(path, label.isEmpty() ? null : label);
                group.addShortcut(s);
                addShortcutItem(s);
                body.revalidate();
                body.repaint();
                onChanged.run();
            }
        }
    }

    private void showEditDialog(Shortcut s) {
        JTextField pathField  = new JTextField(s.getPath(), 30);
        JTextField labelField = new JTextField(s.getLabel() != null ? s.getLabel() : "", 30);

        JPanel panel = new JPanel(new GridLayout(4, 1, 4, 4));
        panel.add(new JLabel("フォルダパス："));
        panel.add(pathField);
        panel.add(new JLabel("ラベル（任意）："));
        panel.add(labelField);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "ショートカットを編集", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String path  = pathField.getText().trim();
            String label = labelField.getText().trim();
            if (!path.isEmpty()) {
                s.setPath(path);
                s.setLabel(label.isEmpty() ? null : label);
                rebuildShortcutItems();
                onChanged.run();
            }
        }
    }

    private void showRenameDialog() {
        String newName = JOptionPane.showInputDialog(this, "グループ名：", group.getName());
        if (newName != null && !newName.isBlank()) {
            group.setName(newName.trim());
            titleLabel.setText((collapsed ? "▶ " : "▼ ") + group.getName());
            onChanged.run();
        }
    }

    private JButton headerBtn(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(HEADER_FG);
        btn.setFont(btn.getFont().deriveFont(11f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(1, 6, 1, 6));
        return btn;
    }
}
