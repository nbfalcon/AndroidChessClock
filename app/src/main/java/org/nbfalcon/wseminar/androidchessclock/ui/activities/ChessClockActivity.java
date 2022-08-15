package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.ChessClock;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.SimpleHandlerTimerImpl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;
import org.nbfalcon.wseminar.androidchessclock.storage.StorageDBHelper;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.TimeControlCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.ui.views.StartButton;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimerView;
import org.nbfalcon.wseminar.androidchessclock.util.android.SimpleMutableListAdapter;
import org.nbfalcon.wseminar.androidchessclock.util.collections.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

public class ChessClockActivity extends AppCompatActivity {

    private static final String PREF_LAST_TIME_CONTROL_SELECTED = "last_time_control";
    private final TimeControlCustomizerDialog myTimeControlCustomizer = new TimeControlCustomizerDialog();
    private ChessClock theClock;
    private @Nullable MenuItem menuRestartGame;
    private SimpleMutableListAdapter<ClockPairTemplate> timeControlsList;
    private AppCompatSpinner timeControlPicker;
    private ActivityResultLauncher<Intent> manageTimeControlsLauncher;
    private ClockPairTemplate theCustomItem;
    private StorageDBHelper dbHelper;
    private SharedPreferences myActivityPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_clock);

        ChessClockUiViewImpl uiView = new ChessClockUiViewImpl(this);
        Handler uiHandler = new Handler(Looper.getMainLooper());
        Timer timer = new SimpleHandlerTimerImpl(uiHandler);

        if (savedInstanceState != null) {
            theClock = savedInstanceState.getParcelable("theClock");
        } else {
            theClock = new ChessClock();
        }
        theClock.injectView(uiView);
        theClock.injectTimer(timer);
        uiView.injectClockModel(theClock);
        uiView.setupCallbacks();

        dbHelper = new StorageDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleMutableList<ClockPairTemplate> timeControlsDBList = StorageDBHelper.getTimeControlsTableList(db);
        timeControlsList = new SimpleMutableListAdapter<>(timeControlsDBList,
                getApplication(), android.R.layout.simple_list_item_1, android.R.layout.simple_list_item_1);
        theCustomItem = new ClockPairTemplate("Custom", BuiltinTimeControls.BUILTIN[0].getPlayer1(), null);
        timeControlsList.setBonusItem(theCustomItem);

        myActivityPreferences = getPreferences(MODE_PRIVATE);

        timeControlPicker = findViewById(R.id.timeControlPicker);
        timeControlPicker.setAdapter(timeControlsList);

        manageTimeControlsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            assert data != null;
            ChangeCollectorList.ChangeList<ClockPairTemplate> changes = data.getExtras().getParcelable(ManageTimeControlsActivity.KEY_RESULT_CHANGES);
            changes.applyTo(timeControlsList.getBackingList());
            timeControlsList.notifyDataSetChanged();
        });

        AdapterView.OnItemSelectedListener tmPickerAdapter = new AdapterView.OnItemSelectedListener() {
            private int lastSelected = savedInstanceState != null ? savedInstanceState.getInt("selectedTimeControl") : -1;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (lastSelected == position) return;
                lastSelected = position;

                // Our state machine now has the right clock, which the dialog now accesses via "shared mutable state",
                //  a rather ugly HACK; showConfigureDialog will select either "Custom" or the newly created time control
                if (position == timeControlsList.getBackingList().size() /* == "Custom" */) {
                    ClockPairTemplate prevSelected = theClock.getClocks();
                    theCustomItem.bindFrom(prevSelected);
                    showConfigureClockDialog(false);
                } else {
                    theClock.setClocks(timeControlsList.getBackingList().get(position));
                    // FIXME: We somehow need to store the custom item
                    myActivityPreferences.edit().putInt(PREF_LAST_TIME_CONTROL_SELECTED, position).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        if (savedInstanceState != null) {
            timeControlPicker.setSelection(savedInstanceState.getInt("selectedTimeControl"));
            timeControlPicker.setOnItemSelectedListener(tmPickerAdapter);
            theClock.updateClocks();
            uiView.onTransition(theClock.getState());
        } else {
            timeControlPicker.setOnItemSelectedListener(tmPickerAdapter);
            int last = myActivityPreferences.getInt(PREF_LAST_TIME_CONTROL_SELECTED, 0);
            if (timeControlPicker.getSelectedItemPosition() != last) {
                timeControlPicker.setSelection(last);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chess_clock_threedot, menu);

        menuRestartGame = menu.findItem(R.id.menuRestartGame);
        menuRestartGame.setEnabled(theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER);
        menuRestartGame.setOnMenuItemClickListener(menuItem -> {
            if (theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER) {
                theClock.onReset();
                return true;
            }
            return false;
        });

        MenuItem item = menu.findItem(R.id.menuManageTimeControls);
        item.setOnMenuItemClickListener(menuItem -> {
            Intent manageTimeControls = new Intent(this, ManageTimeControlsActivity.class);
            manageTimeControls.putExtra(ManageTimeControlsActivity.KEY_CUSTOM_TIME_CONTROLS,
                    SimpleMutableList.toArray(timeControlsList.getBackingList(), ClockPairTemplate.EMPTY_ARRAY));
            manageTimeControls.putExtra(ManageTimeControlsActivity.KEY_NEW_TIME_CONTROL_PRESET, (ClockPairTemplate) timeControlPicker.getSelectedItem());
            manageTimeControlsLauncher.launch(manageTimeControls);
            return true;
        });

        return true;
    }

    private void showConfigureClockDialog(boolean whichPlayer) {
        myTimeControlCustomizer.bind(whichPlayer, theClock.getClocks(), (result) -> {
            ClockPairTemplate newClockPairTemplate = result.getClockPairTemplate();

            if (result.getResultType() == TimeControlCustomizerDialog.HowExited.CREATE_NEW) {
                // Insert before special "Custom" item
                timeControlsList.add(newClockPairTemplate);
                // This will indirectly trigger setClocks
                timeControlPicker.setSelection(timeControlPicker.getCount() - 2);
            } else {
                // FIXME: only if something ackshually changed
                // Force the "Custom" item to be selected (since our mode is not one of the saved ones)
                theCustomItem.bindFrom(newClockPairTemplate);
                timeControlPicker.setSelection(timeControlPicker.getCount() - 1);
                theClock.setClocks(theCustomItem);
            }
        });
        myTimeControlCustomizer.show(getSupportFragmentManager(), "FIXME meow");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (theClock.getState() == ChessClock.State.TICKING) {
            theClock.onFreeze();
        }
    }

    @Override
    public void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("theClock", theClock);
        outState.putInt("selectedTimeControl", timeControlPicker.getSelectedItemPosition());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (theClock.getState() == ChessClock.State.TICKING) {
            theClock.startTimerDelayed();
        }
    }

    private class ChessClockUiViewImpl implements ChessClock.ChessClockView {
        private final @NotNull TimerView player1Clock, player2Clock;
        private final @NotNull StartButton startButton;
        private final @NotNull AppCompatSpinner timeControlPicker;

        private ChessClock theClockModel;

        private ChessClockUiViewImpl(@NotNull ChessClockActivity bindFrom) {
            this.player1Clock = bindFrom.findViewById(R.id.player1Clock);
            this.player2Clock = bindFrom.findViewById(R.id.player2Clock);
            this.startButton = bindFrom.findViewById(R.id.startButton);
            this.timeControlPicker = bindFrom.findViewById(R.id.timeControlPicker);
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
                    timeControlPicker.setEnabled(true);
                    break;
                case PAUSED:
                    startButton.setState(StartButton.State.START);
                    break;
                case GAME_OVER:
                    startButton.setState(StartButton.State.RESTART);
                    break;
                case TICKING:
                    startButton.setState(StartButton.State.STOP);
                    timeControlPicker.setEnabled(false);
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
                // FIXME: tabs that were modified should have a cute little '*' after their title like in Visual Studio
                if (theClockModel.getState() == ChessClock.State.INIT) {
                    showConfigureClockDialog(player);
                }
                return true;
            }
        }
    }
}