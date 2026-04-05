# expression

Math expression evaluator using Shunting Yard algorithm.

## Package Structure
- `dev.simplified.expression` - Expression, ArrayStack, ValidationResult, VariableProvider
- `dev.simplified.expression.exception` - ExpressionException, EvaluationException, InvalidExpressionException, UnknownFunctionException, UnknownOperatorException
- `dev.simplified.expression.function` - BuiltinFunction, MathFunction, VarargFunction
- `dev.simplified.expression.operator` - BuiltinOperator, MathOperator
- `dev.simplified.expression.shuntingyard` - ShuntingYard
- `dev.simplified.expression.tokenizer` - Token (base), Tokenizer, NumberToken, OperatorToken, FunctionToken, VariableToken, ArgumentSeparatorToken, OpenParenthesesToken, CloseParenthesesToken

## Key Classes
- `Expression` - main entry point: parse + evaluate string expressions
- `ArrayStack` - zero-copy primitive `double[]` stack
- `ShuntingYard` - infix-to-postfix conversion
- `Tokenizer` - lexer producing typed Token subtypes

## Dependencies
- `com.github.simplified-dev:collections:master-SNAPSHOT`
- `com.github.simplified-dev:utils:master-SNAPSHOT`
- JetBrains annotations, Lombok

## Build
```bash
./gradlew build
./gradlew test
```

## Info
- Java 21, Gradle (Kotlin DSL)
- Group: `dev.simplified`, artifact: `expression`, version: `1.0.0`
- 24 source files, 7 packages, no tests
