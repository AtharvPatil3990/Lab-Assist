package com.android.labassist.technician;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.DialogAddDeviceBinding;
import com.android.labassist.databinding.FragmentTechLabDevicesBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;

public class TechLabDevicesFragment extends Fragment {
    TechLabDevicesViewModel viewModel;
    private FragmentTechLabDevicesBinding binding;
    private DeviceAdapter adapter;
    private String currentLabId;
    private boolean isAdmin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTechLabDevicesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get the Lab ID passed from the PagerAdapter
        if (getArguments() != null) {
            currentLabId = getArguments().getString("LAB_ID");
            isAdmin = SessionManager.getInstance(requireContext()).getRole().equals(SessionManager.ROLE_ADMIN);
        }

        setupRecyclerView();
        setupViewModel();

        // 2. Fetch the devices for this specific lab from the database
        fetchDevices();

        if(isAdmin){
            setupUI();
            setupObservers();
        }
    }

    private void setupViewModel(){
        viewModel = new ViewModelProvider(this).get(TechLabDevicesViewModel.class);
        viewModel.init(AppDatabase.getInstance(requireContext()).labAssistDao(), currentLabId);
    }

    public void setupUI(){
        if(isAdmin){
            binding.fabAddDevice.setVisibility(View.VISIBLE);
            binding.fabAddDevice.setOnClickListener(v -> {
                showAddDeviceDialog();
            });
        }
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

    private void showAddDeviceDialog() {
        DialogAddDeviceBinding dialogBinding = DialogAddDeviceBinding.inflate(getLayoutInflater());

        String[] uiDeviceTypes = getResources().getStringArray(R.array.device_types_ui);
        String[] valuesArray = getResources().getStringArray(R.array.device_types_values);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, uiDeviceTypes);
        dialogBinding.actvDeviceType.setAdapter(adapter);

        dialogBinding.actvDeviceType.setOnItemClickListener((parent, view, position, id) -> {
            // Use the index-based value check for better reliability
            String dbValue = valuesArray[position];
            if ("OTHER".equals(dbValue)) {
                dialogBinding.tilDeviceTypeOther.setVisibility(View.VISIBLE);
                dialogBinding.etDeviceTypeOther.requestFocus();
            } else {
                dialogBinding.tilDeviceTypeOther.setVisibility(View.GONE);
                dialogBinding.etDeviceTypeOther.setText("");
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Register New Device")
                .setView(dialogBinding.getRoot())
                .setCancelable(false)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add Device", null) // Set to null initially
                .create();

        dialog.show();

        // Override the button click AFTER showing to prevent auto-dismiss on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogBinding.etDeviceName.getText().toString().trim();
            String code = dialogBinding.etDeviceCode.getText().toString().trim();
            String typeOther = dialogBinding.etDeviceTypeOther.getText().toString().trim();
            String selectedUiType = dialogBinding.actvDeviceType.getText().toString().trim();

            int selectedIndex = Arrays.asList(uiDeviceTypes).indexOf(selectedUiType);

            // Validation logic
            if (name.isEmpty() || code.isEmpty() || selectedIndex == -1) {
                Toast.makeText(requireContext(), "Name, Code, and Category are required", Toast.LENGTH_SHORT).show();
                return; // Dialog stays open
            }

            String dbTypeEnum = valuesArray[selectedIndex];

            if ("OTHER".equals(dbTypeEnum) && typeOther.isEmpty()) {
                dialogBinding.etDeviceTypeOther.setError("Please specify type");
                return;
            }

            // Finalize data and call ViewModel
            String finalOtherType = "OTHER".equals(dbTypeEnum) ? typeOther : null;
            viewModel.createDevice(currentLabId, code, name, dbTypeEnum, finalOtherType);

            dialog.dismiss(); // Manually dismiss only when validation passes
        });
    }
    private void setupRecyclerView() {
        adapter = new DeviceAdapter(deviceId -> {
             Bundle args = new Bundle();
             args.putString("DEVICE_ID", deviceId);
             if (isAdmin)
                 Navigation.findNavController(requireView()).navigate(R.id.action_admin_to_device_detail, args);
            else
                 Navigation.findNavController(requireView()).navigate(R.id.action_lab_devices_to_device_details, args);
        });

        binding.rvDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDevices.setAdapter(adapter);
    }

    private void fetchDevices() {
         viewModel.getDevicesByLabId().observe(getViewLifecycleOwner(), devices -> {
             adapter.setDevices(devices);
             toggleEmptyState(devices.isEmpty());
         });
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.rvDevices.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rvDevices.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}