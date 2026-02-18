package com.android.labassist.technician;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.google.android.material.textview.MaterialTextView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotesRVAdapter extends RecyclerView.Adapter<NotesRVAdapter.NotesViewHolder> {
    Context context;
    List<TechNotes> arrNotes;
    public NotesRVAdapter(Context context, List<TechNotes> arrNotes){
        this.context = context;
        this.arrNotes = arrNotes;
    }
    @NonNull
    @Override
    public NotesRVAdapter.NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_technician_notes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesRVAdapter.NotesViewHolder holder, int position) {
        TechNotes note = arrNotes.get(position);
        if(note != null) {
            if (note.getTechnicianName() == null)
                holder.tvTechName.setText("Tech ABC");
            else
                holder.tvTechName.setText(note.getTechnicianName());
            holder.tvDate.setText(setDate(note.getTimestamp()));
            holder.tvNote.setText(note.getText());
        }
    }

    @Override
    public int getItemCount() {
        return arrNotes.size();
    }

    private String setDate(long dateInMills){
        String date;
        LocalDateTime noteDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateInMills), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter;
        if(DateUtils.isToday(dateInMills)){
            formatter = DateTimeFormatter.ofPattern("hh:mm a");
            date = "Today • " + noteDate.format(formatter);
        }
        else if(noteDate.getYear() == now.getYear()){
            formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a");
            date = noteDate.format(formatter);
        }
        else{
            formatter = DateTimeFormatter.ofPattern("dd MMM • hh:mm a");
            date = noteDate.format(formatter);
        }
        return date;
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView tvTechName, tvDate, tvNote;
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTechName = itemView.findViewById(R.id.tvTechName);
            tvNote = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
        }
    }
}
