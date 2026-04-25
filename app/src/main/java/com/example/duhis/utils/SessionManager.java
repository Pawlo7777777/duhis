package com.example.duhis.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME   = "duhis_session";
    private static final String KEY_UID     = "uid";
    private static final String KEY_NAME    = "name";
    private static final String KEY_EMAIL   = "email";
    private static final String KEY_ROLE    = "role";
    private static final String KEY_PHONE   = "phone";
    private static final String KEY_AVATAR  = "avatar_url";
    private static final String KEY_LOGGED  = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createSession(String uid, String name, String email,
                              String role, String phone) {
        editor.putString(KEY_UID,    uid);
        editor.putString(KEY_NAME,   name);
        editor.putString(KEY_EMAIL,  email);
        editor.putString(KEY_ROLE,   role);
        editor.putString(KEY_PHONE,  phone);
        editor.putBoolean(KEY_LOGGED, true);
        editor.apply();
    }

    public boolean isLoggedIn()   { return prefs.getBoolean(KEY_LOGGED, false); }
    public String  getUid()       { return prefs.getString(KEY_UID,    null); }
    public String  getName()      { return prefs.getString(KEY_NAME,   null); }
    public String  getEmail()     { return prefs.getString(KEY_EMAIL,  null); }
    public String  getRole()      { return prefs.getString(KEY_ROLE,   "user"); }
    public String  getPhone()     { return prefs.getString(KEY_PHONE,  null); }
    public String  getAvatarUrl() { return prefs.getString(KEY_AVATAR, null); }
    public boolean isAdmin()      { return "admin".equals(getRole()); }

    public void updateName(String name)     { editor.putString(KEY_NAME,   name).apply(); }
    public void updateAvatar(String url)    { editor.putString(KEY_AVATAR, url).apply(); }

    public void logout() {
        editor.clear().apply();
    }
}