package com.android.labassist.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.labassist.R;
import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentAdminProfileBinding;

import java.util.concurrent.Executors;

public class AdminProfileFragment extends Fragment {

    private FragmentAdminProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdminProfileViewModel viewModel = new ViewModelProvider(this).get(AdminProfileViewModel.class);

        // --- OBSERVE DATA ---
        viewModel.getName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) binding.tvUserName.setText(name);
        });

        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) binding.tvUserEmail.setText(email);
        });

        viewModel.getInstituteName().observe(getViewLifecycleOwner(), institute -> {
            if (institute != null) binding.tvInstituteName.setText(institute);
        });

        // --- THE VISIBILITY LOGIC ---
        viewModel.getDepartment().observe(getViewLifecycleOwner(), dept -> {
            // Check if department is valid and not an organization-wide setting
            boolean isDepartmentLevel = dept != null && !dept.trim().isEmpty() && !dept.equalsIgnoreCase("All");

            if (isDepartmentLevel) {
                // Show everything
                binding.borderAboveDepartment.setVisibility(View.VISIBLE);
                binding.tvDepartmentLabel.setVisibility(View.VISIBLE);
                binding.tvDepartment.setVisibility(View.VISIBLE);

                binding.tvDepartment.setText(dept);
            } else {
                // Hide the border, the label "Department", and the value
                binding.borderAboveDepartment.setVisibility(View.GONE);
                binding.tvDepartmentLabel.setVisibility(View.GONE);
                binding.tvDepartment.setVisibility(View.GONE);
            }
        });

        // Force ViewModel to load data
        viewModel.loadProfileData();

        // --- CLICK LISTENERS ---
        binding.btnSettings.setOnClickListener(v -> {
            // Assuming you have an action in your nav_graph
            // NavHostFragment.findNavController(this).navigate(R.id.action_adminProfile_to_settings);
            Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
        });

        binding.tvHelpOption.setOnClickListener(v -> Toast.makeText(requireContext(), "Help clicked", Toast.LENGTH_SHORT).show());
        binding.tvFAQ.setOnClickListener(v -> Toast.makeText(requireContext(), "FAQ clicked", Toast.LENGTH_SHORT).show());
        binding.tvAbout.setOnClickListener(v -> Toast.makeText(requireContext(), "About clicked", Toast.LENGTH_SHORT).show());

        binding.btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(requireContext()).logout();

            // Clear local database safely
            Executors.newSingleThreadExecutor().execute(() -> {
                 AppDatabase db = AppDatabase.getInstance(requireContext());
                 db.labAssistDao().clearLabs();
                 db.labAssistDao().clearComplaints();
            });

            AuthEventBus.getInstance().triggerLogout();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}