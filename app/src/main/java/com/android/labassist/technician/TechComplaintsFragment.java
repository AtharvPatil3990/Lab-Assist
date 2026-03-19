package com.android.labassist.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.databinding.FragmentTechComplaintsBinding;
import com.android.labassist.database.AppDatabase;

public class TechComplaintsFragment extends Fragment {

    private FragmentTechComplaintsBinding binding;
    private TechComplaintsViewModel viewModel;
     private ComplaintAdapter adapter; // Assuming you have an adapter for complaints

    private String currentLabId;
    private String currentDeviceId;
    private int currentActionType;

    // --- THE FACTORY METHOD ---
    public static TechComplaintsFragment newInstance(String labId, String deviceId, int actionType) {
        TechComplaintsFragment fragment = new TechComplaintsFragment();
        Bundle args = new Bundle();
        args.putString("LAB_ID", labId);
        args.putString("DEVICE_ID", deviceId);
        args.putInt("ACTION_TYPE", actionType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTechComplaintsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Unpack the backpack (Arguments)
        if (getArguments() != null) {
            currentLabId = getArguments().getString("LAB_ID", "");
            currentDeviceId = getArguments().getString("DEVICE_ID", "");
            currentActionType = getArguments().getInt("ACTION_TYPE");
        }

        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
         adapter = new ComplaintAdapter(complaintId -> {
             BottomSheetComplaintTech complaintDetail = BottomSheetComplaintTech.newInstance(complaintId.id);
             complaintDetail.show(getParentFragmentManager(), complaintDetail.getTag());
         });
         binding.rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
         binding.rvComplaints.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TechComplaintsViewModel.class);
        AppDatabase db = AppDatabase.getInstance(requireContext());

        // Pass everything to the ViewModel so it can route the traffic!
        viewModel.init(db.labAssistDao(), currentLabId, currentDeviceId, currentActionType);

        viewModel.getComplaints().observe(getViewLifecycleOwner(), complaints -> {
            if (complaints != null) {
                adapter.setComplaints(complaints);
                toggleEmptyState(complaints.isEmpty());
            }
        });
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.rvComplaints.setVisibility(View.GONE);
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.rvComplaints.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}