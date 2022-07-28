package org.nbfalcon.wseminar.androidchessclock.util.android;

import android.widget.ViewFlipper;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;

public class ViewFlipperUtils {
    protected ViewFlipperUtils() {}

    public static void linkWithTabLayout(@NotNull TabLayout tabLayout, @NotNull ViewFlipper flipper) {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                flipper.setDisplayedChild(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}
