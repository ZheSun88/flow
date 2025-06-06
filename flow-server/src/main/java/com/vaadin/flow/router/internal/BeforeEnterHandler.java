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
package com.vaadin.flow.router.internal;

import java.io.Serializable;

import com.vaadin.flow.router.BeforeEnterEvent;

/**
 * The base interface for every {@link BeforeEnterEvent} handler.
 *
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @since 1.0
 */
@FunctionalInterface
public interface BeforeEnterHandler extends Serializable {

    /**
     * Callback executed before navigation to attaching Component chain is made.
     *
     * @param event
     *            before navigation event with event details
     */
    void beforeEnter(BeforeEnterEvent event);
}
