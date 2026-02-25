package com.android.labassist.technician;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentTechnicianProfileBinding;
import com.android.labassist.endUser.ProfileFragment;
import com.android.labassist.endUser.ProfileViewModel;

public class TechnicianProfileFragment extends Fragment {

    FragmentTechnicianProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTechnicianProfileBinding.bind(inflater.inflate(R.layout.fragment_technician_profile, container, false));


        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(TechnicianProfileFragment.this)
                    .navigate(R.id.action_user_profile_to_settings);
        });

        // 1. Initialize the ViewModel
        TechnicianProfileViewModel viewModel = new ViewModelProvider(this).get(TechnicianProfileViewModel.class);

        viewModel.getTechProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.tvUserName.setText(profile.name);
                binding.tvUserEmail.setText(profile.email);
                binding.tvInstituteName.setText(profile.institute);
                binding.tvOrgCode.setText(profile.orgCode);
                binding.tvRegID.setText(profile.empId);
                binding.tvDepartment.setText(profile.department);
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(requireContext()).logout();
            AppDatabase.getInstance(requireContext()).labAssistDao().clearLabs();
            AppDatabase.getInstance(requireContext()).labAssistDao().clearComplaints();
            AppDatabase.getInstance(requireContext()).labAssistDao().clearDevices();
            AuthEventBus.getInstance().triggerLogout();

//            Todo: retrofit call to clear fcm_token from supabase
        });

        return inflater.inflate(R.layout.fragment_technician_profile, container, false);
    }


}