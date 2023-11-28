package com.tdtu.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IMAGE_URL = "image";

    private static final String KEY_ROLE = "role";


    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public void setKeyImageUrl(String s1) {
        editor.putString(KEY_IMAGE_URL, s1);
        editor.commit();
    }

    public String getKeyImageUrl() {
        return prefs.getString(KEY_IMAGE_URL, null);
    }

    public void setKeyRole(String s1) {
        editor.putString(KEY_ROLE, s1);
        editor.commit();
    }

    public String getKeyRole() {
        return prefs.getString(KEY_ROLE, null);
    }
}

