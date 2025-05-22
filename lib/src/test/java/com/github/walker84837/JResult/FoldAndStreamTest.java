package com.github.walker84837.JResult;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class FoldAndStreamTest {

    @Test
    public void testFoldOnOk() {
        Result<Integer, String> okResult = Result.ok(10);

        // when folding on an Ok, the onOk function should be applied and returned
        String folded = okResult.fold(
            val -> "Value is " + val,
            err -> "Error: " + err
        );
        assertEquals("Value is 10", folded, "fold should apply onOk for Ok variants");
    }

    @Test
    public void testFoldOnErr() {
        Result<Integer, String> errResult = Result.err("something went wrong");

        // when folding on an Err, the onErr function should be applied and returned
        String folded = errResult.fold(
            val -> "Value is " + val,
            err -> "Error: " + err.toUpperCase()
        );
        assertEquals("Error: SOMETHING WENT WRONG", folded, "fold should apply onErr for Err variants");
    }

    @Test
    public void testFoldWithDifferentReturnTypes() {
        // verify that fold can return a different type than T or E
        Result<Integer, Integer> okResult = Result.ok(5);
        Result<Integer, Integer> errResult = Result.err(7);

        // onOk returns Boolean, onErr returns Boolean
        Boolean okFolded = okResult.fold(
            val -> val % 2 == 0,
            err -> err % 2 != 0
        );
        assertFalse(okFolded, "5 is not even, so onOk should return false");

        Boolean errFolded = errResult.fold(
            val -> val % 2 == 0,
            err -> err % 2 != 0
        );
        assertTrue(errFolded, "7 is odd, so onErr should return true");
    }

    @Test
    public void testToStreamOnOk() {
        Result<String, String> okResult = Result.ok("hello");

        // toStream on Ok should produce a single-element Stream containing the value
        List<String> list = okResult.toStream().collect(Collectors.toList());
        assertEquals(1, list.size(), "toStream on Ok should yield a one‐element Stream");
        assertEquals("hello", list.get(0), "the Stream element should be the unwrapped value");
    }

    @Test
    public void testToStreamOnErr() {
        Result<String, String> errResult = Result.err("oops");

        // toStream on Err should produce an empty Stream
        List<String> list = errResult.toStream().collect(Collectors.toList());
        assertTrue(list.isEmpty(), "toStream on Err should yield an empty Stream");
    }

    @Test
    public void testToStreamChaining() {
        // verify that toStream can be chained with other Stream operations without errors
        Result<Integer, String> okResult = Result.ok(3);
        int sum = okResult
            .toStream()
            .map(i -> i * 2)
            .reduce(0, Integer::sum);
        assertEquals(6, sum, "Chaining map and reduce on toStream should work for Ok");

        Result<Integer, String> errResult = Result.err("no value");
        int sumErr = errResult
            .toStream()
            .map(i -> i * 2)
            .reduce(0, Integer::sum);
        assertEquals(0, sumErr, "Chaining map and reduce on toStream should yield zero for Err (empty Stream)");
    }

    @Test
    public void testFoldLazyBehavior() {
        // ensure that fold does not evaluate the other branch
        Result<Integer, RuntimeException> okResult = Result.ok(2);

        // the onErr branch throws if invoked, so if fold is implemented correctly
        // only the onOk side runs and no exception is thrown.
        assertDoesNotThrow(() -> {
            int result = okResult.fold(
                val -> val + 3,
                err -> { throw new RuntimeException("This should not be called"); }
            );
            assertEquals(5, result, "fold on Ok should compute 2 + 3 = 5");
        });

        Result<Integer, RuntimeException> errResult = Result.err(new RuntimeException("bad"));
        // now ensure that onOk is not run.
        assertDoesNotThrow(() -> {
            int result = errResult.fold(
                val -> { throw new RuntimeException("This should not be called either"); },
                err -> err.getMessage().length()
            );
            assertEquals(3, result, "fold on Err should compute length of \"bad\" = 3");
        });
    }
}
