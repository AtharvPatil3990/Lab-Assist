package com.android.labassist.technician;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentTechDeviceDetailsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class TechDeviceDetailsFragment extends Fragment {

    private String deviceId, labId;
    FragmentTechDeviceDetailsBinding binding;

    TechDeviceDetailsViewModel viewModel;


    public TechDeviceDetailsFragment() {
        // Required empty public constructor
    }

    public static TechDeviceDetailsFragment newInstance(String labId, String deviceId) {
        TechDeviceDetailsFragment fragment = new TechDeviceDetailsFragment();
        Bundle args = new Bundle();
        args.putString("DEVICE_ID", deviceId);
        args.putString("LAB_ID", labId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceId = getArguments().getString("DEVICE_ID", null);
            labId = getArguments().getString("LAB_ID", null);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTechDeviceDetailsBinding.inflate(inflater, container, false);

        setupViewModel();
        setupViewPagerAndTabs();
        setupToolbar();

        return binding.getRoot();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TechDeviceDetailsViewModel.class);
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // 1. Give the ViewModel the ID
        viewModel.init(db.labAssistDao(), deviceId);

        // 2. Observe the data and update the Persistent Header!
        viewModel.getDevice().observe(getViewLifecycleOwner(), device -> {
            if (device != null) {
                // Populate the exact XML views we just created
                binding.tvHeaderDeviceName.setText(device.deviceName != null ? device.deviceName : "Unknown Device");
                binding.tvHeaderDeviceCode.setText(device.deviceCode != null ? device.deviceCode : "No Code");
                binding.tvHeaderDeviceType.setText(device.deviceType != null ? device.deviceType : "Standard");

                // Optional: You can also update the toolbar title to match!
                binding.toolbarDeviceHub.setTitle("Device Profile");
            }
        });
    }

    private void setupToolbar() {
        binding.toolbarDeviceHub.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupViewPagerAndTabs() {
        DevicePagerAdapter pagerAdapter = new DevicePagerAdapter(this, deviceId, labId);
        binding.viewPagerDevice.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayoutDevice, binding.viewPagerDevice, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Complaints");
                    break;
                case 1:
                    tab.setText("Notes");
                    break;
            }
        }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}