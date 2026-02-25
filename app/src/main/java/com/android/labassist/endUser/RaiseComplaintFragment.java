package com.android.labassist.endUser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.databinding.FragmentRaiseComplaintBinding;
import com.android.labassist.network.models.RaiseComplaintRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class RaiseComplaintFragment extends Fragment {

    private FragmentRaiseComplaintBinding binding;
    private RaiseComplaintViewModel viewModel;
    private SessionManager sessionManager;

    // Translation Maps: Human-readable name -> Database UUID
    private final HashMap<String, String> labMap = new HashMap<>();
    private final HashMap<String, String> deviceMap = new HashMap<>(); // ADDED: Device map

    // Hardcoded priorities as they don't change
    private final String[] priorities = {"Low", "Medium", "High", "Critical"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRaiseComplaintBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(RaiseComplaintViewModel.class);

        setupStaticDropdowns();
        setupObservers();

        binding.btnSubmitComplaint.setOnClickListener(v -> handleSubmission());
    }

    private void setupStaticDropdowns() {
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities);
        binding.tvPrioritySelection.setAdapter(priorityAdapter);
    }

    private void setupObservers() {
        // 1. Observe Labs from local Room Database
        viewModel.getAvailableLabs().observe(getViewLifecycleOwner(), labs -> {
            if (labs == null || labs.isEmpty()) {
                binding.layoutLab.setEnabled(false);
                binding.tvLabSelection.setText("Syncing labs...");
            } else {
                binding.layoutLab.setEnabled(true);
                binding.tvLabSelection.setText("");

                labMap.clear();
                ArrayList<String> labNames = new ArrayList<>();
                for (LabEntity lab : labs) {
                    labMap.put(lab.labName, lab.id);
                    labNames.add(lab.labName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, labNames);
                binding.tvLabSelection.setAdapter(adapter);
            }
        });

        // 2. Observe Devices (Triggered when a Lab is clicked)
        viewModel.getAvailableDevices().observe(getViewLifecycleOwner(), devices -> {
            if (devices == null || devices.isEmpty()) {
                binding.layoutPc.setEnabled(false);
                binding.tvPcSelection.setText(""); // Clear text
                binding.layoutPc.setHint("No devices in this lab"); // Update hint gracefully

                deviceMap.clear(); // 👈 CRITICAL: Clear old devices!
                binding.tvPcSelection.setAdapter(null); // Clear the old adapter options
            } else {
                binding.layoutPc.setEnabled(true);
                binding.layoutPc.setHint("Select PC (Optional)");
                binding.tvPcSelection.setText("");

                deviceMap.clear();
                ArrayList<String> pcNames = new ArrayList<>();
                for (DeviceEntity device : devices) {
                    deviceMap.put(device.deviceName, device.id);
                    pcNames.add(device.deviceName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, pcNames);
                binding.tvPcSelection.setAdapter(adapter);
            }
        });

        // 3. Handle Lab Selection Click to trigger Device fetch
        binding.tvLabSelection.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            String selectedUuid = labMap.get(selectedName);

            viewModel.selectLab(selectedUuid);

            binding.tvPcSelection.setText("");
        });

        // 4. Observe Network Submission Status
        viewModel.getSubmissionStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            if (status.equals("LOADING")) {
                hideKeyboardAndClearFocus();
                binding.loadingOverlay.setVisibility(View.VISIBLE);

            } else if (status.equals("SUCCESS")) {
                binding.loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Complaint raised successfully!", Toast.LENGTH_SHORT).show();
                viewModel.resetStatus();
                NavHostFragment.findNavController(this).navigateUp();

            } else if (status.startsWith("ERROR")) {
                binding.loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), status, Toast.LENGTH_LONG).show();
                viewModel.resetStatus();
            }
        });
    }

    private void handleSubmission() {
        String labName = binding.tvLabSelection.getText().toString().trim();
        String pcName = binding.tvPcSelection.getText().toString().trim(); // Might be empty!
        String priority = binding.tvPrioritySelection.getText().toString().trim();
        String title = binding.etIssueTitle.getText().toString().trim();
        String description = binding.etIssueDescription.getText().toString().trim();

        // REMOVED pcName from the strict empty check!
        if (labName.isEmpty() || priority.isEmpty() || title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String labId = labMap.get(labName);

        // SAFELY GET DEVICE ID: If pcName is empty, deviceId becomes null.
        // Otherwise, it grabs the UUID from the map.
        String deviceId = pcName.isEmpty() ? null : deviceMap.get(pcName);

        String studentId = sessionManager.getId();
        String orgId = sessionManager.getOrganisationId();

        if (studentId == null || orgId == null || labId == null) {
            Toast.makeText(requireContext(), "Session/Data error. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Conditionally combine the PC name into the description for legacy support
        String finalDescription = description;
        if (!pcName.isEmpty()) {
            finalDescription = "Device: " + pcName + "\n\n" + description;
        }

        // Pass all 7 parameters, including our potentially null deviceId!
        RaiseComplaintRequest request = new RaiseComplaintRequest(
                studentId, orgId, labId, deviceId, title, finalDescription, priority
        );

        viewModel.submitComplaint(request);
    }

    private void hideKeyboardAndClearFocus() {
        View currentFocus = requireActivity().getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}