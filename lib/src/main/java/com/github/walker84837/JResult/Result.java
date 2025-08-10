package com.github.walker84837.JResult;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JResult - A Result type that represents either a success (Ok) or a failure (Err).
 * <p>
 * This class is implemented as a sealed abstract class to guarantee that only
 * the permitted subclasses {@link Ok} and {@link Err} can exist.
 *
 * @param <T> The type of the success value.
 * @param <E> The type of the error value.
 */
public sealed abstract class Result<T, E> permits Result.Ok, Result.Err {

    /**
     * Returns true if the result is an Ok variant.
     *
     * @return true if success, false otherwise.
     */
    public abstract boolean isOk();

    /**
     * Returns true if the result is an Err variant.
     *
     * @return true if error, false otherwise.
     */
    public abstract boolean isErr();

    /**
     * Unwraps the success value.
     *
     * @return the success value.
     * @throws IllegalStateException if this is an error.
     */
    public abstract T unwrap() throws IllegalStateException;

    /**
     * Unwraps the error value.
     *
     * @return the error value.
     * @throws IllegalStateException if this is a success.
     */
    public abstract E unwrapErr() throws IllegalStateException;

    /**
     * Returns the success value if present or the default value if not.
     *
     * @param defaultValue the default value.
     * @return the success value or defaultValue.
     */
    public abstract T unwrapOr(T defaultValue);

    /**
     * Returns the success value if present, or computes it from the error using the fallback function.
     *
     * @param fallback a function to compute a success value if this is an error.
     * @return the success value or the computed value.
     */
    public abstract T unwrapOrElse(Function<E, T> fallback);

    /**
     * Executes the given action if this is an Ok variant.
     *
     * @param action the action to perform on the success value.
     */
    public abstract void ifOk(Consumer<T> action);

    /**
     * Executes the given action if this is an Err variant.
     *
     * @param action the action to perform on the error value.
     */
    public abstract void ifErr(Consumer<E> action);

    /**
     * Creates a new Ok variant of the Result.
     *
     * @param value the success value.
     * @param <T>   the type of the success value.
     * @param <E>   the type of the error value.
     * @return a new Ok instance.
     */
    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a new Err variant of the Result.
     *
     * @param error the error value.
     * @param <T>   the type of the success value.
     * @param <E>   the type of the error value.
     * @return a new Err instance.
     */
    public static <T, E> Result<T, E> err(E error) {
        return new Err<>(error);
    }

    /**
     * Converts an Optional to a Result, producing an Ok if the Optional has a value,
     * or an Err with the value from the errorSupplier if empty.
     *
     * @param optional       the Optional to convert.
     * @param errorSupplier  supplies the error value when the Optional is empty.
     * @param <T>            the type of the success value.
     * @param <E>            the type of the error value.
     * @return a Result representing the Optional's value or the supplied error.
     */
    public static <T, E> Result<T, E> fromOptional(
        Optional<T> optional,
        Supplier<E> errorSupplier
    ) {
        return optional.map(Result::<T, E>ok)
            .orElseGet(() -> Result.err(errorSupplier.get()));
    }

    /**
     * Converts an Optional to a Result with a String error.
     *
     * @param optional      the Optional to convert.
     * @param errorMessage  the error message if the Optional is empty.
     * @param <T>           the type of the success value.
     * @return a Result with Ok if the Optional has a value, or Err(errorMessage) if empty.
     */
    public static <T> Result<T, String> fromOptional(
        Optional<T> optional,
        String errorMessage
    ) {
        return fromOptional(optional, () -> errorMessage);
    }

    /**
     * Converts an Optional to a Result with an exception as the error.
     *
     * @param optional           the Optional to convert.
     * @param exceptionSupplier  supplies the exception when the Optional is empty.
     * @param <T>                the type of the success value.
     * @param <E>                the type of the exception (must extend Throwable).
     * @return a Result with Ok if the Optional has a value, or Err(exception) if empty.
     */
    public static <T, E extends Throwable> Result<T, E> fromOptionalWithException(
        Optional<T> optional,
        Supplier<E> exceptionSupplier
    ) {
        return fromOptional(optional, exceptionSupplier);
    }

    /**
     * Converts an Optional to a Result with an exception as the error,
     * using the provided message and factory function to create the exception.
     *
     * @param optional           the Optional to convert.
     * @param message            the message for the exception if the Optional is empty.
     * @param exceptionFactory   creates an exception from the given message.
     * @param <T>                the type of the success value.
     * @param <E>                the type of the exception (must extend Throwable).
     * @return a Result with Ok if the Optional has a value, or Err(exception) if empty.
     */
    public static <T, E extends Throwable> Result<T, E> fromOptionalException(
            Optional<T> optional,
            String message,
            Function<String, E> exceptionFactory) {
        return fromOptional(optional, () -> exceptionFactory.apply(message));
    }

    /**
     * Converts this Result to a Stream containing the success value if present.
     *
     * @return a one‐element Stream with the success value, or an empty Stream if this is Err.
     */
    public Stream<T> toStream() {
        return isOk()
            ? Stream.of(unwrap())
            : Stream.empty();
    }

    /**
     * Applies either onOk or onErr, depending on whether this Result is Ok or Err.
     *
     * @param onOk   function to transform the success value (type T → R) if this is Ok.
     * @param onErr  function to transform the error value (type E → R) if this is Err.
     * @param <R>    the result type of the fold operation.
     * @return       the value returned by onOk.apply(unwrappedSuccess) or onErr.apply(unwrappedError).
     */
    public <R> R fold(Function<T, R> onOk, Function<E, R> onErr) {
        if (isOk()) {
            return onOk.apply(unwrap());
        } else {
            return onErr.apply(unwrapErr());
        }
    }

    /**
     * Executes the given action if this is an Ok variant, or the other action if this is an Err variant.
     * This method performs side effects and does not return a value.
     *
     * @param onOk  the action to perform on the success value.
     * @param onErr the action to perform on the error value.
     */
     public void match(Consumer<T> onOk, Consumer<E> onErr) {
        if (isOk()) {
            onOk.accept(unwrap());
        } else {
            onErr.accept(unwrapErr());
        }
    }

    /**
     * Returns the provided result if this is Ok, otherwise returns this Err.
     *
     * @param other the alternative result.
     * @param <U>   the type of the alternative success value.
     * @return other if this is Ok; otherwise, this Err.
     */
    public <U> Result<U, E> and(Result<U, E> other) {
        return isOk() ? other : castError();
    }

    /**
     * Calls the provided mapping function with the success value and returns its result if this is Ok.
     * Otherwise, returns this Err.
     *
     * @param op  the function to apply on the success value.
     * @param <U> the type of the mapped success value.
     * @return the mapped result if success; otherwise, this Err.
     */
    public <U> Result<U, E> andThen(Function<T, Result<U, E>> op) {
        return isOk() ? op.apply(unwrap()) : castError();
    }

    /**
     * Returns this Ok if it is success, otherwise returns the provided alternative.
     *
     * @param other the alternative result.
     * @return this if Ok; otherwise other.
     */
    public Result<T, E> or(Result<T, E> other) {
        return isOk() ? this : other;
    }

    /**
     * Calls the provided function with the error value and returns its result if this is Err.
     * Otherwise, returns this Ok.
     *
     * @param op the function to handle the error.
     * @param <F> the new type of error.
     * @return this if Ok; otherwise, the result produced by the function.
     */
    public <F> Result<T, F> orElse(Function<E, Result<T, F>> op) {
        return isOk() ? ok(unwrap()) : op.apply(unwrapErr());
    }

    /**
     * Maps the success value using the provided function if this is Ok.
     * Otherwise, returns this Err.
     *
     * @param mapper the function to map the success value.
     * @param <U>    the type of the mapped success value.
     * @return a new Result with the mapped success value, or this Err.
     */
    public <U> Result<U, E> map(Function<T, U> mapper) {
        if (this instanceof Ok<T, E> ok) {
            return ok(mapper.apply(ok.value));
        } else {
            return err(unwrapErr());
        }
    }

    /**
     * Maps the error value using the provided function if this is Err.
     * Otherwise, returns this Ok.
     *
     * @param mapper the function to map the error value.
     * @param <F>    the type of the mapped error value.
     * @return a new Result with the mapped error value, or this Ok.
     */
    public <F> Result<T, F> mapErr(Function<E, F> mapper) {
        if (this instanceof Err<T, E> err) {
            return err(mapper.apply(err.error));
        } else {
            return ok(unwrap());
        }
    }

    /**
     * Runs the provided consumer on the success value if this is Ok.
     *
     * @param inspector a consumer to inspect the success value.
     * @return this Result unchanged.
     */
    public Result<T, E> inspect(Consumer<T> inspector) {
        if (isOk()) {
            inspector.accept(unwrap());
        }
        return this;
    }

    /**
     * Runs the provided consumer on the error value if this is Err.
     *
     * @param inspector a consumer to inspect the error value.
     * @return this Result unchanged.
     */
    public Result<T, E> inspectErr(Consumer<E> inspector) {
        if (isErr()) {
            inspector.accept(unwrapErr());
        }
        return this;
    }

    /**
     * Returns true if this is Ok and the success value satisfies the given predicate.
     *
     * @param predicate a predicate to test the success value.
     * @return true if this is Ok and the predicate returns true; otherwise false.
     */
    public boolean isOkAnd(Predicate<T> predicate) {
        return isOk() && predicate.test(unwrap());
    }

    /**
     * Returns true if this is Err and the error value satisfies the given predicate.
     *
     * @param predicate a predicate to test the error value.
     * @return true if this is Err and the predicate returns true; otherwise false.
     */
    public boolean isErrAnd(Predicate<E> predicate) {
        return isErr() && predicate.test(unwrapErr());
    }

    /**
     * Unwraps the success value if present, or throws an IllegalStateException with the given message.
     *
     * @param msg the message to use for the exception if this is Err.
     * @return the success value.
     * @throws IllegalStateException if this is Err.
     */
    public T expect(String msg) {
        if (isOk()) {
            return unwrap();
        } else {
            throw new IllegalStateException(msg + ": " + unwrapErr());
        }
    }

    /**
     * Unwraps the error value if present, or throws an IllegalStateException with the given message.
     *
     * @param msg the message to use for the exception if this is Ok.
     * @return the error value.
     * @throws IllegalStateException if this is Ok.
     */
    public E expectErr(String msg) {
        if (isErr()) {
            return unwrapErr();
        } else {
            throw new IllegalStateException(msg + ": " + unwrap());
        }
    }

    /**
     * Returns the success value if present, otherwise returns the result of the provided supplier.
     *
     * @param defaultSupplier a supplier providing a default success value.
     * @return the success value or a default.
     */
    public T unwrapOrDefault(Supplier<T> defaultSupplier) {
        return isOk() ? unwrap() : defaultSupplier.get();
    }

    /**
     * If this is an Ok variant that itself contains a Result, flattens the nested Result.
     * Otherwise, returns this Result.
     *
     * @return a flattened Result if possible; otherwise, this Result.
     */
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> flatten() {
        if (isOk() && unwrap() instanceof Result<?, ?>) {
            return (Result<U, E>) unwrap();
        }
        return (Result<U, E>) this;
    }

    /**
     * Converts this Result to an Optional containing the success value if present.
     *
     * @return an Optional with the success value, or empty if this is Err.
     */
    public Optional<T> ok() {
        return isOk() ? Optional.of(unwrap()) : Optional.empty();
    }

    /**
     * Converts this Result to an Optional containing the error value if present.
     *
     * @return an Optional with the error value, or empty if this is Ok.
     */
    public Optional<E> err() {
        return isErr() ? Optional.of(unwrapErr()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <U> Result<U, E> castError() {
        return (Result<U, E>) this;
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    /**
     * Represents a successful Result.
     *
     * @param <T> The type of the success value.
     * @param <E> The type of the error value.
     */
    public static final class Ok<T, E> extends Result<T, E> {
        private final T value;

        /**
         * Constructs an Ok with the given value.
         *
         * @param value the success value.
         */
        public Ok(T value) {
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
            throw new IllegalStateException("Called unwrapErr on Ok: " + value);
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
            // Do nothing.
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Ok<?, ?> ok)) return false;
            return Objects.equals(value, ok.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "Ok(" + value + ")";
        }
    }

    /**
     * Represents an erroneous Result.
     *
     * @param <T> The type of the success value.
     * @param <E> The type of the error value.
     */
    public static final class Err<T, E> extends Result<T, E> {
        private final E error;

        /**
         * Constructs an Err with the given error.
         *
         * @param error the error value.
         */
        public Err(E error) {
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
            throw new IllegalStateException("Called unwrap on Err: " + error);
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
            // Do nothing.
        }

        @Override
        public void ifErr(Consumer<E> action) {
            action.accept(error);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Err<?, ?> err)) return false;
            return Objects.equals(error, err.error);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(error);
        }

        @Override
        public String toString() {
            return "Err(" + error + ")";
        }
    }
}
