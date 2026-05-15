/*
 * Copyright 2014 Frank Asseg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.simplified.expression.tokenizer;

import dev.simplified.expression.shuntingyard.ShuntingYard;

/**
 * Token representing a close parenthesis, bracket, or brace in a mathematical expression.
 * <p>
 * The {@link Tokenizer} emits this token when it encounters a {@code ')'},
 * {@code ']'}, or {@code '}'} character. All three variants are treated
 * identically by the {@link ShuntingYard ShuntingYard}
 * algorithm.
 *
 * @see Token#TOKEN_PARENTHESES_CLOSE
 * @see OpenParenthesesToken
 * @see Tokenizer
 */
class CloseParenthesesToken extends Token {

    /**
     * Creates a new close-parenthesis token.
     */
    CloseParenthesesToken() {
        super(TOKEN_PARENTHESES_CLOSE);
    }

}
