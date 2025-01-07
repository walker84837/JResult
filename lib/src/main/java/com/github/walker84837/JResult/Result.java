package com.github.walker84837.JResult;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JResult - Result type that can be either an Ok variant or an Err variant.
 * 
 * @param <T> The type of the Ok variant.
 * @param <E> The type of the Err variant.
 */
public abstract class Result<T, E> {
    public abstract boolean isOk();
    public abstract boolean isErr();

    /**
     * Unwraps the value of the Ok variant of the Result.

     * @return The value of the Ok variant of the Result.
     * @throws IllegalStateException If the Result is an Err variant.
     */
    public abstract T unwrap() throws IllegalStateException;

    /**
     * Unwraps the error of the Err variant of the Result.

     * @return The error of the Err variant of the Result.
     * @throws IllegalStateException If the Result is an Ok variant.
     */
    public abstract E unwrapErr() throws IllegalStateException;

    /**
     * Unwraps the value of the Ok variant of the Result, or returns a default value if the Result is an Err variant.

     * @param defaultValue The default value to return if the Result is an Err variant.
     * @return The value of the Ok variant of the Result, or the default value if the Result is an Err variant.
     */
    public abstract T unwrapOr(T defaultValue);
    public abstract T unwrapOrElse(Function<E, T> fallback);

    /**
     * Creates a new Ok variant of the Result.

     * @param value The value to wrap.
     * @return A new Ok variant of the Result.
     */
    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a new Err variant of the Result.

     * @param error The error to wrap.
     * @return A new Err variant of the Result.
     */
    public static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    /**
     * Executes the provided action if the Result is an Ok variant.

     * @param action The action to execute.
     */
    public abstract void ifOk(Consumer<T> action);

    /**
     * Executes the provided action if the Result is an Err variant.
 
     * @param action The action to execute.
     */
    public abstract void ifErr(Consumer<E> action);

    /**
     * Maps the value of the Ok variant of the Result.

     * @param mapper The function to map the value with.
     * @return A new Result with the mapped value.
     */
    public <U> Result<U, E> map(Function<T, U> mapper) {
        if (this instanceof Ok<T, E> ok) {
            return Result.ok(mapper.apply(ok.value));
        } else {
            return Result.err(this.unwrapErr());
        }
    }

    /**
     * Maps the error of the Err variant of the Result.

     * @param mapper The function to map the error with.
     * @return A new Result with the mapped error.
     */
    public <F> Result<T, F> mapErr(Function<E, F> mapper) {
        if (this instanceof Err<T, E> err) {
            return Result.err(mapper.apply(err.error));
        } else {
            return Result.ok(this.unwrap());
        }
    }

    private static final class Ok<T, E> extends Result<T, E> {
        private final T value;

        private Ok(T value) {
            this.value = value;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public T unwrap() {
            return value;
        }

        @Override
        public E unwrapErr() {
            throw new IllegalStateException("Called unwrapErr on an Ok value");
        }

        @Override
        public T unwrapOr(T defaultValue) {
            return value;
        }

        @Override
        public T unwrapOrElse(Function<E, T> fallback) {
            return value;
        }

        @Override
        public void ifOk(Consumer<T> action) {
            action.accept(value);
        }

        @Override
        public void ifErr(Consumer<E> action) {
            // Do nothing
        }
    }

    private static final class Err<T, E> extends Result<T, E> {
        private final E error;

        private Err(E error) {
            this.error = error;
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public T unwrap() {
            throw new IllegalStateException("Called unwrap on an Err value");
        }

        @Override
        public E unwrapErr() {
            return error;
        }

        @Override
        public T unwrapOr(T defaultValue) {
            return defaultValue;
        }

        @Override
        public T unwrapOrElse(Function<E, T> fallback) {
            return fallback.apply(error);
        }

        @Override
        public void ifOk(Consumer<T> action) {
            // Do nothing
        }

        @Override
        public void ifErr(Consumer<E> action) {
            action.accept(error);
        }
    }
}
