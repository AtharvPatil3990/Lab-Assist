package com.android.labassist.technician;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentAssignedLabsBinding;
// Import your database class to get the DAO
import com.android.labassist.database.AppDatabase;

public class AssignedLabsFragment extends Fragment {

    private FragmentAssignedLabsBinding binding;
    // Initialize the adapter with the click listener
// When clicked, navigate to the Lab Detail Hub and pass the labId
    private LabRVAdapter adapter = new LabRVAdapter((labId, labName) -> navigateToLabDetails(labId, labName));
    private AssignedLabsViewModel viewModel;
    private boolean isAdmin;
    private String department_id;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAssignedLabsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null)
            department_id = savedInstanceState.getString("DEPARTMENT_ID", "");

        isAdmin = SessionManager.getInstance(requireContext()).getRole().equals(SessionManager.ROLE_ADMIN);

        setupRecyclerView();
        setupViewModel();

        setupUI();
    }

    private void setupUI(){
        if(isAdmin)
            binding.toolbar.setTitle("Department Labs");
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
        viewModel.init(db.labAssistDao(), isAdmin, department_id != null ? department_id : "");

        // 3. Observe the database!
        viewModel.getAssignedLabs().observe(getViewLifecycleOwner(), labs -> {
            if (labs != null) {
                // Update the RecyclerView instantly
                adapter.setLabs(labs);

                // Show the empty state if the technician has no labs
                toggleEmptyState(labs.isEmpty());
            }
        });
    }

    private void navigateToLabDetails(String labId, String labName) {
        // TODO: We will write the Navigation Component code here next!
        // It will look something like this:
        Bundle args = new Bundle();
        args.putString("LAB_ID", labId);
        args.putString("LAB_NAME", labName);
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