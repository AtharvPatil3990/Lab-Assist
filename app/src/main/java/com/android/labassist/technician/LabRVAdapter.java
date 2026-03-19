package com.android.labassist.technician;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.databinding.ItemAllocatedLabBinding;
import com.android.labassist.database.entities.LabEntity;

import java.util.ArrayList;
import java.util.List;

public class LabRVAdapter extends RecyclerView.Adapter<LabRVAdapter.LabViewHolder> {

    private List<LabEntity> labList = new ArrayList<>();
    private final OnLabClickListener listener;

    public interface OnLabClickListener {
        void onLabClick(String labId, String labName);
    }

    public LabRVAdapter(OnLabClickListener listener) {
        this.listener = listener;
    }

    public void setLabs(List<LabEntity> labs) {
        this.labList = labs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAllocatedLabBinding binding = ItemAllocatedLabBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new LabViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LabViewHolder holder, int position) {
        holder.bind(labList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return labList.size();
    }

    public static class LabViewHolder extends RecyclerView.ViewHolder {

        private final ItemAllocatedLabBinding binding;

        public LabViewHolder(@NonNull ItemAllocatedLabBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LabEntity lab, OnLabClickListener listener) {
            // 1. Set the Title
            binding.tvLabName.setText(lab.labName != null ? lab.labName : "Unknown Lab");

            // 2. Combine Type and Code for the Subtitle
            String type = lab.labType != null ? lab.labType : "General";
            String code = lab.labCode != null ? lab.labCode : "N/A";
            binding.tvLabLocation.setText(code + " • " + type);

            // 3. Handle the Maintenance Status Flag
            if (lab.isUnderMaintenance) {
                binding.tvMaintenanceBadge.setVisibility(View.VISIBLE);
                // Pro-tip: Slightly dim the card to physically show it is out of service
                binding.getRoot().setAlpha(0.6f);
            } else {
                binding.tvMaintenanceBadge.setVisibility(View.GONE);
                binding.getRoot().setAlpha(1.0f);
            }

            // 4. Pass the Primary Key to the click listener when tapped
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLabClick(lab.id, lab.labName);
                }
            });
        }
    }
}
