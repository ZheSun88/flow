/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.flow.data.converter;

import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.flow.data.binder.ErrorMessageProvider;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * A converter that converts from {@link String} to {@link Float} and back. Uses
 * the given locale and a {@link NumberFormat} instance for formatting and
 * parsing.
 * <p>
 * Leading and trailing white spaces are ignored when converting from a String.
 * <p>
 * Override and overwrite {@link #getFormat(Locale)} to use a different format.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public class StringToFloatConverter
        extends AbstractStringToNumberConverter<Float> {

    /**
     * Creates a new converter instance with the given error message. Empty
     * strings are converted to <code>null</code>.
     *
     * @param errorMessage
     *            the error message to use if conversion fails
     */
    public StringToFloatConverter(String errorMessage) {
        this(null, errorMessage);
    }

    /**
     * Creates a new converter instance with the given presentation value for
     * empty string and error message.
     *
     * @param emptyValue
     *            the presentation value to return when converting an empty
     *            string, may be <code>null</code>
     * @param errorMessage
     *            the error message to use if conversion fails
     */
    public StringToFloatConverter(Float emptyValue, String errorMessage) {
        super(emptyValue, errorMessage);
    }

    /**
     * Creates a new converter instance with the given error message provider.
     * Empty strings are converted to <code>null</code>.
     *
     * @param errorMessageProvider
     *            the error message provider to use if conversion fails
     */
    public StringToFloatConverter(ErrorMessageProvider errorMessageProvider) {
        this(null, errorMessageProvider);
    }

    /**
     * Creates a new converter instance with the given presentation value for
     * empty string and error message provider.
     *
     * @param emptyValue
     *            the presentation value to return when converting an empty
     *            string, may be <code>null</code>
     * @param errorMessageProvider
     *            the error message provider to use if conversion fails
     */
    public StringToFloatConverter(Float emptyValue,
            ErrorMessageProvider errorMessageProvider) {
        super(emptyValue, errorMessageProvider);
    }

    @Override
    public Result<Float> convertToModel(String value, ValueContext context) {
        Result<Number> n = convertToNumber(value, context);

        return n.map(number -> {
            if (number == null) {
                return null;
            } else {
                return number.floatValue();
            }
        });
    }

}
