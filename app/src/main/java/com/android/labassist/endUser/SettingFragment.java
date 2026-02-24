package com.android.labassist.endUser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;

public class SettingFragment extends PreferenceFragmentCompat {
//  TODO: Edit profile function
    SharedPreferences sharedPref;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.user_settings_preference, rootKey);

        ListPreference themePref = findPreference("app_theme");

        sharedPref = getPreferenceManager().getSharedPreferences();

//        Setting version of app
        Preference version = findPreference("app_version");
        if(version != null)
            version.setSummary(getVersion());

//        Report Button
        Preference reportProblem = findPreference("report_problem");
        if (reportProblem != null) {
            reportProblem.setOnPreferenceClickListener(preference -> {
                Intent sendMail = new Intent(Intent.ACTION_SENDTO);
                PackageManager pm = requireContext().getPackageManager();
                if(sendMail.resolveActivity(pm) != null) {
                    sendMail.setData(Uri.parse("mailto: " + requireContext().getString(R.string.report_email)));
                    sendMail.putExtra(Intent.EXTRA_SUBJECT, "Report problem regarding Lab-Assist");
                    sendMail.putExtra(Intent.EXTRA_TEXT, "Describe your problem here:\n\n\n\n" +
                            "App Version: " + getVersion() +
                            "\nDevice: " + Build.MODEL +
                            "\nAndroid Version: " + Build.VERSION.RELEASE);
                    startActivity(sendMail);
                }
                return true;
            });
        }

//        Notification Button
        SwitchPreferenceCompat notificationPref = findPreference("notification");
        if(notificationPref != null){
            boolean value = requireContext().getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE).getBoolean("notification", true);
            notificationPref.setChecked(value);
            setNotificationIcon(value, notificationPref);

            notificationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isEnabled = (boolean) newValue;
                setNotificationState(isEnabled, notificationPref);
                return true;
            });
        }
//         Logout Button
        Preference logoutPref = findPreference("account_logout");
        if(logoutPref != null) {
            logoutPref.setOnPreferenceClickListener(preference -> {
//                Todo: Write extra logic for logout here
                SessionManager.getInstance(requireContext()).logout();
                return true;
            });
        }

//        Delete Account
        Preference deleteAccPref = findPreference("account_delete");
        if(deleteAccPref != null){
            deleteAccPref.setOnPreferenceClickListener(preference -> {
//                Todo: Write delete account logic here
                return false;
            });
        }

//        Setting theme of app (light, dark, system)
        if(sharedPref != null) {
            setTheme(themePref, sharedPref.getString("Theme", "system"));
        }
        if(themePref != null){
            themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    String selectedValue = newValue.toString();
                    setTheme(themePref, selectedValue);  // setting theme using function

                    sharedPref = getPreferenceManager().getSharedPreferences();
                    if(sharedPref != null) {
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString("Theme", selectedValue);
                        editor.apply();
                    }

                    return true;
                }
            });
        }
    }

    private void setNotificationState(boolean isEnabled, SwitchPreferenceCompat notificationPref){
        if(isEnabled){
            if(Build.VERSION_CODES.TIRAMISU >= Build.VERSION.SDK_INT &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            else {
                setNotificationIcon(true, notificationPref);
                saveNotificationSharedPref(true);
            }
        }
        else{
            setNotificationIcon(false,notificationPref);
            saveNotificationSharedPref(false);
        }
    }

//    Function saves the Notification shared preference value
    private void saveNotificationSharedPref(boolean isEnabled){
        SharedPreferences pref = requireContext().getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("notification", isEnabled);
        editor.apply();
    }

    private void setNotificationIcon(boolean isEnabled, SwitchPreferenceCompat notificationPref){
        if(isEnabled)
            notificationPref.setIcon(R.drawable.notifications_active_icon);
        else
            notificationPref.setIcon(R.drawable.notifications_off_icon);
    }

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            isGranted -> {
        if(isGranted)
            saveNotificationSharedPref(true);
        else{
//            Showing alert dialog to give notification permission
            new AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.error_icon)
                    .setTitle("Enable Notification")
                    .setMessage("Notifications are required to keep you updated about TechComplaint status.")
                    .setPositiveButton("Enabel", (dialog, which) -> {
                        Intent gotoSettings = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
                        startActivity(gotoSettings);
                    })
                    .setNegativeButton("Later", ((dialog, which) -> {
                        saveNotificationSharedPref(false);
                    }))
                    .show();
        }
    });

    private String getVersion(){
        try {
//            Using PackageInfo class to get version name;
             return requireContext().getPackageManager()
                     .getPackageInfo(requireContext().getPackageName(), 0)
                     .versionName;

        }catch (Exception e){
            return "Unknown";
        }
    }

    private void setTheme(ListPreference themePref, String value){
        switch(value){

            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                themePref.setIcon(R.drawable.light_mode_icon);
                break;

            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                themePref.setIcon(R.drawable.dark_mode_icon);
                break;

            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                themePref.setIcon(R.drawable.system_theme_icon);
                break;
        }
    }
}