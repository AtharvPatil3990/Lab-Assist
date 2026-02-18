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
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ComplaintRVAdapterTech extends RecyclerView.Adapter<ComplaintRVAdapterTech.ComplaintViewHolder> {
    ArrayList<TechComplaint> complaintsArrList;
    Context context;

    ComplaintRVAdapterTech(ArrayList<TechComplaint> complaintsArrList, Context context){
        this.complaintsArrList = complaintsArrList;
        this.context = context;
    }

    @NonNull
    @Override
    public ComplaintRVAdapterTech.ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ComplaintViewHolder(inflater.inflate(R.layout.rv_technician_complaint_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintRVAdapterTech.ComplaintViewHolder holder, int position) {
        TechComplaint techComplaint = complaintsArrList.get(position);

        holder.tvTitle.setText(techComplaint.getTitle());
        holder.tvComplaintID.setText("ID: " + techComplaint.getComplaintID());
        holder.tvLab.setText("Lab: " + techComplaint.getLab());
        holder.tvDepartmentName.setText("Department: " + techComplaint.getDepartment());
        holder.tvAllocatedTime.setText("Assigned: " + getDateTime(techComplaint.getAssignedDate()));
        setComplaintChipStatus(holder.chipStatus, techComplaint.getStatus());
        holder.chipStatus.setText(techComplaint.getStatus().toString());

        holder.layout.setOnClickListener(v->{
            animateAndOpen(v, () ->{
                new BottomSheetComplaintTech(techComplaint)
                        .show(((FragmentActivity)context).getSupportFragmentManager(), "ComplaintBottomSheet");
            });
        });


    }

    @Override
    public int getItemCount() {
        return complaintsArrList.size();
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

    public void setComplaintChipStatus(Chip chipStatus, ComplaintStatus status){
        switch (status) {
            case Resolved:
                chipStatus.setTextColor(context.getColor(R.color.completed_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_completed_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_completed_status_stroke_color);
                break;
            case Cancelled:
                chipStatus.setTextColor(context.getColor(R.color.cancelled_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_cancelled_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_cancelled_status_stroke_color);
                break;
            case Ongoing:
                chipStatus.setTextColor(context.getColor(R.color.ongoing_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_ongoing_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_ongoing_status_stroke_color);
                break;
            case Pending:
                chipStatus.setTextColor(context.getColor(R.color.pending_status));
                chipStatus.setChipBackgroundColorResource(R.color.chip_pending_status_bg);
                chipStatus.setChipStrokeColorResource(R.color.chip_pending_status_stroke_color);
                break;
        }
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLab, tvComplaintID, tvAllocatedTime, tvDepartmentName;
        Chip chipStatus;
        ViewGroup layout;
        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLab = itemView.findViewById(R.id.tvLocation);
            tvComplaintID = itemView.findViewById(R.id.tvComplaintId);
            tvAllocatedTime = itemView.findViewById(R.id.tvAllocatedTime);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvDepartmentName = itemView.findViewById(R.id.tvDepartmentName);
            layout = itemView.findViewById(R.id.MainConstLayout);
        }
    }
}
//•
