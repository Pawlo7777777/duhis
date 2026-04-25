package com.example.duhis.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 8;
    }

    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        // Philippine mobile: 09XXXXXXXXX or +639XXXXXXXXX
        return phone.matches("^(09|\\+639)\\d{9}$");
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.trim().length() >= 2;
    }

    public static boolean passwordsMatch(String p1, String p2) {
        return !TextUtils.isEmpty(p1) && p1.equals(p2);
    }

    public static String formatPhone(String phone) {
        if (phone == null) return "";
        // Convert 09XXXXXXXXX → +639XXXXXXXXX
        if (phone.startsWith("09") && phone.length() == 11) {
            return "+63" + phone.substring(1);
        }
        return phone;
    }
}