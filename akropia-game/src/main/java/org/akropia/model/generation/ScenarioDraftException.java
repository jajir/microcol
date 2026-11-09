package org.akropia.model.generation;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Rejection of an authored scenario draft, a requested edit or a launch. It is
 * thrown before any part of the draft changes.
 */
public final class ScenarioDraftException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final List<ScenarioProblem> problems;

    ScenarioDraftException(final List<ScenarioProblem> problems) {
        super(String.valueOf(problems));
        Preconditions.checkArgument(!problems.isEmpty(), "No problem");
        this.problems = ImmutableList.copyOf(problems);
    }

    ScenarioDraftException(final ScenarioProblem problem) {
        this(ImmutableList.of(problem));
    }

    /**
     * @return every reason, at least one
     */
    public List<ScenarioProblem> getProblems() {
        return problems;
    }

}
