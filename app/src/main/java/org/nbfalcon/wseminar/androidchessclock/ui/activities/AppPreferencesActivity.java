package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import org.nbfalcon.wseminar.androidchessclock.preferences.AppPreferencesFragment;
import org.nbfalcon.wseminar.androidchessclock.util.android.activity.SettingsActivityBase;

public class AppPreferencesActivity extends SettingsActivityBase {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new AppPreferencesFragment()).commit();
    }
}
