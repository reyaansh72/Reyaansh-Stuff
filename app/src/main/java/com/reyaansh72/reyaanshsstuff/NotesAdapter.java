package com.reyaansh72.reyaanshsstuff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(NoteItem note);
    }

    private final List<NoteItem> noteList;
    private final OnNoteClickListener clickListener;

    public NotesAdapter(List<NoteItem> noteList, OnNoteClickListener clickListener) {
        this.noteList = noteList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteItem note = noteList.get(position);
        holder.tvNoteTitle.setText(note.getTitle());
        holder.tvNoteContent.setText(note.getContent());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle;
        TextView tvNoteContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
        }
    }
}