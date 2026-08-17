package org.winlogon.jresult;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {
    @Test
    public void testAttemptSuccess() {
        Result<Integer, Exception> result = Result.attempt(() -> 42);
        assertTrue(result.isOk(), "Expected an Ok result");
        assertEquals(42, result.unwrap(), "Ok value should match the returned value");
    }

    @Test
    public void testAttemptFailure() {
        Exception testEx = new IllegalArgumentException("bad call");
        Result<Integer, Exception> result = Result.attempt(() -> {
            throw testEx;
        });
        assertTrue(result.isErr(), "Expected an Err result");
        Exception ex = result.unwrapErr();
        assertEquals(testEx, ex, "Returned exception should match thrown exception");
    }

    @Test
    public void testAttemptCustomErrorMapper() {
        // Using the overload that allows a custom error mapper.
        Result<Integer, String> result = Result.attempt(() -> {
            throw new RuntimeException("custom error");
        }, ex -> "Mapped: " + ex.getMessage());
        assertTrue(result.isErr(), "Expected an Err result");
        assertEquals("Mapped: custom error", result.unwrapErr(), "Error message should be mapped");
    }

    @Test
    public void testAttemptRunnableSuccess() {
        final boolean[] ran = {false};
        Result<Void, Exception> result = Result.attempt(() -> { ran[0] = true; });
        assertTrue(result.isOk(), "Expected an Ok result");
        assertTrue(ran[0], "Runnable should have been executed");
        assertNull(result.unwrap(), "Ok value of a Runnable attempt is null");
    }

    @Test
    public void testAttemptRunnableFailure() {
        Result<Void, String> result = Result.attempt(() -> {
            throw new RuntimeException("boom");
        }, ex -> "Mapped: " + ex.getMessage());
        assertTrue(result.isErr(), "Expected an Err result");
        assertEquals("Mapped: boom", result.unwrapErr(), "Error message should be mapped");
    }

    @Test
    public void testAttemptNarrowedException() {
        Result<Integer, NumberFormatException> result = Result.attempt(
            NumberFormatException.class,
            () -> Integer.parseInt("not-a-number"));
        assertTrue(result.isErr(), "Expected an Err result");
        assertEquals(NumberFormatException.class, result.unwrapErr().getClass(),
            "Caught exception should keep its specific type");
    }

    @Test
    public void testAttemptNarrowedExceptionMapped() {
        Result<Integer, String> result = Result.attempt(
            NumberFormatException.class,
            () -> Integer.parseInt("not-a-number"),
            ex -> "Bad number: " + ex.getMessage());
        assertTrue(result.isErr(), "Expected an Err result");
        assertEquals("Bad number: For input string: \"not-a-number\"", result.unwrapErr());
    }

    @Test
    public void testAttemptNarrowedExceptionSuccess() {
        Result<Integer, NumberFormatException> result = Result.attempt(
            NumberFormatException.class,
            () -> Integer.parseInt("42"));
        assertTrue(result.isOk(), "Expected an Ok result");
        assertEquals(42, result.unwrap());
    }

    @Test
    public void testAndCombinator() {
        Result<Integer, Exception> first = Result.ok(10);
        Result<String, Exception> second = Result.ok("test");
        Result<String, Exception> combined = first.and(second);
        assertTrue(combined.isOk(), "Combined result should be Ok");
        assertEquals("test", combined.unwrap(), "Second value should be returned when first is Ok");

        Result<Integer, Exception> errorResult = Result.err(new Exception("fail"));
        combined = errorResult.and(second);
        assertTrue(combined.isErr(), "And should propagate error when the first result is Err");
        assertEquals("fail", combined.unwrapErr().getMessage(), "Error message should come from the original Err");
    }

    @Test
    public void testAndThenCombinator() {
        Result<Integer, Exception> okResult = Result.ok(5);
        Result<String, Exception> result = okResult.andThen(val -> Result.ok("Value: " + val));
        assertTrue(result.isOk(), "Expected an Ok result");
        assertEquals("Value: 5", result.unwrap(), "Mapped value should match transformation");

        Result<Integer, Exception> errorResult = Result.err(new Exception("failure"));
        result = errorResult.andThen(val -> Result.ok("Should not get here"));
        assertTrue(result.isErr(), "Expected an Err result from an error input");
        assertEquals("failure", result.unwrapErr().getMessage());
    }

    @Test
    public void testOrCombinator() {
        Result<Integer, Exception> okResult = Result.ok(100);
        Result<Integer, Exception> fallback = Result.ok(200);
        Result<Integer, Exception> result = okResult.or(fallback);
        assertTrue(result.isOk(), "Ok result should not be replaced by the fallback");
        assertEquals(100, result.unwrap());

        Result<Integer, Exception> errorResult = Result.err(new Exception("error"));
        result = errorResult.or(fallback);
        assertTrue(result.isOk(), "Err should be replaced by the fallback when using or");
        assertEquals(200, result.unwrap());
    }

    @Test
    public void testOrElseCombinator() {
        Result<Integer, Exception> okResult = Result.ok(50);
        Result<Integer, String> result = okResult.orElse(err -> Result.ok(999));
        assertTrue(result.isOk(), "Ok result should pass through unmodified");
        assertEquals(50, result.unwrap());

        Result<Integer, Exception> errorResult = Result.err(new Exception("oops"));
        result = errorResult.orElse(err -> Result.ok(err.getMessage().length()));
        assertTrue(result.isOk(), "orElse should convert an error to a new Ok value");
        assertEquals(4, result.unwrap(), "The length of 'oops' is 4");
    }

    @Test
    public void testMapAndMapErr() {
        // Test map on Ok
        Result<Integer, Exception> okResult = Result.ok(7);
        Result<String, Exception> mapped = okResult.map(val -> "Number: " + val);
        assertTrue(mapped.isOk());
        assertEquals("Number: 7", mapped.unwrap());

        // Test map when Err remains unchanged
        Result<Integer, Exception> errorResult = Result.err(new Exception("failure"));
        mapped = errorResult.map(val -> "Should not map");
        assertTrue(mapped.isErr());
        assertEquals("failure", mapped.unwrapErr().getMessage());

        // Test mapErr on an Err variant
        Result<Integer, Exception> errorResult2 = Result.err(new Exception("bad"));
        Result<Integer, String> mappedError = errorResult2.mapErr(err -> "Mapped error: " + err.getMessage());
        assertTrue(mappedError.isErr());
        assertEquals("Mapped error: bad", mappedError.unwrapErr());

        // mapErr on Ok should leave the value unchanged.
        mappedError = okResult.mapErr(err -> "Ignored");
        assertTrue(mappedError.isOk());
        assertEquals(7, mappedError.unwrap());
    }

    @Test
    public void testInspectAndInspectErr() {
        final StringBuilder okLog = new StringBuilder();
        final StringBuilder errLog = new StringBuilder();

        Result<Integer, Exception> okResult = Result.ok(20);
        okResult.inspect(val -> okLog.append("Value:").append(val))
                .inspectErr(err -> errLog.append("Error:").append(err));
        assertEquals("Value:20", okLog.toString());
        assertEquals("", errLog.toString());

        Result<Integer, Exception> errorResult = Result.err(new Exception("Oops"));
        errorResult.inspect(val -> okLog.append("Should not be called"))
                .inspectErr(err -> errLog.append("Error:").append(err.getMessage()));
        assertTrue(errLog.toString().contains("Error:Oops"));
    }

    @Test
    public void testPredicateMethods() {
        Result<Integer, Exception> okResult = Result.ok(15);
        assertTrue(okResult.isOkAnd(val -> val > 10));
        assertFalse(okResult.isOkAnd(val -> val < 10));

        Result<Integer, Exception> errorResult = Result.err(new Exception("error"));
        assertTrue(errorResult.isErrAnd(err -> err.getMessage().toLowerCase().contains("error")));
        assertFalse(errorResult.isErrAnd(err -> err.getMessage().contains("good")));
    }

    @Test
    public void testExpectAndExpectErr() {
        Result<Integer, Exception> okResult = Result.ok(33);
        assertEquals(33, okResult.expect("Should be ok"));

        Result<Integer, Exception> errorResult = Result.err(new Exception("failure"));
        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> okResult.expectErr("Expected error"),
                "expectErr on an Ok should throw an exception"
        );
        IllegalStateException thrown2 = assertThrows(
                IllegalStateException.class,
                () -> errorResult.expect("Expected ok"),
                "expect on an Err should throw an exception"
        );
        assertTrue(thrown2.getMessage().contains("failure"));
    }

    @Test
    public void testUnwrapOrAndUnwrapOrElse() {
        Result<Integer, Exception> okResult = Result.ok(10);
        assertEquals(10, okResult.unwrapOr(99));
        assertEquals(10, okResult.unwrapOrElse(err -> 99));

        Result<Integer, Exception> errorResult = Result.err(new Exception("oops"));
        assertEquals(99, errorResult.unwrapOr(99));
        assertEquals(4, errorResult.unwrapOrElse(err -> err.getMessage().length()));
    }

    @Test
    public void testUnwrapOrDefault() {
        Result<Integer, Exception> okResult = Result.ok(123);
        assertEquals(123, okResult.unwrapOrElse(() -> 0));

        Result<Integer, Exception> errorResult = Result.err(new Exception("error"));
        assertEquals(0, errorResult.unwrapOrElse(() -> 0));
    }

    @Test
    public void testFlatten() {
        // Flatten a nested Ok
        Result<Result<Integer, Exception>, Exception> nestedOk = Result.ok(Result.ok(42));
        Result<Integer, Exception> flat = nestedOk.flatten();
        assertTrue(flat.isOk());
        assertEquals(42, flat.unwrap());

        // Flatten a nested Err contained within an Ok
        Result<Result<Integer, Exception>, Exception> nestedErr = Result.ok(Result.err(new Exception("nested error")));
        Result<Integer, Exception> newFlat = nestedErr.flatten();
        assertTrue(newFlat.isErr());
        assertEquals("nested error", newFlat.unwrapErr().getMessage());

        // Flattening an Err (non-nested) should be a no-op.
        Result<Integer, Exception> simpleErr = Result.err(new Exception("simple error"));
        flat = simpleErr.flatten();
        assertTrue(flat.isErr());
        assertEquals("simple error", flat.unwrapErr().getMessage());
    }

    @Test
    public void testOptionalConversions() {
        Result<Integer, Exception> okResult = Result.ok(777);
        Optional<Integer> optional = okResult.valueOpt();
        assertTrue(optional.isPresent());
        assertEquals(777, optional.get());

        Result<Integer, Exception> errorResult = Result.err(new Exception("missing"));
        Optional<Integer> optionalErr = errorResult.valueOpt();
        assertFalse(optionalErr.isPresent());

        Optional<Exception> errOpt = errorResult.errorOpt();
        assertTrue(errOpt.isPresent());
        assertEquals("missing", errOpt.get().getMessage());
    }

    @Test
    public void testFromOptionalWithException() {
        // Present Optional yields Ok, with the error type constrained to Throwable.
        var okResult = Result.fromOptionalWithException(Optional.of("present"), IllegalStateException::new);
        assertTrue(okResult.isOk());
        assertEquals("present", okResult.unwrap());

        // Empty Optional yields Err carrying the Throwable produced by the supplier.
        var errorResult = Result.fromOptionalWithException(Optional.empty(), IllegalStateException::new);
        assertTrue(errorResult.isErr());
        assertEquals(IllegalStateException.class, errorResult.unwrapErr().getClass());
    }
}
