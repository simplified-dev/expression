package dev.simplified.expression.tokenizer;

import dev.simplified.expression.shuntingyard.ShuntingYard;

/**
 * Token representing a function argument separator (the comma character) in a
 * mathematical expression.
 * <p>
 * When the {@link Tokenizer} encounters a {@code ','} character, it produces
 * an instance of this token. The
 * {@link ShuntingYard ShuntingYard} algorithm
 * uses it to delimit successive arguments within a function call and to
 * increment the argument count on the enclosing {@link FunctionToken}.
 *
 * @see Token#TOKEN_SEPARATOR
 * @see FunctionToken#incrementArgument()
 * @see Tokenizer
 */
class ArgumentSeparatorToken extends Token {

    /**
     * Creates a new argument-separator token.
     */
    ArgumentSeparatorToken() {
        super(TOKEN_SEPARATOR);
    }

}
