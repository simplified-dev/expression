package dev.simplified.expression;

import dev.simplified.expression.exception.InvalidExpressionException;
import dev.simplified.expression.function.BuiltinFunction;
import dev.simplified.expression.function.MathFunction;
import dev.simplified.expression.operator.BuiltinOperator;
import dev.simplified.expression.operator.MathOperator;
import dev.simplified.expression.shuntingyard.ShuntingYard;
import dev.simplified.expression.tokenizer.FunctionToken;
import dev.simplified.expression.tokenizer.NumberToken;
import dev.simplified.expression.tokenizer.OperatorToken;
import dev.simplified.expression.tokenizer.Token;
import dev.simplified.expression.tokenizer.VariableToken;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A compiled mathematical expression that can be evaluated to a {@code double} result.
 * <p>
 * Expressions are created via {@link #builder(String)}, which returns a {@link Builder}
 * for configuring custom functions, operators, and variables before compilation.
 * Once built, the expression can be evaluated repeatedly with different variable
 * assignments using {@link #setVariable(String, double)} and {@link #evaluate()}.
 * </p>
 * <p>
 * The following constants are pre-defined as variables in every expression:
 * <ul>
 *     <li>{@code pi} / {@code \u03C0} - {@link Math#PI}</li>
 *     <li>{@code e} - {@link Math#E}</li>
 *     <li>{@code \u03C6} - the golden ratio (1.61803398874)</li>
 * </ul>
 * </p>
 * <p>
 * Expressions may also be copied via the {@linkplain #Expression(Expression) copy constructor},
 * which produces an independent clone with its own variable bindings.
 * </p>
 *
 * @see Builder
 * @see MathFunction
 * @see MathOperator
 */
@SuppressWarnings("all")
public class Expression {

    /**
     * The compiled token sequence in reverse polish notation.
     */
    private final @NotNull Token[] tokens;

    /**
     * The mutable variable-name-to-value bindings used during evaluation.
     */
    @Getter
    private final @NotNull Map<String, Double> variables;

    /**
     * The names of user-defined functions, used to prevent variable/function name collisions.
     */
    private final @NotNull Set<String> userFunctionNames;

    /**
     * An optional callback that supplies variable values on demand during evaluation.
     */
    @Getter
    @Setter
    private @Nullable VariableProvider variableProvider;

    /**
     * Creates a new expression that is an independent copy of an existing one.
     * <p>
     * The copy has its own token array, variable map, and function-name set,
     * so changes to one expression do not affect the other. The
     * {@link VariableProvider} reference is shared (not deep-copied).
     * </p>
     *
     * @param existing the expression to copy
     */
    public Expression(final @NotNull Expression existing) {
        this.tokens = Arrays.copyOf(existing.tokens, existing.tokens.length);
        this.variables = new HashMap<>();
        this.variables.putAll(existing.variables);
        this.variableProvider = existing.variableProvider;
        this.userFunctionNames = new HashSet<>(existing.userFunctionNames);
    }

    /**
     * Package-private constructor invoked by {@link Builder#build()} after the
     * shunting-yard algorithm has converted the infix expression into RPN tokens.
     *
     * @param tokens the compiled RPN token array
     * @param userFunctionNames the names of user-registered functions
     */
    Expression(final @NotNull Token[] tokens, @NotNull Set<String> userFunctionNames) {
        this.tokens = tokens;
        this.variables = createDefaultVariables();
        this.userFunctionNames = userFunctionNames;
    }

    /**
     * Creates a new {@link Builder} for the given mathematical expression string.
     *
     * @param expression the infix expression to parse (e.g. {@code "2 * x + sin(y)"})
     * @return a new builder for configuring and compiling the expression
     */
    public static Expression.@NotNull Builder builder(@NotNull String expression) {
        return new Builder(expression);
    }

    /**
     * Creates the default variable map containing the mathematical constants
     * {@code pi}, {@code e}, and the golden ratio {@code phi}.
     *
     * @return a mutable map pre-populated with built-in constants
     */
    private static @NotNull Map<String, Double> createDefaultVariables() {
        final Map<String, Double> vars = new HashMap<>(4);
        vars.put("pi", Math.PI);
        vars.put("\u03C0", Math.PI);
        vars.put("\u03C6", 1.61803398874d);
        vars.put("e", Math.E);
        return vars;
    }

    /**
     * Sets the value of a named variable for evaluation.
     *
     * @param name the variable name
     * @param value the value to assign
     * @return this expression for chaining
     * @throws InvalidExpressionException if {@code name} collides with a registered function
     */
    public @NotNull Expression setVariable(final @NotNull String name, final double value) {
        this.checkVariableName(name);
        this.variables.put(name, value);
        return this;
    }

    /**
     * Validates that the given variable name does not conflict with a built-in
     * or user-registered function name.
     *
     * @param name the variable name to check
     * @throws InvalidExpressionException if a function with the same name exists
     */
    private void checkVariableName(@NotNull String name) {
        if (this.userFunctionNames.contains(name) || BuiltinFunction.get(name) != null)
            throw new InvalidExpressionException("The variable name '%s' is invalid. Since there exists a function with the same name", name);

    }

    /**
     * Sets the values of multiple variables at once from the given map.
     *
     * @param variables a map of variable names to their values
     * @return this expression for chaining
     * @throws InvalidExpressionException if any name collides with a registered function
     */
    public @NotNull Expression setVariables(@NotNull Map<String, Double> variables) {
        for (Map.Entry<String, Double> v : variables.entrySet())
            this.setVariable(v.getKey(), v.getValue());

        return this;
    }

    /**
     * Removes all variable bindings (including built-in constants) from this expression.
     *
     * @return this expression for chaining
     */
    public @NotNull Expression clearVariables() {
        this.variables.clear();
        return this;
    }

    /**
     * Returns the set of variable names that appear in this compiled expression.
     * <p>
     * This inspects the RPN token array and collects every variable token name.
     * The returned set is a snapshot and is not connected to the expression.
     * </p>
     *
     * @return an unmodifiable snapshot of variable names referenced in the expression
     */
    public @NotNull Set<String> getVariableNames() {
        final Set<String> variables = new HashSet<>();

        for (final Token t : tokens) {
            if (t.getType() == Token.TOKEN_VARIABLE)
                variables.add(((VariableToken) t).getName());
        }

        return variables;
    }

    /**
     * Validates this expression for structural correctness.
     * <p>
     * When {@code checkVariablesSet} is {@code true}, the validation also verifies
     * that every variable referenced in the expression has a value assigned in the
     * {@linkplain #getVariables() variable map}.
     * </p>
     *
     * @param checkVariablesSet whether to verify that all variables have assigned values
     * @return a {@link ValidationResult} indicating success or listing error messages
     */
    public @NotNull ValidationResult validate(boolean checkVariablesSet) {
        final List<String> errors = new ArrayList<>(0);

        if (checkVariablesSet) {
            /* check that all vars have a value set */
            for (final Token t : this.tokens) {
                if (t.getType() == Token.TOKEN_VARIABLE) {
                    final String var = ((VariableToken) t).getName();

                    if (!variables.containsKey(var))
                        errors.add(String.format("The setVariable '%s' has not been set", var));

                }
            }
        }

        /* Check if the number of operands, functions and operators match.
           The idea is to increment a counter for operands and decrease it for operators.
           When a function occurs the number of available arguments has to be greater
           than or equals to the function's expected number of arguments.
           The count has to be larger than 1 at all times and exactly 1 after all tokens
           have been processed */
        int count = 0;
        for (Token token : this.tokens) {
            switch (token.getType()) {
                case Token.TOKEN_NUMBER:
                case Token.TOKEN_VARIABLE:
                    count++;
                    break;
                case Token.TOKEN_FUNCTION:
                    final MathFunction func = ((FunctionToken) token).getFunction();
                    final int argsNum = ((FunctionToken) token).getArgumentCount();

                    if (func.getMinArguments() > argsNum || func.getMaxArguments() < argsNum)
                        errors.add(String.format("Not enough arguments for '%s'", func.getName()));

                    if (argsNum > 1)
                        count -= argsNum - 1;
                    else if (argsNum == 0) {
                        // see https://github.com/fasseg/exp4j/issues/59
                        count++;
                    }
                    break;
                case Token.TOKEN_OPERATOR:
                    MathOperator op = ((OperatorToken) token).getOperator();
                    if (op.getNumOperands() == 2)
                        count--;

                    break;
            }

            if (count < 1) {
                errors.add("Too many operators");
                return new ValidationResult(false, errors);
            }
        }

        if (count > 1)
            errors.add("Too many operands");

        return errors.size() == 0 ? ValidationResult.SUCCESS : new ValidationResult(false, errors);

    }

    /**
     * Validates this expression for structural correctness, including a check
     * that all referenced variables have assigned values.
     * <p>
     * Equivalent to calling {@link #validate(boolean) validate(true)}.
     * </p>
     *
     * @return a {@link ValidationResult} indicating success or listing error messages
     */
    public @NotNull ValidationResult validate() {
        return validate(true);
    }

    /**
     * Submits this expression for asynchronous evaluation on the given executor.
     *
     * @param executor the executor service to run the evaluation on
     * @return a {@link Future} that will contain the evaluated result
     */
    public @NotNull Future<Double> evaluateAsync(@NotNull ExecutorService executor) {
        return executor.submit(this::evaluate);
    }

    /**
     * Evaluates this expression and returns the computed result.
     * <p>
     * Variables are resolved first from the {@linkplain #getVariables() variable map},
     * and then from the {@linkplain #getVariableProvider() variable provider} if one
     * is set. If a variable cannot be resolved from either source, an
     * {@link InvalidExpressionException} is thrown.
     * </p>
     *
     * @return the result of evaluating this expression
     * @throws InvalidExpressionException if a variable has no assigned value and no
     *                             provider is set, or if the expression is malformed
     */
    public double evaluate() {
        final ArrayStack output = new ArrayStack();

        for (Token token : tokens) {
            if (token.getType() == Token.TOKEN_NUMBER)
                output.push(((NumberToken) token).getValue());
            else if (token.getType() == Token.TOKEN_VARIABLE) {
                final String name = ((VariableToken) token).getName();
                Double value = this.variables.get(name);

                if (value == null) {
                    if (this.variableProvider != null)
                        value = variableProvider.getVariable(name);
                    else
                        throw new InvalidExpressionException("No value has been set for the setVariable '%s'", name);
                }

                output.push(value);
            } else if (token.getType() == Token.TOKEN_OPERATOR) {
                OperatorToken op = (OperatorToken) token;
                if (output.size() < op.getOperator().getNumOperands())
                    throw new InvalidExpressionException("Invalid number of operands available for '%s' operator", op.getOperator().getSymbol());

                if (op.getOperator().getNumOperands() == 2) {
                    /* pop the operands and push the result of the operation */
                    double rightArg = output.pop();
                    double leftArg = output.pop();
                    output.push(op.getOperator().apply(leftArg, rightArg));
                } else if (op.getOperator().getNumOperands() == 1) {
                    /* pop the operand and push the result of the operation */
                    double arg = output.pop();
                    output.push(op.getOperator().apply(arg));
                }
            } else if (token.getType() == Token.TOKEN_FUNCTION) {
                FunctionToken func = (FunctionToken) token;
                final int numArguments = func.getArgumentCount();

                if (numArguments < func.getFunction().getMinArguments() || numArguments > func.getFunction().getMaxArguments() || output.isEmpty())
                    throw new InvalidExpressionException("Invalid number of arguments available for '%s' function", func.getFunction().getName());

                /* collect the arguments from the stack */
                Double[] args = new Double[numArguments];
                for (int j = numArguments - 1; j >= 0; j--)
                    args[j] = output.pop();

                output.push(func.getFunction().apply(args));
            }
        }

        if (output.size() > 1)
            throw new InvalidExpressionException("Invalid number of items on the output queue. Might be caused by an invalid number of arguments for a function");

        return output.pop();
    }

    /**
     * Converts this expression back to an infix string representation using
     * explicit multiplication (the {@code *} sign is preserved).
     * <p>
     * Equivalent to calling {@link #toString(boolean) toString(false)}.
     * </p>
     *
     * @return an infix string representation of this expression with explicit multiplication
     */
    @Override
    public @NotNull String toString() {
        return toString(false);
    }

    /**
     * Converts this expression back to an infix string representation.
     * <p>
     * When {@code implicitMultiplication} is {@code true}, the {@code *} sign is
     * removed wherever it precedes a non-digit character (e.g. {@code 2*x} becomes
     * {@code 2x}, but {@code 2*3} remains {@code 2*3}).
     * </p>
     *
     * @param implicitMultiplication if {@code true}, elides the {@code *} sign where logical
     * @return an infix string representation of this expression
     */
    public @NotNull String toString(boolean implicitMultiplication) {
        String expression = toString(Arrays.asList(tokens), implicitMultiplication);

        if (implicitMultiplication) {
            expression = expression.replaceAll("\\*(\\D)", "$1");
        }

        return expression;
    }

    /**
     * Recursively converts a list of RPN tokens back to an infix string,
     * applying parentheses where necessary to preserve operator precedence.
     *
     * @param tokens the RPN token list to convert
     * @param impMult whether to use implicit multiplication formatting
     * @return the infix string representation of the token list
     */
    private @NotNull String toString(@NotNull List<Token> tokens, boolean impMult) {
        if (tokens.size() == 0)
            return "";
        Token token = tokens.get(tokens.size() - 1);

        switch (token.getType()) {
            case Token.TOKEN_OPERATOR:
                MathOperator operator = ((OperatorToken) token).getOperator();
                List<List<Token>> operands = getTokensArguments(tokens.subList(0, tokens.size() - 1), operator.getNumOperands());
                List<Token> leftTokens;
                List<Token> rightTokens;

                if (operator.getNumOperands() == 1) {
                    if (operator.isLeftAssociative()) {
                        leftTokens = operands.get(0);
                        rightTokens = new ArrayList<>();
                    } else {
                        leftTokens = new ArrayList<>();
                        rightTokens = operands.get(0);
                    }
                } else {
                    if (operator.getSymbol().equals("*") && operands.get(1).size() == 1 && operands.get(0).get(operands.get(0).size() - 1).getType() != Token.TOKEN_NUMBER) {
                        leftTokens = operands.get(1);
                        rightTokens = operands.get(0);
                    } else {
                        leftTokens = operands.get(0);
                        rightTokens = operands.get(1);
                    }
                }

                boolean parentheses_left = leftTokens.size() > 1 && leftTokens.get(leftTokens.size() - 1).getType() != Token.TOKEN_FUNCTION;
                boolean parentheses_right = rightTokens.size() > 1 && rightTokens.get(rightTokens.size() - 1).getType() != Token.TOKEN_FUNCTION;

                if (parentheses_left && leftTokens.get(leftTokens.size() - 1).getType() == Token.TOKEN_OPERATOR) {
                    MathOperator leftOperator = ((OperatorToken) leftTokens.get(leftTokens.size() - 1)).getOperator();

                    if (leftOperator.getNumOperands() == 1 && leftOperator.getSymbol().matches("\\+|-") && !operator.getSymbol().matches("\\+|-"))
                        parentheses_left = true;
                    else {
                        if (leftOperator.getSymbol().matches("\\+|-|\\*"))
                            parentheses_left = operator.getPrecedence() > leftOperator.getPrecedence();
                        else
                            parentheses_left = operator.getPrecedence() >= leftOperator.getPrecedence();
                    }
                }
                if (parentheses_right && rightTokens.get(rightTokens.size() - 1).getType() == Token.TOKEN_OPERATOR) {
                    MathOperator rightOperator = ((OperatorToken) rightTokens.get(rightTokens.size() - 1)).getOperator();

                    if (rightOperator.getNumOperands() == 1 && rightOperator.getSymbol().matches("\\+|-"))
                        parentheses_right = true;
                    else {
                        if (operator.getSymbol().matches("\\+|\\*") && rightOperator.getSymbol().matches("\\+|\\*"))
                            parentheses_right = operator.getPrecedence() > rightOperator.getPrecedence();
                        else
                            parentheses_right = operator.getPrecedence() >= rightOperator.getPrecedence();
                    }
                }

                if (!parentheses_left && leftTokens.size() > 0 && leftTokens.get(leftTokens.size() - 1).getType() == Token.TOKEN_NUMBER)
                    parentheses_left = ((NumberToken) leftTokens.get(0)).getValue() < 0;

                if (!parentheses_right && rightTokens.size() > 0 && rightTokens.get(rightTokens.size() - 1).getType() == Token.TOKEN_NUMBER)
                    parentheses_right = ((NumberToken) rightTokens.get(0)).getValue() < 0;

                String leftOperand = toString(leftTokens, impMult),
                    rightOperand = toString(rightTokens, impMult),
                    symbol = operator.getSymbol();

                if (parentheses_left)
                    leftOperand = "(" + leftOperand + ")";

                if (parentheses_right)
                    rightOperand = "(" + rightOperand + ")";

                return leftOperand + symbol + rightOperand;
            case Token.TOKEN_FUNCTION:
                MathFunction function = ((FunctionToken) token).getFunction();

                if (function.getName().equals("pow")) {
                    tokens.set(tokens.size() - 1, new OperatorToken(BuiltinOperator.get('^', 2)));
                    return toString(tokens, impMult);
                }

                String stringArgs = "";
                List<List<Token>> args = getTokensArguments(tokens.subList(0, tokens.size() - 1), function.getNumArguments());
                for (List<Token> argument : args) {
                    stringArgs += ", " + toString(argument, impMult);
                }
                stringArgs = stringArgs.substring(2);

                return function.getName() + "(" + stringArgs + ")";
            case Token.TOKEN_VARIABLE:
                return ((VariableToken) token).getName();
            case Token.TOKEN_NUMBER:
                double num = ((NumberToken) token).getValue();
                if (num != (long) num)
                    return String.valueOf(num);
                else
                    return String.valueOf((long) num);
            default:
                throw new InvalidExpressionException("The token type '%s' is not supported in this function yet", token.getClass().getName());
        }
    }

    /**
     * Splits an RPN token list into separate argument sub-lists for a function or
     * operator that expects the given number of operands.
     *
     * @param tokens the RPN token list (excluding the function/operator token itself)
     * @param numOperands the number of arguments to extract
     * @return a list of token sub-lists, one per argument in left-to-right order
     */
    private @NotNull List<List<Token>> getTokensArguments(@NotNull List<Token> tokens, int numOperands) {
        List<List<Token>> tArgs = new ArrayList<>(2);
        if (numOperands == 1) {
            tArgs.add(tokens);
        } else {
            int size = 0;
            int[] pos = new int[numOperands - 1];
            for (int i = 0; i < tokens.size() - 1; i++) {
                Token t = tokens.get(i);
                switch (t.getType()) {
                    case Token.TOKEN_NUMBER:
                        size++;
                        break;

                    case Token.TOKEN_VARIABLE:
                        size++;
                        break;

                    case Token.TOKEN_OPERATOR:
                        MathOperator operator = ((OperatorToken) t).getOperator();
                        if (operator.getNumOperands() == 2)
                            size--;
                        break;

                    case Token.TOKEN_FUNCTION:
                        FunctionToken func = (FunctionToken) t;
                        for (int j = 0; j < func.getFunction().getNumArguments(); j++) {
                            size--;
                        }
                        size++;
                        break;
                }
                for (int j = 0; j < pos.length; j++) {
                    if (size == j + 1) {
                        pos[j] = i;
                    }
                }
            }

            tArgs.add(tokens.subList(0, pos[0] + 1));
            for (int i = 1; i < pos.length; i++) {
                tArgs.add(tokens.subList(pos[i - 1] + 1, pos[i] + 1));
            }
            tArgs.add(tokens.subList(pos[pos.length - 1] + 1, tokens.size()));
        }

        return tArgs;
    }

    /**
     * A builder for configuring and compiling {@link Expression} instances.
     * <p>
     * Obtain an instance via {@link Expression#builder(String)}, then register
     * custom {@linkplain MathFunction functions}, {@linkplain MathOperator operators},
     * and variable names before calling {@link #build()} to produce the compiled expression.
     * </p>
     * <p>
     * Example usage:
     * <pre>{@code
     * Expression expr = Expression.builder("2 * x + sin(y)")
     *     .variables("x", "y")
     *     .build();
     * double result = expr.setVariable("x", 3).setVariable("y", Math.PI).evaluate();
     * }</pre>
     * </p>
     *
     * @see Expression
     * @see MathFunction
     * @see MathOperator
     */
    public static final class Builder {

        /**
         * The raw infix expression string to be parsed.
         */
        private final @NotNull String expression;

        /**
         * User-registered functions keyed by name.
         */
        private final @NotNull Map<String, MathFunction> userFunctions;

        /**
         * User-registered operators keyed by symbol.
         */
        private final @NotNull Map<String, MathOperator> userOperators;

        /**
         * Declared variable names that the expression may reference.
         */
        private final @NotNull Set<String> variableNames;

        /**
         * Whether implicit multiplication is enabled (e.g. {@code 2x} treated as {@code 2*x}).
         */
        private boolean implicitMultiplication = true;

        /**
         * Creates a new builder for the given expression string.
         *
         * @param expression the infix expression to parse
         * @throws InvalidExpressionException if {@code expression} is {@code null} or blank
         */
        private Builder(@NotNull String expression) {
            if (expression == null || expression.trim().isEmpty())
                throw new InvalidExpressionException("Expression can not be empty");

            this.expression = expression;
            this.userOperators = new HashMap<>(4);
            this.userFunctions = new HashMap<>(4);
            this.variableNames = new HashSet<>(4);
        }

        /**
         * Registers a custom {@link MathFunction} for use in the expression.
         *
         * @param function the function implementation to register
         * @return this builder for chaining
         */
        public @NotNull Builder function(@NotNull MathFunction function) {
            this.userFunctions.put(function.getName(), function);
            return this;
        }

        /**
         * Registers multiple custom {@link MathFunction} implementations for use in the expression.
         *
         * @param functions the function implementations to register
         * @return this builder for chaining
         */
        public @NotNull Builder functions(@NotNull MathFunction... functions) {
            for (MathFunction f : functions)
                this.userFunctions.put(f.getName(), f);

            return this;
        }

        /**
         * Registers multiple custom {@link MathFunction} implementations from a list.
         *
         * @param functions a list of function implementations to register
         * @return this builder for chaining
         */
        public @NotNull Builder functions(@NotNull List<MathFunction> functions) {
            for (MathFunction f : functions)
                this.userFunctions.put(f.getName(), f);

            return this;
        }

        /**
         * Declares multiple variable names that the expression may reference.
         *
         * @param variableNames the set of variable names
         * @return this builder for chaining
         */
        public @NotNull Builder variables(@NotNull Set<String> variableNames) {
            this.variableNames.addAll(variableNames);
            return this;
        }

        /**
         * Declares multiple variable names that the expression may reference.
         *
         * @param variableNames the variable names
         * @return this builder for chaining
         */
        public @NotNull Builder variables(@NotNull String... variableNames) {
            Collections.addAll(this.variableNames, variableNames);
            return this;
        }

        /**
         * Declares a single variable name that the expression may reference.
         *
         * @param variableName the variable name
         * @return this builder for chaining
         */
        public @NotNull Builder variable(@NotNull String variableName) {
            this.variableNames.add(variableName);
            return this;
        }

        /**
         * Enables or disables implicit multiplication during parsing.
         * <p>
         * When enabled (the default), expressions like {@code 2x} are interpreted
         * as {@code 2*x}.
         * </p>
         *
         * @param enabled {@code true} to enable implicit multiplication, {@code false} to disable
         * @return this builder for chaining
         */
        public @NotNull Builder implicitMultiplication(boolean enabled) {
            this.implicitMultiplication = enabled;
            return this;
        }

        /**
         * Registers a custom {@link MathOperator} for use in the expression.
         *
         * @param operator the operator implementation to register
         * @return this builder for chaining
         * @throws InvalidExpressionException if the operator symbol contains invalid characters
         */
        public @NotNull Builder operator(@NotNull MathOperator operator) {
            this.checkOperatorSymbol(operator);
            this.userOperators.put(operator.getSymbol(), operator);
            return this;
        }

        /**
         * Validates that the given operator's symbol contains only characters
         * permitted by {@link MathOperator#isAllowedOperatorChar(char)}.
         *
         * @param op the operator to validate
         * @throws InvalidExpressionException if the symbol contains an invalid character
         */
        private void checkOperatorSymbol(@NotNull MathOperator op) {
            String name = op.getSymbol();

            for (char ch : name.toCharArray()) {
                if (!MathOperator.isAllowedOperatorChar(ch))
                    throw new InvalidExpressionException("The operator symbol '%s' is invalid", name);
            }
        }

        /**
         * Registers multiple custom {@link MathOperator} implementations for use in the expression.
         *
         * @param operators the operator implementations to register
         * @return this builder for chaining
         * @throws InvalidExpressionException if any operator symbol contains invalid characters
         */
        public @NotNull Builder operator(@NotNull MathOperator... operators) {
            for (MathOperator o : operators)
                this.operator(o);

            return this;
        }

        /**
         * Registers multiple custom {@link MathOperator} implementations from a list.
         *
         * @param operators a list of operator implementations to register
         * @return this builder for chaining
         * @throws InvalidExpressionException if any operator symbol contains invalid characters
         */
        public @NotNull Builder operator(@NotNull List<MathOperator> operators) {
            for (MathOperator o : operators) {
                this.operator(o);
            }
            return this;
        }

        /**
         * Compiles the configured expression into an {@link Expression} instance.
         * <p>
         * This method registers the built-in mathematical constants as variable names,
         * validates that no variable name conflicts with a function name, then runs the
         * shunting-yard algorithm to produce the RPN token sequence.
         * </p>
         *
         * @return a compiled {@link Expression} ready for variable assignment and evaluation
         * @throws InvalidExpressionException if the expression is empty, or if a variable name
         *                             conflicts with a built-in or user-defined function
         */
        public @NotNull Expression build() {
            if (expression.isEmpty())
                throw new InvalidExpressionException("The expression can not be empty");

            /* set the constants' variable names */
            variableNames.add("pi");
            variableNames.add("\u03C0");
            variableNames.add("e");
            variableNames.add("\u03C6");

            /* Check if there are duplicate vars/functions */
            for (String var : variableNames) {
                if (BuiltinFunction.get(var) != null || userFunctions.containsKey(var))
                    throw new InvalidExpressionException("A variable can not have the same name as a function [%s]", var);
            }

            return new Expression(
                ShuntingYard.convertToRPN(
                    this.expression,
                    this.userFunctions,
                    this.userOperators,
                    this.variableNames,
                    this.implicitMultiplication
            ), this.userFunctions.keySet());
        }
    }

}
