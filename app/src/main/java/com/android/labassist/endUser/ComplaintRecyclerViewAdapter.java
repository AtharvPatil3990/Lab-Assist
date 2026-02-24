package com.android.labassist.endUser;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ComplaintRecyclerViewAdapter extends RecyclerView.Adapter<ComplaintRecyclerViewAdapter.ComplaintViewHolder> {
    Context context;
    ArrayList<UserComplaint> complaintsArrList;
    public ComplaintRecyclerViewAdapter(Context context, ArrayList<UserComplaint> complaintsArrList){
        this.complaintsArrList = complaintsArrList;
        this.context = context;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ComplaintViewHolder(inflater.inflate(R.layout.rv_user_complaints_custom_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintRecyclerViewAdapter.ComplaintViewHolder holder, int position) {
        UserComplaint userComplaint = complaintsArrList.get(position);
        String labName = userComplaint.getLabName() + " - " + userComplaint.getPc();
        UserComplaint complaint = complaintsArrList.get(position);

        holder.tvLabName.setText(labName);
        holder.tvTitle.setText(complaint.getIssue());
        holder.tvDate.setText(setDate(complaint.getReportedDate()));
        ComplaintStatus status = complaint.getStatus();
        if(status == ComplaintStatus.Resolved)
            holder.tvStatus.setTextColor(context.getColor(R.color.completed_status));
        holder.tvStatus.setText(complaint.getStatus().toString());
    }

    public void updateList(ArrayList<UserComplaint> filteredList) {
        // 1. Replace the old list with the newly filtered search results
        this.complaintsArrList = filteredList;
        // 2. Tell the RecyclerView to wipe the screen and redraw itself with the new data
        notifyDataSetChanged();
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
