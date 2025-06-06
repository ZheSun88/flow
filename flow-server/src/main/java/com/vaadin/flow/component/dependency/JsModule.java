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
package com.vaadin.flow.component.dependency;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.flow.component.Component;

/**
 * Annotation for defining JavaScript Module dependencies on a {@link Component}
 * class. For adding multiple JavaScript Module files for a single component,
 * you can use this annotation multiple times.
 * <p>
 * The JavaScript module files should be located:
 * <ul>
 * <li>inside {@code frontend} directory in your root project folder in case of
 * WAR project
 * <li>inside {@code META-INF/resources/frontend} directory (inside a project
 * resources folder) in case of JAR project (if you are using Maven this is
 * {@code src/main/resources/META-INF/resources/frontend} directory).
 * </ul>
 * <p>
 * It is guaranteed that dependencies will be loaded only once. The files loaded
 * will be in the same order as the annotations were on the class. However,
 * loading order is only guaranteed on a class level; Annotations from different
 * classes may appear in different order, grouped by the annotated class. Also,
 * files identified by {@code @JsModule} will be loaded before
 * {@link com.vaadin.flow.component.dependency.JavaScript} and
 * {@link com.vaadin.flow.component.dependency.CssImport}.
 * <p>
 * NOTE: Currently all frontend resources are bundled together into one big
 * bundle. This means, that JavaScript files loaded by one class will be present
 * on a view constructed by another class. For example, if there are two classes
 * {@code RootRoute} annotated with {@code @Route("")}, and another class
 * {@code RouteA} annotated with {@code @Route("route-a")} and
 * {@code @JsModule("./src/jsmodule.js")}, the {@code jsmodule.js} will be run
 * on the root route as well.
 *
 * @author Vaadin Ltd
 * @since 2.0
 *
 * @see CssImport
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(JsModule.Container.class)
@Inherited
public @interface JsModule {

    /**
     * JavaScript module to load before using the annotated {@link Component} in
     * the browser.
     * <p>
     * NOTE: In the case of using JsModule with LitTemplate, the value needs to
     * point to a real file as it will be copied to the templates folder under
     * target folder. An exported alias from the package will not work.
     *
     * @return a JavaScript module identifier
     */
    String value();

    /**
     * Defines if the JavaScript should be loaded only when running in
     * development mode (for development tooling etc.) or if it should always be
     * loaded.
     * <p>
     * By default, scripts are always loaded.
     */
    boolean developmentOnly() default false;

    /**
     * Internal annotation to enable use of multiple {@link JsModule}
     * annotations.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Documented
    @Inherited
    @interface Container {

        /**
         * Internally used to enable use of multiple {@link JsModule}
         * annotations.
         *
         * @return an array of the JavaScript annotations
         */
        JsModule[] value();
    }
}
