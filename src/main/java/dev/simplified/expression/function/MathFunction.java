package dev.simplified.expression.function;

import dev.simplified.expression.Expression;
import dev.simplified.expression.exception.InvalidExpressionException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base class for mathematical functions that can be used within an {@link Expression}.
 * <p>
 * Each function has a name, a minimum and maximum argument count, and an
 * {@link VarargFunction#apply(Object...) apply(Double...)} method that performs the computation.
 * Functions with a fixed argument count have equal minimum and maximum values; variable-argument
 * functions specify different bounds.
 * <p>
 * Function names must consist of letters, underscores, and (after the first character) digits.
 * Names are validated at construction time via {@link #isValidFunctionName(String)}.
 *
 * @see BuiltinFunction
 * @see Expression
 * @see Expression.Builder
 */
@Getter
public abstract class MathFunction implements VarargFunction<Double, Double> {

    /**
     * The name of this function, used to reference it in expressions.
     */
    private final @NotNull String name;

    /**
     * The minimum number of arguments this function accepts.
     */
    protected final int minArguments;

    /**
     * The maximum number of arguments this function accepts.
     */
    protected final int maxArguments;

    /**
     * Creates a new function with the given name that takes exactly one argument.
     *
     * @param name the name of the function
     * @throws InvalidExpressionException if the name is not a valid function name
     */
    public MathFunction(@NotNull String name) {
        this(name, 1, 1);
    }

    /**
     * Creates a new function with the given name and a fixed number of arguments.
     *
     * @param name the name of the function
     * @param numArguments the exact number of arguments the function takes
     * @throws InvalidExpressionException if the name is invalid or if {@code numArguments} is negative
     */
    public MathFunction(@NotNull String name, int numArguments) {
        this(name, numArguments, numArguments);
    }

    /**
     * Creates a new function with the given name and a variable number of arguments
     * bounded by the specified minimum and maximum.
     *
     * @param name the name of the function
     * @param minArguments the minimum number of arguments the function takes (must be non-negative)
     * @param maxArguments the maximum number of arguments the function takes (must be {@code >= minArguments})
     * @throws InvalidExpressionException if the name is invalid, or if {@code minArguments} is negative,
     *                             or if {@code minArguments > maxArguments}
     */
    public MathFunction(@NotNull String name, int minArguments, int maxArguments) {
        if (minArguments < 0 || minArguments > maxArguments)
            throw new InvalidExpressionException("The number of function arguments can not be less than 0 or more than '%s' for '%s'", maxArguments, name);

        if (!isValidFunctionName(name))
            throw new InvalidExpressionException("The function name '" + name + "' is invalid");

        this.name = name;
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
    }

    /**
     * Checks whether the given string is a valid function name.
     * <p>
     * A valid function name must be non-null, non-empty, start with a letter or underscore,
     * and contain only letters, digits, and underscores.
     *
     * @param name the name to validate
     * @return {@code true} if the name is valid for use as a function name, {@code false} otherwise
     */
    public static boolean isValidFunctionName(@Nullable final String name) {
        if (name == null) {
            return false;
        }

        final int size = name.length();

        if (size == 0) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            final char c = name.charAt(i);
            if (Character.isLetter(c) || c == '_') {
                continue;
            } else if (Character.isDigit(c) && i > 0) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Returns the number of arguments for a function with a fixed argument count.
     * <p>
     * This method may only be called on functions where {@link #getMinArguments()} equals
     * {@link #getMaxArguments()}. For variable-argument functions, use those two methods instead.
     *
     * @return the fixed number of arguments
     * @throws InvalidExpressionException if this function accepts a variable number of arguments
     */
    public int getNumArguments() {
        if (this.minArguments != this.maxArguments)
            throw new InvalidExpressionException("Calling getNumArgument() is not supported for var arg functions, please use getMaxNumArguments() or getMinNumArguments()");

        return this.minArguments;
    }

}
