# JResult

[![Build and Test](https://github.com/walker84837/JResult/actions/workflows/java.yml/badge.svg)](https://github.com/walker84837/JResult/actions/workflows/java.yml)

`JResult` is a lightweight Java library inspired by Rust's [Result<T, E>](https://doc.rust-lang.org/stable/core/result/enum.Result.html) to help eliminate the need for exception handling in scenarios where a value may either succeed or fail.

## Table of Contents

- [Features](#features)
- [Usage examples](#usage-examples)
  - [Basic usage](#basic-usage)
  - [File handling](#file-handling)
  - [HTTP request](#http-request)
  - [Database operations](#database-operations)
  - [Chaining operations](#chaining-operations)
  - [Advanced error handling](#advanced-error-handling)
  - [Real-world validation example](#real-world-validation-example)
- [Best practices](#best-practices)
- [Contributing](#contributing)
- [License](#license)

## Features

- Strongly-typed representation of success (`Ok`) and error (`Err`) values
- Safe unwrapping with fallback values or custom error handling
- Flexible mapping and chaining of success and error transformations
- Utility functions for handling try-catch scenarios
- Seamless integration with Java 17+ features

## Documentation

The documentation is hosted at <https://walker84837.github.io/JResult>.

## Usage examples

### Basic usage

```java
import org.winlogon.jresult.Result;

var success = Result.ok(42);
var error = Result.err("Something went wrong");

// Pattern matching (Java 21+)
switch (success) {
    case Result.Ok<Integer, String>(var value) -> 
        System.out.println("Success: " + value);
    case Result.Err<Integer, String>(var err) -> 
        System.out.println("Error: " + err);
}

// Safe unwrapping
var value = success.unwrapOr(0); // 42
var fallback = error.unwrapOrElse(err -> {
    System.out.println("Error occurred: " + err);
    return -1;
}); // -1
```

### File handling

```java
import java.nio.file.Files;
import java.nio.file.Path;
import org.winlogon.jresult.Result;

var filePath = Path.of("example.txt");

var fileContent = Result.attempt(() -> Files.readString(filePath));

var processed = fileContent
    .map(content -> content.toUpperCase())
    .inspect(content -> System.out.println("File content: " + content))
    .mapErr(e -> "Failed to read file: " + e.getMessage());

var writeResult = processed.andThen(content -> 
    Result.attempt(() -> {
        Files.writeString(filePath, content);
        return "File written successfully";
    }, e -> "Write error: " + e.getMessage()));

writeResult.ifOk(msg -> System.out.println(msg));
writeResult.ifErr(msg -> System.err.println(msg));
```

### HTTP request

```java
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.winlogon.jresult.Result;

var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .build();

var response = Result.attempt(() ->
    client.send(request, HttpResponse.BodyHandlers.ofString()),
    e -> "HTTP request failed: " + e.getMessage());

var processed = response
    .andThen(res -> {
        if (res.statusCode() == 200) {
            return Result.ok(res.body());
        }
        return Result.err("HTTP error: " + res.statusCode());
    })
    .map(body -> parseJson(body)); // Assume parseJson is defined

processed.ifOk(data -> System.out.println("Received data: " + data));
processed.ifErr(err -> System.err.println("Error: " + err));
// or...
processed.match(
    data -> System.out.println("Received data: " + data),
    err -> System.err.println("Error: " + err)
);
```

### Database operations

```java
import java.sql.*;
import org.winlogon.jresult.Result;

record User(int id, String name) {}

var dbUrl = "jdbc:postgresql://localhost/mydb";

var userResult = Result.attempt(() -> {
    try (var conn = DriverManager.getConnection(dbUrl, "user", "pass");
         var stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
        stmt.setInt(1, 42);
        var rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(rs.getInt("id"), rs.getString("name"));
        }
        return null;
    }
}, e -> "Database error: " + e.getMessage());

var greeting = userResult
    .map(user -> "Hello, " + user.name() + "!")
    .unwrapOr("User not found");

System.out.println(greeting);
```

### Chaining operations

```java
import org.winlogon.jresult.Result;

var result = Result.ok(5)
    .map(x -> x * 2) // 10
    .andThen(x -> x > 0 ? Result.ok(x) : Result.err("Negative value"))
    .map(x -> x + 3) // 13
    .inspect(x -> System.out.println("Current value: " + x));

// Complex pipeline
var finalResult = Result.ok("input.txt")
    .andThen(filename -> Result.attempt(() -> Files.readString(Path.of(filename))))
    .map(content -> content.split("\\s+").length)
    .map(count -> "Word count: " + count)
    .unwrapOr("Could not count words");
```

### Advanced error handling

```java
import org.winlogon.jresult.Result;

enum AppError {
    FILE_NOT_FOUND,
    PERMISSION_DENIED,
    NETWORK_ERROR
}

var operation = Result.ok("data.json")
    // Java 21+ - switch expressions with pattern matching
    .mapErr(e -> switch (e) {
        case FileNotFoundException _ -> AppError.FILE_NOT_FOUND;
        case SecurityException _ -> AppError.PERMISSION_DENIED;
        case IOException _ -> AppError.NETWORK_ERROR;
        default -> throw new RuntimeException("Unexpected error");
    })
    .andThen(filename -> parseFile(filename))
    .orElse(err -> {
        System.err.println("Operation failed: " + err);
        return Result.ok(getDefaultData());
    });
```

### Wrapping exceptions with `Result.attempt`

`Result.attempt` replaces the old `ResultUtils` class and lives directly on `Result`.
It runs code that may throw and wraps the outcome in a `Result`.

```java
import org.winlogon.jresult.Result;

// Catch any Exception (error type is Exception)
Result<String, Exception> read = Result.attempt(() -> Files.readString(path));

// Map the error into your own error type
Result<String, String> read2 = Result.attempt(
    () -> Files.readString(path),
    e -> "Read failed: " + e.getMessage()
);

// Side-effecting operations (no return value) use the Runnable overload
Result<Void, String> write = Result.attempt(
    () -> Files.writeString(path, content),
    e -> "Write failed: " + e.getMessage()
);

// Narrow the caught exception to a specific type, keeping it strongly typed
Result<Integer, NumberFormatException> parsed = Result.attempt(
    NumberFormatException.class,      // only this exception is caught
    () -> Integer.parseInt("42")      // supplier may only throw NumberFormatException
);
```

> Any exception not of the declared type is rethrown wrapped in a
> `RuntimeException`, since the supplier is not allowed to throw it.

### Real-world validation example

```java
record UserInput(String name, String email, int age) {}

Result<UserInput, String> validateInput(String name, String email, int age) {
    if (name == null || name.isBlank()) {
        return Result.err("Name cannot be empty");
    }
    if (!email.contains("@")) {
        return Result.err("Invalid email format");
    }
    if (age < 18) {
        return Result.err("Must be at least 18 years old");
    }
    return Result.ok(new UserInput(name, email, age));
}

var userResult = validateInput("Alice", "alice@example.com", 25)
    .inspect(user -> System.out.println("Valid user: " + user))
    .mapErr(err -> "Validation error: " + err);
```

## Best practices

1. **Prefer `map`/`andThen` over direct unwrapping**:
   ```java
   // Good
   result.map(processData).andThen(saveData);
   
   // Bad
   if (result.isOk()) {
       var data = processData(result.unwrap());
       saveData(data);
   }
   ```

2. **Use descriptive error types**:
   ```java
   // Instead of just String errors
   enum DataError { INVALID_FORMAT, MISSING_FIELD, NETWORK_ISSUE }
   
   Result<Data, DataError> fetchData() { ... }
   ```

3. **Combine with Java 21 features**:
   ```java
   var result = switch (operation()) {
       case Ok(var data) -> process(data);
       case Err(var err) -> handleError(err);
   };
   ```

 4. **Use `Result.attempt` for exception-heavy code**:
    ```java
    var data = Result.attempt(
        () -> parseJson(fetchFromNetwork()),
        e -> "Failed: " + e.getMessage()
    );
    ```

## Contributing

Feel free to submit pull requests or issues on the [GitHub repository](https://github.com/walker84837/JResult). Contributions are welcome!

## License

This library is licensed under the [0BSD License](https://opensource.org/licenses/0BSD).
