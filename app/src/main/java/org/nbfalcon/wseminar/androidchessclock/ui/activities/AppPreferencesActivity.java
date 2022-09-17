package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.util.android.activity.SettingsActivityBase;

import java.util.Objects;

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
        public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Preference ossLicensesButton = findPreference("ossLicensesButton");
            Objects.requireNonNull(ossLicensesButton).setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                return true;
            });
        }

        @Override
        public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey);
        }
    }
}
