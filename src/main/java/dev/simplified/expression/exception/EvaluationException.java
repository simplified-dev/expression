package dev.sbs.api.expression.exception;

import dev.sbs.api.expression.function.BuiltinFunction;
import dev.sbs.api.expression.operator.BuiltinOperator;
import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when a mathematical expression encounters an arithmetic error during
 * evaluation.
 * <p>
 * This covers runtime computation failures such as division by zero in
 * operators and trigonometric functions.
 *
 * @see BuiltinOperator
 * @see BuiltinFunction
 */
public final class EvaluationException extends ExpressionException {

    /**
     * Constructs a new {@code EvaluationException} with the specified cause.
     *
     * @param cause the underlying throwable that caused this exception
     */
    public EvaluationException(@NotNull Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code EvaluationException} with the specified detail message.
     *
     * @param message the detail message
     */
    public EvaluationException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new {@code EvaluationException} with the specified cause and detail message.
     *
     * @param cause the underlying throwable that caused this exception
     * @param message the detail message
     */
    public EvaluationException(@NotNull Throwable cause, @NotNull String message) {
        super(cause, message);
    }

    /**
     * Constructs a new {@code EvaluationException} with a formatted detail message.
     *
     * @param message the format string
     * @param args the format arguments
     */
    public EvaluationException(@NotNull @PrintFormat String message, @Nullable Object... args) {
        super(message, args);
    }

    /**
     * Constructs a new {@code EvaluationException} with the specified cause and a formatted detail message.
     *
     * @param cause the underlying throwable that caused this exception
     * @param message the format string
     * @param args the format arguments
     */
    public EvaluationException(@NotNull Throwable cause, @NotNull @PrintFormat String message, @Nullable Object... args) {
        super(cause, message, args);
    }

}
