package org.nbfalcon.wseminar.androidchessclock.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.ChessClock;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.ClockPair;
import org.nbfalcon.wseminar.androidchessclock.clock.timeControl.FisherIncrement;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.SimpleHandlerTimerImpl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;
import org.nbfalcon.wseminar.androidchessclock.views.StartButton;
import org.nbfalcon.wseminar.androidchessclock.views.TimerView;

public class ChessClockActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_clock);

        ChessClockUiViewImpl view = new ChessClockUiViewImpl(this);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        Timer timer = new SimpleHandlerTimerImpl(uiHandler);

        ChessClock theClock = new ChessClock(view, timer);
        view.injectClockModel(theClock);
        view.setupCallbacks();

        // FIXME
        theClock.setClocks(new ClockPair(new FisherIncrement(300 * 1000, 0), new FisherIncrement(300 * 1000, 0)));
        theClock.init();
    }

    private static class ChessClockUiViewImpl implements ChessClock.ChessClockView {
        private final @NotNull TimerView player1Clock, player2Clock;
        private final @NotNull StartButton startButton;

        private ChessClock theClockModel;

        private ChessClockUiViewImpl(@NotNull ChessClockActivity bindFrom) {
            this.player1Clock = bindFrom.findViewById(R.id.player1Clock);
            this.player2Clock = bindFrom.findViewById(R.id.player2Clock);
            this.startButton = bindFrom.findViewById(R.id.startButton);
        }

        public void injectClockModel(@NotNull ChessClock theClockModel) {
            this.theClockModel = theClockModel;
        }

        @Override
        public void onUpdateTime(boolean player, long millis) {
            TimerView which = !player ? player1Clock : player2Clock;
            which.setTime(millis / 1000);
        }

        @Override
        public void onTransition(ChessClock.@NotNull State toState) {
            switch (toState) {
                case INIT:
                case PAUSED:
                    startButton.setState(StartButton.State.START);
                    break;
                case GAME_OVER:
                    startButton.setState(StartButton.State.RESTART);
                    break;
                case TICKING:
                    startButton.setState(StartButton.State.STOP);
                    break;
            }
        }

        public void setupCallbacks() {
            startButton.setOnClickListener(this::onClickStartButton);
            player1Clock.setOnClickListener(new TimerButtonListener(false));
            player2Clock.setOnClickListener(new TimerButtonListener(true));
        }

        private void onClickStartButton(View ignored) {
            switch (theClockModel.getState()) {
                case INIT:
                    theClockModel.onStart(null);
                    break;
                case TICKING:
                    theClockModel.onPause();
                    break;
                case PAUSED:
                    theClockModel.onResume(null);
                    break;
                case GAME_OVER:
                    theClockModel.onReset();
                    break;
            }
        }

        private class TimerButtonListener implements View.OnClickListener {
            private final boolean player;

            private TimerButtonListener(boolean player) {
                this.player = player;
            }

            @Override
            public void onClick(View v) {
                switch (theClockModel.getState()) {
                    case INIT:
                        theClockModel.onStart(!player);
                        break;
                    case PAUSED:
                        theClockModel.onResume(!player);
                        break;
                    case TICKING:
                        if (theClockModel.getCurrentPlayer() == player) {
                            theClockModel.onFinishMove();
                        }
                        break;
                }
            }
        }
    }
}