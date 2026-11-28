package com.example.runtrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String SECURE_PREF_NAME = "pacepulse_secure_session";
    private static final String FALLBACK_PREF_NAME = "pacepulse_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private static SharedPreferences getPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.w(TAG, "Không thể khởi tạo EncryptedSharedPreferences, sử dụng fallback", e);
            return context.getSharedPreferences(FALLBACK_PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static boolean isLoggedIn(Context context) {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (!isLoggedIn) {
            editor.remove(KEY_AUTH_TOKEN);
            editor.remove(KEY_USER_ID);
            editor.remove(KEY_FULL_NAME);
            editor.remove(KEY_AVATAR_URL);
        }
        editor.apply();
    }

    public static String getAuthToken(Context context) {
        return getPreferences(context).getString(KEY_AUTH_TOKEN, null);
    }

    public static void saveAuthToken(Context context, String token) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public static void clearAuthToken(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    public static String getUsername(Context context) {
        return getPreferences(context).getString(KEY_USERNAME, "NIGHTRUNNER_07");
    }

    public static void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static int getUserId(Context context) {
        return getPreferences(context).getInt(KEY_USER_ID, 0);
    }

    public static void saveUserId(Context context, int userId) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public static String getFullName(Context context) {
        String name = getPreferences(context).getString(KEY_FULL_NAME, null);
        return (name != null && !name.trim().isEmpty()) ? name : getUsername(context);
    }

    public static void saveFullName(Context context, String fullName) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    public static String getAvatarUrl(Context context) {
        return getPreferences(context).getString(KEY_AVATAR_URL, null);
    }

    public static void saveAvatarUrl(Context context, String avatarUrl) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_AVATAR_URL, avatarUrl);
        editor.apply();
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}
