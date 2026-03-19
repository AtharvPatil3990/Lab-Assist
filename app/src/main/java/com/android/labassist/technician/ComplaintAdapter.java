package com.android.labassist.technician;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.android.labassist.databinding.ItemComplaintBinding;
import com.android.labassist.database.entities.ComplaintEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {

    private List<ComplaintEntity> complaintList = new ArrayList<>();
    private final OnComplaintClickListener listener;

    public interface OnComplaintClickListener {
        void onComplaintClick(ComplaintEntity complaint);
    }

    public ComplaintAdapter(OnComplaintClickListener listener) {
        this.listener = listener;
    }

    public void setComplaints(List<ComplaintEntity> complaints) {
        this.complaintList = complaints;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemComplaintBinding binding = ItemComplaintBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ComplaintViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        holder.bind(complaintList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {

        private final ItemComplaintBinding binding;

        public ComplaintViewHolder(@NonNull ItemComplaintBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ComplaintEntity complaint, OnComplaintClickListener listener) {
            Context context = itemView.getContext();

            // 1. Set Title & Date
            binding.tvComplaintTitle.setText(complaint.title != null ? complaint.title : "Unknown Issue");

            binding.tvComplaintDate.setText(parseDate(complaint.createdAt));

            // 2. Handle the Material Chip Priority Badge
            if (complaint.priority != null && complaint.priority.equalsIgnoreCase("High")) {
                binding.tvPriorityBadge.setVisibility(View.VISIBLE);
                binding.tvPriorityBadge.setText("HIGH");
            } else {
                binding.tvPriorityBadge.setVisibility(View.GONE);
            }

            // 3. Handle Dynamic Status Colors using your custom palette
            String status = complaint.status != null ? complaint.status : "Pending";
            binding.tvComplaintStatus.setText(status);

            // Extract the background drawable and mutate it so we can change its color safely
            GradientDrawable statusBg = (GradientDrawable) binding.tvComplaintStatus.getBackground().mutate();

            // Convert 1dp to pixels for the stroke width
            int strokeWidth = (int) (1 * context.getResources().getDisplayMetrics().density);

            switch (status.toLowerCase()) {
                case "resolved":
                case "completed":
                    binding.tvComplaintStatus.setTextColor(ContextCompat.getColor(context, R.color.completed_status));
                    statusBg.setColor(ContextCompat.getColor(context, R.color.chip_completed_status_bg));
                    statusBg.setStroke(strokeWidth, ContextCompat.getColor(context, R.color.chip_completed_status_stroke_color));
                    break;

                case "in progress":
                case "ongoing":
                case "assigned":
                    binding.tvComplaintStatus.setTextColor(ContextCompat.getColor(context, R.color.ongoing_status));
                    statusBg.setColor(ContextCompat.getColor(context, R.color.chip_ongoing_status_bg));
                    statusBg.setStroke(strokeWidth, ContextCompat.getColor(context, R.color.chip_ongoing_status_stroke_color));
                    break;

                case "cancelled":
                    binding.tvComplaintStatus.setTextColor(ContextCompat.getColor(context, R.color.cancelled_status));
                    statusBg.setColor(ContextCompat.getColor(context, R.color.chip_cancelled_status_bg));
                    statusBg.setStroke(strokeWidth, ContextCompat.getColor(context, R.color.chip_cancelled_status_stroke_color));
                    break;

                case "pending":
                default:
                    binding.tvComplaintStatus.setTextColor(ContextCompat.getColor(context, R.color.pending_status));
                    statusBg.setColor(ContextCompat.getColor(context, R.color.chip_pending_status_bg));
                    statusBg.setStroke(strokeWidth, ContextCompat.getColor(context, R.color.chip_pending_status_stroke_color));
                    break;
            }

            // 4. Trigger the Bottom Sheet on click
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onComplaintClick(complaint);
                }
            });
        }

        private String parseDate(long dateInMills) {
            // 1. Safety check for invalid dates
            if (dateInMills <= 0) {
                return "Unknown Date";
            }

            Date dateObj = new Date(dateInMills);
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

            // 2. Is it Today? (e.g., "Today, 10:30 AM")
            if (DateUtils.isToday(dateInMills)) {
                return "Today • " + timeFormat.format(dateObj);
            }

            // Setup Calendars to check for Yesterday and the Year
            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTimeInMillis(dateInMills);

            // 3. Is it Yesterday? (e.g., "Yesterday, 10:30 AM")
            now.add(Calendar.DAY_OF_YEAR, -1);
            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday • " + timeFormat.format(dateObj);
            }

            // Reset the 'now' calendar back to today
            now = Calendar.getInstance();

            // 4. Is it from this current year? (e.g., "Oct 24 • 10:30 AM")
            if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
                SimpleDateFormat sameYearFormat = new SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault());
                return sameYearFormat.format(dateObj);
            }
            // 5. It must be from a previous year (e.g., "24 Oct 2023 • 10:30 AM")
            else {
                SimpleDateFormat diffYearFormat = new SimpleDateFormat("dd MMM yyyy • h:mm a", Locale.getDefault());
                return diffYearFormat.format(dateObj);
            }
        }
    }
}
