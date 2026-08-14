package dev.simplified.expression.operator;

import dev.simplified.annotations.EnumLookup;
import dev.simplified.annotations.Getter;
import dev.simplified.expression.Expression;
import dev.simplified.expression.exception.EvaluationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Enumeration of the built-in mathematical operators available for use in an {@link Expression}.
 * <p>
 * Each constant wraps a {@link MathOperator} instance that defines the symbol, operand count, associativity,
 * precedence, and computation logic. The {@link #get(char, int)} method provides lookup by symbol character
 * and operand count, enabling the expression parser to resolve operators during tokenization.
 * <p>
 * The division character {@code '÷'} is treated as an alias for {@code '/'}.
 *
 * @see MathOperator
 * @see Expression
 */
@Getter
@EnumLookup
public enum BuiltinOperator {

    /**
     * Binary addition: {@code a + b}.
     */
    ADDITION("+", 2, true, MathOperator.PRECEDENCE_ADDITION, args -> args[0] + args[1]),

    /**
     * Binary subtraction: {@code a - b}.
     */
    SUBTRACTION("-", 2, true, MathOperator.PRECEDENCE_ADDITION, args -> args[0] - args[1]),

    /**
     * Binary multiplication: {@code a * b}.
     */
    MULTIPLICATION("*", 2, true, MathOperator.PRECEDENCE_MULTIPLICATION, args -> args[0] * args[1]),

    /**
     * Binary division: {@code a / b}. Throws {@link EvaluationException} if the divisor is zero.
     */
    DIVISION("/", 2, true, MathOperator.PRECEDENCE_DIVISION, args -> {
        if (args[1] == 0d)
            throw new EvaluationException("Division by zero in division");

        return args[0] / args[1];
    }),

    /**
     * Binary exponentiation: {@code a ^ b}. Right-associative.
     */
    POWER("^", 2, false, MathOperator.PRECEDENCE_POWER, args -> Math.pow(args[0], args[1])),

    /**
     * Binary modulo: {@code a % b}. Throws {@link EvaluationException} if the divisor is zero.
     */
    MODULO("%", 2, true, MathOperator.PRECEDENCE_MODULO, args -> {
        if (args[1] == 0d)
            throw new EvaluationException("Division by zero in modulo");

        return args[0] % args[1];
    }),

    /**
     * Unary minus: {@code -a}. Right-associative.
     */
    UNARY_MINUS("-", 1, false, MathOperator.PRECEDENCE_UNARY_MINUS, args -> -args[0]),

    /**
     * Unary plus: {@code +a}. Right-associative.
     */
    UNARY_PLUS("+", 1, false, MathOperator.PRECEDENCE_UNARY_PLUS, args -> args[0]);

    /**
     * The underlying {@link MathOperator} instance that performs the computation.
     */
    private final @NotNull MathOperator actual;

    /**
     * Creates a builtin operator constant with the given properties.
     *
     * @param symbol the operator symbol
     * @param numOperands the number of operands (1 for unary, 2 for binary)
     * @param leftAssociative {@code true} if left-associative, {@code false} if right-associative
     * @param precedence the precedence value
     * @param function the computation function that accepts an array of operands and returns a result
     */
    BuiltinOperator(@NotNull String symbol, int numOperands, boolean leftAssociative, int precedence, @NotNull Function<Double[], Double> function) {
        this.actual = new MathOperator(symbol, numOperands, leftAssociative, precedence) {
            @Override
            public Double apply(Double... args) {
                return function.apply(args);
            }
        };
    }

    /**
     * Returns the builtin operator matching the given symbol character and operand count.
     * <p>
     * The division character {@code '÷'} is normalized to {@code '/'} before lookup.
     *
     * @param symbol the operator symbol character to look up
     * @param numArguments the number of operands (1 for unary, 2 for binary)
     * @return the matching {@link MathOperator}, or {@code null} if no builtin operator matches
     */
    public static @Nullable MathOperator get(char symbol, int numArguments) {
        if (symbol == '÷')
            symbol = '/';

        for (BuiltinOperator operator : CACHED_VALUES) {
            if (operator.getActual().getSymbol().charAt(0) == symbol) {
                if (numArguments == operator.getActual().getNumOperands())
                    return operator.getActual();
            }
        }

        return null;
    }

}
