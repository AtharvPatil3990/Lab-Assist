package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.android.labassist.databinding.FragmentUserProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);

        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_user_profile_to_settings);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize the ViewModel
        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Observe the data and map it to the UI
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.tvUserName.setText(profile.name);
                binding.tvUserEmail.setText(profile.email);
                binding.tvInstituteName.setText(profile.institute);
                binding.tvOrgCode.setText(profile.orgCode);
                binding.tvRegID.setText(profile.regId);
                binding.tvDepartment.setText(profile.department);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}