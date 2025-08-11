
package com.inhatc.localit.fav;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FavoriteStore {

    private static final String PREF = "favorites_pref";
    private static final String KEY_MAP_JSON = "favorites_map_json";

    private final SharedPreferences sp;

    public FavoriteStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static class Favorite {
        public String id;      // 고유키: contentid or fallback
        public String title;
        public String imageUrl;
        public String address;
        public String type;    // "festival" | "tourism"

        public JSONObject toJson() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("title", title);
            o.put("imageUrl", imageUrl);
            o.put("address", address);
            o.put("type", type);
            return o;
        }

        public static Favorite fromJson(JSONObject o) {
            Favorite f = new Favorite();
            f.id = o.optString("id", "");
            f.title = o.optString("title", "");
            f.imageUrl = o.optString("imageUrl", "");
            f.address = o.optString("address", "");
            f.type = o.optString("type", "");
            return f;
        }
    }

    public Map<String, Favorite> getAll() {
        String raw = sp.getString(KEY_MAP_JSON, "{}");
        Map<String, Favorite> map = new HashMap<>();
        try {
            JSONObject root = new JSONObject(raw);
            Iterator<String> it = root.keys();
            while (it.hasNext()) {
                String k = it.next();
                Favorite f = Favorite.fromJson(root.getJSONObject(k));
                map.put(k, f);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void saveAll(Map<String, Favorite> map) {
        JSONObject root = new JSONObject();
        for (Map.Entry<String, Favorite> e : map.entrySet()) {
            try {
                root.put(e.getKey(), e.getValue().toJson());
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        sp.edit().putString(KEY_MAP_JSON, root.toString()).apply();
    }

    public boolean isFav(String id) {
        return getAll().containsKey(id);
    }

    public void add(Favorite f) {
        Map<String, Favorite> map = getAll();
        map.put(f.id, f);
        saveAll(map);
    }

    public void remove(String id) {
        Map<String, Favorite> map = getAll();
        if (map.remove(id) != null) saveAll(map);
    }

    public void toggle(Favorite f) {
        if (isFav(f.id)) remove(f.id);
        else add(f);
    }
}