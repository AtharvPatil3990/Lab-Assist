package com.android.labassist.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentDepartmentsBinding;

public class DepartmentsFragment extends Fragment {

    private FragmentDepartmentsBinding binding;
    private DepartmentsViewModel viewModel;
    private DepartmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDepartmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = SessionManager.getInstance(requireContext());

        // THE BYPASS (Unchanged)
        if (SessionManager.ADMIN_DEPT.equals(sessionManager.getAdminLevel())) {
            Bundle bundle = new Bundle();
            bundle.putString("department_id", sessionManager.getDepartmentID());
            NavOptions navOptions = new NavOptions.Builder().setPopUpTo(R.id.navigation_departments, true).build();
            Navigation.findNavController(view).navigate(R.id.action_admin_to_department_lab, bundle, navOptions);
            return;
        }

        // Standard Fragment-Scoped ViewModel!
        // Note: You will need a ViewModelFactory if your ViewModel requires the Repository in its constructor
        viewModel = new ViewModelProvider(this).get(DepartmentsViewModel.class);

        setupRecyclerView();

        // Observe Room Database directly!
        viewModel.getAllDepartments().observe(getViewLifecycleOwner(), departments -> {
            if (departments != null && !departments.isEmpty()) {
                adapter.setDepartments(departments);
                binding.rvDepartments.setVisibility(View.VISIBLE);
            } else {
                binding.rvDepartments.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new DepartmentAdapter(department -> {
            Bundle bundle = new Bundle();
            bundle.putString("department_id", department.getId()); // Using Entity getter
            bundle.putString("department_name", department.getName());

            Navigation.findNavController(requireView()).navigate(R.id.navigation_departments, bundle);
        });

        binding.rvDepartments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDepartments.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}