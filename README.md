# JResult

`JResult` is a lightweight Java library that provides a functional approach to handling success (`Ok`) and error (`Err`) values. Inspired by Rust's `Result` type, it helps eliminate the need for exception handling in scenarios where a value may either succeed or fail.

This library is licensed under the LGPL.

## Features

- Strongly-typed representation of success (`Ok`) and error (`Err`) values.
- Safe unwrapping with fallback values or custom error handling.
- Flexible mapping and chaining of success and error transformations.
- Utility functions for handling try-catch scenarios.

## Installation

Add the following dependency to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>com.github.walker84837</groupId>
    <artifactId>JResult</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Creating Results

```java
import com.github.walker84837.JResult.Result;

Result<Integer, String> success = Result.ok(42);
Result<Integer, String> error = Result.err("An error occurred");
```

### Checking Result type

```java
if (success.isOk()) {
    System.out.println("Success value: " + success.unwrap());
}

if (error.isErr()) {
    System.out.println("Error value: " + error.unwrapErr());
}
```

### Handling Default values

```java
int result = error.unwrapOr(0); // Fallback to 0 if it's an Err
System.out.println(result); // Outputs: 0
```

### Using Fallback logic

```java
int result = error.unwrapOrElse(err -> {
    System.out.println("Handling error: " + err);
    return -1; // Return a fallback value
});
System.out.println(result); // Outputs: -1
```

### Mapping values

Transform the success or error values using `map` and `mapErr`:

```java
Result<String, String> mappedSuccess = success.map(val -> "Value is: " + val);
Result<Integer, String> mappedError = error.mapErr(err -> "Error: " + err);

System.out.println(mappedSuccess.unwrap()); // Outputs: Value is: 42
System.out.println(mappedError.unwrapErr()); // Outputs: Error: An error occurred
```

### Performing actions with `ifOk` and `ifErr`

```java
success.ifOk(val -> System.out.println("Success: " + val));
error.ifErr(err -> System.out.println("Error: " + err));
```

### Try-catch utility

Convert exceptions into `Result` using `ResultUtils.tryCatch`:

```java
import com.github.walker84837.JResult.ResultUtils;
import com.github.walker84837.JResult.Result;

Result<Integer, Exception> result = ResultUtils.tryCatch(() -> {
    if (Math.random() > 0.5) throw new RuntimeException("Random failure");
    return 123;
});

result.ifOk(val -> System.out.println("Got value: " + val));
result.ifErr(err -> System.out.println("Caught exception: " + err.getMessage()));
```

## Contributing

Feel free to submit pull requests or issues on the [GitHub repository](https://github.com/walker84837/JResult). Contributions are welcome!

## License

This library is distributed under the LGPL. See the [LICENSE](LICENSE) file for details.
