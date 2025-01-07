package com.github.walker84837.JResult;

import java.util.concurrent.Callable;

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
}
