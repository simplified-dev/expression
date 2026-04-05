# Expression

Mathematical expression evaluator using the Shunting Yard algorithm. Parses and evaluates string-based math expressions with support for custom functions, binary/unary operators, and variables. Features a zero-copy `ArrayStack` for primitive double operations.

> [!IMPORTANT]
> This library is part of the [Simplified-Dev](https://github.com/Simplified-Dev) ecosystem and depends on the `collections` and `utils` modules. All dependencies are resolved automatically via [JitPack](https://jitpack.io/).

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage](#usage)
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Shunting Yard algorithm** - Correct operator-precedence parsing with parentheses and function calls
- **Custom functions** - Register named functions (fixed-arity or vararg) alongside builtins like `sin`, `cos`, `abs`
- **Binary and unary operators** - Extensible operator set with configurable precedence and associativity
- **Variables** - Resolve named variables at evaluation time via `VariableProvider`
- **Expression validation** - Validate expressions before evaluation with detailed `ValidationResult` diagnostics
- **Zero-copy ArrayStack** - Primitive `double[]`-backed stack avoids boxing overhead during evaluation
- **Tokenizer pipeline** - Lexer produces typed tokens (number, operator, function, variable, parentheses, separator)

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [Java](https://adoptium.net/) | **21+** | Required |
| [Gradle](https://gradle.org/) | **9.4+** | Wrapper included (`./gradlew`) |
| [Git](https://git-scm.com/) | 2.x+ | For cloning the repository |

### Installation

Add the JitPack repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.simplified-dev:expression:master-SNAPSHOT")
}
```

<details>
<summary>Gradle (Groovy)</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.simplified-dev:expression:master-SNAPSHOT'
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.simplified-dev</groupId>
    <artifactId>expression</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

</details>

### Usage

```java
import dev.simplified.expression.Expression;

// Simple evaluation
Expression expr = new Expression("2 + 3 * 4");
double result = expr.evaluate(); // 14.0

// With variables
Expression expr = new Expression("x * 2 + y");
double result = expr.evaluate(variables); // resolves x, y at runtime

// With built-in functions
Expression expr = new Expression("sin(3.14159) + abs(-5)");
double result = expr.evaluate(); // 5.0 (approximately)
```

> [!TIP]
> Use `Expression.validate()` to check an expression for syntax errors before calling `evaluate()`. The returned `ValidationResult` contains detailed diagnostics.

## API Overview

| Class | Package | Description |
|-------|---------|-------------|
| `Expression` | `expression` | Main entry point - parses and evaluates string expressions |
| `ArrayStack` | `expression` | Zero-copy primitive `double[]` stack used during evaluation |
| `ValidationResult` | `expression` | Result of expression validation with error details |
| `VariableProvider` | `expression` | Functional interface for resolving variable names to values |
| `ShuntingYard` | `shuntingyard` | Core algorithm implementation - converts infix to postfix |
| `Tokenizer` | `tokenizer` | Lexer that splits expression strings into typed tokens |
| `Token` | `tokenizer` | Base token type with subtypes for numbers, operators, functions, etc. |
| `MathFunction` | `function` | Interface for named mathematical functions |
| `BuiltinFunction` | `function` | Enum of built-in functions (sin, cos, abs, etc.) |
| `VarargFunction` | `function` | Function accepting a variable number of arguments |
| `MathOperator` | `operator` | Interface for binary/unary math operators |
| `BuiltinOperator` | `operator` | Enum of built-in operators (+, -, *, /, etc.) |

> [!NOTE]
> All exception types extend `ExpressionException` in the `dev.simplified.expression.exception` package: `EvaluationException`, `InvalidExpressionException`, `UnknownFunctionException`, and `UnknownOperatorException`.

## Project Structure

```
expression/
├── src/main/java/dev/simplified/expression/
│   ├── ArrayStack.java
│   ├── Expression.java
│   ├── ValidationResult.java
│   ├── VariableProvider.java
│   ├── exception/
│   │   ├── ExpressionException.java
│   │   ├── EvaluationException.java
│   │   ├── InvalidExpressionException.java
│   │   ├── UnknownFunctionException.java
│   │   └── UnknownOperatorException.java
│   ├── function/
│   │   ├── BuiltinFunction.java
│   │   ├── MathFunction.java
│   │   └── VarargFunction.java
│   ├── operator/
│   │   ├── BuiltinOperator.java
│   │   └── MathOperator.java
│   ├── shuntingyard/
│   │   └── ShuntingYard.java
│   └── tokenizer/
│       ├── ArgumentSeparatorToken.java
│       ├── CloseParenthesesToken.java
│       ├── FunctionToken.java
│       ├── NumberToken.java
│       ├── OpenParenthesesToken.java
│       ├── OperatorToken.java
│       ├── Token.java
│       ├── Tokenizer.java
│       └── VariableToken.java
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── LICENSE.md
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see [LICENSE.md](LICENSE.md) for the full text.
