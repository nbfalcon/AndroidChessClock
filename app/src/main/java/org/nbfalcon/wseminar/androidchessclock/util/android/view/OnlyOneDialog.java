package org.nbfalcon.wseminar.androidchessclock.util.android.view;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * Helper class to ensure that only one dialog is running per activity.
 * <p>
 * Dialogs don't start immediately, so the user can quickly press on 2 dialoog-starting buttons, creating 2 dialogs.
 * This undesirable. Furthermore, some dialogs conflict with each other, e.g. a dialog to delete an item and one to edit it.
 */
public class OnlyOneDialog {
    private static final String TAG_THE_ONLY_DIALOG = "theOnlyDialog";

    protected OnlyOneDialog() {
    }

    public static boolean ok(FragmentManager fragmentManager) {
        return fragmentManager.findFragmentByTag(TAG_THE_ONLY_DIALOG) == null;
    }

    public static void show(DialogFragment showMe, FragmentManager fragmentManager) {
        showMe.show(fragmentManager, TAG_THE_ONLY_DIALOG);
    }
}
