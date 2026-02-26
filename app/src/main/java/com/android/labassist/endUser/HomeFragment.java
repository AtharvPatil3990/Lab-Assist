package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.FragmentUserHomeBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HomeFragment extends Fragment {

    FragmentUserHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserHomeBinding.bind(inflater.inflate(R.layout.fragment_user_home, container, false));

// 1. Initialize empty data and adapter
        ArrayList<Map<String, String>> data = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                data,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "subtitle"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        binding.lvRecentTask.setAdapter(adapter);

        // 2. Connect to the Shared ViewModel
        ComplaintsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ComplaintsViewModel.class);

        // 3. Observe the Room Database
        viewModel.getAllComplaints().observe(getViewLifecycleOwner(), complaints -> {
            data.clear(); // Clear the old hardcoded or cached data

            if (complaints != null && !complaints.isEmpty()) {
                // Only show the 3 most recent complaints on the dashboard
                int limit = Math.min(complaints.size(), 3);

                for (int i = 0; i < limit; i++) {
                    ComplaintEntity entity = complaints.get(i);

                    Map<String, String> datum = new HashMap<>();
                    datum.put("title", entity.title);
                    // Combine Lab and Device for the subtitle, matching your hardcoded style!
                    datum.put("subtitle", entity.labName + " - " + entity.deviceName);

                    data.add(datum);
                }
            }

            // Tell the ListView to redraw itself with the new data
            adapter.notifyDataSetChanged();
        });
        displayNameDate();

        viewModel.getTotalCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvTotalCompCount.setText(String.valueOf(count));
            }
        });

        viewModel.getPendingCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvPendingCompCount.setText(String.valueOf(count));
            }
        });

        viewModel.getResolvedCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.tvResolvedCompCount.setText(String.valueOf(count));
            }
        });

        binding.btnReportIssue.setOnClickListener(v -> {
            NavHostFragment.
                    findNavController(HomeFragment.this).
                    navigate(R.id.action_navigation_home_to_navigation_raise_complaint);
        });

        return binding.getRoot();
    }

    private void displayNameDate(){
        String userName = SessionManager.getInstance(requireContext()).getUsername();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        String date = sdf.format(calendar.getTime());

        binding.tvDateHome.setText(date);
        binding.tvWelcomeText.setText("Welcome, " + userName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}