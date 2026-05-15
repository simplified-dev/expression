package dev.simplified.expression.tokenizer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Token representing a numeric literal in a mathematical expression.
 * <p>
 * Instances are created by the {@link Tokenizer} when it encounters a sequence
 * of digit characters, decimal points, or scientific-notation indicators
 * ({@code e}/{@code E}) in the input expression.
 *
 * @see Token#TOKEN_NUMBER
 * @see Tokenizer
 */
@Getter
public final class NumberToken extends Token {

    /**
     * The numeric value represented by this token.
     */
    private final double value;

    /**
     * Creates a new number token with the specified value.
     *
     * @param value the numeric value this token represents
     */
    public NumberToken(double value) {
        super(TOKEN_NUMBER);
        this.value = value;
    }

    /**
     * Creates a new number token by parsing a region of the given character array.
     * <p>
     * The substring starting at {@code offset} with the given {@code len} is
     * interpreted as a {@code double} via {@link Double#parseDouble(String)}.
     *
     * @param expression the full expression character array
     * @param offset the starting index of the numeric substring
     * @param len the length of the numeric substring
     */
    NumberToken(final char @NotNull [] expression, final int offset, final int len) {
        this(Double.parseDouble(String.valueOf(expression, offset, len)));
    }

}
