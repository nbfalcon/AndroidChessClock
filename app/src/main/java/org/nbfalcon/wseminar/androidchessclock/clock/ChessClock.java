package org.nbfalcon.wseminar.androidchessclock.clock;

import android.os.Parcel;
import android.os.Parcelable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;
import org.nbfalcon.wseminar.androidchessclock.util.android.compat.ParcelCompatEx;

public class ChessClock implements Timer.TimerHandler, Parcelable {
    public static final Creator<ChessClock> CREATOR = new Creator<ChessClock>() {
        @Override
        public ChessClock createFromParcel(Parcel in) {
            return new ChessClock(in);
        }

        @Override
        public ChessClock[] newArray(int size) {
            return new ChessClock[size];
        }
    };

    private ChessClockView view;
    private Timer timer;
    private boolean currentPlayer = false;
    private State currentState = State.INIT;
    private ClockPairTemplate clocksTemplate;
    private ClockPair clocks;

    public ChessClock() {
    }

    protected ChessClock(Parcel in) {
        currentPlayer = ParcelCompatEx.readBoolean(in);
        currentState = State.values()[in.readInt()];
        clocksTemplate = in.readParcelable(ClockPairTemplate.class.getClassLoader());
        clocks = in.readParcelable(ClockPair.class.getClassLoader());
    }

    private static long getTimeHint(TimeControl clock) {
        return Math.min(-1, clock.getTimeLeft());
    }

    public void injectView(ChessClockView view) {
        this.view = view;
    }

    public void injectTimer(Timer timer) {
        this.timer = timer;
    }

    public void startTimerDelayed() {
        timer.onStartTimer(this, getTimeHint(getClockOfCurrentPlayer()));
    }

    public boolean getCurrentPlayer() {
        return currentPlayer;
    }

    public State getState() {
        return currentState;
    }

    private void setState(State toState) {
        this.currentState = toState;
        view.onTransition(toState);
    }

    public void onStart(@Nullable Boolean currentPlayer) {
        assert currentState == State.INIT;
        onResume1(currentPlayer);
    }

    public void onResume(@Nullable Boolean currentPlayer) {
        assert currentState == State.PAUSED;
        onResume1(currentPlayer);
    }

    private void onResume1(@Nullable Boolean currentPlayer) {
        if (currentPlayer != null) {
            this.currentPlayer = currentPlayer;
            view.onPlayerCurrent(currentPlayer);
        }
        timer.onStartTimer(this, getTimeHint(getClockOfCurrentPlayer()));
        setState(State.TICKING);
    }

    public void onPause() {
        timer.onStopTimer();
        setState(State.PAUSED);
    }

    public void onFreeze() {
        timer.onStopTimer();
    }

    public void onReset() {
        assert currentState == State.PAUSED || currentState == State.GAME_OVER;
        clocks = clocksTemplate.create();
        setState(State.INIT);
        updateClocks();
    }

    public void onPressPlayerClockButton(boolean forPlayer) {
        switch (getState()) {
            case INIT:
                onStart(!forPlayer);
                break;
            case PAUSED:
                onResume(!forPlayer);
                break;
            case TICKING:
                if (getCurrentPlayer() == forPlayer) {
                    onFinishMove();
                }
                break;
        }
    }

    /**
     * When the multi-function (START|PAUSE|RESUME|RESTART) button is pressed.
     */
    public void onPressStartButton() {
        switch (getState()) {
            case INIT:
                onStart(null);
                break;
            case TICKING:
                onPause();
                break;
            case PAUSED:
                onResume(null);
                break;
            case GAME_OVER:
                onReset();
                break;
        }
    }

    public void onTick(long elapsedMS) {
        if (currentState == State.TICKING || currentState == State.PAUSED) {
            TimeControl which = getClockOfCurrentPlayer();
            which.onUpdate(elapsedMS);
            view.onUpdateTime(currentPlayer, Math.max(0, which.getTimeLeft()));
            if (which.getTimeLeft() < 0) {
                setState(State.GAME_OVER);
                timer.onKillTimer();
            }
        }
    }

    private TimeControl getClockOfCurrentPlayer() {
        return clocks.getClockFor(currentPlayer);
    }

    public void onFinishMove() {
        assert currentState == State.TICKING;
        getClockOfCurrentPlayer().onMoveFinished();
        timer.onRenewClock();
        currentPlayer = !currentPlayer;
        view.onPlayerCurrent(currentPlayer);
    }

    public void updateClocks() {
        if (view != null) {
            view.onUpdateTime(false, this.clocks.getClockFor(false).getTimeLeft());
            view.onUpdateTime(true, this.clocks.getClockFor(true).getTimeLeft());
        }
    }

    public ClockPairTemplate getClocks() {
        return clocksTemplate;
    }

    public void setClocks(ClockPairTemplate clocks) {
//        assert currentState == State.INIT;
        clocksTemplate = clocks;
        this.clocks = clocksTemplate.create();
        updateClocks();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelCompatEx.writeBoolean(dest, currentPlayer);
        dest.writeInt(currentState.ordinal());
        dest.writeParcelable(clocksTemplate, flags);
        dest.writeParcelable(clocks, flags);
    }

    public enum State {
        INIT, TICKING, PAUSED, GAME_OVER
    }

    public interface ChessClockView {

        void onUpdateTime(boolean player, long millis);

        void onTransition(@NotNull ChessClock.State toState);

        void onPlayerCurrent(boolean newCurrent);
    }
}
