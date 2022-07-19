package org.nbfalcon.wseminar.androidchessclock.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;
import org.jetbrains.annotations.NotNull;

public class StartButton extends AppCompatImageButton {
    public StartButton(@NonNull @NotNull Context context) {
        super(context);
    }

    public StartButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StartButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        setState(State.START);
    }

    public void setState(@NotNull State newState) {
        @DrawableRes int whichIconRes = -1;
        switch (newState) {
            case START:
                whichIconRes = android.R.drawable.btn_star;
                break;
            case STOP:
                whichIconRes = android.R.drawable.btn_plus;
                break;
            case RESTART:
                whichIconRes = android.R.drawable.btn_minus;
                break;
        }
        Drawable actualIcon = AppCompatResources.getDrawable(getContext(), whichIconRes);
        setImageDrawable(actualIcon);
    }

    public enum State {
        START, STOP, RESTART
    }
}
