package com.android.labassist.technician;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.FragmentTechnicianDashboardBinding;

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