package com.android.labassist.endUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;

import java.util.ArrayList;

public class ComplaintRecyclerViewAdapter extends RecyclerView.Adapter<ComplaintRecyclerViewAdapter.ComplaintViewHolder> {
    Context context;
    ArrayList<Complaint> complaintsArrList;
    public ComplaintRecyclerViewAdapter(Context context, ArrayList<Complaint> complaintsArrList){
        this.complaintsArrList = complaintsArrList;
        this.context = context;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ComplaintViewHolder(inflater.inflate(R.layout.rv_mycomplaints_custom_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintRecyclerViewAdapter.ComplaintViewHolder holder, int position) {
        Complaint complaint = complaintsArrList.get(position);
        String labName = complaint.getLabName() + " - " + complaint.getPc();
        holder.tvLabName.setText(labName);
        holder.tvTitle.setText(complaintsArrList.get(position).getIssue());
        holder.tvDate.setText(complaintsArrList.get(position).getReportedDate());
        String status = complaintsArrList.get(position).getStatus();
        if(status.equals("resolved"))
            holder.tvStatus.setTextColor(context.getColor(R.color.halo_green_dark));
        holder.tvStatus.setText(complaintsArrList.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return complaintsArrList.size();
    }

    public static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus, tvLabName;
        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvIssueTitle);
            tvDate = itemView.findViewById(R.id.tvDateRV);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLabName = itemView.findViewById(R.id.tvLabName);
        }
    }
}
