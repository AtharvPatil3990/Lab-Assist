package com.android.labassist.technician;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.databinding.FragmentTechnicianDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TaskDashboardFragment extends Fragment {

    FragmentTechnicianDashboardBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         binding = FragmentTechnicianDashboardBinding.bind(inflater.inflate(R.layout.fragment_technician_dashboard, container, false));

         binding.rvAssignedComplaints.addItemDecoration(new RVItemDivider(requireContext()));

         setUsernameAndDate();
         ArrayList<TechComplaint> arrComplaintsList = new ArrayList<>();
         arrComplaintsList.add(new TechComplaint(System.currentTimeMillis()-20000, "C245", "CO", "OS Crashed when opening blender", "PC-103", System.currentTimeMillis()-2000, ComplaintStatus.Ongoing, "OS Crash"));
         arrComplaintsList.add(new TechComplaint(System.currentTimeMillis()-10000, "C672", "IT", "RAM is not present in this pc", "PC-215", System.currentTimeMillis()-2000, ComplaintStatus.Resolved, "Hardware not present"));
         arrComplaintsList.add(new TechComplaint(System.currentTimeMillis()-50000, "C123", "ENTC", "Hardware is damaged", "PC-208", System.currentTimeMillis()-2000, ComplaintStatus.Cancelled, "Hardware Problem"));
         arrComplaintsList.add(new TechComplaint(System.currentTimeMillis()-80000, "C097", "CO", "There is no pc here. where is the pc?", "PC-201", System.currentTimeMillis()-2000, ComplaintStatus.Pending, "Hardware Problem"));
         arrComplaintsList.add(new TechComplaint(System.currentTimeMillis()-80000, "C097", "CO", "", "PC-201", System.currentTimeMillis()-2000, ComplaintStatus.Pending, "Hardware Problem"));

         ComplaintRVAdapterTech adapterTech = new ComplaintRVAdapterTech(arrComplaintsList, requireContext());
         binding.rvAssignedComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));
         binding.rvAssignedComplaints.setAdapter(adapterTech);

         return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setUsernameAndDate(){
        String username = "Logged in as, " + SessionManager.getInstance(requireContext()).getUsername();
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM", Locale.getDefault());

        binding.tvTecUsername.setText(username);
        binding.tvDate.setText(sdf.format(System.currentTimeMillis()));
    }

}