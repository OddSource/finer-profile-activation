/*
 * Copyright © 2010-2023 OddSource Code (license@oddsource.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.oddsource.java.maven.profile;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;

/**
 * A collection of helpful utilities.
 */
public final class Utilities
{
    private Utilities()
    {
    }

    /**
     * Get a regex pattern from the property value.
     *
     * @param property The property
     * @param problems A collector of problems
     * @return a pattern, or {@code null} if problems were added to the collector.
     */
    public static Pattern getPattern(final ActivationProperty property, final ModelProblemCollector problems)
    {
        return Utilities.getPattern(property, problems, 0);
    }

    /**
     * Get a regex pattern from the property value.
     *
     * @param property The property
     * @param problems A collector of problems
     * @param flags Bitwise regular expression compilation flags, see {@link Pattern}
     * @return a pattern, or {@code null} if problems were added to the collector.
     */
    public static Pattern getPattern(
        final ActivationProperty property,
        final ModelProblemCollector problems,
        final int flags
    )
    {
        final String value = property.getValue();
        try
        {
            return Pattern.compile(value, flags);
        }
        catch(final PatternSyntaxException e)
        {
            Utilities.problem(problems, property,
                "The property 'value' ('" + value + "') could not be compiled to a regular expression."
            );
            return null;
        }
    }

    /**
     * Tokenize a command string into command arguments, accounting for quotes and spaces.
     *
     * @param commandString The command string
     * @return the command arguments.
     */
    public static List<String> tokenize(final String commandString)
    {
        final List<String> command = new ArrayList<>();
        final StringBuilder currentArgument = new StringBuilder(commandString.length());
        Character quoteToken = null;
        char previous = '\0';
        for(final char c : commandString.toCharArray())
        {
            if(quoteToken != null && quoteToken.equals(c))
            {
                if(previous == '\\')
                {
                    //currentArgument.setLength(currentArgument.length() - 1);
                    // erase the previous backslash
                    currentArgument.setCharAt(currentArgument.length() - 1, c);
                }
                else
                {
                    quoteToken = null;
                    command.add(currentArgument.toString());
                    currentArgument.setLength(0);
                }
            }
            else if(quoteToken == null && (c == '"' || c == '\'') && previous != '\\')
            {
                quoteToken = c;
            }
            else if(quoteToken == null && c == ' ')
            {
                if(currentArgument.length() > 0)
                {
                    command.add(currentArgument.toString());
                    currentArgument.setLength(0);
                }
            }
            else
            {
                currentArgument.append(c);
            }

            previous = c;
        }
        if(currentArgument.length() > 0)
        {
            command.add(currentArgument.toString());
        }

        return command;
    }

    /**
     * Register a problem with the collector of problems.
     *
     * @param problems A collector of problems
     * @param property The property
     * @param message The message
     * @return {@code false}
     */
    public static boolean problem(
        final ModelProblemCollector problems,
        final ActivationProperty property,
        final String message
    )
    {
        problems.add(
            new ModelProblemCollectorRequest(ModelProblem.Severity.ERROR, ModelProblem.Version.BASE).
                setMessage(message).
                setLocation(property.getLocation(""))
        );
        return false;
    }

    /**
     * A utility to extract a "[CHARSET]" from the beginning of a string.
     */
    public static class CharsetAndRemainder
    {
        private static final Pattern PATTERN = Pattern.compile(
            "^(?:\\[(?<charset>[a-zA-Z0-9_-]+)])?(?<remainder>.+)$"
        );

        private final Charset charset;

        private final String remainder;

        public CharsetAndRemainder(
            final String subject,
            final Charset defaultCharset,
            final ActivationProperty property,
            final ModelProblemCollector problems
        )
        {
            final Matcher matcher = CharsetAndRemainder.PATTERN.matcher(subject);
            if(matcher.matches())
            {
                final String charsetName = matcher.group("charset");
                if(charsetName == null)
                {
                    this.charset = defaultCharset;
                }
                else
                {
                    Charset charset = null;
                    try
                    {
                        charset = Charset.forName(charsetName);
                    }
                    catch(final UnsupportedCharsetException e)
                    {
                        Utilities.problem(problems, property,
                            "The charset from the property 'name' ('" + subject + "') is not supported"
                        );
                    }
                    this.charset = charset;
                }
                this.remainder = matcher.group("remainder");
            }
            else
            {
                Utilities.problem(problems, property,
                    "The property 'name' ('" + subject +
                    "') did not match the optional charset extraction pattern"
                );
                this.charset = null;
                this.remainder = null;
            }
        }

        /**
         * Get the parsed or default charset.
         *
         * @return the charset, or {@code null} if a problem occurred.
         */
        public Charset getCharset()
        {
            return this.charset;
        }

        /**
         * Get the parsed remainder.
         *
         * @return the remainder, or {@code null} if a problem occurred.
         */
        public String getRemainder()
        {
            return this.remainder;
        }
    }
}
