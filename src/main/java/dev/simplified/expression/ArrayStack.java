package dev.simplified.expression;

import dev.simplified.expression.exception.InvalidExpressionException;

import java.util.Stack;

/**
 * A lightweight stack of primitive {@code double} values backed by a resizable array.
 * <p>
 * This class provides a more memory-efficient alternative to {@link Stack}{@code <Double>}
 * by avoiding autoboxing overhead. The internal array grows automatically by 20% plus one element
 * when capacity is exceeded.
 * <p>
 * Used internally by the {@link Expression} evaluator to manage intermediate computation values
 * during expression evaluation.
 *
 * @see Expression
 */
class ArrayStack {

    /**
     * The backing array that stores the stack elements.
     */
    private double[] data;

    /**
     * The index of the top element, or {@code -1} if the stack is empty.
     */
    private int idx;

    /**
     * Creates a new stack with a default initial capacity of 5.
     */
    ArrayStack() {
        this(5);
    }

    /**
     * Creates a new stack with the specified initial capacity.
     *
     * @param initialCapacity the initial size of the backing array
     * @throws InvalidExpressionException if {@code initialCapacity} is less than or equal to zero
     */
    ArrayStack(int initialCapacity) {
        if (initialCapacity <= 0)
            throw new InvalidExpressionException("Stack's capacity must be positive");

        data = new double[initialCapacity];
        idx = -1;
    }

    /**
     * Pushes a value onto the top of this stack. If the backing array is full, it is resized
     * to accommodate the new element.
     *
     * @param value the {@code double} value to push
     */
    void push(double value) {
        if (idx + 1 == data.length) {
            double[] temp = new double[(int) (data.length * 1.2) + 1];
            System.arraycopy(data, 0, temp, 0, data.length);
            data = temp;
        }

        data[++idx] = value;
    }

    /**
     * Returns the value at the top of this stack without removing it.
     *
     * @return the {@code double} value at the top of this stack
     * @throws InvalidExpressionException if this stack is empty
     */
    @SuppressWarnings("unused")
    double peek() {
        if (idx == -1)
            throw new InvalidExpressionException("Stack is empty");

        return data[idx];
    }

    /**
     * Removes and returns the value at the top of this stack.
     *
     * @return the {@code double} value that was removed from the top of this stack
     * @throws InvalidExpressionException if this stack is empty
     */
    double pop() {
        if (idx == -1)
            throw new InvalidExpressionException("Stack is empty");

        return data[idx--];
    }

    /**
     * Checks whether this stack contains no elements.
     *
     * @return {@code true} if this stack is empty, {@code false} otherwise
     */
    boolean isEmpty() {
        return idx == -1;
    }

    /**
     * Returns the number of elements currently in this stack.
     *
     * @return the number of elements in this stack
     */
    int size() {
        return idx + 1;
    }

}
