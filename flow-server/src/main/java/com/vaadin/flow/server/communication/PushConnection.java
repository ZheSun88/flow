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

package com.vaadin.flow.server.communication;

import java.io.Serializable;

import com.vaadin.flow.component.UI;

/**
 * Represents a bidirectional ("push") connection between a single UI and its
 * client-side. A single {@code PushConnection} instance is bound to a UI as
 * long as push is enabled in that UI, even if the actual connection is
 * momentarily dropped either due to a network failure or as a normal part of
 * the transport mechanism.
 * <p>
 * This interface is an internal API, only meant to be used by the framework.
 * May be renamed or removed in a future release.
 *
 * @author Vaadin Ltd
 * @since 1.0
 */
public interface PushConnection extends Serializable {

    /**
     * Pushes pending state changes and client RPC calls to the client. Can be
     * called even if {@link #isConnected()} is false; the push will be deferred
     * until a connection is available. It is NOT safe to invoke this method if
     * not holding the session lock.
     * <p>
     * This is internal API; please use {@link UI#push()} instead.
     */
    void push();

    /**
     * Closes the connection. Cannot be called if {@link #isConnected()} is
     * false.
     */
    void disconnect();

    /**
     * Returns whether this connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    boolean isConnected();

}
