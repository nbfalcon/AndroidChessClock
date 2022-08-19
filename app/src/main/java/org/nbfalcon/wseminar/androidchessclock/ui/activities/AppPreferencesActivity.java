package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.util.android.activity.SettingsActivityBase;

public class AppPreferencesActivity extends SettingsActivityBase {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new AppPreferencesFragment()).commit();
    }

    public static boolean getPrefEnableGameOverSound(SharedPreferences preferences) {
        // Note the code duplication with the XML
        return preferences.getBoolean("enableGameOverSound", true);
    }

    public static boolean getPrefEnableLowTimeSound(SharedPreferences preferences) {
        return preferences.getBoolean("enableLowTimeSound", false);
    }

    // public needed so that the App doesn't crash
    public static class AppPreferencesFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey);
        }
    }
}
