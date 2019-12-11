/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.buildoption;

import org.gradle.cli.CommandLineParser;
import org.gradle.cli.ParsedCommandLine;

import java.util.Map;

/**
 * A build option that takes a string value e.g. {@code "--max-workers=4"}.
 *
 * @since 4.3
 */
public abstract class EnumBuildOption<E extends Enum<E>, T> extends AbstractBuildOption<T, CommandLineOptionConfiguration> {

    private final Class<E> enumClass;

    public EnumBuildOption(Class<E> enumClass, String gradleProperty) {
        super(gradleProperty);
        this.enumClass = enumClass;
    }

    public EnumBuildOption(Class<E> enumClass, String gradleProperty, CommandLineOptionConfiguration... commandLineOptionConfigurations) {
        super(gradleProperty, commandLineOptionConfigurations);
        this.enumClass = enumClass;
    }

    @Override
    public void applyFromProperty(Map<String, String> properties, T settings) {
        String value = properties.get(gradleProperty);

        if (value != null) {
            applyTo(value, settings, Origin.forGradleProperty(gradleProperty));
        }
    }

    @Override
    public void configure(CommandLineParser parser) {
        for (CommandLineOptionConfiguration config : commandLineOptionConfigurations) {
            configureCommandLineOption(parser, config.getAllOptions(), config.getDescription(), config.isDeprecated(), config.isIncubating()).hasArgument();
        }
    }

    @Override
    public void applyFromCommandLine(ParsedCommandLine options, T settings) {
        for (CommandLineOptionConfiguration config : commandLineOptionConfigurations) {
            if (options.hasOption(config.getLongOption())) {
                String value = options.option(config.getLongOption()).getValue();
                applyTo(value, settings, Origin.forCommandLine(config.getLongOption()));
            }
        }
    }

    private void applyTo(String value, T settings, Origin origin) {
        applyTo(getValue(value), settings, origin);
    }

    private E getValue(String value) {
        E enumValue = null;
        if (value != null) {
            enumValue = tryGetValue(value);
            if (enumValue == null) {
                enumValue = tryGetValue(value.toLowerCase());
            }
            if (enumValue == null) {
                enumValue = tryGetValue(value.toUpperCase());
            }
        }
        if (enumValue == null) {
            throw new RuntimeException("No such value '" + value + "'");
        }
        return enumValue;
    }

    private E tryGetValue(String value) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public abstract void applyTo(E value, T settings, Origin origin);
}