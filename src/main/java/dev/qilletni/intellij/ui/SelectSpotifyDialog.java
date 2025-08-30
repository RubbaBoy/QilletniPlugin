package dev.qilletni.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBImageIcon;
import com.intellij.util.ui.JBUI;
import dev.qilletni.intellij.spotify.QilletniSpotifyService;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicTypeContext;
import dev.qilletni.intellij.spotify.auth.SpotifyAuthService;
import com.intellij.openapi.options.ShowSettingsUtil;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicChoice;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DialogWrapper for Spotify selection with type switching, debounced search, artwork, and simple token UX.
 */
public class SelectSpotifyDialog extends DialogWrapper {
    // Sorting/filtering helpers
    private JComboBox<String> sortBoxRef;
    private JBTextField filterFieldRef;
    private static final Map<String, Icon> ICON_CACHE = new ConcurrentHashMap<>();
    private JButton retryButtonRef;

    private final Project project;
    private MusicType selectedType;
    private final JBCheckBox includeSongKeyword;
    private final DefaultListModel<MusicChoice> model = new DefaultListModel<>();
    private final JBList<MusicChoice> list = new JBList<>(model);
    private final JBTextField search = new JBTextField();
    private final QilletniSpotifyService service = new QilletniSpotifyService();

    private final Timer debounceTimer;

    // Token & error UI
    private JBLabel errorLabel;
    private JPanel settingsPanel;
    private JButton openSettingsButton;

    public SelectSpotifyDialog(Project project, MusicTypeContext initialType) {
        super(project, true);
        this.project = project;
        this.selectedType = MusicType.fromContext(initialType);

        setTitle("Select Spotify Music");
        includeSongKeyword = new JBCheckBox("Include \"song\" keyword", false); // default OFF per spec
        includeSongKeyword.setEnabled(initialType == MusicTypeContext.SONG);
        debounceTimer = new Timer(300, e -> performSearch());
        debounceTimer.setRepeats(false);
        init();
        getOKAction().setEnabled(true);
        list.addListSelectionListener(e -> getOKAction().setEnabled(list.getSelectedValue() != null));
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && getOKAction().isEnabled()) doOKAction();
            }
        });
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        var panel = new JPanel(new BorderLayout(JBUI.scale(8), JBUI.scale(8)));

        // North: Type switch + search field
        var north = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        var typeBox = new JComboBox<>(new MusicType[]{MusicType.SONG, MusicType.ALBUM, MusicType.COLLECTION});
        typeBox.setSelectedItem(selectedType);
        typeBox.addActionListener(e -> {
            selectedType = (MusicType) typeBox.getSelectedItem();
            includeSongKeyword.setEnabled(selectedType == MusicType.SONG);
            debounceTimer.restart();
        });
        north.add(typeBox, BorderLayout.WEST);

        var searchAndSort = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        var sortBox = new JComboBox<>(new String[]{"Best match","Name","Year"});
        sortBox.addActionListener(e -> applyFilterAndSort());
        var filterField = new JBTextField();
        filterField.getEmptyText().setText("Filter results");
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { applyFilterAndSort(); }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        });

        var searchPanel = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        search.getEmptyText().setText("Type to search Spotify");
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { debounceTimer.restart(); }
            @Override public void insertUpdate(DocumentEvent e) { changed(); }
            @Override public void removeUpdate(DocumentEvent e) { changed(); }
            @Override public void changedUpdate(DocumentEvent e) { changed(); }
        });
        searchPanel.add(search, BorderLayout.CENTER);
        searchAndSort.add(searchPanel, BorderLayout.CENTER);

        var rightTools = new JPanel(new BorderLayout(JBUI.scale(8), 0));
        rightTools.add(sortBox, BorderLayout.WEST);
        rightTools.add(filterField, BorderLayout.CENTER);
        searchAndSort.add(rightTools, BorderLayout.EAST);

        north.add(searchAndSort, BorderLayout.CENTER);
        panel.add(north, BorderLayout.NORTH);

        // store components for later use
        this.sortBoxRef = sortBox;
        this.filterFieldRef = filterField;

        // Error + token panel (hidden until needed)
        var infoPanel = new JPanel(new BorderLayout());
        errorLabel = new JBLabel("");
        errorLabel.setForeground(UIManager.getColor("Label.errorForeground"));
        errorLabel.setBorder(JBUI.Borders.empty(2));
        infoPanel.add(errorLabel, BorderLayout.NORTH);
        // Retry button for transient errors
        var retryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(4), 0));
        retryButtonRef = new JButton("Retry");
        retryButtonRef.addActionListener(e -> performSearch());
        retryButtonRef.setVisible(false);
        retryPanel.add(retryButtonRef);
        infoPanel.add(retryPanel, BorderLayout.CENTER);
        settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, JBUI.scale(4), 0));
        openSettingsButton = new JButton("Open Spotify Settings…");
        openSettingsButton.addActionListener(e -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Qilletni: Spotify");
        });
        settingsPanel.add(openSettingsButton);
        settingsPanel.setVisible(false);
        infoPanel.add(settingsPanel, BorderLayout.SOUTH);
        panel.add(infoPanel, BorderLayout.WEST);

        // Center: results list with icon + two-line text
        list.setCellRenderer((lst, value, index, isSelected, cellHasFocus) -> {
            var root = new JPanel(new BorderLayout(JBUI.scale(6), 0));
            root.setBorder(JBUI.Borders.empty(4));
            var iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(JBUI.scale(24), JBUI.scale(24)));
            if (value.imageUrl() != null) {
                iconLabel.setIcon(getIcon(value.imageUrl()));
            }
            root.add(iconLabel, BorderLayout.WEST);
            var textPanel = new JPanel(new BorderLayout());
            var name = new JLabel(value.name());
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            var secondary = switch (value.type()) {
                case SONG, ALBUM -> String.join(", ", value.artists() == null ? java.util.List.of() : value.artists());
                case COLLECTION -> value.owner() != null ? value.owner() : "";
            };
            var meta = new JLabel(secondary);
            meta.setForeground(UIManager.getColor("Label.disabledForeground"));
            textPanel.add(name, BorderLayout.NORTH);
            textPanel.add(meta, BorderLayout.SOUTH);
            root.add(textPanel, BorderLayout.CENTER);
            if (isSelected) {
                root.setBackground(lst.getSelectionBackground());
                textPanel.setBackground(lst.getSelectionBackground());
            }
            return root;
        });
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        // South: options
        var south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(includeSongKeyword);
        panel.add(south, BorderLayout.SOUTH);

        // If not signed in, reveal settings button
        if (!SpotifyAuthService.getInstance().isSignedIn()) {
            showError("Not signed in to Spotify. Open Settings to sign in.");
            settingsPanel.setVisible(true);
        }
        return panel;
    }

    private Icon getIcon(String url) {
        var cached = ICON_CACHE.get(url);
        if (cached != null) return cached;
        // Load async and return placeholder now
        var placeholder = new JBLabel().getIcon();
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage img = ImageIO.read(new URL(url));
                if (img != null) {
                    var size = JBUI.scale(24);
                    Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                    var icon = new JBImageIcon((Image) scaled);
                    ICON_CACHE.put(url, icon);
                    SwingUtilities.invokeLater(() -> {
                        list.revalidate();
                        list.repaint();
                    });
                }
            } catch (Exception ignored) {}
        });
        return placeholder;
    }

    private void showError(@Nullable String msg) {
        errorLabel.setText(msg == null ? "" : msg);
        errorLabel.setVisible(msg != null);
    }

    private void performSearch() {
        String q = search.getText().trim();
        model.clear();
        if (q.isEmpty()) return;
        if (!SpotifyAuthService.getInstance().isSignedIn()) {
            showError("Not signed in to Spotify. Open Settings to sign in.");
            settingsPanel.setVisible(true);
            return;
        }
        showError(null);
        settingsPanel.setVisible(false);
        CompletableFuture<java.util.List<MusicChoice>> cf;
        switch (selectedType) {
            case SONG -> cf = service.searchTracks(q, 25, 0);
            case ALBUM -> cf = service.searchAlbums(q, 25, 0);
            case COLLECTION -> cf = service.searchPlaylists(q, 25, 0);
            default -> cf = CompletableFuture.completedFuture(java.util.List.of());
        }
        cf.thenAccept(this::setResults).exceptionally(ex -> {
            handleError(ex);
            return null;
        });
    }

    private void applyFilterAndSort() {
        // Apply simple client-side filter and sort on the current model
        if (model.isEmpty()) return;
        var filter = filterFieldRef != null ? filterFieldRef.getText().trim().toLowerCase() : "";
        var sortKey = sortBoxRef != null ? (String) sortBoxRef.getSelectedItem() : "Best match";
        // Snapshot current items
        java.util.List<MusicChoice> items = new java.util.ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) items.add(model.getElementAt(i));
        // Filter
        if (!filter.isEmpty()) {
            items.removeIf(mc -> {
                var name = mc.name() != null ? mc.name().toLowerCase() : "";
                var meta = switch (mc.type()) {
                    case SONG, ALBUM -> String.join(", ", mc.artists() == null ? java.util.List.of() : mc.artists()).toLowerCase();
                    case COLLECTION -> mc.owner() != null ? mc.owner().toLowerCase() : "";
                };
                return !(name.contains(filter) || meta.contains(filter));
            });
        }
        // Sort
        if ("Name".equals(sortKey)) {
            items.sort(java.util.Comparator.comparing(mc -> mc.name() == null ? "" : mc.name(), String.CASE_INSENSITIVE_ORDER));
        } else if ("Year".equals(sortKey)) {
            items.sort((a, b) -> {
                Integer ya = a.year() == null ? Integer.MIN_VALUE : a.year();
                Integer yb = b.year() == null ? Integer.MIN_VALUE : b.year();
                int cmp = Integer.compare(yb, ya); // desc
                if (cmp != 0) return cmp;
                return (a.name() == null ? "" : a.name()).compareToIgnoreCase(b.name() == null ? "" : b.name());
            });
        }
        // Push back
        model.clear();
        for (var mc : items) model.addElement(mc);
        if (!model.isEmpty()) list.setSelectedIndex(0);
        list.revalidate();
        list.repaint();
    }

    private void handleError(Throwable ex) {
        // Inspect cause chain for SpotifyWebApiException or IO
        Throwable cause = ex instanceof java.util.concurrent.CompletionException ? ex.getCause() : ex;
        String msg = "Error contacting Spotify.";
        if (cause != null) {
            if (cause instanceof se.michaelthelin.spotify.exceptions.SpotifyWebApiException) {
                var text = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase();
                if (text.contains("401") || text.contains("unauthorized") || text.contains("invalid access")) {
                    msg = "Invalid or expired token. Please re‑sign in via Settings.";
                    settingsPanel.setVisible(true);
                } else if (text.contains("429") || text.contains("rate limit")) {
                    msg = "Rate-limited by Spotify. Please retry shortly.";
                } else {
                    msg = "Spotify API error. Please retry.";
                }
            } else if (cause instanceof java.io.IOException) {
                msg = "Network error. Check connection and Retry.";
            }
        }
        final String fmsg = msg;
        SwingUtilities.invokeLater(() -> {
            showError(fmsg);
            if (retryButtonRef != null) retryButtonRef.setVisible(true);
        });
    }

    private void setResults(java.util.List<MusicChoice> results) {
        SwingUtilities.invokeLater(() -> {
            model.clear();
            if (results == null || results.isEmpty()) {
                showError("No results or an error occurred. Retry? ");
                if (retryButtonRef != null) retryButtonRef.setVisible(true);
                return;
            }
            showError(null);
            if (retryButtonRef != null) retryButtonRef.setVisible(false);
            for (var mc : results) model.addElement(mc);
            if (!model.isEmpty()) list.setSelectedIndex(0);
            applyFilterAndSort();
        });
    }

    @Override
    protected void doOKAction() {
        if (list.getSelectedValue() == null) return;
        super.doOKAction();
    }

    public Optional<MusicChoice> getSelection() { return Optional.ofNullable(list.getSelectedValue()); }

    public boolean includeKeyword() { return includeSongKeyword.isSelected(); }

    public MusicType getType() { return selectedType; }
}
