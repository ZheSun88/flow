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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;

/**
 * Class containing data on requested client download.
 *
 * @since 24.8
 */
public class DownloadRequest implements Serializable {

    private final VaadinRequest request;
    private final VaadinResponse response;
    private final VaadinSession session;

    private final String fileName;
    private String contentType;

    private Component owningComponent;

    /**
     * Create a new download event with required data.
     *
     * @param request
     *            current request
     * @param response
     *            current response to write response data to
     * @param session
     *            current session
     * @param fileName
     *            defined download file name
     */
    public DownloadRequest(VaadinRequest request, VaadinResponse response,
            VaadinSession session, String fileName) {
        this.request = request;
        this.response = response;
        this.session = session;
        this.fileName = fileName;
    }

    /**
     * Set the owning component for the download event from the element instance
     * if a component is available.
     *
     * @param owningElement
     *            owning element for the event
     * @return this Event instance
     */
    DownloadRequest withOwningComponent(Element owningElement) {
        if (owningElement != null) {
            Optional<Component> component = owningElement.getComponent();
            component.ifPresent(this::withOwningComponent);
        }
        return this;
    }

    /**
     * Set the owning component for the download event.
     *
     * @param owningComponent
     *            owning component for the event
     * @return this Event instance
     */
    DownloadRequest withOwningComponent(Component owningComponent) {
        this.owningComponent = owningComponent;
        return this;
    }

    /**
     * Set the DownloadEvent content type.
     *
     * @param contentType
     *            content type of the event content
     * @return this Event instance
     */
    DownloadRequest withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Returns a <code>OutputStream</code> for writing binary data in the
     * response.
     * <p>
     * Either this method or getWriter() may be called to write the response,
     * not both.
     *
     * @return a <code>OutputStream</code> for writing binary data or empty
     *         optional if an error happened
     */
    public OutputStream getOutputStream() {
        try {
            return response.getOutputStream();
        } catch (IOException e) {
            LoggerFactory.getLogger(DownloadRequest.class)
                    .error("Error getting output stream", e);
            throw new UncheckedIOException("Error getting output stream", e);
        }
    }

    /**
     * Returns a <code>PrintWriter</code> object that can send character text to
     * the client. The PrintWriter uses the character encoding defined using
     * setContentType.
     * <p>
     * Either this method or getOutputStream() may be called to write the
     * response, not both.
     *
     * @return a <code>PrintWriter</code> for writing character text or empty
     *         optional if an error happened
     */
    public PrintWriter getWriter() {
        try {
            return response.getWriter();
        } catch (IOException e) {
            LoggerFactory.getLogger(DownloadRequest.class)
                    .error("Error getting print writer");
            throw new UncheckedIOException("Error getting writer", e);
        }
    }

    /**
     * Get {@link VaadinRequest} for download event.
     *
     * @return vaadin request
     */
    public VaadinRequest getRequest() {
        return request;
    }

    /**
     * Get {@link VaadinResponse} for download event.
     *
     * @return vaadin response
     */
    public VaadinResponse getResponse() {
        return response;
    }

    /**
     * Get {@link VaadinSession} for download event.
     *
     * @return vaadin session
     */
    public VaadinSession getSession() {
        return session;
    }

    /**
     * Get the set file name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName == null ? "" : fileName;
    }

    /**
     * Get the content type for the data to download.
     *
     * @return set content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get owner {@link Component} for this event.
     *
     * @return owning component or null in none defined
     */
    public Component getOwningComponent() {
        return owningComponent;
    }
}
