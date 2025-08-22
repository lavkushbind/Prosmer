package com.healweal.prosmer;

// In your main package
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private List<JournalEntry> entries;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener { void onItemClick(JournalEntry entry); }
    public interface OnItemLongClickListener { void onItemLongClick(JournalEntry entry); }

    public JournalAdapter(List<JournalEntry> entries, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.entries = entries;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal_entry, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override public int getItemCount() { return entries.size(); }

    class JournalViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvContent;
        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvEntryDate);
            tvTitle = itemView.findViewById(R.id.tvEntryTitle);
            tvContent = itemView.findViewById(R.id.tvEntryContentSnippet);

            itemView.setOnClickListener(v -> {
                if(listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(entries.get(getAdapterPosition()));
                }
            });
            itemView.setOnLongClickListener(v -> {
                if(longClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(entries.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }

        void bind(JournalEntry entry) {
            tvTitle.setText(entry.getTitle());
            tvContent.setText(entry.getContent());
            if (entry.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                tvDate.setText(sdf.format(entry.getTimestamp().toDate()));
            }
        }
    }
}