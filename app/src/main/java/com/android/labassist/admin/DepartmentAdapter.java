package com.android.labassist.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.labassist.databinding.ItemDepartmentsBinding;
import com.android.labassist.database.entities.DepartmentEntity;
import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private List<DepartmentEntity> departmentList = new ArrayList<>();
    private final OnDepartmentClickListener listener;

    public interface OnDepartmentClickListener {
        void onDepartmentClick(DepartmentEntity department);
    }

    public DepartmentAdapter(OnDepartmentClickListener listener) {
        this.listener = listener;
    }

    public void setDepartments(List<DepartmentEntity> newDepartments) {
        this.departmentList = newDepartments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepartmentsBinding binding = ItemDepartmentsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DepartmentEntity dept = departmentList.get(position);
        holder.bind(dept, listener);
    }

    @Override
    public int getItemCount() {
        return departmentList == null ? 0 : departmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDepartmentsBinding binding;

        ViewHolder(ItemDepartmentsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DepartmentEntity dept, OnDepartmentClickListener listener) {
            binding.tvDepartmentName.setText(dept.getName());
            binding.tvDepartmentCode.setText("Dept Code: " + (dept.getCode() != null ? dept.getCode() : "N/A"));

            if (dept.getName() != null && !dept.getName().isEmpty()) {
                binding.tvDepartmentInitial.setText(String.valueOf(dept.getName().charAt(0)).toUpperCase());
            } else {
                binding.tvDepartmentInitial.setText("?");
            }

            binding.getRoot().setOnClickListener(v -> listener.onDepartmentClick(dept));
        }
    }
}