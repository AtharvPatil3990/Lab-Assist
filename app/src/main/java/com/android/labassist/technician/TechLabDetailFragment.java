package com.android.labassist.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.TechnicianEntity;
import com.android.labassist.databinding.DialogAssignTechToLabBinding;
import com.android.labassist.databinding.FragmentTechLabDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TechLabDetailFragment extends Fragment {

    private FragmentTechLabDetailBinding binding;
    private String currentLabId;
    private String currentLabName;
    private String currDeptId;

    private boolean isAdmin;
    TechLabDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTechLabDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Extract the Lab ID that was passed from the Assigned Labs Directory
        if (getArguments() != null) {
            currentLabId = getArguments().getString("LAB_ID");
            currDeptId = getArguments().getString("DEPARTMENT_ID", "");
            currentLabName = getArguments().getString("LAB_NAME", "Lab");
        }

        viewModel = new ViewModelProvider(this).get(TechLabDetailViewModel.class);


        isAdmin = SessionManager.getInstance(requireContext()).getRole().equals(SessionManager.ROLE_ADMIN);

        setupToolbar();
        setupViewPagerAndTabs();
    }

    private void setupToolbar() {
        // Handle the back button click
        binding.toolbarLabHub.setNavigationOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        if(isAdmin) {
            binding.toolbarLabHub.setTitle("Department Lab");
            setupToolbarOverflow();
            setupObservers();
        }

        if(currentLabName != null && !currentLabName.isEmpty())
            binding.toolbarLabHub.setTitle(currentLabName);
    }

    private void setupViewPagerAndTabs() {
        // 1. Attach the Adapter to the ViewPager
        LabPagerAdapter pagerAdapter = new LabPagerAdapter(this, currentLabId);
        binding.viewPager.setAdapter(pagerAdapter);

        // 2. Use the Mediator to link the TabLayout and the ViewPager together
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Devices");
                    break;
                case 1:
                    tab.setText("Complaints");
                    break;
                case 2:
                    tab.setText("Notes");
                    break;
            }
        }).attach();
    }

    private void showAssignTechDialog(List<TechnicianEntity> technicians) {

        if (technicians == null || technicians.isEmpty()) {
            Toast.makeText(requireContext(), "No technicians available.", Toast.LENGTH_SHORT).show();
            return;
        }

        DialogAssignTechToLabBinding dialogBinding = DialogAssignTechToLabBinding.inflate(getLayoutInflater());

        // 1. Create the dynamic array for the UI
        String[] uiTechNames = new String[technicians.size()];
        for (int i = 0; i < technicians.size(); i++) {
            TechnicianEntity tech = technicians.get(i);
            uiTechNames[i] = tech.name + " (" + tech.empCode + ")";
        }

        // 2. Setup the Dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, uiTechNames);
        dialogBinding.actvTechnician.setAdapter(adapter);

        // 3. Build the Dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Assign Technician")
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Assign", (dialog, which) -> {

                    String selectedUiName = dialogBinding.actvTechnician.getText().toString().trim();

                    if (selectedUiName.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select a technician", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 4. Capture the boolean from the CheckBox!
                    boolean isPrimary = dialogBinding.cbIsPrimary.isChecked();

                    // 5. Find the selected technician's ID
                    int selectedIndex = Arrays.asList(uiTechNames).indexOf(selectedUiName);

                    if (selectedIndex >= 0) {
                        String selectedTechId = technicians.get(selectedIndex).id;

                        // 6. Trigger the API with the ID and the boolean
                        viewModel.assignTechnicianToLab(currentLabId, selectedTechId, isPrimary);
                    }
                })
                .show();
    }

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

    private void setupToolbarOverflow() {
        // 1. Inflate the menu into your Toolbar
        binding.toolbarLabHub.setOverflowIcon(ContextCompat.getDrawable(requireContext(), R.drawable.overflow_menu_icon));
        binding.toolbarLabHub.inflateMenu(R.menu.menu_lab_detail);

        // 2. Listen for clicks on the menu items
        binding.toolbarLabHub.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_assign_tech) {
                showAssignTechDialog(viewModel.getTechniciansForDepartment(currDeptId));
                return true;
            }

            return false;
        });

        // Optional: Handle the back arrow click
        binding.toolbarLabHub.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}