package dev.sbs.api.expression.exception;

import org.intellij.lang.annotations.PrintFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when the expression evaluator encounters a parsing, evaluation,
 * or operator error.
 *
 * @see InvalidExpressionException
 * @see EvaluationException
 * @see UnknownFunctionException
 * @see UnknownOperatorException
 */
public class ExpressionException extends RuntimeException {

    /**
     * Constructs a new {@code ExpressionException} with the specified cause.
     *
     * @param cause the underlying throwable that caused this exception
     */
    public ExpressionException(@NotNull Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new {@code ExpressionException} with the specified detail message.
     *
     * @param message the detail message
     */
    public ExpressionException(@NotNull String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ExpressionException} with the specified cause and detail message.
     *
     * @param cause the underlying throwable that caused this exception
     * @param message the detail message
     */
    public ExpressionException(@NotNull Throwable cause, @NotNull String message) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ExpressionException} with a formatted detail message.
     *
     * @param message the format string
     * @param args the format arguments
     */
    public ExpressionException(@NotNull @PrintFormat String message, @Nullable Object... args) {
        super(String.format(message, args));
    }

    /**
     * Constructs a new {@code ExpressionException} with the specified cause and a formatted detail message.
     *
     * @param cause the underlying throwable that caused this exception
     * @param message the format string
     * @param args the format arguments
     */
    public ExpressionException(@NotNull Throwable cause, @NotNull @PrintFormat String message, @Nullable Object... args) {
        super(String.format(message, args), cause);
    }

}
