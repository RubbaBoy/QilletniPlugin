package dev.qilletni.intellij.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import dev.qilletni.intellij.spotify.auth.SpotifyAuthService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class QilletniSpotifyConfigurable implements Configurable {
    private JPanel panel;
    private JBTextField clientIdField;
    private JSpinner portSpinner;
    private JBLabel statusLabel;
    private JButton signInButton;
    private JButton signOutButton;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() { return "Qilletni: Spotify"; }

    @Override
    public @Nullable JComponent createComponent() {
        var svc = SpotifyAuthService.getInstance();
        panel = new JPanel(new GridBagLayout());
        var gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = JBUI.insets(4); gc.anchor = GridBagConstraints.WEST;
        panel.add(new JBLabel("Client ID:"), gc);
        gc.gridx = 1;
        clientIdField = new JBTextField(40);
        clientIdField.setText(svc.getClientId());
        panel.add(clientIdField, gc);

        gc.gridx = 0; gc.gridy++;
        panel.add(new JBLabel("Redirect Port (loopback):"), gc);
        gc.gridx = 1;
        portSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1, svc.getPort()), 1, 65535, 1));
        panel.add(portSpinner, gc);

        gc.gridx = 0; gc.gridy++;
        panel.add(new JBLabel("Status:"), gc);
        gc.gridx = 1;
        statusLabel = new JBLabel(statusText());
        panel.add(statusLabel, gc);

        gc.gridx = 0; gc.gridy++;
        signInButton = new JButton("Sign In");
        signInButton.addActionListener(e -> onSignInOrCancel());
        panel.add(signInButton, gc);
        gc.gridx = 1;
        signOutButton = new JButton("Sign Out");
        signOutButton.addActionListener(e -> doSignOut());
        panel.add(signOutButton, gc);
        updateButtons();
        return panel;
    }

    private String statusText() {
        return SpotifyAuthService.getInstance().isSignedIn() ? "Signed in" : "Signed out";
    }

    private void onSignInOrCancel() {
        var svc = SpotifyAuthService.getInstance();
        if (svc.isSignInInProgress()) {
            svc.cancelSignIn();
            updateButtons();
            return;
        }
        apply();
        var project = ProjectManager.getInstance().getDefaultProject();
        updateButtons();
        svc.signIn(project).whenComplete((v, ex) -> SwingUtilities.invokeLater(() -> {
            statusLabel.setText(statusText());
            updateButtons();
        }));
    }

    private void doSignOut() {
        SpotifyAuthService.getInstance().signOut();
        statusLabel.setText(statusText());
        updateButtons();
    }

    private void updateButtons() {
        var svc = SpotifyAuthService.getInstance();
        boolean inProgress = svc.isSignInInProgress();
        signInButton.setText(inProgress ? "Cancel" : "Sign In");
        // Disable Client ID and Port while a sign-in is in progress to avoid changing during flow
        clientIdField.setEnabled(!inProgress);
        portSpinner.setEnabled(!inProgress);
    }

    @Override
    public boolean isModified() {
        var svc = SpotifyAuthService.getInstance();
        String id = clientIdField != null ? clientIdField.getText().trim() : null;
        int port = (portSpinner != null) ? ((Number) portSpinner.getValue()).intValue() : 0;
        return !java.util.Objects.equals(id.isEmpty() ? null : id, svc.getClientId()) || port != svc.getPort();
    }

    @Override
    public void apply() {
        var svc = SpotifyAuthService.getInstance();
        String id = clientIdField.getText().trim();
        svc.setClientId(id.isEmpty() ? null : id);
        int port = ((Number) portSpinner.getValue()).intValue();
        svc.setPort(port);
    }
}
