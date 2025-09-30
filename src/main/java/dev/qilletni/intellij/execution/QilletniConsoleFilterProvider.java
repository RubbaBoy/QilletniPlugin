package dev.qilletni.intellij.execution;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class QilletniConsoleFilterProvider implements ConsoleFilterProvider {
    @Override
    public Filter @NotNull [] getDefaultFilters(@NotNull Project project) {
        return new Filter[]{ new QilletniOutputHyperlinkFilter(project) };
    }
}
