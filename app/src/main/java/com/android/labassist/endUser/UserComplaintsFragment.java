package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class UserComplaintsFragment extends Fragment {
    ArrayList<UserComplaint> userComplaintArrList;
    RecyclerView rvComplaints;
    TextInputEditText etSearchComplaint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_complaints, container, false);

        rvComplaints = view.findViewById(R.id.rvComplaints);
        etSearchComplaint = view.findViewById(R.id.etSearchComplaint);

        rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));

        userComplaintArrList = new ArrayList<>();
        userComplaintArrList.add(new UserComplaint("Lab 2", "Pc 14", "Monitor not working", ComplaintStatus.Resolved, System.currentTimeMillis()));
        userComplaintArrList.add(new UserComplaint("Lab 1", "Pc 3", "Software crash", ComplaintStatus.Pending, System.currentTimeMillis()));
        userComplaintArrList.add(new UserComplaint("Lab 3", "Pc 8", "Network issue", ComplaintStatus.Ongoing, System.currentTimeMillis()));
        ComplaintRecyclerViewAdapter adapter = new ComplaintRecyclerViewAdapter(requireContext(), userComplaintArrList);
        rvComplaints.setAdapter(adapter);

        return view;
    }
}