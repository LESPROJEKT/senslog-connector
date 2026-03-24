// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.json;

import com.google.gson.*;
import cz.senslog.connector.tools.exception.ParseException;
import cz.senslog.connector.tools.exception.SyntaxException;
import cz.senslog.connector.tools.util.Tuple;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.*;

/**
 * The class {@code BasicJson} represents a basic wrapper for {@link Gson} library.
 * Provides basic converter from object to string and string to object.

 * Configuration contains basic formatters for {@see LocalDateTime}, {@see ZonedDateTime} and {@see Class}.


 * Both time classes are formatter to ISO format e.q. '2011-12-03T10:15:30',
 * '2011-12-03T10:15:30+01:00' or '2011-12-03T10:15:30+01:00[Europe/Paris]'.

 * Class is formatted as the full name of the class.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class BasicJson {

    /** Instance of json converter. */
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
            .registerTypeAdapter(Class.class, new ClassAdapter())
            .registerTypeAdapter(Optional.class, new OptionalAdapter())
            .create();

    /** Formatter for {@see LocalDateTime}. */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(localDateTime.format(ISO_DATE_TIME));
        }

        @Override
        public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return LocalDateTime.parse(jsonElement.getAsString(), ISO_DATE_TIME);
        }
    }

    /** Formatter for {@see LocalDateTime}. */
    private static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

        @Override
        public JsonElement serialize(LocalTime localTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(localTime.format(ISO_TIME));
        }

        @Override
        public LocalTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return LocalTime.parse(jsonElement.getAsString(), ISO_TIME);
        }
    }

    /** Formatter for {@see ZonedDateTime}. */
    private static class ZonedDateTimeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {

        @Override
        public JsonElement serialize(ZonedDateTime zonedDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(zonedDateTime.format(ISO_DATE_TIME));
        }

        @Override
        public ZonedDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return ZonedDateTime.parse(jsonElement.getAsString(), ISO_DATE_TIME);
        }
    }

    /** Formatter for {@see OffsetDateTime}. */
    private static class OffsetDateTimeAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

        @Override
        public JsonElement serialize(OffsetDateTime offsetDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(offsetDateTime.format(ISO_OFFSET_DATE_TIME));
        }

        @Override
        public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return OffsetDateTime.parse(jsonElement.getAsString(), ISO_OFFSET_DATE_TIME);
        }
    }

    /** Formatter for {@see Class}. */
    private static class ClassAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                return Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    private static class OptionalAdapter implements JsonSerializer<Optional<?>>, JsonDeserializer<Optional<?>> {

        @Override
        public JsonElement serialize(Optional<?> optional, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(optional.isPresent() ? optional.get().toString() : "null");
        }

        @Override
        public Optional<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Type actualType = ((ParameterizedType) type).getActualTypeArguments()[0];
            return jsonElement.getAsString().equals("null") ? Optional.empty() : Optional.ofNullable(jsonToObject(jsonElement.getAsString(), actualType));
        }
    }

    /**
     * Deserialize json to a typed object according to class.
     * @param jsonString - json string.
     * @param aClass - class of the object.
     * @param <T> - generic type object.
     * @return new instance of the input class.
     */
    public static <T> T jsonToObject(String jsonString, Class<T> aClass) {
        try {
            return gson.fromJson(jsonString, aClass);
        } catch (JsonSyntaxException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    /**
     * Deserialize json to a typed object according to type.
     * @param jsonString - json string.
     * @param type - type of the object.
     * @param <T> - generic type object.
     * @return new instance of the input type.
     */
    public static <T> T jsonToObject(String jsonString, Type type) {
        try {
            return gson.fromJson(jsonString, type);
        } catch (JsonSyntaxException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    /**
     * Serialize object to string json.
     * @param object - input object.
     * @param <T> - generic type of object.
     * @return string json.
     */
    public static <T> String objectToJson(T object) {
        try {
            return gson.toJson(object);
        } catch (JsonSyntaxException e) {
            throw new SyntaxException(e.getMessage());
        }
    }

    @SafeVarargs
    public static <R, E> R jsonToObject(String json, Type type, Tuple<Class<E>, FormatFunction<E>>... formatters) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        for (Tuple<Class<E>, FormatFunction<E>> formatter : formatters) {
            gsonBuilder.registerTypeAdapter(formatter.getItem1(), new BasicJsonDeserializer<>(formatter.getItem2()));
        }
        try {
            Gson gson = gsonBuilder.create();
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            throw new SyntaxException(e.getMessage());
        } catch (RuntimeException e) {
            throw new ParseException(e.getMessage());
        }
    }
}
