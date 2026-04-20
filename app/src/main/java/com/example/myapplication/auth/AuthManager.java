package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Single source of truth for authentication state.
 *
 * The JWT token and user info are persisted in SharedPreferences so the user
 * stays logged in across app restarts. The token has a 1-hour server-side
 * expiry; if the backend returns 401, the app redirects to LoginActivity.
 */
public class AuthManager {

    private static final String PREFS_NAME   = "auth_prefs";
    private static final String KEY_TOKEN    = "jwt_token";
    private static final String KEY_USER_ID  = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE     = "role";

    private static AuthManager instance;
    private final SharedPreferences prefs;

    private AuthManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public void saveSession(String token, Long userId, String username, String role) {
        prefs.edit()
             .putString(KEY_TOKEN, token)
             .putLong(KEY_USER_ID, userId)
             .putString(KEY_USERNAME, username)
             .putString(KEY_ROLE, role)
             .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public String getToken()    { return prefs.getString(KEY_TOKEN, null); }
    public long   getUserId()   { return prefs.getLong(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, null); }
    public String getRole()     { return prefs.getString(KEY_ROLE, null); }
}
