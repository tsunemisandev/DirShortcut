package com.dirshortcut.ui;

import com.dirshortcut.model.Shortcut;
import com.dirshortcut.util.FolderOpener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ShortcutItem extends JPanel {

    private final Shortcut shortcut;
    private final Runnable onDelete;
    private final Runnable onEdit;
    private final JLabel nameLabel;

    private static final Color HOVER_BG    = new Color(230, 236, 255);
    private static final Color SELECTED_BG = new Color(200, 212, 255);
    private static final Color NORMAL_BG   = new Color(245, 246, 250);
    private static final Color ERROR_COLOR = new Color(200, 50, 50);

    private boolean selected = false;
    private Runnable onSelected; // called when this item becomes selected

    public void setOnSelected(Runnable onSelected) {
        this.onSelected = onSelected;
    }

    public void deselect() {
        selected = false;
        setBackground(NORMAL_BG);
    }

    public ShortcutItem(Shortcut shortcut, Runnable onDelete, Runnable onEdit) {
        this.shortcut = shortcut;
        this.onDelete = onDelete;
        this.onEdit   = onEdit;

        setLayout(new BorderLayout(8, 0));
        setBackground(NORMAL_BG);
        setBorder(new EmptyBorder(6, 12, 6, 8));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Icon + label
        JLabel icon = new JLabel("📁");
        icon.setFont(icon.getFont().deriveFont(14f));

        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
        updateDisplay(null);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(icon);
        left.add(nameLabel);

        // Action buttons (visible on hover)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        actions.setOpaque(false);
        actions.setVisible(false);

        JButton editBtn   = smallButton("✏");
        JButton deleteBtn = smallButton("✕");
        actions.add(editBtn);
        actions.add(deleteBtn);

        add(left,    BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);

        // Tooltip: full path
        setToolTipText(shortcut.getPath());

        // Hover / click effects
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!selected) setBackground(HOVER_BG);
                actions.setVisible(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ShortcutItem.this);
                if (!ShortcutItem.this.contains(p)) {
                    setBackground(selected ? SELECTED_BG : NORMAL_BG);
                    actions.setVisible(false);
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (e.getClickCount() == 1) {
                    // Single click: select this item (notify others to deselect)
                    selected = true;
                    setBackground(SELECTED_BG);
                    if (onSelected != null) onSelected.run();
                } else if (e.getClickCount() == 2) {
                    // Double click: open folder
                    selected = false;
                    setBackground(HOVER_BG);
                    boolean opened = FolderOpener.open(shortcut.getPath());
                    if (!opened) {
                        nameLabel.setForeground(ERROR_COLOR);
                        nameLabel.setToolTipText("パスが見つかりません：" + shortcut.getPath());
                    }
                }
            }
        };

        // Apply hover adapter to this panel AND all child components so
        // entering/exiting children doesn't flicker the hover state.
        addMouseListener(hoverAdapter);
        for (Component child : getComponents()) {
            addHoverToComponent(child, hoverAdapter);
        }
        addHoverToComponent(editBtn, hoverAdapter);
        addHoverToComponent(deleteBtn, hoverAdapter);

        editBtn.addActionListener(e -> onEdit.run());
        deleteBtn.addActionListener(e -> onDelete.run());
    }

    /** Re-render label, optionally highlighting a search query. */
    public void updateDisplay(String searchQuery) {
        String display = shortcut.getDisplayName();
        if (searchQuery == null || searchQuery.isBlank()) {
            nameLabel.setText(display);
            nameLabel.setForeground(Color.DARK_GRAY);
        } else {
            String lower   = display.toLowerCase();
            String query   = searchQuery.toLowerCase();
            int idx        = lower.indexOf(query);
            if (idx >= 0) {
                String before  = escapeHtml(display.substring(0, idx));
                String match   = escapeHtml(display.substring(idx, idx + query.length()));
                String after   = escapeHtml(display.substring(idx + query.length()));
                nameLabel.setText("<html>" + before
                        + "<span style='background:#FFE066;'>" + match + "</span>"
                        + after + "</html>");
                nameLabel.setForeground(Color.DARK_GRAY);
            } else {
                nameLabel.setText(display);
                nameLabel.setForeground(Color.DARK_GRAY);
            }
        }
    }

    /** Returns true if this shortcut matches the given query. */
    public boolean matches(String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase();
        return shortcut.getDisplayName().toLowerCase().contains(q)
                || shortcut.getPath().toLowerCase().contains(q);
    }

    public Shortcut getShortcut() {
        return shortcut;
    }

    private JButton smallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(11f));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(1, 4, 1, 4));
        return btn;
    }

    private void addHoverToComponent(Component c, MouseAdapter adapter) {
        if (c instanceof JComponent jc) {
            jc.addMouseListener(adapter);
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
