package org.nbfalcon.wseminar.androidchessclock.preferences;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import org.nbfalcon.wseminar.androidchessclock.R;

public class AppPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState, @Nullable @org.jetbrains.annotations.Nullable String rootKey) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey);
    }
}
