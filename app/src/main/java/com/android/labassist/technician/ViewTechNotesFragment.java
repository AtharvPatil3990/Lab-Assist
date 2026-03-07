package com.android.labassist.technician;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.database.entities.DeviceEntity;
import com.android.labassist.database.entities.LabEntity;
import com.android.labassist.databinding.FragmentViewTechNotesBinding;
import com.android.labassist.repositories.NoteRepository;

public class ViewTechNotesFragment extends Fragment {

    private FragmentViewTechNotesBinding binding;
    private NotesViewModel viewModel;
    private NotesRVAdapter adapter;

    // IDs passed from the previous screen
    private String complaintId;
    private String deviceId;
    private String labId;

    // Search Debounce variables
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private int action;
    public static int actionDeviceNotes = 1;
    public static int actionLabNotes = 0;
    private String targetName;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentViewTechNotesBinding.bind(inflater.inflate(R.layout.fragment_view_tech_notes, container, false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Extract the IDs sent from the Bottom Sheet
        if (getArguments() != null) {
            complaintId = getArguments().getString("COMPLAINT_ID");
            deviceId = getArguments().getString("DEVICE_ID");
            labId = getArguments().getString("LAB_ID");
            action = getArguments().getInt("action", actionDeviceNotes);
            targetName = getArguments().getString("target_name", "");
        }

        binding.toolbarNotes.setNavigationOnClickListener(v -> NavHostFragment.findNavController(requireParentFragment()).navigateUp());

        setupUI();
        setupViewModel();
        setupSearchDebounce();
        setupSendButton();
    }

    private void setupUI() {
        // Dynamically change the toolbar title based on what we are looking at
//        if(deviceId != null && labId != null)
//            viewModel.loadHeaderTitle(deviceId, labId);

        adapter = new NotesRVAdapter(requireContext());
        binding.rvNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotes.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        viewModel.init(new NoteRepository(requireContext()), action);

        // 1. Ask the ViewModel to load the correct data from the database
        viewModel.loadNotes(deviceId, labId).observe(getViewLifecycleOwner(), dbNotes -> {
            // Send fresh DB data to the ViewModel to be filtered
            viewModel.updateMasterListAndFilter(dbNotes);
        });

        // 2. Observe the final filtered list to display on the screen
        viewModel.getDisplayNotes().observe(getViewLifecycleOwner(), displayNotes -> {
            if (displayNotes == null || displayNotes.isEmpty()) {
                binding.rvNotes.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.rvNotes.setVisibility(View.VISIBLE);
                adapter.setNotes(displayNotes);

                // Keep the list scrolled to the top where the newest notes appear
                binding.rvNotes.scrollToPosition(0);
            }
        });
    }

    private void setupSearchDebounce() {
        binding.etSearchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> viewModel.applySearchFilter(s.toString());
                searchHandler.postDelayed(searchRunnable, 300); // 300ms buffer
            }
        });
    }

    private void setupSendButton() {
        binding.btnSendNote.setOnClickListener(v -> {
            String noteText = binding.etNoteContent.getText().toString().trim();
            boolean isInternal = binding.cbInternalNote.isChecked();

            if (noteText.isEmpty()) {
                binding.tilNoteInput.setError("Note cannot be empty");
                return;
            }

            if (complaintId == null || complaintId.isEmpty()) {
                Toast.makeText(requireContext(), "Error: Missing Complaint ID", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.tilNoteInput.setError(null);

            // Tell the ViewModel to create the note
            viewModel.createNewNote(complaintId, deviceId, noteText, isInternal);

            // Instantly clear the UI so they can type another note
            binding.etNoteContent.setText("");
            binding.cbInternalNote.setChecked(false);

            Toast.makeText(requireContext(), "Sending note...", Toast.LENGTH_SHORT).show();

            // Note: Once the Repository finishes the API call, it will insert the note into Room.
            // Room will automatically trigger the LiveData, and the list will update on its own!
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null); // Prevent memory leaks from the timer
        binding = null;
    }
}