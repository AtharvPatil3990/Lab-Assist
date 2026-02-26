package com.android.labassist.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentAdminDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // 1. Set up the static UI (Greeting and Date)
        setupHeader();

        // 2. Setup Observers
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefreshAdmin.setRefreshing(isLoading);
        });

        viewModel.getTicketStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                int pendingCount = stats.open + stats.queued;
                binding.tvCountPending.setText(String.valueOf(pendingCount));
                binding.tvCountOngoing.setText(String.valueOf(stats.inProgress));
                binding.tvCountResolved.setText(String.valueOf(stats.resolved));
                binding.tvCountTotal.setText(String.valueOf(stats.total));
            }
        });

        viewModel.getPerformanceStats().observe(getViewLifecycleOwner(), perf -> {
            if (perf != null) {
                binding.tvAvgResolution.setText(String.format(Locale.getDefault(), "%.1f hrs", perf.avgResolutionHours));
                binding.tvTotalDowntime.setText(String.format(Locale.getDefault(), "%.1f hrs", perf.totalDowntimeHours));
            }
        });

        viewModel.getLabsStats().observe(getViewLifecycleOwner(), labs -> {
            if (labs != null) {
                binding.tvTotalLabs.setText(String.valueOf(labs.total));
                binding.tvActiveLabs.setText(String.valueOf(labs.active));
                binding.tvMaintenanceLabs.setText(String.valueOf(labs.maintenance));
            }
        });

        // 3. Setup Pull-to-Refresh
        binding.swipeRefreshAdmin.setOnRefreshListener(() -> viewModel.fetchStatistics());

        // 4. Initial Fetch
        viewModel.fetchStatistics();
    }

    private void setupHeader() {
        SessionManager session = SessionManager.getInstance(requireContext());
        binding.tvAdminGreeting.setText("Welcome, " + session.getUsername());

        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM", Locale.getDefault());
        binding.tvAdminDate.setText(sdf.format(System.currentTimeMillis()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}