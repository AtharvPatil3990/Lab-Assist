package com.android.labassist.technician;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.DialogAddLabBinding;
import com.android.labassist.databinding.FragmentAssignedLabsBinding;
// Import your database class to get the DAO
import com.android.labassist.database.AppDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;

public class AssignedLabsFragment extends Fragment {

    private FragmentAssignedLabsBinding binding;
    // Initialize the adapter with the click listener
// When clicked, navigate to the Lab Detail Hub and pass the labId
    private LabRVAdapter adapter = new LabRVAdapter((labId, labName) -> navigateToLabDetails(labId, labName));
    private AssignedLabsViewModel viewModel;
    private boolean isAdmin;
    private String department_id;
    private String deptName;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAssignedLabsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null) {
            department_id = getArguments().getString("department_id", "");
            deptName = getArguments().getString("department_name", "");
        }

        sessionManager = SessionManager.getInstance(requireContext());
        isAdmin = sessionManager.getRole().equals(SessionManager.ROLE_ADMIN);

        setupRecyclerView();
        setupViewModel();

        setupUI();
    }

    private void setupUI(){
        if(isAdmin) {
            binding.toolbar.setTitle("Department Labs");
            binding.toolbar.setSubtitle(deptName);
            binding.fabAddLab.setVisibility(View.VISIBLE);

            if (sessionManager.getAdminLevel().equals(SessionManager.ADMIN_ORG)) {
                binding.toolbar.setNavigationIcon(R.drawable.arrow_back_icon);
                binding.toolbar.setNavigationOnClickListener(v -> {
                    Navigation.findNavController(requireView()).navigateUp();
                });
            }

            binding.tvEmptyStateText.setText("No Labs Configured");
            binding.tvEmptyStateSubText.setText("This department does not have any laboratory infrastructure mapped to it yet.");
        }
    }

    private void setupRecyclerView() {
        binding.rvAssignedLabs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAssignedLabs.setAdapter(adapter);
    }

    private void setupViewModel() {
        // 1. Get the ViewModel
        viewModel = new ViewModelProvider(this).get(AssignedLabsViewModel.class);

        // 2. Get your DAO from your Room Database instance
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Log.d("DepartmentId", "Inside AssignedLabs department id: " + department_id);
        viewModel.init(db.labAssistDao(), isAdmin, department_id);

        // 3. Observe the database!
        viewModel.getAssignedLabs().observe(getViewLifecycleOwner(), labs -> {
            if (labs != null) {
                // Update the RecyclerView instantly
                adapter.setLabs(labs);

                // Show the empty state if the technician has no labs
                toggleEmptyState(labs.isEmpty());
            }
        });

        if(isAdmin){
            setupObservers();
        }
    }

    private void showAddLabDialog() {
        DialogAddLabBinding dialogBinding = DialogAddLabBinding.inflate(getLayoutInflater());

        // 1. Setup the Dropdown using the UI array
        String[] uiLabTypes = getResources().getStringArray(R.array.lab_types_ui);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, uiLabTypes);
        dialogBinding.actvLabType.setAdapter(adapter);

        // =========================================================================
        // 2. THE MISSING LOGIC: Listen for Dropdown Selections to show "Others"
        // =========================================================================
        dialogBinding.actvLabType.setOnItemClickListener((parent, view, position, id) -> {
            // Grab the exact string they just tapped
            String selectedType = (String) parent.getItemAtPosition(position);

            // Check if it matches the "Others" item from your UI array
            if ("Others".equals(selectedType)) {
                // Reveal the hidden TextInputLayout
                dialogBinding.tilLabTypeOther.setVisibility(View.VISIBLE);
            } else {
                // Hide it, and clear out any text they might have typed previously
                dialogBinding.tilLabTypeOther.setVisibility(View.GONE);
                dialogBinding.etLabTypeOther.setText("");
            }
        });

        // 3. Build and show the dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Register New Lab")
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Create", (dialog, which) -> {

                    String name = dialogBinding.etLabName.getText().toString().trim();
                    String code = dialogBinding.etLabCode.getText().toString().trim();
                    String typeOther = dialogBinding.etLabTypeOther.getText().toString().trim();

                    // Get the pretty string they selected
                    String selectedUiType = dialogBinding.actvLabType.getText().toString().trim();

                    // Load the hidden Database Values array
                    String[] valuesArray = getResources().getStringArray(R.array.lab_types_values);

                    // Find the index and map it!
                    int selectedIndex = Arrays.asList(uiLabTypes).indexOf(selectedUiType);

                    String dbTypeEnum = "OTHERS"; // Safe fallback
                    if (selectedIndex >= 0 && selectedIndex < valuesArray.length) {
                        dbTypeEnum = valuesArray[selectedIndex].toUpperCase();
                    }

                    // Validation
                    if (name.isEmpty() || code.isEmpty() || selectedUiType.isEmpty()) {
                        Toast.makeText(requireContext(), "Name, Code, and Type are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("OTHERS".equals(dbTypeEnum) && typeOther.isEmpty()) {
                        Toast.makeText(requireContext(), "Please specify the custom lab type", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ensure typeOther is null for the database if they didn't pick "Others"
                    if (!"OTHERS".equals(dbTypeEnum)) {
                        typeOther = null;
                    }

                    // Trigger the ViewModel!
                    viewModel.createNewLab(name, code, dbTypeEnum, typeOther, department_id);
                })
                .show();
    }

    private void setupObservers(){
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) binding.loadingOverlay.setVisibility(View.VISIBLE);
            else binding.loadingOverlay.setVisibility(View.GONE);
        });

        binding.fabAddLab.setOnClickListener(v -> {
            showAddLabDialog();
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

    private void navigateToLabDetails(String labId, String labName) {
        Bundle args = new Bundle();
        args.putString("LAB_ID", labId);
        args.putString("LAB_NAME", labName);
        args.putString("DEPARTMENT_ID", department_id);

        if(isAdmin)
            Navigation.findNavController(requireView()).navigate(R.id.action_admin_to_lab_detail, args);
        else
            Navigation.findNavController(requireView()).navigate(R.id.action_assigned_lab_to_lab_detail, args);
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.rvAssignedLabs.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rvAssignedLabs.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}