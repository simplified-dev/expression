package dev.simplified.expression.operator;

import dev.simplified.annotations.Getter;
import dev.simplified.expression.function.VarargFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for mathematical operators that can be used within an {@link Expression}.
 * <p>
 * Each operator has a symbol (such as {@code +}, {@code -}, {@code *}), a number of operands (1 for unary,
 * 2 for binary), an associativity direction, and a precedence value that controls evaluation order.
 * <p>
 * Precedence constants are provided as static fields (e.g., {@link #PRECEDENCE_ADDITION},
 * {@link #PRECEDENCE_POWER}) so that custom operators can be defined with well-known relative priorities.
 * <p>
 * Subclasses must implement {@link VarargFunction#apply(Object...) apply(Double...)} to define the
 * computation performed by the operator.
 *
 * @see BuiltinOperator
 * @see Expression
 */
@Getter
public abstract class MathOperator implements VarargFunction<Double, Double> {

    /**
     * The precedence value for the addition operation.
     */
    public static final int PRECEDENCE_ADDITION = 500;

    /**
     * The precedence value for the subtraction operation (equal to {@link #PRECEDENCE_ADDITION}).
     */
    public static final int PRECEDENCE_SUBTRACTION = PRECEDENCE_ADDITION;

    /**
     * The precedence value for the multiplication operation.
     */
    public static final int PRECEDENCE_MULTIPLICATION = 1000;

    /**
     * The precedence value for the division operation (equal to {@link #PRECEDENCE_MULTIPLICATION}).
     */
    public static final int PRECEDENCE_DIVISION = PRECEDENCE_MULTIPLICATION;

    /**
     * The precedence value for the modulo operation (equal to {@link #PRECEDENCE_DIVISION}).
     */
    public static final int PRECEDENCE_MODULO = PRECEDENCE_DIVISION;

    /**
     * The precedence value for the power/exponentiation operation.
     */
    public static final int PRECEDENCE_POWER = 10000;

    /**
     * The precedence value for the unary minus operation.
     */
    public static final int PRECEDENCE_UNARY_MINUS = 5000;

    /**
     * The precedence value for the unary plus operation (equal to {@link #PRECEDENCE_UNARY_MINUS}).
     */
    public static final int PRECEDENCE_UNARY_PLUS = PRECEDENCE_UNARY_MINUS;

    /**
     * The set of characters that are permitted as operator symbols.
     */
    public static final char[] ALLOWED_OPERATOR_CHARS = { '+', '-', '*', '/', '%', '^', '!', '#', '§',
        '$', '&', ';', ':', '~', '<', '>', '|', '=', '÷', '√', '∛', '⌈', '⌊' };

    /**
     * The number of operands this operator takes (1 for unary, 2 for binary).
     */
    private final int numOperands;

    /**
     * Whether this operator is left-associative ({@code true}) or right-associative ({@code false}).
     */
    private final boolean leftAssociative;

    /**
     * The symbol representing this operator (e.g., {@code "+"}, {@code "*"}).
     */
    private final @NotNull String symbol;

    /**
     * The precedence value of this operator, used to determine evaluation order.
     */
    private final int precedence;

    /**
     * Creates a new operator for use in mathematical expressions.
     *
     * @param symbol the symbol representing the operator (e.g., {@code "+"})
     * @param numberOfOperands the number of operands the operator takes (1 for unary, 2 for binary)
     * @param leftAssociative {@code true} if the operator is left-associative, {@code false} if right-associative
     * @param precedence the precedence value of the operator; higher values bind more tightly
     */
    public MathOperator(@NotNull String symbol, int numberOfOperands, boolean leftAssociative, int precedence) {
        this.numOperands = numberOfOperands;
        this.leftAssociative = leftAssociative;
        this.symbol = symbol;
        this.precedence = precedence;
    }

    /**
     * Checks whether the given character is an allowed operator character.
     * <p>
     * Only characters present in {@link #ALLOWED_OPERATOR_CHARS} may be used as operator symbols.
     *
     * @param ch the character to check
     * @return {@code true} if the character is allowed as part of an operator symbol, {@code false} otherwise
     */
    public static boolean isAllowedOperatorChar(char ch) {
        for (char allowed : ALLOWED_OPERATOR_CHARS) {
            if (ch == allowed)
                return true;
        }

        return false;
    }

}
