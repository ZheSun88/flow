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
package com.vaadin.flow.server;

/**
 * Defines the interface to handle exceptions thrown during the execution of a
 * FutureAccess.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public interface ErrorHandlingCommand extends Command {

    /**
     * Handles exceptions thrown during the execution of a FutureAccess.
     *
     * @param exception
     *            the thrown exception.
     */
    void handleError(Exception exception);

}
