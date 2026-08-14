package dev.simplified.expression.tokenizer;

import dev.simplified.annotations.Getter;
import dev.simplified.expression.Expression;
import org.jetbrains.annotations.NotNull;

/**
 * Token representing a named variable reference in a mathematical expression.
 * <p>
 * Variable names are resolved by the {@link Tokenizer} against a caller-supplied
 * set of known variable names. At evaluation time the
 * {@link Expression Expression} substitutes the variable's
 * current value.
 *
 * @see Token#TOKEN_VARIABLE
 * @see Tokenizer
 */
@Getter
public class VariableToken extends Token {

    /**
     * The name of the variable this token references.
     */
    private final @NotNull String name;

    /**
     * Creates a new variable token with the specified name.
     *
     * @param name the variable name, as it appears in the expression
     */
    public VariableToken(@NotNull String name) {
        super(TOKEN_VARIABLE);
        this.name = name;
    }

}
