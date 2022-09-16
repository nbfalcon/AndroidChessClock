package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.BuiltinTimeControls;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.TimeControlCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.util.CollectionUtilsEx;
import org.nbfalcon.wseminar.androidchessclock.util.android.activity.SettingsActivityBase;
import org.nbfalcon.wseminar.androidchessclock.util.android.adapter.SimpleMutableListRecyclerAdapter;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.DialogOnce;
import org.nbfalcon.wseminar.androidchessclock.util.collections.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.Arrays;

public class ManageTimeControlsActivity extends SettingsActivityBase {
    public static final String KEY_CUSTOM_TIME_CONTROLS = "org.nbfalcon.wseminar.AndroidChessClock.customTimeControls";
    public static final String KEY_NEW_TIME_CONTROL_PRESET = "org.nbfalcon.wseminar.AndroidChessClock.newTimeControlPreset";
    public static final String KEY_RESULT_CHANGES = "org.nbfalcon.wseminar.AndroidChessClock.manageTimeControlsResult";
    private final TimeControlCustomizerDialog myTimeControlCustomizer = new TimeControlCustomizerDialog();
    private final DialogOnce onlyOneDialog = new DialogOnce();
    private ChangeCollectorList<ClockPairTemplate> changesResult;
    private ClockPairTemplate newTimeControlPreset;
    private TimeControlsAdapter tcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_time_controls);

        if (savedInstanceState != null) {
            changesResult = savedInstanceState.getParcelable("changesResult");
            newTimeControlPreset = savedInstanceState.getParcelable("newTimeControlPreset");
        } else {
            Bundle extras = getIntent().getExtras();

            ClockPairTemplate[] customTimeControls = CollectionUtilsEx.downCastArray(extras.getParcelableArray(KEY_CUSTOM_TIME_CONTROLS), ClockPairTemplate.EMPTY_ARRAY);
            changesResult = new ChangeCollectorList<>(Arrays.asList(customTimeControls), ClockPairTemplate.class);

            newTimeControlPreset = extras.getParcelable(KEY_NEW_TIME_CONTROL_PRESET);
        }

        RecyclerView timeControls = findViewById(R.id.manageTimeControlsList);
        tcAdapter = new TimeControlsAdapter(changesResult);
        timeControls.setAdapter(tcAdapter);
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
                    tcAdapter.move(from, to);
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
            if (onlyOneDialog.withDialog(myTimeControlCustomizer)) {
                myTimeControlCustomizer.bind(false, newTimeControlPreset, (result) -> {
                    ClockPairTemplate newClockPairTemplate = myTimeControlCustomizer.getClockPairTemplate();
                    tcAdapter.add(newClockPairTemplate);
                });
                myTimeControlCustomizer.setSettingWantSaveAs(false);
                myTimeControlCustomizer.show(getSupportFragmentManager(), null);
            }
        });
    }

    @Override
    public void finish() {
        setResultForFinish();
        super.finish();
    }

    private void setResultForFinish() {
        Intent result = new Intent();
        result.putExtra(KEY_RESULT_CHANGES, changesResult.getChangeList());
        setResult(RESULT_OK, result);
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manage_time_controls_threedot, menu);

        menu.findItem(R.id.menuResetTimeControls).setOnMenuItemClickListener(item -> {
            AlertDialog.Builder confirmBuilder = onlyOneDialog.withBuilder(this);
            if (confirmBuilder != null) {
                @SuppressLint("NotifyDataSetChanged") AlertDialog confirm = confirmBuilder
                        .setIcon(R.drawable.ic_material_reset_device)
                        .setTitle("Reset Time Controls")
                        .setMessage("Really reset all time controls to default?\nYour custom time controls will be deleted.")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            changesResult.clear();
                            for (ClockPairTemplate template : BuiltinTimeControls.BUILTIN) {
                                changesResult.add(template);
                            }
                            // Well, the entire dataset /has/ changed
                            tcAdapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                confirm.show();
                return true;
            }
            return false;
        });

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("changesResult", changesResult);
        outState.putParcelable("newTimeControlPreset", newTimeControlPreset);
    }

    private class TimeControlsAdapter extends SimpleMutableListRecyclerAdapter<ClockPairTemplate, TimeControlsAdapter.ViewHolder> {
        private RecyclerView attachedTo = null;

        private TimeControlsAdapter(SimpleMutableList<ClockPairTemplate> backingList) {
            super(backingList);
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
            holder.currentlyBinding = true;
            ClockPairTemplate item = get(position);
            holder.label.setText(item.toString());
            holder.currentlyBinding = false;

            if (getItemCount() == 1) {
                holder.deleteRow.setEnabled(false);
            }
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
            // We don't care about more than one view currently
            this.attachedTo = recyclerView;
        }

        public void handleOnly1MoreItemRemaining() {
            // We have one more left, so disable the delete buttons
            if (getItemCount() == 1) {
                ViewHolder remaining = (ViewHolder) attachedTo.findViewHolderForAdapterPosition(0);
                assert remaining != null;
                remaining.deleteRow.setEnabled(false);
            }
        }

        @Override
        public void add(ClockPairTemplate item) {
            super.add(item);
            if (getItemCount() == 2) {
                ViewHolder prevSingle = (ViewHolder) attachedTo.findViewHolderForAdapterPosition(0);
                assert prevSingle != null;
                prevSingle.deleteRow.setEnabled(true);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final EditText label;
            public final ImageButton editRow;
            public final ImageButton deleteRow;
            public boolean currentlyBinding;

            public ViewHolder(@NonNull @NotNull View itemView) {
                super(itemView);
                this.label = itemView.findViewById(R.id.label);
                label.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!currentlyBinding) {
                            int myIndex = getAdapterPosition();
                            ClockPairTemplate thisItem = get(myIndex);
                            thisItem.setName(s.toString());
                            // FIXME: proper SQL?
                            set(myIndex, thisItem);
                        }
                    }
                });

                this.editRow = itemView.findViewById(R.id.editRow);
                editRow.setOnClickListener((view) -> {
                    if (onlyOneDialog.withDialog(myTimeControlCustomizer)) {
                        ClockPairTemplate prev = get(getAdapterPosition());
                        myTimeControlCustomizer.bind(false, prev, (result) -> {
                            ClockPairTemplate newClockPairTemplate = result.getClockPairTemplate();

                            if (result.getResultType() == TimeControlCustomizerDialog.HowExited.CREATE_NEW) {
                                add(newClockPairTemplate);
                            } else if (!prev.equals(newClockPairTemplate)) {
                                set(getAdapterPosition(), newClockPairTemplate);
                            }
                        });
                        myTimeControlCustomizer.setSettingWantSaveAs(true);
                        myTimeControlCustomizer.show(getSupportFragmentManager(), null);
                    }
                });
                this.deleteRow = itemView.findViewById(R.id.deleteRow);
                deleteRow.setOnClickListener((view) -> {
                    // Don't delete the last item; that's illegal
                    if (getItemCount() <= 1) return;

                    AlertDialog.Builder confirmBuilder = onlyOneDialog.withBuilder(itemView.getContext());
                    if (confirmBuilder != null) {
                        String message = String.format("Really delete time control '%s' forever?",
                                get(getAdapterPosition()));
                        AlertDialog confirm = confirmBuilder
                                .setIcon(R.drawable.ic_material_delete_forever)
                                .setTitle("Really delete?")
                                .setMessage(message)
                                .setPositiveButton("Ok", (dialog, which) -> {
                                    remove(getAdapterPosition());
                                    handleOnly1MoreItemRemaining();
                                })
                                .setNegativeButton("Cancel", null)
                                .create();
                        confirm.show();
                    }
                });
            }
        }
    }
}