package org.trade.utils;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonUtils {
	static Gson gson = new Gson();

	public static Object getObject(String json, Type type) {
		return gson.fromJson(json, type);
	}

	public static String getString(Object object) {
		return gson.toJson(object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T copyObject(T object1, Class<T> classType) {
		return (T) getObject(getString(object1), classType);
	}

}
