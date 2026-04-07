package dev.simplified.expression;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the result of validating an {@link Expression}.
 * <p>
 * A validation result is either successful (no errors) or unsuccessful (contains a list of
 * error messages describing the problems found). The singleton {@link #SUCCESS} instance
 * represents a successful validation with no errors.
 *
 * @param valid  {@code true} if the validation was successful, {@code false} otherwise
 * @param errors the list of error messages if validation failed, or {@code null} if successful
 * @see Expression
 * @see Expression.Builder
 */
public record ValidationResult(boolean valid, @Nullable List<String> errors) {

    /** A shared instance representing a successful validation with no errors. */
    public static final @NotNull ValidationResult SUCCESS = new ValidationResult(true, null);

}
