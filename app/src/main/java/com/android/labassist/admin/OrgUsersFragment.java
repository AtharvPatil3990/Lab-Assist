package com.android.labassist.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.entities.DepartmentEntity;
import com.android.labassist.database.entities.StudentEntity;
import com.android.labassist.database.entities.TechnicianEntity;
import com.android.labassist.databinding.DialogInviteUserBinding;
import com.android.labassist.databinding.FragmentAdminReportsBinding;
import com.android.labassist.network.models.UserModel;
import com.google.android.material.tabs.TabLayout;

import android.widget.ArrayAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Arrays;

import java.util.ArrayList;

public class OrgUsersFragment extends Fragment {

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

        setupObservers();

        // 3. Observe the Student Data
        viewModel.getStudentsList().observe(getViewLifecycleOwner(), students -> {
            if (students != null) {
//                List<UserModel> currentStudents = mapStudentEntityToUserModel(students);
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
//                List<UserModel> currentTechnicians = mapTechnicianEntityToUserModel(techs);
                currentTechnicians = techs;
                // If the "Technicians" tab is currently selected (index 1), update the UI
                if (binding.tabLayoutUsers.getSelectedTabPosition() == 1) {
                    adapter.updateData(currentTechnicians);
                }
            }
        });

        binding.fabInviteUser.setOnClickListener(v -> {
            showInviteUserDialog();
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

    private void showInviteUserDialog() {
        // 1. Fetch current admin context from ViewModel
        SessionManager session = SessionManager.getInstance(requireContext());
        String currentOrgId = session.getOrganisationId();
        String currentAdminLevel = session.getAdminLevel(); // "ORG_ADMIN" or "DEPT_ADMIN"
        String currentAdminDeptId = session.getDepartmentID(); // Might be null if ORG_ADMIN

        DialogInviteUserBinding dialogBinding = DialogInviteUserBinding.inflate(getLayoutInflater());

        // 2. Setup Dynamic Roles & UI based on Admin Level
        String[] uiRoles;
        String[] dbRoles;
        boolean isOrgAdmin = SessionManager.ADMIN_ORG.equals(currentAdminLevel);

        if (isOrgAdmin) {
            // Org Admin sees everything
            uiRoles = new String[]{"Student", "Technician", "Department Admin"};
            dbRoles = new String[]{"STUDENT", "TECHNICIAN", "DEPT_ADMIN"};
            dialogBinding.actvInviteDepartment.setVisibility(View.VISIBLE);

            // Fetch and populate departments ONLY for Org Admins
            List<DepartmentEntity> departments = viewModel.getDepartmentsSync(currentOrgId);
            Log.d("DeptList", "Is dept list empty = " + departments.isEmpty());

            if (departments != null && !departments.isEmpty()) {
                String[] deptNames = new String[departments.size()];
                for (int i = 0; i < departments.size(); i++) {
                    deptNames[i] = departments.get(i).getName();
                }
                ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, deptNames);
                dialogBinding.actvInviteDepartment.setAdapter(deptAdapter);
            }
        } else {
            // Dept Admin restrictions
            uiRoles = new String[]{"Student", "Technician"};
            dbRoles = new String[]{"STUDENT", "TECHNICIAN"};
            dialogBinding.actvInviteDepartment.setVisibility(View.GONE); // Hide the dropdown completely!
        }

        // Apply the restricted Role Adapter
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, uiRoles);
        dialogBinding.actvInviteRole.setAdapter(roleAdapter);

        // 3. The UX Magic: Dynamic Hints
        dialogBinding.actvInviteRole.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRole = uiRoles[position];
            if (selectedRole.equals("Student")) {
                dialogBinding.tilInviteIdNumber.setHint("Roll Number *");
            } else {
                dialogBinding.tilInviteIdNumber.setHint("Employee Code *");
            }
        });

        // 4. Build the Dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Invite New User")
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Send Invite", (dialog, which) -> {

                    String email = dialogBinding.etInviteEmail.getText().toString().trim();
                    String idNumber = dialogBinding.etInviteIdNumber.getText().toString().trim();
                    String selectedUiRole = dialogBinding.actvInviteRole.getText().toString().trim();

                    // Base validation
                    if (email.isEmpty() || idNumber.isEmpty() || selectedUiRole.isEmpty()) {
                        Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Map UI Role to Database Enum
                    int roleIndex = Arrays.asList(uiRoles).indexOf(selectedUiRole);
                    String roleEnum = dbRoles[roleIndex];

                    // Determine the Target Department ID securely
                    String targetDeptId = null;

                    if (isOrgAdmin) {
                        String selectedDeptName = dialogBinding.actvInviteDepartment.getText().toString().trim();
                        if (selectedDeptName.isEmpty()) {
                            Toast.makeText(requireContext(), "Please select a department", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Map name to ID
                        List<DepartmentEntity> departments = viewModel.getDepartmentsSync(currentOrgId);
                        for (DepartmentEntity dept : departments) {
                            if (dept.getName().equals(selectedDeptName)) {
                                targetDeptId = dept.getId();
                                break;
                            }
                        }
                    } else {
                        // Force the Dept Admin's own department ID!
                        targetDeptId = currentAdminDeptId;
                    }

                    // Fire the API call!
                    viewModel.sendUserInvite(email, idNumber, roleEnum, currentOrgId, targetDeptId);
                })
                .show();
    }

//    private List<UserModel> mapStudentEntityToUserModel(List<StudentEntity> students){
//        List<UserModel> studentUserModel = new ArrayList<>();
//
//        for(StudentEntity stud: students){
//            UserModel u = new UserModel();
//            u.id = stud.id;
//            u.role = SessionManager.ROLE_STUDENT;
//            u.name = stud.name;
//            u.rollNumber = stud.rollNumber;
//            studentUserModel.add(u);
//        }
//
//        return studentUserModel;
//    }
//    private List<UserModel> mapTechnicianEntityToUserModel(List<TechnicianEntity> technicians){
//        List<UserModel> techniciansUserModel = new ArrayList<>();
//
//        for(TechnicianEntity tech: technicians){
//            UserModel u = new UserModel();
//            u.id = tech.id;
//            u.role = SessionManager.ROLE_TECH;
//            u.name = tech.name;
//            u.employeeCode = tech.empCode;
//            u.level = tech.level;
//            techniciansUserModel.add(u);
//        }
//        return techniciansUserModel;
//    }

    private void setupObservers(){
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) binding.loadingOverlay.setVisibility(View.VISIBLE);
            else binding.loadingOverlay.setVisibility(View.GONE);
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                // SHOW THE MESSAGE!
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                // Turn off the loading overlay
                binding.loadingOverlay.setVisibility(View.GONE);

                // Optional: Tell the ViewModel we saw the message so it doesn't show again on phone rotation
                viewModel.clearMessages();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                // SHOW THE MESSAGE!
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                // Turn off the loading overlay
                binding.loadingOverlay.setVisibility(View.GONE);

                // Optional: Tell the ViewModel we saw the message so it doesn't show again on phone rotation
                viewModel.clearMessages();
            }
        });
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