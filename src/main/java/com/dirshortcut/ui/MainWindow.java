package com.dirshortcut.ui;

import com.dirshortcut.model.Group;
import com.dirshortcut.persistence.Storage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {

    private final List<Group> groups;
    private final List<GroupPanel> groupPanels = new ArrayList<>();
    private final JPanel groupsContainer;
    private final JTextField searchField;
    private JScrollPane scrollPane;

    public MainWindow() {
        super("DirShortcut");
        this.groups = Storage.load();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Position: full screen height, fixed width, pinned to right edge
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screen = ge.getMaximumWindowBounds();
        int width = 420;
        setMinimumSize(new Dimension(width, screen.height));
        setPreferredSize(new Dimension(width, screen.height));
        setLocation(screen.x + screen.width - width, screen.y);

        // ── Root layout ───────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(240, 242, 250));
        setContentPane(root);

        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(new Color(40, 55, 140));
        topBar.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel appTitle = new JLabel("DirShortcut");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(appTitle.getFont().deriveFont(Font.BOLD, 16f));

        JButton newGroupBtn = new JButton("+ グループ");
        newGroupBtn.setForeground(Color.WHITE);
        newGroupBtn.setFont(newGroupBtn.getFont().deriveFont(12f));
        newGroupBtn.setFocusPainted(false);
        newGroupBtn.setBorderPainted(false);
        newGroupBtn.setContentAreaFilled(false);
        newGroupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topBar.add(appTitle,    BorderLayout.WEST);
        topBar.add(newGroupBtn, BorderLayout.EAST);

        // ── Search bar ────────────────────────────────────────────────────────
        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(new Color(230, 232, 248));
        searchBar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel searchIcon = new JLabel("🔍");
        searchField = new JTextField();
        searchField.setFont(searchField.getFont().deriveFont(13f));
        searchField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        searchField.putClientProperty("JTextField.placeholderText", "フォルダを検索...");

        searchBar.add(searchIcon,  BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(topBar,    BorderLayout.NORTH);
        topSection.add(searchBar, BorderLayout.SOUTH);

        // ── Groups container ──────────────────────────────────────────────────
        groupsContainer = new JPanel();
        groupsContainer.setLayout(new BoxLayout(groupsContainer, BoxLayout.Y_AXIS));
        groupsContainer.setBackground(new Color(240, 242, 250));
        groupsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(groupsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);

        root.add(topSection, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);

        // ── Populate groups ───────────────────────────────────────────────────
        for (Group g : groups) {
            addGroupPanel(g);
        }

        // ── Events ────────────────────────────────────────────────────────────
        newGroupBtn.addActionListener(e -> showNewGroupDialog());

        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                applyFilter(searchField.getText().trim());
            }
        });

        pack();
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private void addGroupPanel(Group group) {
        GroupPanel panel = new GroupPanel(group, this::save);

        panel.setOnAnyItemSelected(() ->
            groupPanels.stream().filter(p -> p != panel).forEach(GroupPanel::deselectAll)
        );
        panel.setOnMoveUp(() -> {
            int idx = groupPanels.indexOf(panel);
            if (idx > 0) moveGroup(idx, idx - 1);
        });
        panel.setOnMoveDown(() -> {
            int idx = groupPanels.indexOf(panel);
            if (idx < groupPanels.size() - 1) moveGroup(idx, idx + 1);
        });

        panel.addPropertyChangeListener("groupDeleted", evt -> {
            Group deleted = (Group) evt.getNewValue();
            groups.remove(deleted);
            groupsContainer.remove(panel);
            groupPanels.remove(panel);
            groupsContainer.revalidate();
            groupsContainer.repaint();
            save();
        });

        groupPanels.add(panel);
        groupsContainer.add(panel);
    }

    private void moveGroup(int fromIdx, int toIdx) {
        // Swap in data model
        Group g = groups.remove(fromIdx);
        groups.add(toIdx, g);

        // Swap panels
        GroupPanel p = groupPanels.remove(fromIdx);
        groupPanels.add(toIdx, p);

        // Rebuild container order (preserve scroll position)
        int scrollY = scrollPane.getVerticalScrollBar().getValue();
        groupsContainer.removeAll();
        for (GroupPanel gp : groupPanels) groupsContainer.add(gp);
        groupsContainer.revalidate();
        groupsContainer.repaint();
        SwingUtilities.invokeLater(() ->
            scrollPane.getVerticalScrollBar().setValue(scrollY)
        );
        save();
    }

    private void showNewGroupDialog() {
        String name = JOptionPane.showInputDialog(this, "グループ名：");
        if (name != null && !name.isBlank()) {
            Group g = new Group(name.trim());
            groups.add(g);
            addGroupPanel(g);
            groupsContainer.revalidate();
            groupsContainer.repaint();
            save();
        }
    }

    private void applyFilter(String query) {
        for (GroupPanel panel : groupPanels) {
            panel.applyFilter(query);
        }
        groupsContainer.revalidate();
        groupsContainer.repaint();
    }

    private void save() {
        Storage.save(groups);
    }
}
