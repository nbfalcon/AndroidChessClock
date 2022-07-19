package org.nbfalcon.wseminar.androidchessclock.clock.timer;

import android.os.Handler;
import android.os.SystemClock;

public class SimpleHandlerTimerImpl implements Timer {
    private final Handler handlerForTimers;
    private TimerMessage lastQueued;

    public SimpleHandlerTimerImpl(Handler handlerForTimers) {
        this.handlerForTimers = handlerForTimers;
    }

    @Override
    public void onStartTimer(TimerHandler then, long initialDelayHintMS) {
        lastQueued = new TimerMessage(then); // FIXME: hint
        lastQueued.queueMe();
    }

    @Override
    public void onStopTimer() {
        lastQueued.cancel();
        lastQueued = null;
    }

    @Override
    public void onRenewClock() {
        lastQueued.cancel();
        lastQueued.cancelled = false;
        lastQueued.queueMe();
    }

    private class TimerMessage implements Runnable {
        private final Timer.TimerHandler then;
        private boolean cancelled = false;
        private long lastTimeStamp = SystemClock.elapsedRealtime();

        private TimerMessage(TimerHandler then) {
            this.then = then;
        }

        @Override
        public void run() {
            if (!cancelled) {
                final long now = SystemClock.elapsedRealtime();
                then.onTick(now - lastTimeStamp);

                final long nowAfterTick = SystemClock.elapsedRealtime();
                // Schedule on next second
                handlerForTimers.postDelayed(this, Math.max(0, 1000 - (nowAfterTick - lastTimeStamp)));

                lastTimeStamp = nowAfterTick;
            }
        }

        public void queueMe() {
            // FIXME: how long does the first delay have to be, really?
            // FIXME: also, what about restarts?
            handlerForTimers.postDelayed(this, 500);
        }

        public void cancel() {
            cancelled = true;
            final long now = SystemClock.elapsedRealtime();
            then.onTick(now - lastTimeStamp);
            lastTimeStamp = now;
        }
    }
}
