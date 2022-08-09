package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.PlayerClockCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.util.CastUtils;
import org.nbfalcon.wseminar.androidchessclock.util.collections.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.android.ObservableList;

import java.util.Arrays;

public class ManageTimeControlsActivity extends AppCompatActivity {
    public static final String KEY_CUSTOM_TIME_CONTROLS = "org.nbfalcon.wseminar.AndroidChessClock.customTimeControls";
    public static final String KEY_NEW_TIME_CONTROL_PRESET = "org.nbfalcon.wseminar.AndroidChessClock.newTimeControlPreset";
    public static final String KEY_RESULT_CHANGES = "org.nbfalcon.wseminar.AndroidChessClock.manageTimeControlsResult";
    private ChangeCollectorList<ClockPairTemplate> changesResult;
    private ClockPairTemplate newTimeControlPreset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_time_controls);

        Bundle extras = getIntent().getExtras();
        ClockPairTemplate[] customTimeControls = CastUtils.downCastArray(extras.getParcelableArray(KEY_CUSTOM_TIME_CONTROLS), ClockPairTemplate.EMPTY_ARRAY);
        changesResult = new ChangeCollectorList<>(Arrays.asList(customTimeControls), ClockPairTemplate.class);
        ObservableList<ClockPairTemplate> observableList = new ObservableList<>(changesResult);

        newTimeControlPreset = extras.getParcelable(KEY_NEW_TIME_CONTROL_PRESET);

        finishSetResult();

        RecyclerView timeControls = findViewById(R.id.manageTimeControlsList);
        timeControls.setAdapter(new TimeControlsAdapter(observableList));
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                // FIXME: the spinner does not keep whatever was selected previously; we need ids somehow

                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();
                if (from != -1 && to != -1) {
                    observableList.move(from, to);
                    return true;
                }
                // This case no longer seems to happen
                return false;
            }

            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        }).attachToRecyclerView(timeControls);

        View addNewTimeControl = findViewById(R.id.addNewTimeControl);
        addNewTimeControl.setOnClickListener((view) -> {
            PlayerClockCustomizerDialog clockDialog = new PlayerClockCustomizerDialog(false, (dialog) -> {
                SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage1OrBoth().getBaseTimeMS(), dialog.getStage1OrBoth().getIncrementMS(), dialog.getStage1OrBoth().getIncrementType());

                @NotNull String name = dialog.getTimeControlName();

                ClockPairTemplate newClockPairTemplate;
                if (dialog.shouldSetForBothPlayers()) {
                    newClockPairTemplate = new ClockPairTemplate(name, p1, null);
                } else {
                    SingleStageTimeControlTemplate p2 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage2().getBaseTimeMS(), dialog.getStage2().getIncrementMS(), dialog.getStage2().getIncrementType());
                    newClockPairTemplate = new ClockPairTemplate(name, p1, p2);
                }

                observableList.add(newClockPairTemplate);
            });
            clockDialog.setSettingWantSaveAs(false);
            clockDialog.bindFrom(newTimeControlPreset);
            clockDialog.show(getSupportFragmentManager(), "FIXME meow");
        });
    }

    @Override
    public void onBackPressed() {
        finishSetResult();
        super.onBackPressed();
    }

    private void finishSetResult() {
        Intent result = new Intent();
        result.putExtra(KEY_RESULT_CHANGES, changesResult.getChangeList());
        setResult(RESULT_OK, result);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class TimeControlsAdapter extends RecyclerView.Adapter<TimeControlsAdapter.ViewHolder> {
        private final ObservableList<ClockPairTemplate> backingList;

        private TimeControlsAdapter(ObservableList<ClockPairTemplate> backingList) {
            this.backingList = backingList;
            backingList.registerAdapterDataObserver(ObservableList.observerFromAdapter(this));
        }

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_time_control, parent, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ManageTimeControlsActivity.TimeControlsAdapter.ViewHolder holder, int position) {
            ClockPairTemplate item = backingList.get(position);
            holder.label.setText(item.toString());
        }

        @Override
        public int getItemCount() {
            return backingList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView label;
            public final ImageButton editRow;
            public final ImageButton deleteRow;

            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                this.label = itemView.findViewById(R.id.label);

                this.editRow = itemView.findViewById(R.id.editRow);
                editRow.setOnClickListener((view) -> {
                    PlayerClockCustomizerDialog customizerDialog = new PlayerClockCustomizerDialog(false, dialog -> {
                        int index = getAdapterPosition();

                        String name = dialog.getTimeControlName();
                        PlayerClockCustomizerDialog.HowExited howExited = dialog.getResultType();

                        SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage1OrBoth().getBaseTimeMS(), dialog.getStage1OrBoth().getIncrementMS(), dialog.getStage1OrBoth().getIncrementType());
                        ClockPairTemplate newClockPairTemplate;
                        if (dialog.shouldSetForBothPlayers()) {
                            newClockPairTemplate = new ClockPairTemplate(name, p1, null);
                        } else {
                            SingleStageTimeControlTemplate p2 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage2().getBaseTimeMS(), dialog.getStage2().getIncrementMS(), dialog.getStage2().getIncrementType());
                            newClockPairTemplate = new ClockPairTemplate(name, p1, p2);
                        }

                        if (howExited == PlayerClockCustomizerDialog.HowExited.CREATE_NEW) {
                            backingList.add(newClockPairTemplate);
                        } else {
                            backingList.set(index, newClockPairTemplate);
                        }
                    });
                    customizerDialog.setSettingWantSaveAs(false);
                    customizerDialog.bindFrom(backingList.get(getAdapterPosition()));
                    customizerDialog.show(getSupportFragmentManager(), "FIXME meow");
                });
                this.deleteRow = itemView.findViewById(R.id.deleteRow);
                deleteRow.setOnClickListener((view) -> {
                    String message = String.format("Really delete time control '%s' forever?",
                            backingList.get(getAdapterPosition()));
                    AlertDialog confirm = new AlertDialog.Builder(itemView.getContext())
                            .setIcon(R.drawable.ic_material_delete_forever)
                            .setTitle("Really delete?")
                            .setMessage(message)
                            .setPositiveButton("Ok", (dialog, which) -> {
                                backingList.remove(getAdapterPosition());
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    confirm.show();
                });
            }
        }
    }
}