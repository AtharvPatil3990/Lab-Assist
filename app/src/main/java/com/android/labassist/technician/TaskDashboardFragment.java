package com.android.labassist.technician;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentTechnicianDashboardBinding;
import com.android.labassist.endUser.HomeFragment;
import com.android.labassist.network.models.ComplaintsResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TaskDashboardFragment extends Fragment {

    private FragmentTechnicianDashboardBinding binding;
    private TaskDashboardViewModel viewModel;
    private ComplaintRVAdapterTech adapterTech;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTechnicianDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TaskDashboardViewModel.class);

        setupUI();
        setupDataObserver();

        // Initial sync from server
        viewModel.refreshDashboard();
        viewModel.getStats().observe(getViewLifecycleOwner(), this::updateDashboardCard);

        if(SessionManager.getInstance(requireContext()).getLastLabSyncTime() <= System.currentTimeMillis() + 18000000)
            viewModel.syncLabArchitecture();


        // 👇 ADD THIS TOOLBAR LISTENER HERE 👇
        binding.toolbarTechHome.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_notifications) {
                NavHostFragment.findNavController(TaskDashboardFragment.this)
                        .navigate(R.id.action_dashboard_to_notification);
                return true; // Tells Android we successfully handled the click
            }
            return false;
        });
    }

    private void updateDashboardCard(ComplaintsResponse.Stats stats) {
        if (stats == null) return;

        // 1. Fill the main 3 columns (Assigned, Ongoing, On Hold)
        binding.tvAssignedComplaintsCount.setText(String.valueOf(stats.assigned));
        binding.tvOngoingCount.setText(String.valueOf(stats.ongoing));
        binding.tvOnHoldCount.setText(String.valueOf(stats.onHold));

        // 2. Handle the "Resolved Today" Morale Booster
        if (stats.resolvedToday > 0) {
            binding.tvResolvedToday.setVisibility(View.VISIBLE);
            binding.tvResolvedToday.setText("You have resolved " + stats.resolvedToday + " complaints today!");
        } else {
            // Keep the UI clean if they haven't finished anything yet today
            binding.tvResolvedToday.setVisibility(View.GONE);
        }

        // 3. Handle the High Priority Alert Banner
        if (stats.highPriority > 0) {
            // Show the red banner and update the number
            binding.cardHighPriority.setVisibility(View.VISIBLE);
            binding.tvHighPriorityCount.setText(String.valueOf(stats.highPriority));
        } else {
            // Hide the red banner completely when there are no emergencies
            binding.cardHighPriority.setVisibility(View.GONE);
        }
    }

    private void setupUI() {
        setUsernameAndDate();

        // 1. Initialize Adapter with an empty list initially
        adapterTech = new ComplaintRVAdapterTech(new ArrayList<>(), requireContext());

        binding.rvAssignedComplaints.addItemDecoration(new RVItemDivider(requireContext()));
        binding.rvAssignedComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAssignedComplaints.setAdapter(adapterTech);
    }

    private void setupDataObserver() {
        viewModel.getAssignedComplaints().observe(getViewLifecycleOwner(), complaints -> {
            if (complaints != null) {
                // 4. Update existing adapter instead of creating a new one
                // Wrap in ArrayList to avoid the casting crash!
                adapterTech.updateData(new ArrayList<>(complaints));

                if (complaints.isEmpty()) {
                    binding.rvAssignedComplaints.setVisibility(View.GONE);
                    // binding.tvEmptyMessage.setVisibility(View.VISIBLE);
                } else {
                    binding.rvAssignedComplaints.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setUsernameAndDate(){
        String username = "Logged in as, " + SessionManager.getInstance(requireContext()).getUsername();
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM", Locale.getDefault());

        binding.tvTecUsername.setText(username);
        binding.tvDate.setText(sdf.format(System.currentTimeMillis()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}