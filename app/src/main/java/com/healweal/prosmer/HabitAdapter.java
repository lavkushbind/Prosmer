package com.healweal.prosmer;

// In your main package
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList;
    private OnHabitToggleListener listener;

    public interface OnHabitToggleListener {
        void onHabitToggled(String habitId, boolean isCompleted);
    }

    public HabitAdapter(List<Habit> habitList, OnHabitToggleListener listener) {
        this.habitList = habitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit currentHabit = habitList.get(position);
        holder.cbHabit.setText(currentHabit.getName());
        holder.cbHabit.setChecked(currentHabit.isCompleted());

        holder.cbHabit.setOnCheckedChangeListener(null); // Avoid listener conflicts
        holder.cbHabit.setChecked(currentHabit.isCompleted());
        holder.cbHabit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onHabitToggled(currentHabit.getId(), isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbHabit;
        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            cbHabit = itemView.findViewById(R.id.cbHabit);
        }
    }
}