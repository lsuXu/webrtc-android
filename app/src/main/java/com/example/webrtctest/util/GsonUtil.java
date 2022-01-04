package com.example.webrtctest.util;

import com.google.gson.Gson;

public class GsonUtil {

    private static final Gson gson = new Gson();

    public static Gson getGson() {
        return gson;
    }

    public static String toJson(Object obj){
        return getGson().toJson(obj);
    }

    public static <T> T toObject(String str,Class<T> c){
        return getGson().fromJson(str,c);
    }
}
