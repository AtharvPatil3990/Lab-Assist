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

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.RvTechnicianComplaintLayoutBinding;
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
                        (item.id.toLowerCase().contains(filterPattern))) {
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

        holder.binding.tvTitle.setText(techComplaint.title); // Using direct access if public, or getters
        holder.binding.tvDeviceCode.setText(String.format("Device: %s", techComplaint.deviceName + " • " + techComplaint.deviceCode));
        holder.binding.tvLocation.setText(String.format("Lab: %s", techComplaint.labName));

        holder.binding.tvAllocatedTime.setText(String.format("Assigned: %s", getDateTime(techComplaint.createdAt)));

        ComplaintStatus status;
        switch (techComplaint.status){
            case "RESOLVED": status = ComplaintStatus.RESOLVED;
                        break;
            case "OPEN": status = ComplaintStatus.OPEN;
                        break;
            case "IN_PROGRESS": status = ComplaintStatus.IN_PROGRESS;
                        break;
            case "ON_HOLD": status = ComplaintStatus.ON_HOLD;
                        break;
            case "CANCELLED": status = ComplaintStatus.CANCELLED;
                        break;
            case "CLOSED": status = ComplaintStatus.CLOSED;
                        break;
            case "QUEUED": status = ComplaintStatus.QUEUED;
                        break;
            case "ASSIGNED":
            default: status = ComplaintStatus.ASSIGNED;
        }

        // Apply status and style
        setComplaintChipStatus(holder.binding.chipStatus, status);
        holder.binding.chipStatus.setText(techComplaint.status);

        holder.binding.MainConstLayout.setOnClickListener(v -> animateAndOpen(v, () -> {
            // SAFE CASTING CHECK
            if (context instanceof FragmentActivity) {

                // 1. Use the newInstance method and pass ONLY the ID
                BottomSheetComplaintTech complaintDetail = BottomSheetComplaintTech.newInstance(techComplaint.getId());

                // 2. Show the bottom sheet (this automatically triggers the slide-up animation)
                complaintDetail.show(((FragmentActivity) context).getSupportFragmentManager(), "ComplaintBottomSheet");
            }
        }));
    }

    @Override
    public int getItemCount() {
        return displayList != null ? displayList.size() : 0;
    }

    // Helper to keep onBindViewHolder clean
    public void setComplaintChipStatus(Chip chipStatus, ComplaintStatus status) {
        if (status == null) return;

        // Default to Pending style
//        OPEN, ASSIGNED, IN_PROGRESS, ON_HOLD, RESOLVED, CANCELLED, CLOSED, QUEUED
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            chipStatus.setTextColor(context.getColor(R.color.completed_status));
            chipStatus.setChipBackgroundColorResource(R.color.chip_completed_status_bg);
            chipStatus.setChipStrokeColorResource(R.color.chip_completed_status_stroke_color);
        }
        else if (status == ComplaintStatus.CANCELLED || status == ComplaintStatus.QUEUED) {
            chipStatus.setTextColor(context.getColor(R.color.cancelled_status));
            chipStatus.setChipBackgroundColorResource(R.color.chip_cancelled_status_bg);
            chipStatus.setChipStrokeColorResource(R.color.chip_cancelled_status_stroke_color);
        }
        else if (status == ComplaintStatus.IN_PROGRESS || status == ComplaintStatus.OPEN) { // Added an extra common case
            chipStatus.setTextColor(context.getColor(R.color.ongoing_status));
            chipStatus.setChipBackgroundColorResource(R.color.chip_ongoing_status_bg);
            chipStatus.setChipStrokeColorResource(R.color.chip_ongoing_status_stroke_color);
        }
        else {
//          Assigned, pending, on_hold
            chipStatus.setTextColor(context.getColor(R.color.pending_status));
            chipStatus.setChipBackgroundColorResource(R.color.chip_pending_status_bg);
            chipStatus.setChipStrokeColorResource(R.color.chip_pending_status_stroke_color);
        }
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        RvTechnicianComplaintLayoutBinding binding;
        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RvTechnicianComplaintLayoutBinding.bind(itemView);
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