package org.nbfalcon.wseminar.androidchessclock.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;

public class StartButton extends AppCompatImageButton {
    {
        setState(State.INIT);
    }

    public StartButton(@NonNull @NotNull Context context) {
        super(context);
    }

    public StartButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StartButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setState(@NotNull State newState) {
        @DrawableRes int whichIconRes = -1;
        String contentDescription;
        switch (newState) {
            case INIT:
                whichIconRes = R.drawable.ic_material_not_started;
                contentDescription = "Start the Clock";
                break;
            case START:
                whichIconRes = R.drawable.ic_material_play;
                contentDescription = "Continue";
                break;
            case STOP:
                whichIconRes = R.drawable.ic_material_pause;
                contentDescription = "Stop";
                break;
            case RESTART:
                whichIconRes = R.drawable.ic_material_restart_alt;
                contentDescription = "Restart Game";
                break;
            default:
                throw new AssertionError("Invalid state?! " + newState);
        }
        setImageDrawable(AppCompatResources.getDrawable(getContext(), whichIconRes));
        setContentDescription(contentDescription);
    }

    public enum State {
        INIT, START, STOP, RESTART
    }
}
