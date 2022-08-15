package org.nbfalcon.wseminar.androidchessclock.util.android;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Nemo my name forevermore
 */
public class DialogOnce {
    private boolean isDialogRunning = false;

    public @Nullable AlertDialog.Builder withBuilder(Context context) {
        if (isDialogRunning) {
            return null;
        }
        else {
            isDialogRunning = true;
            return new AlertDialog.Builder(context).setOnDismissListener(dialog -> isDialogRunning = false);
        }
    }

    public boolean withDialog(DialogWithOnDismiss dialog) {
        if (!isDialogRunning) {
            isDialogRunning = true;
            dialog.registerOnDismissListenerOnce(dialog1 -> isDialogRunning = false);
            return true;
        }
        else {
            return false;
        }
    }

    public interface DialogWithOnDismiss {
        void registerOnDismissListenerOnce(DialogInterface.OnDismissListener onDismiss);
    }
}
