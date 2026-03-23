package dev.sbs.api.expression.exception;

import dev.sbs.api.expression.tokenizer.Tokenizer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when the {@link Tokenizer} cannot resolve an identifier as either a
 * known function or a declared variable.
 * <p>
 * The exception captures the full expression text, the unrecognized token, and its
 * character position so that callers can produce precise error diagnostics.
 *
 * @see Tokenizer
 */
@Getter
public final class UnknownFunctionException extends ExpressionException {

    /** The full expression string that was being tokenized. */
    private final @NotNull String expression;

    /** The unrecognized token text extracted from the expression. */
    private final @NotNull String token;

    /** The zero-based character position where the unrecognized token begins. */
    private final int position;

    /**
     * Creates a new exception for an unrecognized function or variable.
     *
     * @param expression the full expression string being tokenized
     * @param position the zero-based index where the unknown token starts
     * @param length the length of the identifier that was being parsed
     */
    public UnknownFunctionException(@NotNull String expression, int position, int length) {
        super("Unknown function or variable '" + token(expression, position, length) + "' at pos " + position + " in expression '" + expression + "'");
        this.expression = expression;
        this.token = token(expression, position, length);
        this.position = position;
    }

    /**
     * Extracts the unrecognized token substring from the expression.
     *
     * @param expression the full expression string
     * @param position the starting index of the token
     * @param length the raw length of the token being parsed
     * @return the token substring, clamped to the expression bounds
     */
    private static @NotNull String token(@NotNull String expression, int position, int length) {
        int len = expression.length();
        int end = position + length - 1;

        if (len < end)
            end = len;

        return expression.substring(position, end);
    }

}
