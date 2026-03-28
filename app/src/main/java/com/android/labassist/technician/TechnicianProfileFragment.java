package com.android.labassist.technician;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.LoginActivity;
import com.android.labassist.R;
import com.android.labassist.auth.AuthEventBus;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.auth.TokenManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.databinding.FragmentTechnicianProfileBinding;
import com.android.labassist.endUser.ProfileFragment;
import com.android.labassist.endUser.ProfileViewModel;
import com.android.labassist.network.ApiController;
import com.android.labassist.network.models.UpdateFcmTokenRequest;
import com.android.labassist.network.models.UpdateFcmTokenResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                    .navigate(R.id.action_tech_profile_to_settings);
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

        viewModel.getOrgCode().observe(getViewLifecycleOwner(), orgCode -> {
            if(orgCode != null) binding.tvOrgCode.setText(orgCode);
        });

        viewModel.getRegId().observe(getViewLifecycleOwner(), regId -> {
            if (regId != null) binding.tvRegID.setText(regId);
        });

        viewModel.getDepartment().observe(getViewLifecycleOwner(), department -> {
            if (department != null) binding.tvDepartment.setText(department);
        });

        viewModel.loadUserProfile();

        // 3. Setup Click Listeners
        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(TechnicianProfileFragment.this)
                    .navigate(R.id.action_tech_profile_to_settings);
        });

        binding.btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance(requireContext()).logout();
            TokenManager.getInstance(requireContext()).clearTokens();

            // Run database clearing on a managed background thread
             Executors.newSingleThreadExecutor()
                    .execute(() -> AppDatabase.getInstance(requireContext()).clearAllTables());

            // Trigger the logout event to send the user back to the Login screen
            AuthEventBus.getInstance().triggerLogout();

            ApiController.getInstance(requireContext()).getAuthApi()
                    .updateFcmToken(new UpdateFcmTokenRequest("removed"))
                    .enqueue(new Callback<UpdateFcmTokenResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<UpdateFcmTokenResponse> call, @NonNull Response<UpdateFcmTokenResponse> response) {
                            if(response.isSuccessful()){
                                Log.d("FCMToken", "Response success code: " + response.code());
                            }
                            else
                                Log.d("FCMToken", "Response success code: " + response.message());
                        }

                        @Override
                        public void onFailure(@NonNull Call<UpdateFcmTokenResponse> call, @NonNull Throwable t) {
                            Log.d("FCMToken", "onFailure msg: " + t.getMessage());
                        }
                    });
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}