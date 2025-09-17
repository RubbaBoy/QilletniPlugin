package dev.qilletni.intellij.project;

import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * First-class Qilletni entry in the New Project wizard.
 */
public final class QilletniLanguageNewProjectWizard implements LanguageGeneratorNewProjectWizard {
    @Override public @NotNull String getName() { return "Qilletni"; }
    @Override public Icon getIcon() { return null; }
    @Override public int getOrdinal() { return 100; }

    @Override
    public @NotNull NewProjectWizardStep createStep(@NotNull NewProjectWizardStep parent) {
        return new QilletniNewProjectWizardStep(parent);
    }
}
