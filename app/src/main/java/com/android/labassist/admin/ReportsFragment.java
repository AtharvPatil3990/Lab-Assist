package com.android.labassist.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.android.labassist.databinding.FragmentAdminReportsBinding;
import com.android.labassist.network.models.UserModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    private FragmentAdminReportsBinding binding;
    private UserAdapter adapter;
    private UsersViewModel viewModel;

    // Cache the lists locally so switching tabs is instant
    private List<UserModel> currentStudents = new ArrayList<>();
    private List<UserModel> currentTechnicians = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Safe and strict ViewBinding
        binding = FragmentAdminReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Setup RecyclerView
        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter(new ArrayList<>());
        binding.rvAdminUsers.setAdapter(adapter);

        // 2. Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        // 3. Observe the Student Data
        viewModel.getStudentsList().observe(getViewLifecycleOwner(), students -> {
            if (students != null) {
                currentStudents = students;
                // If the "Students" tab is currently selected (index 0), update the UI
                if (binding.tabLayoutUsers.getSelectedTabPosition() == 0) {
                    adapter.updateData(currentStudents);
                }
            }
        });

        // 4. Observe the Technician Data
        viewModel.getTechniciansList().observe(getViewLifecycleOwner(), techs -> {
            if (techs != null) {
                currentTechnicians = techs;
                // If the "Technicians" tab is currently selected (index 1), update the UI
                if (binding.tabLayoutUsers.getSelectedTabPosition() == 1) {
                    adapter.updateData(currentTechnicians);
                }
            }
        });

        // 5. Handle Tab Clicks
        binding.tabLayoutUsers.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.updateData(currentStudents);
                } else {
                    adapter.updateData(currentTechnicians);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 6. Trigger the network calls to fetch both lists
        viewModel.fetchAllUsers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Prevent memory leaks
    }

    // --- Inner Adapter Class ---
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<UserModel> users;

        public UserAdapter(List<UserModel> users) {
            this.users = users;
        }

        public void updateData(List<UserModel> newUsers) {
            this.users = newUsers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserModel user = users.get(position);

            holder.tvName.setText(user.name != null ? user.name : "Unknown User");

            // Check if it's a student or technician to show the right ID
            if (user.rollNumber != null) {
                holder.tvEmail.setText("Roll No: " + user.rollNumber);
            } else if (user.employeeCode != null) {
                holder.tvEmail.setText("Emp Code: " + user.employeeCode);
            } else {
                holder.tvEmail.setText("No ID provided");
            }

            String role = user.role != null ? user.role.toUpperCase() : "STUDENT";
            holder.tvRole.setText(role);

            // Apply color coding
            if (role.equals("TECHNICIAN") || role.equals("TECH")) {
                holder.tvRole.setTextColor(0xFF2563EB); // Blue
            } else {
                holder.tvRole.setTextColor(0xFFEB8C00); // Orange
            }
        }

        @Override
        public int getItemCount() { return users.size(); }

        static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRole;
            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvUserName);
                tvEmail = itemView.findViewById(R.id.tvUserEmail);
                tvRole = itemView.findViewById(R.id.tvUserRole);
            }
        }
    }
}