package com.android.labassist.technician;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.android.labassist.database.entities.ComplaintEntity;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ComplaintRVAdapterTech extends RecyclerView.Adapter<ComplaintRVAdapterTech.ComplaintViewHolder> {
    private ArrayList<ComplaintEntity> displayList; // What is currently on screen
    private ArrayList<ComplaintEntity> originalList; // Backup of the data the fragment provided
    private final Context context;

    public ComplaintRVAdapterTech(ArrayList<ComplaintEntity> list, Context context) {
        this.displayList = list;
        this.originalList = new ArrayList<>(list); // Initialize backup
        this.context = context;
    }

    public void updateData(ArrayList<ComplaintEntity> newList) {
        this.displayList = newList;
        this.originalList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    /**
     * The Search Logic: Works on whichever list was provided to the adapter.
     */
    public void filter(String query) {
        ArrayList<ComplaintEntity> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (ComplaintEntity item : originalList) {
                // Check Title or ID
                if ((item.title != null && item.title.toLowerCase().contains(filterPattern)) ||
                        (item.id != null && item.id.toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
        }

        this.displayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use parent.getContext() to ensure the theme is inherited correctly
        return new ComplaintViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_technician_complaint_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        ComplaintEntity techComplaint = displayList.get(position);

        holder.tvTitle.setText(techComplaint.title); // Using direct access if public, or getters
        holder.tvComplaintID.setText(String.format("ID: %s", techComplaint.id));
        holder.tvLab.setText(String.format("Lab: %s", techComplaint.labName));

        holder.tvAllocatedTime.setText(String.format("Assigned: %s", getDateTime(techComplaint.createdAt)));

        // Apply status and style
        setComplaintChipStatus(holder.chipStatus, techComplaint.status);
        holder.chipStatus.setText(techComplaint.status);

        holder.layout.setOnClickListener(v -> animateAndOpen(v, () -> {
            // SAFE CASTING CHECK
            if (context instanceof FragmentActivity) {
                new BottomSheetComplaintTech(techComplaint)
                        .show(((FragmentActivity) context).getSupportFragmentManager(), "ComplaintBottomSheet");
            }
        }));
    }

    @Override
    public int getItemCount() {
        return displayList != null ? displayList.size() : 0;
    }

    // Helper to keep onBindViewHolder clean
    public void setComplaintChipStatus(Chip chipStatus, String status) {
        if (status == null) return;

        // Use a consistent case check
        String normalizedStatus = status.toLowerCase();

        switch (normalizedStatus) {
            case "resolved":
                chipStatus.setTextColor(context.getColor(R.color.completed_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_completed_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_completed_status_stroke_color);
                break;
            case "cancelled":
                chipStatus.setTextColor(context.getColor(R.color.cancelled_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_cancelled_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_cancelled_status_stroke_color);
                break;
            case "ongoing":
            case "in progress": // Added an extra common case
                chipStatus.setTextColor(context.getColor(R.color.ongoing_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_ongoing_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_ongoing_status_stroke_color);
                break;
            default: // Default to Pending style
                chipStatus.setTextColor(context.getColor(R.color.pending_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_pending_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_pending_status_stroke_color);
                break;
        }
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLab, tvComplaintID, tvAllocatedTime;
        Chip chipStatus;
        ViewGroup layout;
        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLab = itemView.findViewById(R.id.tvLocation);
            tvComplaintID = itemView.findViewById(R.id.tvComplaintId);
            tvAllocatedTime = itemView.findViewById(R.id.tvAllocatedTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);

            layout = itemView.findViewById(R.id.MainConstLayout);
        }
    }

    private String getDateTime(long dateInMilli){
        String time;
        if(DateUtils.isToday(dateInMilli)){
            SimpleDateFormat timeSDF = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            time = "Today, " + timeSDF.format(dateInMilli);
        }
        else{
            SimpleDateFormat dateSDF = new SimpleDateFormat("dd MM, hh:mm a", Locale.getDefault());
            time = dateSDF.format(dateInMilli);
        }
        return time;
    }

    private void animateAndOpen(View view, Runnable onEnd) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(60)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .withEndAction(onEnd)
                            .start();
                })
                .start();
    }
}
//•
