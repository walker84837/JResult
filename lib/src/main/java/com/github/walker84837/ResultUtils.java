package com.github.walker84837;

import java.util.concurrent.Callable;

public class ResultUtils {
    public static <T> Result<T, Exception> tryCatch(Callable<T> callable) {
        try {
            return Result.ok(callable.call());
        } catch (Exception e) {
            return Result.err(e);
        }
    }
}
