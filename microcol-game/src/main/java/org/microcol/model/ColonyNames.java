package org.microcol.model;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

final class ColonyNames {
    private static final List<String> NAMES = List.of("New Amsterdam", "Delft",
            "Haarlem", "Leiden", "Rotterdam", "Utrecht", "Eindhoven", "Tilburg",
            "Groningen", "Almere Stad", "Breda");
    private final Model model;

    ColonyNames(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    static String normalize(final String value) {
        Preconditions.checkNotNull(value, "Name is null");
        int start = 0;
        int end = value.length();
        while (start < end && isSpace(value.codePointAt(start))) {
            start += Character.charCount(value.codePointAt(start));
        }
        while (end > start && isSpace(value.codePointBefore(end))) {
            end -= Character.charCount(value.codePointBefore(end));
        }
        return Normalizer.normalize(value.substring(start, end),
                Normalizer.Form.NFC);
    }

    private static boolean isSpace(final int codePoint) {
        return Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint);
    }

    Optional<ColonyNameProblem> getProblem(final String value,
            final Colony except) {
        final String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return Optional.of(ColonyNameProblem.EMPTY);
        }
        if (normalized.codePointCount(0, normalized.length()) > 64) {
            return Optional.of(ColonyNameProblem.TOO_LONG);
        }
        if (normalized.codePoints()
                .anyMatch(point -> Character.isISOControl(point)
                        || (point >= 0xD800 && point <= 0xDFFF))) {
            return Optional.of(ColonyNameProblem.INVALID);
        }
        if (model.getColonies().stream().anyMatch(colony -> colony != except
                && normalized.equalsIgnoreCase(normalize(colony.getName())))) {
            return Optional.of(ColonyNameProblem.DUPLICATE);
        }
        return Optional.empty();
    }

    String suggest(final Set<String> excluded) {
        for (final String candidate : NAMES) {
            if (available(candidate, excluded)) {
                return candidate;
            }
        }
        for (long suffix = 2; suffix < Long.MAX_VALUE; suffix++) {
            final String candidate = "New Amsterdam " + suffix;
            if (available(candidate, excluded)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No colony names available");
    }

    private boolean available(final String candidate,
            final Set<String> excluded) {
        return getProblem(candidate, null).isEmpty()
                && excluded.stream().noneMatch(
                        value -> candidate.equalsIgnoreCase(normalize(value)));
    }
}
