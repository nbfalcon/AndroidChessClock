package org.nbfalcon.wseminar.androidchessclock.ui.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import org.nbfalcon.wseminar.androidchessclock.R;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.ClockPairTemplate;
import org.nbfalcon.wseminar.androidchessclock.clock.gameClock.template.SingleStageTimeControlTemplate;
import org.nbfalcon.wseminar.androidchessclock.ui.dialogs.PlayerClockCustomizerDialog;
import org.nbfalcon.wseminar.androidchessclock.util.CastUtils;
import org.nbfalcon.wseminar.androidchessclock.util.collections.android.ChangeCollectorList;
import org.nbfalcon.wseminar.androidchessclock.util.collections.android.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;

public class ManageTimeControlsActivity extends AppCompatActivity {
    public static final String KEY_CUSTOM_TIME_CONTROLS = "org.nbfalcon.wseminar.AndroidChessClock.customTimeControls";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_time_controls);

        ClockPairTemplate[] customTimeControls = CastUtils.downCastArray(getIntent().getExtras().getParcelableArray(KEY_CUSTOM_TIME_CONTROLS), ClockPairTemplate.EMPTY_ARRAY);
        ChangeCollectorList<ClockPairTemplate> changesResult = new ChangeCollectorList<>(Arrays.asList(customTimeControls), ClockPairTemplate.class);
        ObservableList<ClockPairTemplate> backingList = new ObservableList<>(changesResult);

        RecyclerView timeControls = findViewById(R.id.manageTimeControlsList);
        timeControls.setAdapter(new TimeControlsAdapter(backingList));

        View addNewTimeControl = findViewById(R.id.addNewTimeControl);
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
                        ClockPairTemplate prev = backingList.get(index);

                        SingleStageTimeControlTemplate p1 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage1OrBoth().getBaseTimeMS(), dialog.getStage1OrBoth().getIncrementMS(), dialog.getStage1OrBoth().getIncrementType());
                        ClockPairTemplate newClockPairTemplate;
                        if (dialog.shouldSetForBothPlayers()) {
                            newClockPairTemplate = new ClockPairTemplate(prev.toString(), p1, null);
                        } else {
                            SingleStageTimeControlTemplate p2 = new SingleStageTimeControlTemplate("FIXME", dialog.getStage2().getBaseTimeMS(), dialog.getStage2().getIncrementMS(), dialog.getStage2().getIncrementType());
                            newClockPairTemplate = new ClockPairTemplate(prev.toString(), p1, p2);
                        }

                        backingList.set(index, newClockPairTemplate);
                    });
                    customizerDialog.bindFrom(backingList.get(getAdapterPosition()));
                    customizerDialog.show(getSupportFragmentManager(), "FIXME meow");
                });
                this.deleteRow = itemView.findViewById(R.id.deleteRow);
                deleteRow.setOnClickListener((view) -> backingList.remove(getAdapterPosition()));
            }
        }
    }
}