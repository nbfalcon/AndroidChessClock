package org.nbfalcon.wseminar.androidchessclock.util.android.sound;

import android.content.Context;
import android.media.MediaPlayer;
import androidx.annotation.RawRes;

public class SoundUtil {
    protected SoundUtil() {
    }

    public static void playSound(Context context, @RawRes int playMe) {
        MediaPlayer player = MediaPlayer.create(context, playMe);
        player.setOnCompletionListener(MediaPlayer::release);
        player.start();
    }
}
