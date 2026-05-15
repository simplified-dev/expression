package dev.simplified.expression.tokenizer;

import dev.simplified.expression.Expression;
import dev.simplified.expression.shuntingyard.ShuntingYard;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Abstract base class for all token types produced by the {@link Tokenizer}.
 * <p>
 * Each token carries a {@linkplain #getType() type constant} that identifies its
 * syntactic role in a mathematical expression. The type constants defined here
 * ({@link #TOKEN_NUMBER}, {@link #TOKEN_OPERATOR}, etc.) are used by the
 * {@link ShuntingYard ShuntingYard} algorithm to
 * convert infix expressions into reverse-polish notation for evaluation by
 * {@link Expression}.
 *
 * @see Tokenizer
 * @see Expression
 * @see ShuntingYard
 */
@Getter
@RequiredArgsConstructor
public abstract class Token {

    /**
     * Type constant for numeric literal tokens.
     */
    public static final short TOKEN_NUMBER = 1;

    /**
     * Type constant for operator tokens.
     */
    public static final short TOKEN_OPERATOR = 2;

    /**
     * Type constant for function tokens.
     */
    public static final short TOKEN_FUNCTION = 3;

    /**
     * Type constant for open-parenthesis tokens.
     */
    public static final short TOKEN_PARENTHESES_OPEN = 4;

    /**
     * Type constant for close-parenthesis tokens.
     */
    public static final short TOKEN_PARENTHESES_CLOSE = 5;

    /**
     * Type constant for variable tokens.
     */
    public static final short TOKEN_VARIABLE = 6;

    /**
     * Type constant for argument-separator tokens (commas).
     */
    public static final short TOKEN_SEPARATOR = 7;

    /**
     * The type constant identifying the kind of this token.
     */
    private final int type;

}
