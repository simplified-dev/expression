package dev.simplified.expression.function;

import dev.simplified.expression.Expression;
import dev.simplified.expression.exception.EvaluationException;
import dev.simplified.util.ArrayUtil;
import dev.simplified.util.NumberUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enumeration of built-in mathematical functions available for use in an {@link Expression}.
 * <p>
 * Each constant wraps a {@link MathFunction} instance that defines the function name, argument count bounds,
 * and computation logic. The {@link #get(String)} method provides case-insensitive lookup by name, enabling
 * the expression parser to resolve function references during tokenization.
 * <p>
 * Includes trigonometric, hyperbolic, logarithmic, rounding, and aggregate functions.
 *
 * @see MathFunction
 * @see Expression
 */
@Getter
public enum BuiltinFunction {

    /**
     * Sine function: {@code sin(x)}.
     */
    SIN("sin", 1, args -> Math.sin(args[0])),

    /**
     * Cosine function: {@code cos(x)}.
     */
    COS("cos", 1, args -> Math.cos(args[0])),

    /**
     * Tangent function: {@code tan(x)}.
     */
    TAN("tan", 1, args -> Math.tan(args[0])),

    /**
     * Cosecant function: {@code csc(x) = 1/sin(x)}. Throws {@link EvaluationException} if {@code sin(x) == 0}.
     */
    CSC("csc", 1, args -> {
        double sin = Math.sin(args[0]);
        if (sin == 0d)
            throw new EvaluationException("Division by zero in cosecant");
        return 1d / sin;
    }),

    /**
     * Secant function: {@code sec(x) = 1/cos(x)}. Throws {@link EvaluationException} if {@code cos(x) == 0}.
     */
    SEC("sec", 1, args -> {
        double cos = Math.cos(args[0]);
        if (cos == 0d)
            throw new EvaluationException("Division by zero in secant");
        return 1d / cos;
    }),

    /**
     * Cotangent function: {@code cot(x) = 1/tan(x)}. Throws {@link EvaluationException} if {@code tan(x) == 0}.
     */
    COT("cot", 1, args -> {
        double tan = Math.tan(args[0]);
        if (tan == 0d)
            throw new EvaluationException("Division by zero in cotangent");
        return 1d / tan;
    }),

    /**
     * Hyperbolic sine function: {@code sinh(x)}.
     */
    SINH( "sinh", 1, args -> Math.sinh(args[0])),

    /**
     * Hyperbolic cosine function: {@code cosh(x)}.
     */
    COSH( "cosh", 1, args -> Math.cosh(args[0])),

    /**
     * Hyperbolic tangent function: {@code tanh(x)}.
     */
    TANH( "tanh", 1, args -> Math.tanh(args[0])),

    /**
     * Hyperbolic cosecant function: {@code csch(x) = 1/sinh(x)}. Returns {@code 0.0} when {@code x == 0}.
     */
    CSCH("csch", 1, args -> {
        // This would throw an ArithmeticException later as sinh(0) = 0
        if (args[0] == 0d)
            return 0d;

        return 1d / Math.sinh(args[0]);
    }),

    /**
     * Hyperbolic secant function: {@code sech(x) = 1/cosh(x)}.
     */
    SECH("sech", 1, args -> 1d / Math.cosh(args[0])),

    /**
     * Hyperbolic cotangent function: {@code coth(x) = cosh(x)/sinh(x)}.
     */
    COTH("coth", 1, args -> Math.cosh(args[0]) / Math.sinh(args[0])),

    /**
     * Inverse sine (arc sine) function: {@code asin(x)}.
     */
    ASIN( "asin", 1, args -> Math.asin(args[0])),

    /**
     * Inverse cosine (arc cosine) function: {@code acos(x)}.
     */
    ACOS( "acos", 1, args -> Math.acos(args[0])),

    /**
     * Inverse tangent (arc tangent) function: {@code atan(x)}.
     */
    ATAN( "atan", 1, args -> Math.atan(args[0])),

    /**
     * Square root function: {@code sqrt(x)}.
     */
    SQRT( "sqrt", 1, args -> Math.sqrt(args[0])),

    /**
     * Cube root function: {@code cbrt(x)}.
     */
    CBRT( "cbrt", 1, args -> Math.cbrt(args[0])),

    /**
     * Absolute value function: {@code abs(x)}.
     */
    ABS( "abs", 1, args -> Math.abs(args[0])),

    /**
     * Ceiling function: {@code ceil(x)}, returns the smallest integer greater than or equal to {@code x}.
     */
    CEIL( "ceil", 1, args -> Math.ceil(args[0])),

    /**
     * Floor function: {@code floor(x)}, returns the largest integer less than or equal to {@code x}.
     */
    FLOOR( "floor", 1, args -> Math.floor(args[0])),

    /**
     * Power function: {@code pow(base, exponent)}.
     */
    POW( "pow", 2, args -> Math.pow(args[0], args[1])),

    /**
     * Exponential function: {@code exp(x) = e^x}.
     */
    EXP( "exp", 1, args -> Math.exp(args[0])),

    /**
     * Exponential minus one function: {@code expm1(x) = e^x - 1}, accurate for small values of {@code x}.
     */
    EXPM1( "expm1", 1, args -> Math.expm1(args[0])),

    /**
     * Base-10 logarithm function: {@code log10(x)}.
     */
    LOG10( "log10", 1, args -> Math.log10(args[0])),

    /**
     * Base-2 logarithm function: {@code log2(x)}.
     */
    LOG2( "log2", 1, args -> Math.log(args[0]) / Math.log(2d)),

    /**
     * Natural logarithm function: {@code log(x) = ln(x)}.
     */
    LOG( "log", 1, args -> Math.log(args[0])),

    /**
     * Natural logarithm of (1 + x): {@code log1p(x)}, accurate for small values of {@code x}.
     */
    LOG1P( "log1p", 1, args -> Math.log1p(args[0])),

    /**
     * Logarithm with arbitrary base: {@code logb(base, x) = log(x) / log(base)}.
     */
    LOGB("logb", 2, args -> Math.log(args[1]) / Math.log(args[0])),

    /**
     * Signum function: returns {@code 1.0} for positive, {@code -1.0} for negative, {@code 0.0} for zero.
     */
    SIGNUM("signum", 1, args -> {
        if (args[0] > 0)
            return 1d;
        else if (args[0] < 0)
            return -1d;
        else
            return 0d;
    }),

    /**
     * Degrees-to-radians conversion: {@code toradian(degrees)}.
     */
    TO_RADIAN("toradian", 1, args -> Math.toRadians(args[0])),

    /**
     * Radians-to-degrees conversion: {@code todegree(radians)}.
     */
    TO_DEGREE("todegree", 1, args -> Math.toDegrees(args[0])),

    /**
     * Digit length function: returns the number of digits in the integer part of {@code x}.
     */
    LENGTH("length", 1, args -> ArrayUtil.isNotEmpty(args) ? (int) (Math.log10(args[0]) + 1) : 0d),

    /**
     * Maximum function: {@code max(a, b, ...)}. Accepts 1 to 100 arguments and returns the largest value.
     */
    MAX("max", 1, 100, args -> {
        double value = args[0];

        for (int i = 1; i < args.length; i++)
            value = Math.max(value, args[i]);

        return value;
    }),

    /**
     * Minimum function: {@code min(a, b, ...)}. Accepts 1 to 100 arguments and returns the smallest value.
     */
    MIN("min", 1, 100, args -> {
        double value = args[0];

        for (int i = 1; i < args.length; i++)
            value = Math.min(value, args[i]);

        return value;
    }),

    /**
     * Sum function: {@code sum(a, b, ...)}. Accepts 1 to 100 arguments and returns their total.
     */
    SUM("sum", 1, 100, args -> {
        double value = args[0];

        for (int i = 1; i < args.length; i++)
            value += args[i];

        return value;
    }),

    /**
     * Average function: {@code avg(a, b, ...)}. Accepts 1 to 100 arguments and returns their arithmetic mean.
     */
    AVG("avg", 1, 100, args -> {
        double value = args[0];

        for (int i = 1; i < args.length; i++)
            value += args[i];

        return value / args.length;
    }),

    /**
     * Round function: {@code round(x)} or {@code round(x, precision)}. Rounds to the nearest integer or to the given decimal precision.
     */
    ROUND("round", 1, 2, args -> {
        if (args.length == 1)
            return (double) Math.round(args[0]);
        else
            return NumberUtil.round(args[0], args[1].intValue());
    }),

    /**
     * Round-up function: {@code roundup(x, precision)}. Rounds the value up (away from zero) to the given decimal precision.
     */
    ROUND_UP("roundup", 1, args -> (double) NumberUtil.roundUp(args[0], args[1].intValue())),

    /**
     * Round-down function: {@code rounddown(x, precision)}. Rounds the value down (toward zero) to the given decimal precision.
     */
    ROUND_DOWN("rounddown", 1, args -> (double) NumberUtil.roundDown(args[0], args[1].intValue()));

    /**
     * Cached array of all enum values for efficient iteration.
     */
    private static final BuiltinFunction[] VALUES = values();

    /**
     * The underlying {@link MathFunction} instance that performs the computation.
     */
    private final @NotNull MathFunction actual;

    /**
     * Creates a builtin function constant with a fixed number of arguments.
     *
     * @param name the function name
     * @param numArguments the exact number of arguments the function takes
     * @param function the computation logic that accepts an array of arguments and returns a result
     */
    BuiltinFunction(@NotNull String name, int numArguments, @NotNull VarargFunction<Double, Double> function) {
        this(name, numArguments, numArguments, function);
    }

    /**
     * Creates a builtin function constant with a variable number of arguments.
     *
     * @param name the function name
     * @param minArguments the minimum number of arguments the function takes
     * @param maxArguments the maximum number of arguments the function takes
     * @param function the computation logic that accepts an array of arguments and returns a result
     */
    BuiltinFunction(@NotNull String name, int minArguments, int maxArguments, @NotNull VarargFunction<Double, Double> function) {
        this.actual = new MathFunction(name, minArguments, maxArguments) {
            @Override
            public Double apply(Double... args) {
                return function.apply(args);
            }
        };
    }

    /**
     * Returns the builtin function matching the given name, ignoring case.
     *
     * @param name the function name to look up
     * @return the matching {@link MathFunction}, or {@code null} if no builtin function matches
     */
    public static @Nullable MathFunction get(@NotNull String name) {
        for (BuiltinFunction function : VALUES) {
            if (function.getActual().getName().equalsIgnoreCase(name))
                return function.getActual();
        }

        return null;
    }

}
