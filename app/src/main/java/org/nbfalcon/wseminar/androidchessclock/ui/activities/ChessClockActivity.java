package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.ChessClock;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.PlayerClockTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.SimpleHandlerTimerImpl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.PlayerClockCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.ui.views.StartButton;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimerView;

public class ChessClockActivity extends AppCompatActivity {

    private ChessClock theClock;
    private @Nullable MenuItem menuRestartGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_clock);

        ChessClockUiViewImpl view = new ChessClockUiViewImpl(this);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        Timer timer = new SimpleHandlerTimerImpl(uiHandler);

        this.theClock = new ChessClock(view, timer);
        view.injectClockModel(theClock);
        view.setupCallbacks();

        // FIXME
//        theClock.setClocks(new ClockPairTemplate(
//                new SingleStageTimeControlTemplate("5+0", 300 * 1000, 0, TimeControlStageTemplate.Type.FISHER),
//                new SingleStageTimeControlTemplate("3s+0 (DEBUG)", 3 * 1000, 0, TimeControlStageTemplate.Type.FISHER)));

        AppCompatSpinner timeModePicker = findViewById(R.id.timeModePicker);
        timeModePicker.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, BuiltinTimeControls.BUILTIN));
        timeModePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PlayerClockTemplate timeMode = (PlayerClockTemplate) timeModePicker.getAdapter().getItem(position);
                theClock.setClocks(new ClockPairTemplate(timeMode, timeMode));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chess_clock_threedot, menu);

        menuRestartGame = menu.findItem(R.id.menuRestartGame);
        menuRestartGame.setEnabled(theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER);
        menuRestartGame.setOnMenuItemClickListener(menuItem -> {
            if (theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER) {
                theClock.onReset();
                return true;
            }
            return false;
        });

        return true;
    }

    private class ChessClockUiViewImpl implements ChessClock.ChessClockView {
        private final @NotNull TimerView player1Clock, player2Clock;
        private final @NotNull StartButton startButton;
        private final @NotNull AppCompatSpinner timeModePicker;

        private ChessClock theClockModel;

        private ChessClockUiViewImpl(@NotNull ChessClockActivity bindFrom) {
            this.player1Clock = bindFrom.findViewById(R.id.player1Clock);
            this.player2Clock = bindFrom.findViewById(R.id.player2Clock);
            this.startButton = bindFrom.findViewById(R.id.startButton);
            this.timeModePicker = bindFrom.findViewById(R.id.timeModePicker);
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
                    startButton.setState(StartButton.State.START);
                    timeModePicker.setEnabled(false);
                    break;
                case PAUSED:
                    startButton.setState(StartButton.State.START);
                    break;
                case GAME_OVER:
                    startButton.setState(StartButton.State.RESTART);
                    break;
                case TICKING:
                    startButton.setState(StartButton.State.STOP);
                    timeModePicker.setEnabled(false);
                    break;
            }

            if (menuRestartGame != null) {
                menuRestartGame.setEnabled(toState == ChessClock.State.GAME_OVER || toState == ChessClock.State.PAUSED);
            }
        }

        public void setupCallbacks() {
            startButton.setOnClickListener(this::onClickStartButton);
            player1Clock.setOnClickListener(new TimerButtonListener(false));
            player2Clock.setOnClickListener(new TimerButtonListener(true));

            player1Clock.setOnLongClickListener(new TimerButtonLongClickListener(false));
            player2Clock.setOnLongClickListener(new TimerButtonLongClickListener(true));
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

        private class TimerButtonLongClickListener implements View.OnLongClickListener {
            private final boolean player;

            private TimerButtonLongClickListener(boolean player) {
                this.player = player;
            }

            @Override
            public boolean onLongClick(View v) {
                if (theClockModel.getState() == ChessClock.State.INIT) {
                    new PlayerClockCustomizerDialog(player, (dialog) -> {
                        ClockPairTemplate template = theClockModel.getClocks();

                        ClockPairTemplate newClockPairTemplate;
                        SingleStageTimeControlTemplate changedPlayerTemplate = new SingleStageTimeControlTemplate("FIXME",
                                dialog.getStage().getBaseTimeMS(),
                                dialog.getStage().getIncrementMS(),
                                dialog.getStage().getIncrementType());
                        if (dialog.shouldSetForBothPlayers()) {
                            newClockPairTemplate = new ClockPairTemplate(changedPlayerTemplate, changedPlayerTemplate);
                        } else {
                            if (!player) {
                                newClockPairTemplate = new ClockPairTemplate(changedPlayerTemplate, template.getPlayer2());
                            } else {
                                newClockPairTemplate = new ClockPairTemplate(template.getPlayer1(), changedPlayerTemplate);
                            }
                        }

                        theClockModel.setClocks(newClockPairTemplate);
                    }).show(getSupportFragmentManager(), "FIXME meow");
                }
                return true;
            }
        }
    }
}