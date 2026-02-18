package com.android.labassist;

import android.content.Context;
import android.content.SharedPreferences;

public class RoleManager {
    private static String role;
    private static final String PREF_NAME = "LabAssistSession";
    private static final String KEY_ROLE = "role";

    public static String getRole(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        role = sharedPreferences.getString(KEY_ROLE, "student");
        return role;
    }

    public static String getStoredRole(){
        return role;
    }
}
