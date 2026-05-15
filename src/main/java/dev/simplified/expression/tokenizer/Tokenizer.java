package dev.simplified.expression.tokenizer;

import dev.simplified.expression.Expression;
import dev.simplified.expression.exception.InvalidExpressionException;
import dev.simplified.expression.exception.UnknownFunctionException;
import dev.simplified.expression.exception.UnknownOperatorException;
import dev.simplified.expression.function.BuiltinFunction;
import dev.simplified.expression.function.MathFunction;
import dev.simplified.expression.operator.BuiltinOperator;
import dev.simplified.expression.operator.MathOperator;
import dev.simplified.expression.shuntingyard.ShuntingYard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Lexical tokenizer that converts a mathematical expression string into a
 * sequence of {@link Token} objects.
 * <p>
 * The tokenizer is consumed by calling {@link #hasNext()} and {@link #nextToken()}
 * in a loop. It recognizes:
 * <ul>
 *   <li>Numeric literals (integers, decimals, and scientific notation)</li>
 *   <li>Operators (built-in and user-defined)</li>
 *   <li>Functions (built-in and user-defined)</li>
 *   <li>Variable references</li>
 *   <li>Parentheses, brackets, and braces</li>
 *   <li>Argument separators (commas)</li>
 * </ul>
 * <p>
 * When {@code implicitMultiplication} is enabled (the default), the tokenizer
 * automatically inserts multiplication operator tokens between adjacent operands
 * where no explicit operator is present (e.g., {@code 2x} becomes {@code 2*x}).
 *
 * @see Token
 * @see ShuntingYard
 * @see Expression.Builder
 */
public class Tokenizer {

    /**
     * The expression being tokenized, stored as a character array.
     */
    private final char @NotNull [] expression;

    /**
     * The total length of the expression character array.
     */
    private final int expressionLength;

    /**
     * User-defined functions available during tokenization, keyed by name.
     */
    private final @Nullable Map<String, MathFunction> userFunctions;

    /**
     * User-defined operators available during tokenization, keyed by symbol.
     */
    private final @Nullable Map<String, MathOperator> userOperators;

    /**
     * The set of valid variable names that may appear in the expression.
     */
    private final @Nullable Set<String> variableNames;

    /**
     * Whether implicit multiplication insertion is enabled.
     */
    private final boolean implicitMultiplication;

    /**
     * The current character position within the expression array.
     */
    private int pos = 0;

    /**
     * The most recently returned token, used for context-sensitive parsing.
     */
    private @Nullable Token lastToken;

    /**
     * Creates a new tokenizer for the given expression with implicit multiplication control.
     *
     * @param expression the mathematical expression to tokenize
     * @param userFunctions user-defined functions available during tokenization, or {@code null}
     * @param userOperators user-defined operators available during tokenization, or {@code null}
     * @param variableNames the set of recognized variable names, or {@code null}
     * @param implicitMultiplication {@code true} to insert implicit multiplication tokens
     */
    public Tokenizer(@NotNull String expression, final @Nullable Map<String, MathFunction> userFunctions,
                     final @Nullable Map<String, MathOperator> userOperators, final @Nullable Set<String> variableNames, final boolean implicitMultiplication) {
        this.expression = expression.trim().toCharArray();
        this.expressionLength = this.expression.length;
        this.userFunctions = userFunctions;
        this.userOperators = userOperators;
        this.variableNames = variableNames;
        this.implicitMultiplication = implicitMultiplication;
    }

    /**
     * Creates a new tokenizer for the given expression with implicit multiplication enabled.
     *
     * @param expression the mathematical expression to tokenize
     * @param userFunctions user-defined functions available during tokenization, or {@code null}
     * @param userOperators user-defined operators available during tokenization, or {@code null}
     * @param variableNames the set of recognized variable names, or {@code null}
     */
    public Tokenizer(@NotNull String expression, final @Nullable Map<String, MathFunction> userFunctions,
                     final @Nullable Map<String, MathOperator> userOperators, final @Nullable Set<String> variableNames) {
        this.expression = expression.trim().toCharArray();
        this.expressionLength = this.expression.length;
        this.userFunctions = userFunctions;
        this.userOperators = userOperators;
        this.variableNames = variableNames;
        this.implicitMultiplication = true;
    }

    /**
     * Checks whether a character is part of a numeric literal.
     * <p>
     * Digits, decimal points, and the scientific notation marker ({@code e}/{@code E})
     * are always numeric. A sign character ({@code +}/{@code -}) is numeric only when
     * it immediately follows an exponent marker.
     *
     * @param ch the character to test
     * @param lastCharE {@code true} if the preceding character was {@code 'e'} or {@code 'E'}
     * @return {@code true} if the character is part of a numeric literal
     */
    private static boolean isNumeric(char ch, boolean lastCharE) {
        return Character.isDigit(ch) || ch == '.' || ch == 'e' || ch == 'E' ||
            (lastCharE && (ch == '-' || ch == '+'));
    }

    /**
     * Checks whether a Unicode code point is an alphabetic letter.
     *
     * @param codePoint the Unicode code point to test
     * @return {@code true} if the code point is a letter
     */
    public static boolean isAlphabetic(int codePoint) {
        return Character.isLetter(codePoint);
    }

    /**
     * Checks whether a Unicode code point is valid within a variable or function name.
     * <p>
     * Valid characters are letters, digits, underscores, and dots.
     *
     * @param codePoint the Unicode code point to test
     * @return {@code true} if the code point may appear in a variable or function name
     */
    public static boolean isVariableOrFunctionCharacter(int codePoint) {
        return isAlphabetic(codePoint) ||
            Character.isDigit(codePoint) ||
            codePoint == '_' ||
            codePoint == '.';
    }

    /**
     * Checks whether there are more tokens to consume.
     *
     * @return {@code true} if at least one more token can be read from the expression
     */
    public boolean hasNext() {
        return this.expression.length > pos;
    }

    /**
     * Returns the next token from the expression.
     * <p>
     * This method advances the internal position past the consumed characters.
     * When implicit multiplication is enabled, a synthetic
     * {@link OperatorToken multiplication token} may be returned before the
     * actual next operand.
     *
     * @return the next parsed {@link Token}
     * @throws InvalidExpressionException if the current character cannot be parsed
     *                            into any recognized token type
     */
    public @NotNull Token nextToken() {
        char ch = expression[pos];
        while (Character.isWhitespace(ch)) {
            ch = expression[++pos];
        }
        if (Character.isDigit(ch) || ch == '.') {
            if (lastToken != null) {
                if (lastToken.getType() == Token.TOKEN_NUMBER) {
                    throw new InvalidExpressionException("Unable to parse char '%s' (Code:%s) at [%s]", ch, (int) ch, pos);
                } else if (implicitMultiplication && (lastToken.getType() != Token.TOKEN_OPERATOR
                    && lastToken.getType() != Token.TOKEN_PARENTHESES_OPEN
                    && lastToken.getType() != Token.TOKEN_FUNCTION
                    && lastToken.getType() != Token.TOKEN_SEPARATOR)) {
                    // insert an implicit multiplication token
                    lastToken = new OperatorToken(BuiltinOperator.MULTIPLICATION.getActual());
                    return lastToken;
                }
            }
            return parseNumberToken(ch);
        } else if (isArgumentSeparator(ch)) {
            return parseArgumentSeparatorToken();
        } else if (isOpenParentheses(ch)) {
            if (lastToken != null && implicitMultiplication &&
                (lastToken.getType() != Token.TOKEN_OPERATOR
                    && lastToken.getType() != Token.TOKEN_PARENTHESES_OPEN
                    && lastToken.getType() != Token.TOKEN_FUNCTION
                    && lastToken.getType() != Token.TOKEN_SEPARATOR)) {
                // insert an implicit multiplication token
                lastToken = new OperatorToken(BuiltinOperator.MULTIPLICATION.getActual());
                return lastToken;
            }
            return parseParentheses(true);
        } else if (isCloseParentheses(ch)) {
            return parseParentheses(false);
        } else if (MathOperator.isAllowedOperatorChar(ch)) {
            return parseOperatorToken(ch);
        } else if (isAlphabetic(ch) || ch == '_') {
            // parse the name which can be a setVariable or a function
            if (lastToken != null && implicitMultiplication &&
                (lastToken.getType() != Token.TOKEN_OPERATOR
                    && lastToken.getType() != Token.TOKEN_PARENTHESES_OPEN
                    && lastToken.getType() != Token.TOKEN_FUNCTION
                    && lastToken.getType() != Token.TOKEN_SEPARATOR)) {
                // insert an implicit multiplication token
                lastToken = new OperatorToken(BuiltinOperator.MULTIPLICATION.getActual());
                return lastToken;
            }

            return parseFunctionOrVariable();
        }

        throw new InvalidExpressionException("Unable to parse char '%s' (Code:%s) at [%s]", ch, (int) ch, pos);
    }

    /**
     * Parses an argument-separator token and advances the position.
     *
     * @return a new {@link ArgumentSeparatorToken}
     */
    private @NotNull Token parseArgumentSeparatorToken() {
        this.pos++;
        this.lastToken = new ArgumentSeparatorToken();
        return lastToken;
    }

    /**
     * Checks whether the given character is a function argument separator.
     *
     * @param ch the character to test
     * @return {@code true} if the character is a comma
     */
    private boolean isArgumentSeparator(char ch) {
        return ch == ',';
    }

    /**
     * Parses a parenthesis token (open or close) and advances the position.
     *
     * @param open {@code true} for an open parenthesis, {@code false} for a close parenthesis
     * @return the new parenthesis token
     */
    private @NotNull Token parseParentheses(final boolean open) {
        if (open) {
            this.lastToken = new OpenParenthesesToken();
        } else {
            this.lastToken = new CloseParenthesesToken();
        }
        this.pos++;
        return lastToken;
    }

    /**
     * Checks whether the given character is an open-parenthesis character.
     *
     * @param ch the character to test
     * @return {@code true} if the character is {@code '('}, {@code '{'}, or {@code '['}
     */
    private boolean isOpenParentheses(char ch) {
        return ch == '(' || ch == '{' || ch == '[';
    }

    /**
     * Checks whether the given character is a close-parenthesis character.
     *
     * @param ch the character to test
     * @return {@code true} if the character is {@code ')'}, {@code '}'}, or {@code ']'}
     */
    private boolean isCloseParentheses(char ch) {
        return ch == ')' || ch == '}' || ch == ']';
    }

    /**
     * Parses a function name or variable name starting at the current position.
     * <p>
     * The parser uses a longest-match strategy: it progressively checks longer
     * substrings against the known variable names and function registry, keeping
     * track of the longest valid match found. If no match is found at all, an
     * {@link UnknownFunctionException} is thrown.
     *
     * @return the parsed {@link FunctionToken} or {@link VariableToken}
     * @throws UnknownFunctionException if the identifier matches
     *                                            neither a known function nor a declared variable
     */
    private @NotNull Token parseFunctionOrVariable() {
        final int offset = this.pos;
        int testPos;
        int lastValidLen = 1;
        Token lastValidToken = null;
        int len = 1;

        if (isEndOfExpression(offset))
            this.pos++;

        testPos = offset + len - 1;
        while (!isEndOfExpression(testPos) && isVariableOrFunctionCharacter(expression[testPos])) {
            String name = new String(expression, offset, len);

            if (variableNames != null && variableNames.contains(name)) {
                lastValidLen = len;
                lastValidToken = new VariableToken(name);
            } else {
                final MathFunction f = getFunction(name);

                if (f != null) {
                    lastValidLen = len;
                    lastValidToken = new FunctionToken(f);
                }
            }

            len++;
            testPos = offset + len - 1;
        }

        if (lastValidToken == null)
            throw new UnknownFunctionException(new String(expression), pos, len);

        pos += lastValidLen;
        lastToken = lastValidToken;
        return lastToken;
    }

    /**
     * Looks up a function by name, checking user-defined functions first, then built-in functions.
     *
     * @param name the function name to look up
     * @return the matching {@link MathFunction}, or {@code null} if not found
     */
    private @Nullable MathFunction getFunction(@NotNull String name) {
        MathFunction f = null;

        if (this.userFunctions != null)
            f = this.userFunctions.get(name);

        if (f == null)
            f = BuiltinFunction.get(name);

        return f;
    }

    /**
     * Parses an operator token starting at the current position.
     * <p>
     * The parser greedily reads all consecutive operator characters and then
     * tries to match the longest prefix that corresponds to a known operator,
     * shortening the candidate from the right until a match is found.
     *
     * @param firstChar the first operator character at the current position
     * @return the parsed {@link OperatorToken}
     * @throws IllegalArgumentException if no valid operator can be matched
     */
    private @NotNull Token parseOperatorToken(char firstChar) {
        final int offset = this.pos;
        int len = 1;
        final StringBuilder symbol = new StringBuilder();
        MathOperator lastValid = null;
        symbol.append(firstChar);

        while (!isEndOfExpression(offset + len) && MathOperator.isAllowedOperatorChar(expression[offset + len]))
            symbol.append(expression[offset + len++]);

        while (!symbol.isEmpty()) {
            MathOperator op = this.getOperator(symbol.toString());

            if (op == null)
                symbol.setLength(symbol.length() - 1);
            else {
                lastValid = op;
                break;
            }
        }

        if (lastValid == null)
            throw new UnknownOperatorException(new String(expression), pos, len);

        pos += symbol.length();
        lastToken = new OperatorToken(lastValid);
        return lastToken;
    }

    /**
     * Looks up an operator by symbol, checking user-defined operators first, then
     * built-in operators.
     * <p>
     * For single-character symbols, the operator's arity (unary vs. binary) is
     * inferred from the preceding token context.
     *
     * @param symbol the operator symbol to look up
     * @return the matching {@link MathOperator}, or {@code null} if not found
     */
    private @Nullable MathOperator getOperator(@NotNull String symbol) {
        MathOperator op = null;
        if (this.userOperators != null) {
            op = this.userOperators.get(symbol);
        }
        if (op == null && symbol.length() == 1) {
            int argc = 2;
            if (lastToken == null) {
                argc = 1;
            } else {
                int lastTokenType = lastToken.getType();
                if (lastTokenType == Token.TOKEN_PARENTHESES_OPEN || lastTokenType == Token.TOKEN_SEPARATOR) {
                    argc = 1;
                } else if (lastTokenType == Token.TOKEN_OPERATOR) {
                    final MathOperator lastOp = ((OperatorToken) lastToken).getOperator();
                    if (lastOp.getNumOperands() == 2 || (lastOp.getNumOperands() == 1 && !lastOp.isLeftAssociative())) {
                        argc = 1;
                    }
                }

            }
            op = BuiltinOperator.get(symbol.charAt(0), argc);
        }
        return op;
    }

    /**
     * Parses a numeric literal token starting at the current position.
     * <p>
     * Handles integers, decimals, and scientific notation (e.g., {@code 1.5e10}).
     * A trailing {@code 'e'} or {@code 'E'} without a following digit or sign
     * is not considered part of the number and causes a position rollback.
     *
     * @param firstChar the first character of the number at the current position
     * @return the parsed {@link NumberToken}
     */
    private @NotNull Token parseNumberToken(final char firstChar) {
        final int offset = this.pos;
        int len = 1;
        this.pos++;
        if (isEndOfExpression(offset + len)) {
            lastToken = new NumberToken(Double.parseDouble(String.valueOf(firstChar)));
            return lastToken;
        }
        while (!isEndOfExpression(offset + len) &&
            isNumeric(expression[offset + len], expression[offset + len - 1] == 'e' ||
                expression[offset + len - 1] == 'E')) {
            len++;
            this.pos++;
        }
        // check if the e is at the end
        if (expression[offset + len - 1] == 'e' || expression[offset + len - 1] == 'E') {
            // since the e is at the end it's not part of the number and a rollback is necessary
            len--;
            pos--;
        }
        lastToken = new NumberToken(expression, offset, len);
        return lastToken;
    }

    /**
     * Checks whether the given offset is at or beyond the end of the expression.
     *
     * @param offset the zero-based index to test
     * @return {@code true} if the offset is past the last character of the expression
     */
    private boolean isEndOfExpression(int offset) {
        return this.expressionLength <= offset;
    }

}
