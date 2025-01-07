package com.github.walker84837.JResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;

class ResultTest {

    @Test
    void testOkResult() {
        Result<Integer, String> result = Result.ok(42);

        assertTrue(result.isOk());
        assertFalse(result.isErr());
        assertEquals(42, result.unwrap());
        assertEquals(42, result.unwrapOr(0));
        assertEquals(42, result.unwrapOrElse(error -> -1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, result::unwrapErr);
        assertEquals("Called unwrapErr on an Ok value", exception.getMessage());
    }

    @Test
    void testErrResult() {
        Result<Integer, String> result = Result.err("Error occurred");

        assertFalse(result.isOk());
        assertTrue(result.isErr());
        assertEquals("Error occurred", result.unwrapErr());
        assertEquals(0, result.unwrapOr(0));
        assertEquals(-1, result.unwrapOrElse(error -> -1));

        IllegalStateException exception = assertThrows(IllegalStateException.class, result::unwrap);
        assertEquals("Called unwrap on an Err value", exception.getMessage());
    }

    @Test
    void testIfOk() {
        Result<Integer, String> result = Result.ok(42);
        StringBuilder output = new StringBuilder();

        result.ifOk(value -> output.append("Value: ").append(value));
        result.ifErr(error -> output.append("Error: ").append(error));

        assertEquals("Value: 42", output.toString());
    }

    @Test
    void testIfErr() {
        Result<Integer, String> result = Result.err("Error occurred");
        StringBuilder output = new StringBuilder();

        result.ifOk(value -> output.append("Value: ").append(value));
        result.ifErr(error -> output.append("Error: ").append(error));

        assertEquals("Error: Error occurred", output.toString());
    }

    @Test
    void testMapOk() {
        Result<Integer, String> result = Result.ok(42);
        Result<String, String> mapped = result.map(value -> "Mapped: " + value);

        assertTrue(mapped.isOk());
        assertEquals("Mapped: 42", mapped.unwrap());
    }

    @Test
    void testMapErr() {
        Result<Integer, String> result = Result.err("Error occurred");
        Result<Integer, String> mapped = result.map(value -> -1);

        assertTrue(mapped.isErr());
        assertEquals("Error occurred", mapped.unwrapErr());
    }

    @Test
    void testMapErrFunctionality() {
        Result<Integer, String> result = Result.err("Error occurred");
        Result<Integer, Integer> mapped = result.mapErr(error -> error.length());

        assertTrue(mapped.isErr());
        assertEquals(14, mapped.unwrapErr());
    }
}

class ResultUtilsTest {

    @Test
    void testTryCatchWithSuccess() {
        Callable<Integer> callable = () -> 42;
        Result<Integer, Exception> result = ResultUtils.tryCatch(callable);

        assertTrue(result.isOk());
        assertEquals(42, result.unwrap());
    }

    @Test
    void testTryCatchWithException() {
        Callable<Integer> callable = () -> { throw new RuntimeException("Failure"); };
        Result<Integer, Exception> result = ResultUtils.tryCatch(callable);

        assertTrue(result.isErr());
        assertEquals("Failure", result.unwrapErr().getMessage());
    }
}
