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
package com.vaadin.flow.server.frontend.scanner;

import java.io.Serializable;
import java.util.Objects;

/**
 * A container for CssImport information when scanning the class path. It
 * overrides equals and hashCode in order to use HashSet to eliminate
 * duplicates.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 *
 * @since 2.0
 */
public final class CssData implements Serializable {
    private String value;
    private String id;
    private String include;
    private String themefor;

    public CssData() {
    }

    public CssData(String value, String id, String include, String themefor) {
        this.value = value;
        this.id = id;
        this.include = include;
        this.themefor = themefor;
    }

    /**
     * The value getter.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * The id getter.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * The include getter.
     *
     * @return include
     */
    public String getInclude() {
        return include;
    }

    /**
     * The themefor getter.
     *
     * @return themefor
     */
    public String getThemefor() {
        return themefor;
    }

    void setValue(String value) {
        this.value = value;
    }

    void setId(String id) {
        this.id = id;
    }

    void setInclude(String include) {
        this.include = include;
    }

    void setThemefor(String themefor) {
        this.themefor = themefor;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof CssData)) {
            return false;
        }
        CssData that = (CssData) other;
        return Objects.equals(value, that.value) && Objects.equals(id, that.id)
                && Objects.equals(include, that.include)
                && Objects.equals(themefor, that.themefor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, id, include, themefor);
    }

    @Override
    public String toString() {
        return "value: " + value + (id != null ? " id:" + id : "")
                + (include != null ? " include:" + include : "")
                + (themefor != null ? " themefor:" + themefor : "");
    }
}
