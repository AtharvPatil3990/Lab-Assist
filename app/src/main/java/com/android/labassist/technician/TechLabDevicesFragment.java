package com.android.labassist.technician;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentTechLabDevicesBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

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
    }

    private void setupViewModel(){
        viewModel = new ViewModelProvider(this).get(TechLabDevicesViewModel.class);
        viewModel.init(AppDatabase.getInstance(requireContext()).labAssistDao(), currentLabId);
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