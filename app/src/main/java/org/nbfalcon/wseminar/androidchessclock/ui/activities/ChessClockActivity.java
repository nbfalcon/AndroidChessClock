package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.preference.PreferenceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.ChessClock;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.SimpleHandlerTimerImpl;
import org.nbfalcon.wseminar.androidchessclock.clock.timer.Timer;
import org.nbfalcon.wseminar.androidchessclock.storage.StorageDBHelper;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.AddSubPenaltyTimeDialog;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.TimeControlCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.ui.views.StartButton;
import org.nbfalcon.wseminar.androidchessclock.ui.views.TimerView;
import org.nbfalcon.wseminar.androidchessclock.util.android.adapter.SimpleMutableListAdapter;
import org.nbfalcon.wseminar.androidchessclock.util.android.sound.SoundUtil;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.NoListenerSelection;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.OnlyOneDialog;
import org.nbfalcon.wseminar.androidchessclock.util.collections.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

public class ChessClockActivity extends AppCompatActivity {

    private static final String PREF_LAST_TIME_CONTROL_SELECTED = "last_time_control";
    private static final String PREF_THE_CUSTOM_ITEM_PARCEL = "the_custom_item";
    // Ignore PREF_THE_CUSTOM_ITEM_PARCEL if the version header does not match
    private static final int THE_CUSTOM_ITEM_PREF_VERSION = 5; // Not 0 and not 1

    private static final String TAG = "ChessClockActivity";

    private final TimeControlCustomizerDialog myTimeControlCustomizer = new TimeControlCustomizerDialog();
    private final AddSubPenaltyTimeDialog myPenaltyDialog = new AddSubPenaltyTimeDialog();
    private ChessClock theClock;
    private @Nullable MenuItem menuRestartGame;
    private SimpleMutableListAdapter<ClockPairTemplate> timeControlsList;
    private NoListenerSelection<Spinner, SpinnerAdapter> timeControlPicker;
    private ActivityResultLauncher<Intent> manageTimeControlsLauncher;
    private ClockPairTemplate theCustomItem;
    private StorageDBHelper dbHelper;
    private SharedPreferences myActivityPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_clock);

        ChessClock savedClock = null;
        if (savedInstanceState != null) {
            savedClock = savedInstanceState.getParcelable("theClock");
        }

        theClock = savedClock != null ? savedClock : new ChessClock();
        Handler uiHandler = new Handler(Looper.getMainLooper());
        Timer timer = new SimpleHandlerTimerImpl(uiHandler);
        theClock.injectTimer(timer);

        // Load saved time controls
        dbHelper = new StorageDBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SimpleMutableList<ClockPairTemplate> timeControlsDBList = StorageDBHelper.getTimeControlsTableList(db);
        timeControlsList = new SimpleMutableListAdapter<>(timeControlsDBList,
                getApplication(), android.R.layout.simple_list_item_1, android.R.layout.simple_list_item_1);
        myActivityPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        theCustomItem = loadTheCustomItem();
        timeControlsList.setBonusItem(theCustomItem);

        timeControlPicker = new NoListenerSelection<>(findViewById(R.id.timeControlPicker));
        timeControlPicker.setAdapter(timeControlsList);
        AdapterView.OnItemSelectedListener timeControlPickerOnSelection = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (theClock.getState() == ChessClock.State.INIT) {
                    // Our state machine now has the right clock, which the dialog now accesses via "shared mutable state"
                    if (position == timeControlsList.getBackingList().size() /* == "Custom" */) {
                        ClockPairTemplate prevSelected = theClock.getClocks();
                        setTheCustomItem(prevSelected);
                        showConfigureClockDialog(false);
                    } else {
                        theClock.setClocks(timeControlsList.getBackingList().get(position));
                    }
                    myActivityPreferences.edit().putInt(PREF_LAST_TIME_CONTROL_SELECTED, position).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        timeControlPicker.setOnItemSelectedListener(timeControlPickerOnSelection);

        View addSubTimeButton = findViewById(R.id.addSubTimeButton);
        addSubTimeButton.setOnClickListener((ignored) -> showAddTimePenaltyDialog(theClock.getCurrentPlayer()));

        if (savedClock != null) {
            timeControlPicker.setSelectionNoListener(savedInstanceState.getInt("selectedTimeControl"));
            // We don't go through theClock.setClocks() -> listeners
        } else {
            int last = myActivityPreferences.getInt(PREF_LAST_TIME_CONTROL_SELECTED, 0);
            timeControlPicker.setSelectionNoListener(last);
            // We must update the UI now, otherwise we'll get crashes when rotating the app on app startup
            //  (since spinner listeners are called on the ui thread the first time eventually, not *now*)
            theClock.setClocks(timeControlsList.getItem(last));
        }
        ChessClockUiViewImpl uiView = new ChessClockUiViewImpl(this, theClock);
        uiView.setupCallbacks();
        theClock.injectView(uiView);
        uiView.init();

        manageTimeControlsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            if (data != null) {
                ChangeCollectorList.ChangeList<ClockPairTemplate> changes = data.getExtras().getParcelable(ManageTimeControlsActivity.KEY_RESULT_CHANGES);
                Object previouslySelected = timeControlPicker.getSelectedItem();
                changes.applyTo(timeControlsList.getBackingList());
                timeControlsList.notifyDataSetChanged();


                ClockPairTemplate currentlySelected;
                if (timeControlPicker.getSelectedItem() == theCustomItem && previouslySelected != theCustomItem) {
                    // We have accidentally selected the custom item
                    int index = timeControlsList.getBackingList().size() - 1;
                    timeControlPicker.setSelectionNoListener(index);
                    currentlySelected = timeControlsList.get(index);
                } else {
                    currentlySelected = (ClockPairTemplate) timeControlPicker.getSelectedItem();
                }

                if (currentlySelected != theClock.getClocks()) {
                    if (theClock.getState() == ChessClock.State.INIT) {
                        theClock.setClocks(currentlySelected);
                    } else {
                        theClock.setClocksTemplate(currentlySelected);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chess_clock_threedot, menu);

        menuRestartGame = menu.findItem(R.id.menuRestartGame);
        menuRestartGame.setEnabled(theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuRestartGame) {
            if (theClock.getState() == ChessClock.State.PAUSED || theClock.getState() == ChessClock.State.GAME_OVER) {
                theClock.onReset();
                return true;
            }
            return false;
        } else if (id == R.id.menuManageTimeControls) {
            Intent manageTimeControls = new Intent(this, ManageTimeControlsActivity.class);
            manageTimeControls.putExtra(ManageTimeControlsActivity.KEY_CUSTOM_TIME_CONTROLS,
                    timeControlsList.getBackingList().toArray(ClockPairTemplate.EMPTY_ARRAY));
            manageTimeControls.putExtra(ManageTimeControlsActivity.KEY_NEW_TIME_CONTROL_PRESET, (Parcelable) timeControlPicker.getSelectedItem());
            manageTimeControlsLauncher.launch(manageTimeControls);
            return true;
        } else if (id == R.id.menuSettings) {
            Intent appPreferencesActivity = new Intent(this, AppPreferencesActivity.class);
            startActivity(appPreferencesActivity);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showConfigureClockDialog(boolean whichPlayer) {
        if (OnlyOneDialog.ok(getSupportFragmentManager())) {
            ClockPairTemplate prev = theClock.getClocks();
            myTimeControlCustomizer.bind(whichPlayer, prev, (result) -> {
                ChessClockActivity self = (ChessClockActivity) result.getActivity();
                if (self == null) return;

                ClockPairTemplate newClockPairTemplate = result.getClockPairTemplate();

                if (result.getResultType() == TimeControlCustomizerDialog.HowExited.CREATE_NEW) {
                    // Insert before special "Custom" item
                    self.timeControlsList.add(newClockPairTemplate);
                    // This will indirectly trigger setClocks
                    self.timeControlPicker.setSelectionWithListener(self.timeControlPicker.getCount() - 2);
                } else if (!prev.equalsNoName(newClockPairTemplate)) {
                    newClockPairTemplate.setName("Custom");

                    // Force the "Custom" item to be selected (since our mode is not one of the saved ones)
                    self.setTheCustomItem(newClockPairTemplate);
                    int position = self.timeControlPicker.getCount() - 1;
                    self.timeControlPicker.setSelectionNoListener(position);
                    self.theClock.setClocks(self.theCustomItem);
                    self.myActivityPreferences.edit().putInt(PREF_LAST_TIME_CONTROL_SELECTED, position).apply();
                }
            });
            OnlyOneDialog.show(myTimeControlCustomizer, getSupportFragmentManager());
        }
    }

    private void setTheCustomItem(ClockPairTemplate newClockPairTemplate) {
        theCustomItem.bindFrom(newClockPairTemplate);
        saveTheCustomItem();
    }

    private void saveTheCustomItem() {
        Parcel parcelHack = Parcel.obtain();
        parcelHack.writeInt(THE_CUSTOM_ITEM_PREF_VERSION);

        String name = theCustomItem.toString();
        theCustomItem.setName(null);
        theCustomItem.writeToParcel(parcelHack, 0);
        theCustomItem.setName(name);

        parcelHack.setDataPosition(0);
        byte[] bytes = parcelHack.marshall();
        myActivityPreferences.edit().putString(PREF_THE_CUSTOM_ITEM_PARCEL, Base64.encodeToString(bytes, Base64.DEFAULT)).apply();

        parcelHack.recycle();
    }

    private ClockPairTemplate loadTheCustomItem() {
        String custom = myActivityPreferences.getString(PREF_THE_CUSTOM_ITEM_PARCEL, null);
        if (custom != null) {
            byte[] bytes = Base64.decode(custom, Base64.DEFAULT);
            Parcel parcelHack = Parcel.obtain();
            parcelHack.unmarshall(bytes, 0, bytes.length);
            parcelHack.setDataPosition(0);

            int version = parcelHack.readInt();
            ClockPairTemplate customItem = null;
            if (version == THE_CUSTOM_ITEM_PREF_VERSION) {
                customItem = ClockPairTemplate.CREATOR.createFromParcel(parcelHack);
                customItem.setName("Custom");
            } else {
                Log.i(TAG, "theCustomItem parcel version mismatch: expected: " + THE_CUSTOM_ITEM_PREF_VERSION + "; found: " + version);
            }

            parcelHack.recycle();
            return customItem;
        }

        return new ClockPairTemplate("Custom", BuiltinTimeControls.BUILTIN[0].getPlayer1(), null);
    }

    private void showAddTimePenaltyDialog(boolean whichPlayer) {
        if (theClock.getState() == ChessClock.State.TICKING) theClock.onPause();
        if (OnlyOneDialog.ok(getSupportFragmentManager())) {
            myPenaltyDialog.bind(theClock.getRunningClocks(), whichPlayer, (tp) -> {
                tp.applyTo(theClock.getRunningClocks());
                theClock.updateClocks();
            });
            OnlyOneDialog.show(myPenaltyDialog, getSupportFragmentManager());
            if (theClock.getState() == ChessClock.State.GAME_OVER) {
                theClock.onResumeFromDeath();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (theClock.getState() == ChessClock.State.TICKING) {
            // NOTE: we freeze the clock even during rotation, so *technically* while rotating the clock doesn't tick;
            //  this isn't an advantage for the player though, since they can't see the clock properly while the phone
            //  is rotating for a few milliseconds
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
        // The custom item isn't selected and the clock is in a non-default state; this means we must store its state
        if (theClock.getState() != ChessClock.State.INIT || timeControlPicker.getSelectedItemPosition() != timeControlPicker.getCount() - 1) {
            outState.putParcelable("theClock", theClock);
        }
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

        private final @NotNull ChessClock myClock;


        private boolean lowTimeAlreadyTriggered = false;


        private ChessClockUiViewImpl(@NotNull ChessClockActivity bindFrom, @NotNull ChessClock myClock) {
            this.player1Clock = bindFrom.findViewById(R.id.player1Clock);
            this.player2Clock = bindFrom.findViewById(R.id.player2Clock);
            this.startButton = bindFrom.findViewById(R.id.startButton);
            this.timeControlPicker = bindFrom.findViewById(R.id.timeControlPicker);
            this.myClock = myClock;
        }

        public void setupCallbacks() {
            startButton.setOnClickListener(view -> myClock.onPressStartButton());
            player1Clock.setOnClickListener(new TimerButtonListener(false));
            player2Clock.setOnClickListener(new TimerButtonListener(true));

            player1Clock.setOnLongClickListener(new TimerButtonLongClickListener(false));
            player2Clock.setOnLongClickListener(new TimerButtonLongClickListener(true));
        }

        public void init() {
            myClock.updateClocks();
            onPlayerCurrent(myClock.getCurrentPlayer());
            updateUIForTransition(myClock.getState());
        }

        @Override
        public void onTransition(ChessClock.@NotNull State toState) {
            updateUIForTransition(toState);
            // Don't call this on every screen rotation
            if (toState == ChessClock.State.GAME_OVER
                    && AppPreferencesActivity.getPrefEnableGameOverSound(myActivityPreferences)) {
                SoundUtil.playSound(ChessClockActivity.this, R.raw.sound_lichess_standard_genericnotify);
            }
        }

        private void updateUIForTransition(ChessClock.@NotNull State toState) {
            switch (toState) {
                case INIT:
                    startButton.setState(StartButton.State.INIT);
                    timeControlPicker.setEnabled(true);
                    lowTimeAlreadyTriggered = false;
                    break;
                case PAUSED:
                    startButton.setState(StartButton.State.START);
                    break;
                case GAME_OVER:
                    startButton.setState(StartButton.State.RESTART);
                    player1Clock.setEnabled(false);
                    player2Clock.setEnabled(false);
                    break;
                case TICKING:
                    startButton.setState(StartButton.State.STOP);
                    timeControlPicker.setEnabled(false);
                    onPlayerCurrent(myClock.getCurrentPlayer());
                    break;
            }

            // A "paused" state
            if (toState == ChessClock.State.INIT || toState == ChessClock.State.PAUSED) {
                player1Clock.setEnabled(true);
                player2Clock.setEnabled(true);
            }

            if (menuRestartGame != null) {
                menuRestartGame.setEnabled(toState == ChessClock.State.GAME_OVER || toState == ChessClock.State.PAUSED);
            }
        }

        @Override
        public void onPlayerCurrent(boolean newCurrent) {
            player1Clock.setEnabled(!newCurrent);
            player2Clock.setEnabled(newCurrent);
            (!newCurrent ? player1Clock : player2Clock).requestFocus();
        }

        @Override
        public void onUpdateTime(boolean player, long millis) {
            final long seconds = millis / 1000;

            if (AppPreferencesActivity.getPrefEnableLowTimeSound(myActivityPreferences)
                    && myClock.getState() == ChessClock.State.TICKING
                    && !lowTimeAlreadyTriggered && seconds < 10 /* lichess does this */) {
                lowTimeAlreadyTriggered = true;
                SoundUtil.playSound(ChessClockActivity.this, R.raw.sound_lichess_standard_lowtime);
            }

            TimerView which = !player ? player1Clock : player2Clock;
            which.setTime(seconds);
        }

        private class TimerButtonListener implements View.OnClickListener {
            private final boolean player;

            private TimerButtonListener(boolean player) {
                this.player = player;
            }

            @Override
            public void onClick(View v) {
                // Don't start the clock while a dialog is running
                if (OnlyOneDialog.ok(getSupportFragmentManager())) {
                    myClock.onPressPlayerClockButton(player);
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
                if (myClock.getState() == ChessClock.State.INIT) {
                    if (OnlyOneDialog.ok(getSupportFragmentManager())) {
                        showConfigureClockDialog(player);
                        return true;
                    }
                } else if (myClock.getState() == ChessClock.State.PAUSED) {
                    showAddTimePenaltyDialog(player);
                    return true;
                }
                return false;
            }
        }
    }
}