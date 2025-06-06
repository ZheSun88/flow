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
package com.vaadin.flow.ccdmtest;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.server.HttpStatusCode;

@Tag(Tag.DIV)
public class UnauthenticatedExceptionHandler extends Component
        implements HasErrorParameter<UnauthenticatedException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event,
            ErrorParameter<UnauthenticatedException> parameter) {
        setId("errorView");
        getElement().setText(
                "Tried to navigate to a view without being authenticated");
        return HttpStatusCode.UNAUTHORIZED.getCode();
    }
}
