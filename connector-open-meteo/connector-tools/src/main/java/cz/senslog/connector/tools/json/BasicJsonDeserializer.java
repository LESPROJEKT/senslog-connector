// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class BasicJsonDeserializer<T> implements JsonDeserializer<T> {

    private final FormatFunction<T> formatter;

    public BasicJsonDeserializer(FormatFunction<T> formatter) {
        this.formatter = formatter;
    }

    @Override
    public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return formatter.apply(jsonElement.getAsString());
    }
}
