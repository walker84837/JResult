package com.github.walker84837.JResult;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Utility class for creating Results from Callables that may throw Exceptions.
 */
public class ResultUtils {
    /**
     * Creates a Result from a Callable that may throw an Exception.

     * @param callable The Callable to create the Result from.
     * @return The Result created from the Callable.
     */
    public static <T> Result<T, Exception> tryCatch(Callable<T> callable) {
        try {
            return Result.ok(callable.call());
        } catch (Exception e) {
            return Result.err(e);
        }
    }

    /**
     * Executes the given Callable and wraps the result in an {@code Ok}, or catches any
     * Exception and returns an {@code Err} using the provided error mapping function.
     *
     * @param callable    The Callable to execute.
     * @param errorMapper A function that converts a caught Exception into an error of type {@code E}.
     * @param <T>         The type of the success value.
     * @param <E>         The type of the error value.
     * @return A Result containing the success value or an error produced by the mapper.
     */
    public static <T, E> Result<T, E> tryCatch(Callable<T> callable, Function<Exception, E> errorMapper) {
        try {
            return Result.ok(callable.call());
        } catch (Exception e) {
            return Result.err(errorMapper.apply(e));
        }
    }
}
