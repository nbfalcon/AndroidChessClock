package org.nbfalcon.wseminar.androidchessclock.util.android.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class FragmentAlertDialog extends DialogOnce.DialogWithOnDismissBase {
    private String title;
    private String ok;
    private String cancel;
    private String content;
    private @DrawableRes int icon = ResourcesCompat.ID_NULL;

    private ButtonHandler okHandler;

    public FragmentAlertDialog() {}

    public FragmentAlertDialog(String title, String ok, String cancel, String content) {
        this.title = title;
        this.ok = ok;
        this.cancel = cancel;
        this.content = content;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title");
            ok = savedInstanceState.getString("ok");
            cancel = savedInstanceState.getString("cancel");
            content = savedInstanceState.getString("content");
            icon = savedInstanceState.getInt("icon");
            okHandler = (ButtonHandler) savedInstanceState.getSerializable("callback");
        }
    }

    public FragmentAlertDialog setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public FragmentAlertDialog then(ButtonHandler okHandler) {
        this.okHandler = okHandler;
        return this;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("ok", ok);
        outState.putString("cancel", cancel);
        outState.putString("content", content);
        outState.putInt("icon", icon);
        outState.putSerializable("callback", okHandler);
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(ok, (which, dialog1) -> okHandler.handle(this))
                .setNegativeButton(cancel, null);
        if (icon != ResourcesCompat.ID_NULL) {
            builder.setIcon(icon);
        }
        return builder.create();
    }

    public interface ButtonHandler extends Serializable {
        void handle(FragmentAlertDialog dialog);
    }
}
