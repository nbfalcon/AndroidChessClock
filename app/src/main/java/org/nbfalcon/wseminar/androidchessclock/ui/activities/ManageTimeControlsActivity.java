package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
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
import org.nbfalcon.wseminar.androidchessclock.util.android.view.FragmentAlertDialog;
import org.nbfalcon.wseminar.androidchessclock.util.android.view.OnlyOneDialog;
import org.nbfalcon.wseminar.androidchessclock.util.collections.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.SimpleMutableList;

import java.util.Arrays;

public class ManageTimeControlsActivity extends SettingsActivityBase {
    public static final String KEY_CUSTOM_TIME_CONTROLS = "org.nbfalcon.wseminar.AndroidChessClock.customTimeControls";
    public static final String KEY_NEW_TIME_CONTROL_PRESET = "org.nbfalcon.wseminar.AndroidChessClock.newTimeControlPreset";
    public static final String KEY_RESULT_CHANGES = "org.nbfalcon.wseminar.AndroidChessClock.manageTimeControlsResult";

    private final TimeControlCustomizerDialog myTimeControlCustomizer = new TimeControlCustomizerDialog();

    private ChangeCollectorList<ClockPairTemplate> changesResult;
    private ClockPairTemplate newTimeControlPreset;
    private TimeControlsAdapter tcAdapter;

    private boolean cancelled = false;

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
        tcAdapter = new TimeControlsAdapter(this, changesResult);
        timeControls.setAdapter(tcAdapter);
        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
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
            if (OnlyOneDialog.ok(getSupportFragmentManager())) {
                myTimeControlCustomizer.bind(false, newTimeControlPreset, (result) -> {
                    ManageTimeControlsActivity self = (ManageTimeControlsActivity) result.getActivity();
                    if (self == null) return;

                    ClockPairTemplate newClockPairTemplate = self.myTimeControlCustomizer.getClockPairTemplate();
                    self.tcAdapter.add(newClockPairTemplate);
                });
                myTimeControlCustomizer.setSettingWantSaveAs(false);
                OnlyOneDialog.show(myTimeControlCustomizer, getSupportFragmentManager());
            }
        });
    }

    @Override
    public void finish() {
        if (!cancelled) setResultForFinish();
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuDiscardChanges) {
            if (OnlyOneDialog.ok(getSupportFragmentManager())) {
                FragmentAlertDialog confirm = new FragmentAlertDialog("Discard changes and return to Chess Clock", "Ok", "Cancel",
                        "Discard all time control modifications done here and return to the main Chess Clock screen.")
                        .setIcon(R.drawable.ic_material_close)
                        .then((dialog) -> {
                            ManageTimeControlsActivity self = (ManageTimeControlsActivity) dialog.getActivity();
                            if (self != null) {
                                self.cancelled = true;
                                self.finish();
                            }
                        });
                OnlyOneDialog.show(confirm, getSupportFragmentManager());
                return true;
            }
            return false;
        } else if (id == R.id.menuResetTimeControls) {
            if (OnlyOneDialog.ok(getSupportFragmentManager())) {
                @SuppressLint("NotifyDataSetChanged")
                FragmentAlertDialog confirm = new FragmentAlertDialog("Reset Time Controls", "Ok", "Cancel",
                        "Really reset all time controls to default?\nYour custom time controls will be deleted.")
                        .setIcon(R.drawable.ic_material_reset_device)
                        .then((dialog) -> {
                            ManageTimeControlsActivity self = (ManageTimeControlsActivity) dialog.getActivity();
                            if (self == null) return;
                            self.changesResult.clear();
                            for (ClockPairTemplate template : BuiltinTimeControls.BUILTIN) {
                                self.changesResult.add(template);
                            }
                            // Well, the entire dataset /has/ changed
                            self.tcAdapter.notifyDataSetChanged();
                        });
                OnlyOneDialog.show(confirm, getSupportFragmentManager());
                return true;
            }
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
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

    private static class TimeControlsAdapter extends SimpleMutableListRecyclerAdapter<ClockPairTemplate, TimeControlsAdapter.ViewHolder> {
        private final ManageTimeControlsActivity parent;
        private RecyclerView attachedTo = null;

        private TimeControlsAdapter(ManageTimeControlsActivity parent, SimpleMutableList<ClockPairTemplate> backingList) {
            super(backingList);
            this.parent = parent;
        }

        public static TimeControlsAdapter maybeTheParentChanged(Activity newParent) {
            return ((ManageTimeControlsActivity) newParent).tcAdapter;
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

        @Override
        public void remove(int index) {
            super.remove(index);
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
                label = itemView.findViewById(R.id.label);
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
                            set(myIndex, thisItem);
                        }
                    }
                });

                editRow = itemView.findViewById(R.id.editRow);
                editRow.setOnClickListener((view) -> {
                    if (OnlyOneDialog.ok(parent.getSupportFragmentManager())) {
                        int position = getAdapterPosition();
                        ClockPairTemplate prev = get(position);
                        parent.myTimeControlCustomizer.bind(false, prev, (result) -> {
                            TimeControlsAdapter self = maybeTheParentChanged(result.getActivity());

                            ClockPairTemplate newClockPairTemplate = result.getClockPairTemplate();
                            if (result.getResultType() == TimeControlCustomizerDialog.HowExited.CREATE_NEW) {
                                self.add(newClockPairTemplate);
                            } else if (!prev.equals(newClockPairTemplate)) {
                                self.set(position, newClockPairTemplate);
                            }
                        });
                        parent.myTimeControlCustomizer.setSettingWantSaveAs(true);
                        OnlyOneDialog.show(parent.myTimeControlCustomizer, parent.getSupportFragmentManager());
                    }
                });
                deleteRow = itemView.findViewById(R.id.deleteRow);
                deleteRow.setOnClickListener((view) -> {
                    // Don't delete the last item; that's illegal
                    if (getItemCount() <= 1) return;

                    if (OnlyOneDialog.ok(parent.getSupportFragmentManager())) {
                        int position = getAdapterPosition();
                        String message = String.format("Really delete time control '%s'?", get(position));
                        OnlyOneDialog.show(new FragmentAlertDialog("Really delete?", "Ok", "Cancel", message)
                                .setIcon(R.drawable.ic_material_delete_forever)
                                .then((dialog) -> {
                                    TimeControlsAdapter self = maybeTheParentChanged(dialog.getActivity());
                                    self.remove(position);
                                }), parent.getSupportFragmentManager());
                    }
                });
            }
        }
    }
}