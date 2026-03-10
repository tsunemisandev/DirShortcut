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
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GroupPanel extends JPanel {

    private final Group group;
    private final Runnable onChanged;
    private final List<ShortcutItem> shortcutItems = new ArrayList<>();
    private Runnable onAnyItemSelected;
    private Runnable onMoveUp;
    private Runnable onMoveDown;
    private Supplier<List<String>> groupNamesProvider;
    private BiConsumer<Shortcut, String> onMoveToGroup;

    public void setOnAnyItemSelected(Runnable r)                          { this.onAnyItemSelected  = r; }
    public void setOnMoveUp(Runnable r)                                   { this.onMoveUp           = r; }
    public void setOnMoveDown(Runnable r)                                 { this.onMoveDown         = r; }
    public void setGroupNamesProvider(Supplier<List<String>> p)           { this.groupNamesProvider = p; }
    public void setOnMoveToGroup(BiConsumer<Shortcut, String> c)          { this.onMoveToGroup      = c; }

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

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
        headerActions.setOpaque(false);

        JButton upBtn     = headerBtn("▲");
        JButton downBtn   = headerBtn("▼");
        JButton addBtn    = headerBtn("+");
        JButton renameBtn = headerBtn("✏");
        JButton deleteBtn = headerBtn("✕");
        headerActions.add(upBtn);
        headerActions.add(downBtn);
        headerActions.add(addBtn);
        headerActions.add(renameBtn);
        headerActions.add(deleteBtn);

        header.add(titleLabel,    BorderLayout.CENTER);
        header.add(headerActions, BorderLayout.EAST);

        // ── Body ─────────────────────────────────────────────────────────────
        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(250, 251, 255));
        body.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, HEADER_BG));

        add(header);
        add(body);

        rebuildShortcutItems();

        // ── Events ───────────────────────────────────────────────────────────
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!(e.getSource() instanceof JButton)) toggleCollapse();
            }
        });
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { toggleCollapse(); }
        });

        upBtn.addActionListener(e     -> { if (onMoveUp   != null) onMoveUp.run();   });
        downBtn.addActionListener(e   -> { if (onMoveDown != null) onMoveDown.run(); });
        addBtn.addActionListener(e    -> showAddDialog());
        renameBtn.addActionListener(e -> showRenameDialog());
        deleteBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "グループ「" + group.getName() + "」とすべてのショートカットを削除しますか？",
                    "削除の確認", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                firePropertyChange("groupDeleted", null, group);
            }
        });
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public Group getGroup() { return group; }

    public void rebuild() { rebuildShortcutItems(); }

    public void deselectAll() {
        shortcutItems.forEach(ShortcutItem::deselect);
    }

    public boolean applyFilter(String query) {
        if (query == null || query.isBlank()) {
            shortcutItems.forEach(i -> { i.setVisible(true); i.updateDisplay(null); });
            setVisible(true);
            return true;
        }
        boolean groupMatches = group.getName().toLowerCase().contains(query.toLowerCase());
        boolean anyVisible = false;
        for (ShortcutItem item : shortcutItems) {
            boolean visible = groupMatches || item.matches(query);
            item.setVisible(visible);
            item.updateDisplay(groupMatches ? null : query);
            if (visible) anyVisible = true;
        }
        setVisible(anyVisible);
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
        item.setOnSelected(() -> {
            shortcutItems.stream().filter(i -> i != item).forEach(ShortcutItem::deselect);
            if (onAnyItemSelected != null) onAnyItemSelected.run();
        });
        item.setGroupNamesProvider(groupNamesProvider);
        item.setOnMoveToGroup(targetGroupName -> {
            if (onMoveToGroup != null) onMoveToGroup.accept(s, targetGroupName);
        });

        int currentIndex = group.getShortcuts().indexOf(s);
        item.setOnMoveUp(() -> {
            int idx = group.getShortcuts().indexOf(s);
            if (idx > 0) {
                group.moveShortcut(idx, idx - 1);
                rebuildShortcutItems();
                onChanged.run();
            }
        });
        item.setOnMoveDown(() -> {
            int idx = group.getShortcuts().indexOf(s);
            if (idx < group.getShortcuts().size() - 1) {
                group.moveShortcut(idx, idx + 1);
                rebuildShortcutItems();
                onChanged.run();
            }
        });

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

        JPanel panel = new JPanel(new GridLayout(5, 1, 4, 4));
        panel.add(new JLabel("フォルダパス："));
        panel.add(pathField);
        panel.add(new JLabel("<html><small style='color:gray'>例：C:\\Users\\you\\folder　または　\\\\server\\share\\folder</small></html>"));
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
        btn.setMargin(new Insets(1, 4, 1, 4));
        return btn;
    }
}
