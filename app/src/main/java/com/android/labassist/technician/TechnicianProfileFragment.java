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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            NavHostFragment.findNavController(TechnicianProfileFragment.this)
                    .navigate(R.id.action_user_profile_to_settings);
        });

        binding.btnLogout.setOnClickListener(v -> {
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
//            Todo: retrofit call to clear fcm_token from supabase
        });

        return inflater.inflate(R.layout.fragment_technician_profile, container, false);
    }


}