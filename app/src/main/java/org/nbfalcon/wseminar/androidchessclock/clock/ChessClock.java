package org.nbfalcon.wseminar.androidchessclock.clock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.TimeControl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;

public class ChessClock implements Timer.TimerHandler {
    private final ChessClockView view;
    private final Timer timer;
    private boolean currentPlayer = false;
    private State currentState = State.INIT;
    private ClockPair clocks;
    public ChessClock(ChessClockView view, Timer timer) {
        this.timer = timer;
        this.view = view;
    }

    private static long getTimeHint(TimeControl clock) {
        return Math.min(-1, clock.getTimeLeft());
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

    public void init() {
        view.onUpdateTime(false, clocks.getClockFor(false).getTimeLeft());
        view.onUpdateTime(true, clocks.getClockFor(true).getTimeLeft());
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
        }
        timer.onStartTimer(this, getTimeHint(getClockOfCurrentPlayer()));
        setState(State.TICKING);
    }

    public void onPause() {
        timer.onStopTimer();
        setState(State.PAUSED);
    }

    public void onReset() {
        assert currentState == State.GAME_OVER;
        setState(State.INIT);
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
    }

    public void setClocks(ClockPair clocks) {
        assert currentState == State.INIT;
        this.clocks = clocks;
    }

    public enum State {
        INIT, TICKING, PAUSED, GAME_OVER
    }

    public interface ChessClockView {

        void onUpdateTime(boolean player, long millis);

        void onTransition(@NotNull ChessClock.State toState);
    }
}
