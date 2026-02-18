package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.android.labassist.UserInfo;
import com.android.labassist.databinding.FragmentUserProfileBinding;

public class ProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentUserProfileBinding.bind(inflater.inflate(R.layout.fragment_user_profile, container, false));

        if(UserInfo.isUserInfoSaved()) {
            binding.tvUserEmail.setText(UserInfo.getEmail());
            binding.tvUserName.setText(UserInfo.getUserName());
            binding.tvInstituteName.setText(UserInfo.getInstituteName());
            binding.tvOrgCode.setText(UserInfo.getOrgCode());
            binding.tvRegID.setText(UserInfo.getRegistrationID());
            binding.tvDepartment.setText(UserInfo.getDepartment());
        }

        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_user_profile_to_settings);
        });


        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}