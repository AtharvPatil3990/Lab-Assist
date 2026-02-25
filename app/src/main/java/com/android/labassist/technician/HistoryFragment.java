package com.android.labassist.technician;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.databinding.FragmentTechnicianHistoryBinding;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {

    private FragmentTechnicianHistoryBinding binding;
    private ComplaintHistoryViewModel viewModel;
    private ComplaintRVAdapterTech adapterTech;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Initialize Binding correctly
        binding = FragmentTechnicianHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ComplaintHistoryViewModel.class);

        // 3. Setup RecyclerView
        setupRecyclerView();

        // 4. Setup Observers
        setupDataObservers();

        // 5. Setup Search logic
        setupSearch();

        // 6. Initial Sync
        viewModel.refreshHistory();
    }

    private void setupRecyclerView() {
        adapterTech = new ComplaintRVAdapterTech(new ArrayList<>(), requireContext());
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapterTech);
    }

    private void setupDataObservers() {
        viewModel.getFullHistory().observe(getViewLifecycleOwner(), complaints -> {
            if (complaints != null) {
                adapterTech.updateData(new ArrayList<>(complaints));
                updateEmptyState(complaints.isEmpty());
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    if (adapterTech != null) {
                        adapterTech.filter(s.toString());
                        // If searching, we show/hide results differently
                        boolean noResults = adapterTech.getItemCount() == 0;
                        toggleNoResultsView(noResults, s.toString().isEmpty());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvNoHistoryMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void toggleNoResultsView(boolean noResults, boolean queryIsEmpty) {
        if (queryIsEmpty) {
            // If the user cleared the search, revert to standard empty state logic
            updateEmptyState(adapterTech.getItemCount() == 0);
        } else {
            // If they are searching and found nothing, show search-specific message
            binding.rvHistory.setVisibility(noResults ? View.GONE : View.VISIBLE);
            binding.tvNoHistoryMessage.setVisibility(noResults ? View.VISIBLE : View.GONE);
            if (noResults) binding.tvNoHistoryMessage.setText("No complaints match your search.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}