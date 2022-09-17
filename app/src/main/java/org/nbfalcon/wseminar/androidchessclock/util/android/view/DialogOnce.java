package org.nbfalcon.wseminar.androidchessclock.util.android.view;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Nemo my name forevermore
 */
public class DialogOnce {
    private boolean isDialogRunning = false;

    public @Nullable AlertDialog.Builder withBuilder(Context context) {
        if (isDialogRunning) {
            return null;
        } else {
            isDialogRunning = true;
            return new AlertDialog.Builder(context).setOnDismissListener(dialog -> isDialogRunning = false);
        }
    }

    public boolean withDialog(DialogWithOnDismiss dialog) {
        if (!isDialogRunning) {
            isDialogRunning = true;
            dialog.registerOnDismissListenerOnce(dialog1 -> isDialogRunning = false);
            return true;
        } else {
            return false;
        }
    }

    public boolean haveDialog() {
        return isDialogRunning;
    }

    public boolean ok() {
        return !isDialogRunning;
    }

    public <T extends DialogFragment & DialogWithOnDismiss> void show(T showMe, FragmentManager fragmentManager) {
        isDialogRunning = true;
        showMe.registerOnDismissListenerOnce(dialog1 -> isDialogRunning = false);
        showMe.show(fragmentManager, null);
    }

    public interface DialogWithOnDismiss {
        void registerOnDismissListenerOnce(DialogInterface.OnDismissListener onDismiss);
    }

    public static class DialogWithOnDismissBase extends DialogFragment implements DialogWithOnDismiss {
        private @Nullable DialogInterface.OnDismissListener onDismissCB = null;

        @Override
        public void registerOnDismissListenerOnce(DialogInterface.OnDismissListener onDismiss) {
            this.onDismissCB = onDismiss;
        }

        @Override
        public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
            super.onDismiss(dialog);

            if (onDismissCB != null) {
                onDismissCB.onDismiss(dialog);
                onDismissCB = null;
            }
        }
    }
}
