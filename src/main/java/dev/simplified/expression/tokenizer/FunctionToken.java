package dev.simplified.expression.tokenizer;

import dev.simplified.expression.function.MathFunction;
import dev.simplified.expression.shuntingyard.ShuntingYard;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Token representing a function call in a mathematical expression.
 * <p>
 * Each function token wraps a {@link MathFunction} instance and tracks the
 * number of arguments that have been parsed for it. The argument count starts
 * at {@code 1} (for a single-argument call) and is incremented by the
 * {@link ShuntingYard ShuntingYard} algorithm
 * each time an {@link ArgumentSeparatorToken} is encountered within the
 * function's parenthesized argument list.
 *
 * @see Token#TOKEN_FUNCTION
 * @see MathFunction
 * @see Tokenizer
 */
@Getter
public class FunctionToken extends Token {

    /**
     * The mathematical function this token represents.
     */
    private final @NotNull MathFunction function;

    /**
     * The number of arguments parsed so far for this function call.
     */
    int argumentCount;

    /**
     * Creates a new function token wrapping the given function.
     * <p>
     * The argument count is initialized to {@code 1}.
     *
     * @param function the mathematical function this token represents
     */
    public FunctionToken(final @NotNull MathFunction function) {
        super(TOKEN_FUNCTION);
        this.function = function;
        this.argumentCount = 1;
    }

    /**
     * Increments the argument count by one.
     * <p>
     * Called by the shunting-yard algorithm when a comma separator is
     * encountered inside this function's argument list.
     */
    public void incrementArgument() {
        this.argumentCount++;
    }

}
