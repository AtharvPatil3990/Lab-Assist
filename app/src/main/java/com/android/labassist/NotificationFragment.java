package com.android.labassist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.dao.LabAssistDao;
import com.android.labassist.databinding.FragmentNotificationBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private NotificationAdapter adapter;
    private LabAssistDao notificationDao;

    // Background thread for database updates
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FragmentNotificationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        notificationDao = AppDatabase.getInstance(requireContext()).labAssistDao();

        binding.toolbarNotifications.setNavigationOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigateUp();
        });

        setupRecyclerView();
        observeNotifications();

    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notification -> {
            // 1. Mark as read in the database (Must be on a background thread)
            if (!notification.isRead()) {
                executorService.execute(() -> notificationDao.markAsRead(notification.getId()));
            }

            // 2. Deep link directly to the complaint bottom sheet!
            if (notification.getComplaintId() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("complaint_id", notification.getComplaintId());

                NavController navController = Navigation.findNavController(requireView());
                try {
                    navController.navigate(R.id.action_global_to_complaintBottomSheet, bundle);
                } catch (IllegalArgumentException e) {
                    // Safe fallback if the action isn't accessible from this graph
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void observeNotifications() {
        // Room's LiveData automatically runs on a background thread and updates the UI!
        notificationDao.getAllNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.VISIBLE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setNotifications(notifications);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown(); // Clean up the background thread
        binding = null;
    }
}