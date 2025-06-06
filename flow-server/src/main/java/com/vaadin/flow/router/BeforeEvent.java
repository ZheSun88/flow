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
package com.vaadin.flow.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.router.internal.ErrorStateRenderer;
import com.vaadin.flow.router.internal.ErrorTargetEntry;
import com.vaadin.flow.router.internal.HasUrlParameterFormat;
import com.vaadin.flow.router.internal.NavigationStateRenderer;
import com.vaadin.flow.router.internal.PathUtil;

/**
 * Abstract before event class that has the common functionalities for
 * {@link BeforeLeaveEvent} and {@link BeforeEnterEvent}.
 *
 * @since 1.0
 */
public abstract class BeforeEvent extends EventObject {
    private final Location location;
    private final NavigationTrigger trigger;
    private final UI ui;

    private NavigationHandler forwardTarget;
    private NavigationHandler rerouteTarget;

    private final Class<?> navigationTarget;
    private final RouteParameters parameters;
    private QueryParameters redirectQueryParameters;
    private final List<Class<? extends RouterLayout>> layouts;
    private NavigationState forwardTargetState;
    private NavigationState rerouteTargetState;
    private ErrorParameter<?> errorParameter;

    private String unknownForward = null;
    private String unknownReroute = null;

    private String externalForwardUrl = null;
    private boolean useForwardCallback;

    /**
     * Constructs event from a NavigationEvent.
     *
     * @param event
     *            NavigationEvent that is on-going, not <code>null</code>
     * @param navigationTarget
     *            navigation target, not <code>null</code>
     * @param layouts
     *            Navigation layout chain, not <code>null</code>
     */
    public BeforeEvent(NavigationEvent event, Class<?> navigationTarget,
            List<Class<? extends RouterLayout>> layouts) {
        this(event.getSource(), event.getTrigger(), event.getLocation(),
                navigationTarget, event.getUI(), layouts);
    }

    /**
     * Constructs event from a NavigationEvent.
     *
     * @param event
     *            NavigationEvent that is on-going, not <code>null</code>
     * @param navigationTarget
     *            navigation target, not <code>null</code>
     * @param parameters
     *            route parameters, not <code>null</code>
     * @param layouts
     *            Navigation layout chain, not <code>null</code>
     */
    public BeforeEvent(NavigationEvent event, Class<?> navigationTarget,
            RouteParameters parameters,
            List<Class<? extends RouterLayout>> layouts) {
        this(event.getSource(), event.getTrigger(), event.getLocation(),
                navigationTarget, parameters, event.getUI(), layouts);

    }

    /**
     * Constructs a new BeforeEvent.
     *
     * @param router
     *            the router that triggered the change, not <code>null</code>
     * @param trigger
     *            the type of user action that triggered this location change,
     *            not <code>null</code>
     * @param location
     *            the new location, not <code>null</code>
     * @param navigationTarget
     *            navigation target class, not <code>null</code>
     * @param ui
     *            the UI related to the navigation, not <code>null</code>
     * @param layouts
     *            the layout chain for the navigation target, not
     *            <code>null</code>
     */
    public BeforeEvent(Router router, NavigationTrigger trigger,
            Location location, Class<?> navigationTarget, UI ui,
            List<Class<? extends RouterLayout>> layouts) {
        this(router, trigger, location, navigationTarget,
                RouteParameters.empty(), ui, layouts);
    }

    /**
     * Constructs a new BeforeEvent.
     *
     * @param router
     *            the router that triggered the change, not <code>null</code>
     * @param trigger
     *            the type of user action that triggered this location change,
     *            not <code>null</code>
     * @param location
     *            the new location, not <code>null</code>
     * @param navigationTarget
     *            navigation target class, not <code>null</code>
     * @param parameters
     *            route parameters, not <code>null</code>
     * @param ui
     *            the UI related to the navigation, not <code>null</code>
     * @param layouts
     *            the layout chain for the navigation target, not
     *            <code>null</code>
     */
    public BeforeEvent(Router router, NavigationTrigger trigger,
            Location location, Class<?> navigationTarget,
            RouteParameters parameters, UI ui,
            List<Class<? extends RouterLayout>> layouts) {
        super(router);

        assert trigger != null;
        assert location != null;
        assert navigationTarget != null;
        assert parameters != null;
        assert ui != null;
        assert layouts != null;

        this.trigger = trigger;
        this.location = location;
        this.navigationTarget = navigationTarget;
        this.parameters = parameters;
        this.ui = ui;
        this.layouts = Collections.unmodifiableList(new ArrayList<>(layouts));
    }

    /**
     * Gets if forward route is unknown. This is true only when a forward route
     * is not found using {@link #forwardTo(String)} and
     * {@link #forwardTo(String, QueryParameters)} methods.
     *
     * @return forward route is not found in the route registry.
     */
    public boolean hasUnknownForward() {
        return unknownForward != null;
    }

    /**
     * Gets if reroute route is unknown. This is true only when a reroute route
     * is not found using {@link #rerouteTo(String)} and
     * {@link #rerouteTo(String, QueryParameters)} method.
     *
     * @return reroute is not found in the route registry.
     */
    public boolean hasUnknownReroute() {
        return unknownReroute != null;
    }

    /**
     * Gets the unknown forward.
     *
     * @return the unknown forward.
     */
    public String getUnknownForward() {
        return unknownForward;
    }

    /**
     * Gets the unknown reroute.
     *
     * @return the unknown reroute.
     */
    public String getUnknownReroute() {
        return unknownReroute;
    }

    /**
     * Gets the external forward url.
     *
     * @return the external forward url or {@code null} if none has been set
     */
    public String getExternalForwardUrl() {
        return externalForwardUrl;
    }

    /**
     * Gets the new location.
     *
     * @return the new location, not {@code null}
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the type of user action that triggered this location change.
     *
     * @return the type of user action that triggered this location change, not
     *         <code>null</code>
     */
    public NavigationTrigger getTrigger() {
        return trigger;
    }

    @Override
    public Router getSource() {
        return (Router) super.getSource();
    }

    /**
     * Check if we have a forward target.
     *
     * @return forward target exists
     */
    public boolean hasForwardTarget() {
        return forwardTarget != null;
    }

    /**
     * Check if we have a forward for an external URL.
     *
     * @return forward target exists
     */
    public boolean hasExternalForwardUrl() {
        return externalForwardUrl != null;
    }

    /**
     * Check if we have a reroute target.
     *
     * @return reroute target exists
     */
    public boolean hasRerouteTarget() {
        return rerouteTarget != null;
    }

    /**
     * Gets the forward target handler to use if the user should be forwarded to
     * some other view.
     *
     * @return navigation handler
     */
    public NavigationHandler getForwardTarget() {
        return forwardTarget;
    }

    /**
     * Gets the reroute target handler to use if the user should be rerouted to
     * some other view.
     *
     * @return an navigation handler
     */
    public NavigationHandler getRerouteTarget() {
        return rerouteTarget;
    }

    /**
     * Forward the navigation to use the provided navigation handler instead of
     * the currently used handler.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTarget
     *            the navigation handler to use, or {@code null} to clear a
     *            previously set forward target
     * @param targetState
     *            the target navigation state of the rerouting
     */
    public void forwardTo(NavigationHandler forwardTarget,
            NavigationState targetState) {
        this.forwardTargetState = targetState;
        this.forwardTarget = forwardTarget;
    }

    /**
     * Forward the navigation to the given navigation state.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param targetState
     *            the target navigation state, not {@code null}
     */
    public void forwardTo(NavigationState targetState) {
        Objects.requireNonNull(targetState, "targetState cannot be null");
        forwardTo(new NavigationStateRenderer(targetState), targetState);
    }

    /**
     * Forward the navigation to show the given component instead of the
     * component that is currently about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     */
    public void forwardTo(Class<? extends Component> forwardTargetComponent) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        forwardTo(getNavigationState(forwardTargetComponent,
                RouteParameters.empty(), null));
    }

    /**
     * Forward the navigation to show the given component instead of the
     * component that is currently about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param useForwardCallback
     *            {@literal true} to request navigation callback from client
     */
    public void forwardTo(Class<? extends Component> forwardTargetComponent,
            boolean useForwardCallback) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        this.useForwardCallback = useForwardCallback;
        forwardTo(getNavigationState(forwardTargetComponent,
                RouteParameters.empty(), null));
    }

    /**
     * Forward the navigation to show the given component with given route
     * parameter instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param routeParameter
     *            route parameter for the target
     * @param <T>
     *            route parameter type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void forwardTo(
            Class<? extends C> forwardTargetComponent, T routeParameter) {
        forwardTo(forwardTargetComponent,
                Collections.singletonList(routeParameter));
    }

    /**
     * Forward the navigation to show the given component with given route
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param routeParameters
     *            route parameters for the target
     * @param <T>
     *            route parameters type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void forwardTo(
            Class<? extends C> forwardTargetComponent,
            List<T> routeParameters) {
        forwardTo(getNavigationState(forwardTargetComponent,
                HasUrlParameterFormat.getParameters(routeParameters), null));
    }

    /**
     * Forward the navigation to show the given component with given route
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param parameters
     *            route parameters for the target
     */
    public void forwardTo(Class<? extends Component> forwardTargetComponent,
            RouteParameters parameters) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        forwardTo(getNavigationState(forwardTargetComponent, parameters, null));
    }

    /**
     * Forward the navigation to show the given component with given route
     * parameter and query parameters instead of the component that is currently
     * about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param routeParameter
     *            route parameter for the target
     * @param queryParameters
     *            query parameters for the target
     * @param <T>
     *            route parameter type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void forwardTo(
            Class<? extends C> forwardTargetComponent, T routeParameter,
            QueryParameters queryParameters) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        this.redirectQueryParameters = queryParameters;
        forwardTo(getNavigationState(forwardTargetComponent,
                HasUrlParameterFormat.getParameters(routeParameter), null));
    }

    /**
     * Forward the navigation to show the given component with given route
     * parameters and query parameters instead of the component that is
     * currently about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param routeParameters
     *            route parameters for the target
     * @param queryParameters
     *            query parameters for the target
     * @param <C>
     *            navigation target type
     */
    public <C extends Component> void forwardTo(
            Class<? extends C> forwardTargetComponent,
            RouteParameters routeParameters, QueryParameters queryParameters) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        this.redirectQueryParameters = queryParameters;
        forwardTo(getNavigationState(forwardTargetComponent, routeParameters,
                null));
    }

    /**
     * Forward the navigation to show the given component with given query
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     *
     * @param forwardTargetComponent
     *            the component type to display, not {@code null}
     * @param queryParameters
     *            query parameters for the target
     * @param <C>
     *            navigation target type
     */
    public <C extends Component> void forwardTo(
            Class<? extends C> forwardTargetComponent,
            QueryParameters queryParameters) {
        Objects.requireNonNull(forwardTargetComponent,
                "forwardTargetComponent cannot be null");
        this.redirectQueryParameters = queryParameters;
        forwardTo(getNavigationState(forwardTargetComponent,
                RouteParameters.empty(), null));
    }

    /**
     * Forward to navigation component registered for given location string
     * instead of the component about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param location
     *            forward target location string
     */
    public void forwardTo(String location) {
        final Optional<NavigationState> navigationState = getSource()
                .resolveNavigationTarget(new Location(location));

        if (navigationState.isPresent()) {
            forwardTo(navigationState.get());
        } else {
            // Inform that forward target location is not known.
            unknownForward = PathUtil.trimPath(location);
        }
    }

    /**
     * Forward to the given URL instead of the component about to be displayed.
     * <p>
     * This function performs a page reload in the browser with the new URL.
     *
     * @param externalForwardUrl
     *            forward target location string
     */
    public void forwardToUrl(String externalForwardUrl) {
        this.externalForwardUrl = externalForwardUrl;
    }

    /**
     * Forward to navigation component registered for given location string with
     * given location parameter instead of the component about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param location
     *            forward target location string
     * @param locationParam
     *            location parameter
     * @param <T>
     *            location parameter type
     */
    public <T> void forwardTo(String location, T locationParam) {
        forwardTo(location, Collections.singletonList(locationParam));
    }

    /**
     * Forward to navigation component registered for given location string with
     * given location parameters instead of the component about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     * <p>
     * Note that query parameters of the event are preserved in the forwarded
     * URL.
     *
     * @param location
     *            forward target location string
     * @param locationParams
     *            location parameters
     * @param <T>
     *            location parameters type
     */
    public <T> void forwardTo(String location, List<T> locationParams) {
        forwardTo(getNavigationState(location, locationParams));
    }

    /**
     * Forward to navigation component registered for given location string with
     * given query parameters instead of the component about to be displayed.
     * <p>
     * This function changes the browser URL as opposed to
     * <code>rerouteTo()</code>.
     *
     * @param locationString
     *            forward target location string
     * @param queryParameters
     *            query parameters for the target
     */
    public void forwardTo(String locationString,
            QueryParameters queryParameters) {
        final Optional<NavigationState> navigationState = getSource()
                .resolveNavigationTarget(new Location(locationString));
        this.redirectQueryParameters = queryParameters;
        if (navigationState.isPresent()) {
            forwardTo(navigationState.get());
        } else {
            // Inform that forward target location is not known.
            unknownForward = PathUtil.trimPath(locationString);
        }
    }

    /**
     * Reroutes the navigation to use the provided navigation handler instead of
     * the currently used handler.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param rerouteTarget
     *            the navigation handler to use, or {@code null} to clear a
     *            previously set reroute target
     * @param targetState
     *            the target navigation state of the rerouting
     */
    public void rerouteTo(NavigationHandler rerouteTarget,
            NavigationState targetState) {
        rerouteTargetState = targetState;
        this.rerouteTarget = rerouteTarget;
    }

    /**
     * Reroutes the navigation to the given navigation state.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param targetState
     *            the target navigation state of the rerouting, not {@code null}
     */
    public void rerouteTo(NavigationState targetState) {
        Objects.requireNonNull(targetState, "targetState cannot be null");
        rerouteTo(new NavigationStateRenderer(targetState), targetState);
    }

    /**
     * Reroutes the navigation to show the given component instead of the
     * component that is currently about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     */
    public void rerouteTo(Class<? extends Component> routeTargetType) {
        Objects.requireNonNull(routeTargetType,
                "routeTargetType cannot be null");
        rerouteTo(getNavigationState(routeTargetType, RouteParameters.empty(),
                null));
    }

    /**
     * Reroutes the navigation to show the given component with given route
     * parameter instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param routeParameter
     *            route parameter for the target
     * @param <T>
     *            route parameter type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void rerouteTo(
            Class<? extends C> routeTargetType, T routeParameter) {
        rerouteTo(routeTargetType, Collections.singletonList(routeParameter));
    }

    /**
     * Reroutes the navigation to show the given component with given route
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param routeParameters
     *            route parameters for the target
     * @param <T>
     *            route parameter type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void rerouteTo(
            Class<? extends C> routeTargetType, List<T> routeParameters) {
        rerouteTo(getNavigationState(routeTargetType,
                HasUrlParameterFormat.getParameters(routeParameters), null));
    }

    /**
     * Reroutes the navigation to show the given component with given route
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param parameters
     *            parameters for the target url.
     */
    public void rerouteTo(Class<? extends Component> routeTargetType,
            RouteParameters parameters) {
        Objects.requireNonNull(routeTargetType,
                "routeTargetType cannot be null");
        rerouteTo(getNavigationState(routeTargetType, parameters, null));
    }

    /**
     * Reroutes the navigation to show the given component with given route
     * parameter and query parameters instead of the component that is currently
     * about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param routeParameter
     *            route parameter for the target
     * @param queryParameters
     *            query parameters for the target
     * @param <T>
     *            route parameter type
     * @param <C>
     *            navigation target type
     */
    public <T, C extends Component & HasUrlParameter<T>> void rerouteTo(
            Class<? extends C> routeTargetType, T routeParameter,
            QueryParameters queryParameters) {
        Objects.requireNonNull(routeTargetType,
                "routeTargetType cannot be null");
        this.redirectQueryParameters = queryParameters;
        rerouteTo(getNavigationState(routeTargetType,
                HasUrlParameterFormat.getParameters(routeParameter), null));
    }

    /**
     * Reroutes the navigation to show the given component with given route
     * parameters and query parameters instead of the component that is
     * currently about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param routeParameters
     *            route parameters for the target
     * @param queryParameters
     *            query parameters for the target
     * @param <C>
     *            navigation target type
     */
    public <C extends Component> void rerouteTo(
            Class<? extends C> routeTargetType, RouteParameters routeParameters,
            QueryParameters queryParameters) {
        Objects.requireNonNull(routeTargetType,
                "routeTargetComponent cannot be null");
        this.redirectQueryParameters = queryParameters;
        rerouteTo(getNavigationState(routeTargetType, routeParameters, null));
    }

    /**
     * Reroutes the navigation to show the given component with given query
     * parameters instead of the component that is currently about to be
     * displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     *
     * @param routeTargetType
     *            the component type to display, not {@code null}
     * @param queryParameters
     *            query parameters for the target
     * @param <C>
     *            navigation target type
     */
    public <C extends Component> void rerouteTo(
            Class<? extends C> routeTargetType,
            QueryParameters queryParameters) {
        Objects.requireNonNull(routeTargetType,
                "routeTargetComponent cannot be null");
        this.redirectQueryParameters = queryParameters;
        rerouteTo(getNavigationState(routeTargetType, RouteParameters.empty(),
                null));
    }

    /**
     * Reroute to navigation component registered for given location string
     * instead of the component about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param route
     *            reroute target location string
     */
    public void rerouteTo(String route) {
        final Optional<NavigationState> navigationState = getSource()
                .resolveNavigationTarget(new Location(route));

        if (navigationState.isPresent()) {
            rerouteTo(navigationState.get());
        } else {
            // Inform that reroute target location is not known.
            unknownReroute = PathUtil.trimPath(route);
        }
    }

    /**
     * Reroute to navigation component registered for given location string with
     * given route parameter instead of the component about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param route
     *            reroute target location string
     * @param routeParam
     *            route parameter
     * @param <T>
     *            route parameter type
     */
    public <T> void rerouteTo(String route, T routeParam) {
        rerouteTo(route, Collections.singletonList(routeParam));
    }

    /**
     * Reroute to navigation component registered for given location string with
     * given route parameters instead of the component about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     * <p>
     * Note that rerouting preserves the query parameters of the event.
     *
     * @param route
     *            reroute target location string
     * @param routeParams
     *            route parameters
     * @param <T>
     *            route parameters type
     */
    public <T> void rerouteTo(String route, List<T> routeParams) {
        rerouteTo(getNavigationState(route, routeParams));
    }

    /**
     * Reroute to navigation component registered for given location string with
     * given query parameters instead of the component about to be displayed.
     * <p>
     * This function doesn't change the browser URL as opposed to
     * <code>forwardTo()</code>.
     *
     * @param route
     *            reroute target location string
     * @param queryParameters
     *            query parameters for the target
     */
    public void rerouteTo(String route, QueryParameters queryParameters) {
        final Optional<NavigationState> navigationState = getSource()
                .resolveNavigationTarget(new Location(route));
        this.redirectQueryParameters = queryParameters;
        if (navigationState.isPresent()) {
            rerouteTo(navigationState.get());
        } else {
            // Inform that reroute target location is not known.
            unknownReroute = PathUtil.trimPath(route);
        }
    }

    private Class<? extends Component> getTargetOrThrow(String route,
            List<String> segments) {
        Optional<Class<? extends Component>> target = getSource().getRegistry()
                .getNavigationTarget(route, segments);

        if (!target.isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "No route '%s' accepting the parameters %s was found.",
                    route, segments));
        }
        return target.get();
    }

    private <T> void checkUrlParameterType(T routeParam,
            Class<? extends Component> target) {
        Class<?> genericInterfaceType = ReflectTools
                .getGenericInterfaceType(target, HasUrlParameter.class);
        if (!genericInterfaceType.isAssignableFrom(routeParam.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "Given route parameter '%s' is of the wrong type. Required '%s'.",
                    routeParam.getClass(), genericInterfaceType));
        }
    }

    private <T> NavigationState getNavigationState(String url,
            List<T> routeParams) {
        List<String> segments = routeParams.stream().map(Object::toString)
                .collect(Collectors.toList());
        Class<? extends Component> target = getTargetOrThrow(url, segments);

        if (!routeParams.isEmpty()) {
            checkUrlParameterType(routeParams.get(0), target);
        }

        return getNavigationState(target,
                HasUrlParameterFormat.getParameters(segments),
                HasUrlParameterFormat.getUrl(url, routeParams));
    }

    private NavigationState getNavigationState(
            Class<? extends Component> target, RouteParameters parameters,
            String resolvedUrl) {
        return new NavigationStateBuilder(ui.getInternals().getRouter())
                .withTarget(target, parameters).withPath(resolvedUrl).build();
    }

    /**
     * Get the forward target type for forwarding.
     *
     * @return forward target type
     * @throws NullPointerException
     *             if no forward target is set. Check
     *             {@link #hasForwardTarget()} before accessing this method.
     */
    public Class<? extends Component> getForwardTargetType() {
        return forwardTargetState.getNavigationTarget();
    }

    /**
     * Gets the URL parameters of the forward target.
     *
     * @return URL parameters of forward target
     * @throws NullPointerException
     *             if no forward target is set. Check
     *             {@link #hasForwardTarget()} before accessing this method.
     */
    public RouteParameters getForwardTargetRouteParameters() {
        return forwardTargetState.getRouteParameters();
    }

    /**
     * Gets the reroute url.
     *
     * @return the reroute url.
     * @throws NullPointerException
     *             if no forward target is set. Check
     *             {@link #hasForwardTarget()} before accessing this method.
     */
    public String getForwardUrl() {
        return forwardTargetState.getResolvedPath();
    }

    /**
     * Get the route target type for rerouting.
     *
     * @return route target type
     * @throws NullPointerException
     *             if no reroute target is set. Check
     *             {@link #hasRerouteTarget()} before accessing this method.
     */
    public Class<? extends Component> getRerouteTargetType() {
        return rerouteTargetState.getNavigationTarget();
    }

    /**
     * Get the URL parameters of the reroute target.
     *
     * @return URL parameters of reroute target
     * @throws NullPointerException
     *             if no reroute target is set. Check
     *             {@link #hasRerouteTarget()} before accessing this method.
     */
    public RouteParameters getRerouteTargetRouteParameters() {
        return rerouteTargetState.getRouteParameters();
    }

    /**
     * Gets the reroute url.
     *
     * @return the reroute url.
     * @throws NullPointerException
     *             if no reroute target is set. Check
     *             {@link #hasRerouteTarget()} before accessing this method.
     */
    public String getRerouteUrl() {
        return rerouteTargetState.getResolvedPath();
    }

    /**
     * Get the navigation target.
     *
     * @return navigation target
     */
    public Class<?> getNavigationTarget() {
        return navigationTarget;
    }

    /**
     * Gets the route parameters associated with this event.
     *
     * @return route parameters retrieved from the navigation url.
     */
    public RouteParameters getRouteParameters() {
        return parameters;
    }

    /**
     * Check if we have query parameters for forwarded and rerouted URL.
     *
     * @return query parameters exists
     */
    public boolean hasRedirectQueryParameters() {
        return redirectQueryParameters != null;
    }

    /**
     * Gets the query parameters for forwarded and rerouted URL. {@code null}
     * means that query parameters of the event are preserved in the forwarded
     * and rerouted URL.
     *
     * @return query parameters for forwarding and rerouting
     */
    public QueryParameters getRedirectQueryParameters() {
        return redirectQueryParameters;
    }

    /**
     * Get the layout chain for the {@link #getNavigationState(String, List)
     * navigation target}.
     *
     * @return layout chain
     */
    public List<Class<? extends RouterLayout>> getLayouts() {
        return layouts;
    }

    /**
     * Reroute to error target for given exception without custom message.
     * <p>
     * Exception class needs to have default no-arg constructor.
     *
     * @param exception
     *            exception to get error target for
     * @see BeforeLeaveEvent#rerouteToError(Exception, String)
     */
    public void rerouteToError(Class<? extends Exception> exception) {
        rerouteToError(exception, "");
    }

    /**
     * Reroute to error target for given exception with given custom message.
     * <p>
     * Exception class needs to have default no-arg constructor.
     *
     * @param exception
     *            exception to get error target for
     * @param customMessage
     *            custom message to send to error target
     * @see BeforeLeaveEvent#rerouteToError(Exception, String)
     */
    public void rerouteToError(Class<? extends Exception> exception,
            String customMessage) {
        Exception instance = ReflectTools.createInstance(exception);
        rerouteToError(instance, customMessage);
    }

    /**
     * Reroute to error target for given exception with given custom message.
     *
     * @param exception
     *            exception to get error target for
     * @param customMessage
     *            custom message to send to error target
     */
    public void rerouteToError(Exception exception, String customMessage) {
        Optional<ErrorTargetEntry> maybeLookupResult = getSource()
                .getErrorNavigationTarget(exception);

        if (maybeLookupResult.isPresent()) {
            ErrorTargetEntry lookupResult = maybeLookupResult.get();

            rerouteTargetState = new NavigationStateBuilder(
                    ui.getInternals().getRouter())
                    .withTarget(lookupResult.getNavigationTarget()).build();
            rerouteTarget = new ErrorStateRenderer(rerouteTargetState);

            errorParameter = new ErrorParameter<>(
                    lookupResult.getHandledExceptionType(), exception,
                    customMessage);
        } else {
            throw new RuntimeException(customMessage, exception);
        }
    }

    /**
     * Check if we have an error parameter set for this navigation event.
     *
     * @return true if error parameter is set
     */
    public boolean hasErrorParameter() {
        return errorParameter != null;
    }

    /**
     * Get the set error parameter.
     *
     * @return error parameter
     */
    public ErrorParameter<?> getErrorParameter() {
        return errorParameter;
    }

    /**
     * Determines is client side callback should be requested when executing
     * pending forward operation.
     *
     * @return {@literal true} if callback should be used,
     *         {@literal false otherwise}
     */
    public boolean isUseForwardCallback() {
        return useForwardCallback;
    }

    /**
     * Gets the UI this navigation takes place inside.
     *
     * @return the related UI instance
     */
    public UI getUI() {
        return ui;
    }

}
