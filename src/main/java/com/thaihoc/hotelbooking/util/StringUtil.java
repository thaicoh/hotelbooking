package com.thaihoc.hotelbooking.util;

public class StringUtil {
    public static String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
