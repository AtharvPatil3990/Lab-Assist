package com.android.labassist.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentTechLabDetailBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class TechLabDetailFragment extends Fragment {

    private FragmentTechLabDetailBinding binding;
    private String currentLabId;
    private String currentLabName;

    private boolean isAdmin;

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
            currentLabName = getArguments().getString("LAB_NAME", "Lab");
        }

        isAdmin = SessionManager.getInstance(requireContext()).getRole().equals(SessionManager.ROLE_ADMIN);

        setupToolbar();
        setupViewPagerAndTabs();
    }

    private void setupToolbar() {
        // Handle the back button click
        binding.toolbarLabHub.setNavigationOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        if(isAdmin)
            binding.toolbarLabHub.setTitle("Department Lab");

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}