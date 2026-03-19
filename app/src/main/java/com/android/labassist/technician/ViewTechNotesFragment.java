package com.android.labassist.technician;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.database.AppDatabase;
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
    boolean showTitleBar = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentViewTechNotesBinding.bind(inflater.inflate(R.layout.fragment_view_tech_notes, container, false));
        return binding.getRoot();
    }

    public static ViewTechNotesFragment newInstance(String complaintId, String deviceId, String labId, int action, boolean showTitleBar){
        ViewTechNotesFragment fragment = new ViewTechNotesFragment();
        Bundle args = new Bundle();

        args.putString("COMPLAINT_ID", complaintId);
        args.putString("DEVICE_ID", deviceId);
        args.putString("LAB_ID", labId);
        args.putInt("action", action);
        args.putBoolean("SHOW_TITLE_BAR", showTitleBar);
        fragment.setArguments(args);

        return fragment;
    }
    public static ViewTechNotesFragment newInstance(String deviceId, String labId, int action, boolean showTitleBar){
        ViewTechNotesFragment fragment = new ViewTechNotesFragment();
        Bundle args = new Bundle();
        args.putString("DEVICE_ID", deviceId);
        args.putString("LAB_ID", labId);
        args.putInt("action", action);
        args.putBoolean("SHOW_TITLE_BAR", showTitleBar);
        fragment.setArguments(args);

        return fragment;
    }

    public static ViewTechNotesFragment newInstance(String labId, int action, boolean showTitleBar){
        ViewTechNotesFragment fragment = new ViewTechNotesFragment();
        Bundle args = new Bundle();

        args.putString("LAB_ID", labId);
        args.putInt("action", action);
        args.putBoolean("SHOW_TITLE_BAR", showTitleBar);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Extract the IDs sent from the Bottom Sheet
        if (getArguments() != null) {
            complaintId = getArguments().getString("COMPLAINT_ID", "");
            deviceId = getArguments().getString("DEVICE_ID", "");
            labId = getArguments().getString("LAB_ID", "");
            action = getArguments().getInt("action", actionDeviceNotes);
            showTitleBar = getArguments().getBoolean("SHOW_TITLE_BAR", true);
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

        if(!showTitleBar)
            binding.toolbarNotes.setVisibility(View.GONE);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        viewModel.init(new NoteRepository(requireContext()), action);
        viewModel.initArchitectureRepos(AppDatabase.getInstance(requireContext()).labAssistDao());

        viewModel.getToolbarTitle().observe(getViewLifecycleOwner(), title ->{
            binding.toolbarNotes.setSubtitle(title);
        });

        if((deviceId != null && !deviceId.isEmpty()) || (labId != null && !labId.isEmpty())) {
            viewModel.loadHeaderTitle(deviceId, labId);
        }
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

        viewModel.getNoteCreationStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            if (status.equals("LOADING")) {
                // 1. Dim screen and show the floating card
                binding.loadingOverlay.setVisibility(View.VISIBLE);

            } else if (status.equals("SUCCESS")) {
                // 2. Hide the card
                binding.loadingOverlay.setVisibility(View.GONE);

                // 3. Clear the text field for the next note
                binding.etNoteContent.setText("");

                // 4. Give the user a success message
                Toast.makeText(requireContext(), "Note added successfully!", Toast.LENGTH_SHORT).show();

            } else if (status.startsWith("ERROR")) {
                // 5. Hide the card
                binding.loadingOverlay.setVisibility(View.GONE);

                // 6. Show the exact error message that came from the ViewModel/Backend
                Toast.makeText(requireContext(), status, Toast.LENGTH_LONG).show();
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
            String text = binding.etNoteContent.getText().toString().trim();
            boolean isInternal = binding.cbInternalNote.isChecked();

            if (text.isEmpty()) {
                binding.etNoteContent.setError("Note cannot be empty");
                return;
            }

            // Hide the software keyboard so the loading overlay is clearly visible
            hideKeyboard();

            // Trigger the API call!
            viewModel.createNewNote(requireContext(), complaintId, deviceId, labId, text, isInternal);
        });
    }
    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchHandler.removeCallbacksAndMessages(null); // Prevent memory leaks from the timer
        binding = null;
    }
}