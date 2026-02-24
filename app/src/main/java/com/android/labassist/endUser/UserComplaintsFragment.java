package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.databinding.FragmentUserComplaintsBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class UserComplaintsFragment extends Fragment {

    private final ArrayList<UserComplaint> masterList = new ArrayList<>();
    FragmentUserComplaintsBinding binding;
    private ComplaintRecyclerViewAdapter adapter;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserComplaintsBinding.bind(inflater.inflate(R.layout.fragment_user_complaints, container, false));

        binding.rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ComplaintRecyclerViewAdapter(requireContext(), new ArrayList<>());
        binding.rvComplaints.setAdapter(adapter);

        handler = new Handler(Looper.getMainLooper());

        setupSearchFeature();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ComplaintsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ComplaintsViewModel.class);
        viewModel.getComplaintsForUi().observe(getViewLifecycleOwner(), userComplaints -> {
            masterList.clear();
            if (userComplaints != null) {
                masterList.addAll(userComplaints); // Save to master list
            }
            // Apply the search filter immediately (in case they are currently searching while data updates)
            filterList(binding.etSearchComplaint.getText() != null ? binding.etSearchComplaint.getText().toString() : "");
        });
    }
    private void setupSearchFeature() {
        // 2. Set your buffer time (300ms is the industry standard for search bars)
        final long DELAY = 300;

        binding.etSearchComplaint.addTextChangedListener(new TextWatcher() {
            // This Runnable holds the instruction to filter the list
            Runnable searchRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // We leave this empty and do the work in afterTextChanged
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 3. If the user types another letter before 300ms is up, cancel the old search!
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                // 4. Define the new search action with the current text
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        filterList(s.toString());
                    }
                };

                // 5. Post the action to run after the 300ms delay
                handler.postDelayed(searchRunnable, DELAY);
            }
        });
    }

    private void filterList(String query) {
        ArrayList<UserComplaint> filteredList = new ArrayList<>();

        for (UserComplaint complaint : masterList) {
            // Search by Issue (Title) or PC name, ignoring uppercase/lowercase
            if (complaint.getIssue().toLowerCase().contains(query.toLowerCase()) ||
                    complaint.getPc().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(complaint);
            }
        }
        adapter.updateList(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        handler.removeCallbacksAndMessages(null);
    }
}