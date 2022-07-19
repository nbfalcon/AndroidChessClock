package org.nbfalcon.wseminar.androidchessclock.clock.timer;

import android.os.Handler;
import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler-based implementation of {@link Timer}.
 * <p>
 * Thread-safety:
 * - {@link Timer.TimerHandler#onTick(long)} may be invoked either on the handler or on the thread stopping the timer.
 * - The timer must be started and stopped on the same thread always; the timer may not be
 * started or stopped concurrently.
 */
public class HandlerTimerImpl implements Timer {
    private final Handler myHandler;
    private final long minimumDelay;

    private TimerMessage lastQueued = null;

    public HandlerTimerImpl(Handler myHandler, long minimumDelay) {
        this.myHandler = myHandler;
        this.minimumDelay = minimumDelay;
    }

    @Override
    public void onStartTimer(TimerHandler then, long initialDelayHintMS) {
        lastQueued = new TimerMessage(then);
        final long delay = initialDelayHintMS == -1 ? minimumDelay : Math.min(initialDelayHintMS, minimumDelay);
        myHandler.postDelayed(lastQueued, delay);
    }

    @Override
    public void onStopTimer() {
        final TimerMessage curQueued = lastQueued;
        if (curQueued == null) throw new IllegalStateException("Must call only after onStartTimer()");

        curQueued.wasStopped = true;
        if (curQueued.startOrStopCriticalSection.getAndSet(true)) {
            curQueued.tickNow();
        } else {
            // The onTick callback is currently running
            while (curQueued.startOrStopCriticalSection.get()) {
                try {
                    synchronized (curQueued) {
                        curQueued.wait();
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        lastQueued = null;
    }

    @Override
    public void onRenewClock() {
    }

    private class TimerMessage implements Runnable {
        public final AtomicBoolean startOrStopCriticalSection = new AtomicBoolean(false);
        private final TimerHandler then;
        public volatile boolean wasStopped = false;
        private long startTime = SystemClock.elapsedRealtime();

        private TimerMessage(TimerHandler then) {
            this.then = then;
        }

        public void tickNow() {
            then.onTick(SystemClock.elapsedRealtime() - startTime);
        }

        @Override
        public void run() {
            if (!startOrStopCriticalSection.getAndSet(true)) {
                long now = SystemClock.elapsedRealtime(), prev = startTime;
                startTime = now;
                then.onTick(now - prev);

                myHandler.postDelayed(this, minimumDelay);
                startOrStopCriticalSection.set(false);

            }
            if (wasStopped) {
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }
}
