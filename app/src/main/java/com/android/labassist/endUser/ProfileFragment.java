package com.android.labassist.endUser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.labassist.R;
import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentUserProfileBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Standard ViewBinding inflation
//        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize the ViewModel
        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Observe each piece of data individually and bind it to the UI
        viewModel.getName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) binding.tvUserName.setText(name);
        });

        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) binding.tvUserEmail.setText(email);
        });

        viewModel.getInstitute().observe(getViewLifecycleOwner(), institute -> {
            if (institute != null) binding.tvInstituteName.setText(institute);
        });

        viewModel.getRegId().observe(getViewLifecycleOwner(), regId -> {
            if (regId != null) binding.tvRegID.setText(regId);
        });

        viewModel.getDepartment().observe(getViewLifecycleOwner(), department -> {
            if (department != null) binding.tvDepartment.setText(department);
        });

        // 3. Setup Click Listeners
        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(ProfileFragment.this)
                    .navigate(R.id.action_user_profile_to_settings);
        });

        viewModel.loadUserProfile();

        binding.btnLogout.setOnClickListener(v -> {
            // Clear session data
            SessionManager.getInstance(requireContext()).logout();

            // Run database clearing on a managed background thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(requireContext());
                 db.labAssistDao().clearLabs();
                 db.labAssistDao().clearComplaints();
                 db.labAssistDao().clearDevices();
            });

            // Trigger the logout event to send the user back to the Login screen
            AuthEventBus.getInstance().triggerLogout();

            // Todo: retrofit call to clear fcm_token from supabase
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}