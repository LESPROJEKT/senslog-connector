// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import cz.senslog.connector.tools.exception.PropertyNotFoundException;
import cz.senslog.connector.tools.util.ClassUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

/**
 * The class {@code PropertyConfig} represents a general configuration class.
 * Contains map of properties which represents a tree of configuration.
 * Each node is a {@code PropertyConfig} which contains {@see #id}
 * and could be generally located. Each leave can be represented
 * as {@see Integer}, {@see String} or {@see LocalDateTime}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class PropertyConfig {

    /** Path delimiter separates nodes. */
    private static final String PATH_DELIMITER = ".";

    /** Identifier of path. */
    private final String id;

    /** Map of properties. */
    private final Map<String, Object> properties;

    /**
     * Constructor sets new identifier of node.
     * @param id - identifier of node.
     */
    protected PropertyConfig(String id) {
        this.id = id;
        this.properties = new HashMap<>();
    }

    /**
     * Adds new property to properties.
     * @param name - name of new property.
     * @param value - value of new property.
     */
    public boolean setProperty(String name, Object value) {
        Object res = properties.put(name, value);
        return res == value;
    }

    /**
     * Returns value. It could be anything.
     * @param name - name of property.
     * @return object of value.
     */
    public Object getProperty(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        }

        throw new PropertyNotFoundException(format(
                "Property '%s' does not exist.", getNewPropertyId(name))
        );
    }

    /**
     * Checks if property key is presents in properties.
     * @param name - name of property
     * @return boolean
     */
    public boolean containsProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns optional value. It could be anything.
     * @param name - name of property.
     * @return optional object
     */
    public Optional<Object> getOptionalProperty(String name) {
        return ofNullable(properties.get(name));
    }

    /**
     * Returns property as a String.
     * @param name - name of property.
     * @return string value.
     */
    public String getStringProperty(String name) {
        Object object = getProperty(name);
        if (object instanceof String) {
            return (String) object;
        } else {
            return object.toString();
        }
    }

    /**
     * Returns property as an Integer.
     * @param name - name of property.
     * @return integer value.
     */
    public Integer getIntegerProperty(String name) {
        return ClassUtils.cast(getProperty(name), Integer.class);
    }

    /**
     * Returns property as a Double object.
     * @param name - name of property.
     * @return double value.
     */
    public Double getDoubleProperty(String name) {
        return ClassUtils.cast(getProperty(name), Double.class);
    }

    /**
     * Returns property as a LocalDateTime.
     * @param name - name of property.
     * @return localDateTime value.
     */
    public LocalDateTime getLocalDateTimeProperty(String name) {
        Object object = getProperty(name);

        if (object instanceof LocalDateTime) {
            return (LocalDateTime) object;
        } else if (object instanceof Date) {
            Date date = (Date) object;
            return date.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
        } else if (object instanceof String) {
            return LocalDateTime.parse((String)object, DateTimeFormatter.ISO_DATE_TIME);

        } else {
            throw new ClassCastException(format(
                    "Property '%s' can not be cast to %s", getNewPropertyId(name), LocalDateTime.class)
            );
        }
    }

    /**
     * Returns property as a LocalTime.
     * @param name - name of property.
     * @return localTime value.
     */
    public LocalTime getLocalTimeProperty(String name) {
        Object object = getProperty(name);

        if (object instanceof LocalTime) {
            return (LocalTime) object;
        } else if (object instanceof Date) {
            Date date = (Date) object;
            return date.toInstant().atZone(ZoneOffset.systemDefault()).toLocalTime();
        } else if (object instanceof String) {
            return LocalTime.parse((String)object, DateTimeFormatter.ISO_TIME);

        } else {
            throw new ClassCastException(format(
                    "Property '%s' can not be cast to %s", getNewPropertyId(name), LocalTime.class)
            );
        }
    }

    /**
     * Returns property as a LocalTime.
     * @param name - name of property.
     * @return localTime value.
     */
    public LocalDate getLocalDateProperty(String name) {
        Object object = getProperty(name);

        if (object instanceof LocalDate) {
            return (LocalDate) object;
        } else if (object instanceof Date) {
            Date date = (Date) object;
            return date.toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
        } else if (object instanceof String) {
            return LocalDate.parse((String)object, DateTimeFormatter.ISO_DATE);

        } else {
            throw new ClassCastException(format(
                    "Property '%s' can not be cast to %s", getNewPropertyId(name), LocalTime.class)
            );
        }
    }

    /**
     * Returns property as a optional of LocalDateTime
     * @param name - name of property.
     * @return optional of localDateTime value.
     */
    public Optional<LocalDateTime> getOptionalLocalDateTimeProperty(String name) {
        return properties.containsKey(name) ? Optional.of(getLocalDateTimeProperty(name)) : Optional.empty();
    }

    /**
     * Returns property as a set of the 'type'.
     * @param name - name of property.
     * @param type - type of attributes
     * @param <T> - generic type of attribute
     * @return Set of attributes defined by type
     */
    public <T> Set<T> getSetProperty(String name, Class<T> type) {
        Object value = properties.get(name);
        if (value instanceof  Collection) {
            Collection<?> list = (Collection<?>) value;
            Set<T> res = new HashSet<>(list.size());
            for (Object o : list) {
                res.add(ClassUtils.cast(o, type));
            }
            return res;
        }

        return emptySet();
    }

    public <T> List<T> getListProperty(String name, Class<T> type) {
        Object object = getProperty(name);
        if (object instanceof Collection) {
            Collection<?> list = (Collection<?>) object;
            List<T> res = new ArrayList<>(list.size());
            for (Object o : list) {
                res.add(ClassUtils.cast(o, type));
            }
            return res;
        }
        return emptyList();
    }

    private static PropertyConfig mapMapObjectoToPropertyConfig(String id, Object mapObject) {
        PropertyConfig config = new PropertyConfig(id);
        if (mapObject instanceof Map) {
            Map<?, ?> properties = (Map<?, ?>) mapObject;
            for (Map.Entry<?, ?> propertyEntry : properties.entrySet()) {
                config.setProperty(propertyEntry.getKey().toString(), propertyEntry.getValue());
            }
        }
        return config;
    }

    public Set<PropertyConfig> getSetProperty(String name) {
        Object value = properties.get(name);
        if (value instanceof  Collection) {
            Collection<?> collection = (Collection<?>) value;
            Iterator<?> iter = collection.iterator();
            Set<PropertyConfig> res = new HashSet<>(collection.size());
            int index = 0;
            while (iter.hasNext()) {
                res.add(mapMapObjectoToPropertyConfig(getNewPropertyId(name, index++), iter.next()));
            }
            return res;
        }

        return emptySet();
    }

    /**
     * Returns new node of configuration.
     * @param name - name of property.
     * @return node of configuration.
     */
    public PropertyConfig getPropertyConfig(String name) {
        Object property = getProperty(name);
        return mapMapObjectoToPropertyConfig(getNewPropertyId(name), property);
    }

    public Set<String> getAttributes() {
        return properties.keySet();
    }

    private String getNewPropertyId(String name) {
        return id + PATH_DELIMITER + name;
    }

    private String getNewPropertyId(String name, int index) {
        return getNewPropertyId(name) + PATH_DELIMITER + index;
    }

    public String getId() {
        return id;
    }
}
