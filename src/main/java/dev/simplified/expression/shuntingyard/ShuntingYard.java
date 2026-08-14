package dev.simplified.expression.shuntingyard;

import dev.simplified.annotations.UtilityClass;
import dev.simplified.expression.Expression;
import dev.simplified.expression.exception.InvalidExpressionException;
import dev.simplified.expression.function.MathFunction;
import dev.simplified.expression.operator.MathOperator;
import dev.simplified.expression.tokenizer.FunctionToken;
import dev.simplified.expression.tokenizer.OperatorToken;
import dev.simplified.expression.tokenizer.Token;
import dev.simplified.expression.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Implementation of the
 * <a href="https://en.wikipedia.org/wiki/Shunting-yard_algorithm">shunting-yard algorithm</a>
 * that converts an infix mathematical expression into reverse-polish notation (RPN).
 * <p>
 * This class is stateless and exposes a single static method,
 * {@link #convertToRPN(String, Map, Map, Set, boolean)}, which tokenizes the input
 * expression via the {@link Tokenizer} and then reorders the resulting tokens
 * according to operator precedence and associativity rules.
 * <p>
 * The resulting {@link Token} array is consumed by
 * {@link Expression Expression} for stack-based evaluation.
 *
 * @see Tokenizer
 * @see Expression
 * @see Expression.Builder
 */
@UtilityClass
public class ShuntingYard {

    /**
     * Converts an infix mathematical expression into an array of tokens in
     * reverse-polish notation (RPN).
     * <p>
     * The algorithm handles operator precedence, left/right associativity,
     * parenthesized sub-expressions, multi-argument function calls, and
     * optional implicit multiplication.
     *
     * @param expression the infix expression string to convert
     * @param userFunctions user-defined functions keyed by name, or {@code null}
     * @param userOperators user-defined operators keyed by symbol, or {@code null}
     * @param variableNames the set of recognized variable names, or {@code null}
     * @param implicitMultiplication {@code true} to enable implicit multiplication
     *     between adjacent operands
     * @return an array of {@link Token} instances ordered in reverse-polish notation
     * @throws InvalidExpressionException if the expression contains mismatched
     *                            parentheses, misplaced separators, or
     *                            unparseable characters
     */
    public static Token @NotNull [] convertToRPN(final @NotNull String expression, final @Nullable Map<String, MathFunction> userFunctions,
                                                 final @Nullable Map<String, MathOperator> userOperators, final @Nullable Set<String> variableNames, final boolean implicitMultiplication) {
        final Stack<Token> stack = new Stack<>();
        final List<Token> output = new ArrayList<>();
        final Stack<FunctionToken> functionStack = new Stack<>();
        final Tokenizer tokenizer = new Tokenizer(expression, userFunctions, userOperators, variableNames, implicitMultiplication);

        while (tokenizer.hasNext()) {
            Token token = tokenizer.nextToken();

            switch (token.getType()) {
                case Token.TOKEN_NUMBER:
                case Token.TOKEN_VARIABLE:
                    output.add(token);
                    break;
                case Token.TOKEN_FUNCTION:
                    functionStack.add((FunctionToken) token);
                    stack.add(token);
                    break;
                case Token.TOKEN_SEPARATOR:
                    if (!functionStack.empty())
                        functionStack.peek().incrementArgument();

                    while (!stack.empty() && stack.peek().getType() != Token.TOKEN_PARENTHESES_OPEN)
                        output.add(stack.pop());

                    if (stack.empty() || stack.peek().getType() != Token.TOKEN_PARENTHESES_OPEN)
                        throw new InvalidExpressionException("Misplaced function separator ',' or mismatched parentheses");

                    break;
                case Token.TOKEN_OPERATOR:
                    while (!stack.empty() && stack.peek().getType() == Token.TOKEN_OPERATOR) {
                        OperatorToken o1 = (OperatorToken) token;
                        OperatorToken o2 = (OperatorToken) stack.peek();
                        if (o1.getOperator().getNumOperands() == 1 && o2.getOperator().getNumOperands() == 2)
                            break;
                        else if ((o1.getOperator().isLeftAssociative() && o1.getOperator().getPrecedence() <= o2.getOperator().getPrecedence())
                            || (o1.getOperator().getPrecedence() < o2.getOperator().getPrecedence())) {
                            output.add(stack.pop());
                        } else
                            break;
                    }

                    stack.push(token);
                    break;
                case Token.TOKEN_PARENTHESES_OPEN:
                    stack.push(token);
                    break;
                case Token.TOKEN_PARENTHESES_CLOSE:
                    while (stack.peek().getType() != Token.TOKEN_PARENTHESES_OPEN)
                        output.add(stack.pop());

                    stack.pop();
                    if (!stack.isEmpty() && stack.peek().getType() == Token.TOKEN_FUNCTION) {
                        functionStack.pop();
                        output.add(stack.pop());
                    }

                    break;
                default:
                    throw new InvalidExpressionException("Unknown Token type encountered. This should not happen");
            }
        }

        while (!stack.empty()) {
            Token t = stack.pop();
            if (t.getType() == Token.TOKEN_PARENTHESES_CLOSE || t.getType() == Token.TOKEN_PARENTHESES_OPEN)
                throw new InvalidExpressionException("Mismatched parentheses detected. Please check the expression");
            else
                output.add(t);
        }

        return output.toArray(new Token[0]);
    }

}
