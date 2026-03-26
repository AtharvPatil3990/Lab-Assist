package com.android.labassist.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.DialogAddDepartmentBinding;
import com.android.labassist.databinding.FragmentDepartmentsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DepartmentsFragment extends Fragment {

    private FragmentDepartmentsBinding binding;
    private DepartmentsViewModel viewModel;
    private DepartmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDepartmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = SessionManager.getInstance(requireContext());

        // THE BYPASS (Unchanged)
        if (SessionManager.ADMIN_DEPT.equals(sessionManager.getAdminLevel())) {
            Bundle bundle = new Bundle();
            bundle.putString("department_id", sessionManager.getDepartmentID());
            NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.navigation_departments, true).build();
            Navigation.findNavController(view).navigate(R.id.action_admin_to_department_lab, bundle, navOptions);
            return;
        }

        // Standard Fragment-Scoped ViewModel!
        // Note: You will need a ViewModelFactory if your ViewModel requires the Repository in its constructor
        viewModel = new ViewModelProvider(this).get(DepartmentsViewModel.class);
        setupRecyclerView();
        setupObservers();

        binding.swipeRefreshDepartments.setOnRefreshListener(() -> {
            viewModel.refreshArchitecture();
            binding.swipeRefreshDepartments.setRefreshing(false);
        } );
    }

    private void setupObservers(){

        // Observe Room Database directly!
        viewModel.getAllDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments != null && !departments.isEmpty()) {
                adapter.setDepartments(departments);
                binding.rvDepartments.setVisibility(View.VISIBLE);
            } else {
                binding.rvDepartments.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) binding.loadingOverlay.setVisibility(View.VISIBLE);
            else binding.loadingOverlay.setVisibility(View.GONE);
        });

        binding.fabAddDepartment.setOnClickListener(v -> {
            showCreateDeptDialog();
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

    private void showCreateDeptDialog(){
        DialogAddDepartmentBinding dialogBinding = DialogAddDepartmentBinding.inflate(getLayoutInflater());

        // 2. Build the Material Dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New Department")
                .setView(dialogBinding.getRoot())
                .setCancelable(false) // Forces them to click Cancel or Create
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Create", (dialog, which) -> {

                    // 3. Extract the text
                    String name = dialogBinding.etDeptName.getText().toString().trim();
                    String code = dialogBinding.etDeptCode.getText().toString().trim();
                    String desc = dialogBinding.etDeptDescription.getText().toString().trim();

                    // 4. Basic Validation
                    if (name.isEmpty() || code.isEmpty()) {
                        Toast.makeText(requireContext(), "Name and Code are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 5. Update the loading text specifically for this action!
                    binding.tvLoadingText.setText("Creating Department...");

                    viewModel.createNewDepartment(name, code, desc);
                })
                .show();
    }

    private void setupRecyclerView() {
        adapter = new DepartmentAdapter(department -> {
            Bundle bundle = new Bundle();
            Log.d("DepartmentId", "department_id: " + department.getId());
            bundle.putString("department_id", department.getId()); // Using Entity getter
            bundle.putString("department_name", department.getName());

            Navigation.findNavController(requireView()).navigate(R.id.action_admin_to_department_lab, bundle);
        });

        binding.rvDepartments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDepartments.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}