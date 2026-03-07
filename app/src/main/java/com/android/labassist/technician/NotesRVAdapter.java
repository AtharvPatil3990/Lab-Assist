package com.android.labassist.technician;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.android.labassist.database.entities.NoteEntity;
import com.android.labassist.databinding.RvTechnicianNotesBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesRVAdapter extends RecyclerView.Adapter<NotesRVAdapter.NoteViewHolder>{
    Context context;
    List<NoteEntity> notesList;
    NotesRVAdapter(Context context){
        this.context = context;
    }
    @NonNull
    @Override
    public NotesRVAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new NoteViewHolder(inflater.inflate(R.layout.rv_technician_notes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotesRVAdapter.NoteViewHolder holder, int position) {
        NoteEntity note = notesList.get(position);
        Context context = holder.itemView.getContext();

        // 1. Basic Text
        holder.binding.tvAuthorName.setText(note.authorName);
        holder.binding.tvNoteContent.setText(note.noteText);
        holder.binding.tvRoleBadge.setText(note.createdByRole);

        // 2. Format Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        holder.binding.tvNoteDate.setText(sdf.format(new Date(note.createdAt)));

        // 3. Complaint Context
        if (note.complaintTitle != null && !note.complaintTitle.isEmpty()) {
            holder.binding.tvComplaintContext.setVisibility(View.VISIBLE);
            holder.binding.tvComplaintContext.setText("Re: " + note.complaintTitle);
        } else {
            holder.binding.tvComplaintContext.setVisibility(View.GONE);
        }

        // 4. Handle Backgrounds & Borders (Dynamic Theme Support)
        if (note.isInternal) {
            // Internal Warning styling
            holder.binding.cardNoteContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.note_internal_bg));
            holder.binding.cardNoteContainer.setStrokeColor(ContextCompat.getColor(context, R.color.note_internal_stroke));
        } else {
            // Standard styling using your brand colors
            holder.binding.cardNoteContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card));
            holder.binding.cardNoteContainer.setStrokeColor(ContextCompat.getColor(context, R.color.card_stroke));
        }

        // 5. Handle Role Badge Colors
        if ("ADMIN".equals(note.createdByRole)) {
            holder.binding.tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_admin_text));
            holder.binding.tvRoleBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.role_admin_bg));
        } else {
            holder.binding.tvRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_tech_text));
            holder.binding.tvRoleBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.role_tech_bg));
        }
    }

    @Override
    public int getItemCount() {
        return notesList != null ? notesList.size() : 0;
    }

    public void setNotes(List<NoteEntity> notesList){
        this.notesList = notesList;
        notifyDataSetChanged();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        RvTechnicianNotesBinding binding;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvTechnicianNotesBinding.bind(itemView);
        }
    }
}
