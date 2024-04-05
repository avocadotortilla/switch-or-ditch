package com.example.switchorditch;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences userSession;
    SharedPreferences.Editor editor;
    Context context;

    private static final String isLoggedIn = "IsLoggedIn";

    public static final String KEY_USERNAME = "Username";

    SessionManager(Context _context){
        context = _context;
        userSession = context.getSharedPreferences("userLoginSession", Context.MODE_PRIVATE);
        editor = userSession.edit();
    }

    // creates a login session for the specific user after successfully logging in
    public void createLoginSession(String username){
        editor.putBoolean(isLoggedIn, true);

        editor.putString(KEY_USERNAME, username);

        editor.commit();
    }

    // keeps a list of all the current user's details. For now, it just keeps the username.
    // Honestly this function may not be needed at all.
    public HashMap<String, String> getUsernameFromSession(){
        HashMap<String, String> userData = new HashMap<String, String>();
        userData.put(KEY_USERNAME, userSession.getString(KEY_USERNAME, null));
        return userData;
    }

    // I assume this checks if a user is logged in??
    public boolean checkLogin(){
        return userSession.getBoolean(isLoggedIn, false);
    }

    // used to log a user out of the session
    public void logoutUserSession(){
        editor.clear();
        editor.commit();
    }
}
