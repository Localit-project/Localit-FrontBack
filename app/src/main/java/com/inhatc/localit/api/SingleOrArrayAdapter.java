package com.inhatc.localit.api;

import com.google.gson.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SingleOrArrayAdapter<T> implements JsonDeserializer<List<T>> {
    @Override
    public List<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
            throws JsonParseException {

        List<T> list = new ArrayList<>();
        if (json == null || json.isJsonNull()) return list;

        Type itemType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];

        if (json.isJsonArray()) {
            for (JsonElement e : json.getAsJsonArray()) {
                list.add(ctx.deserialize(e, itemType));
            }
        } else if (json.isJsonObject()) {
            list.add(ctx.deserialize(json, itemType));
        }
        return list;
    }
}
