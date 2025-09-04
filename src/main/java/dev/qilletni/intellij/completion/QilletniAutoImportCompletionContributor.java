package dev.qilletni.intellij.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import org.jetbrains.annotations.NotNull;

/**
 * Scaffold for auto-import suggestions. Currently no-ops; future iterations will
 * query stub indices for unimported symbols and add LookupElements that insert imports.
 */
public final class QilletniAutoImportCompletionContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        // TODO
    }
}