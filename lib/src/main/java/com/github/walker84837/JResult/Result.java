package com.github.walker84837.JResult;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Result<T, E> {
    public abstract boolean isOk();
    public abstract boolean isErr();
    public abstract T unwrap() throws IllegalStateException;
    public abstract E unwrapErr() throws IllegalStateException;
    public abstract T unwrapOr(T defaultValue);
    public abstract T unwrapOrElse(Function<E, T> fallback);

    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    public static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    public abstract void ifOk(Consumer<T> action);
    public abstract void ifErr(Consumer<E> action);

    /**
     * Maps the value of the Ok variant of the Result.
     * 
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
