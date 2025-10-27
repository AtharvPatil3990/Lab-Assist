package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MyComplaintsFragment extends Fragment {
    ArrayList<Complaint> complaintArrList;
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
        View view = inflater.inflate(R.layout.fragment_my_complaints, container, false);

        rvComplaints = view.findViewById(R.id.rvComplaints);
        etSearchComplaint = view.findViewById(R.id.etSearchComplaint);

        rvComplaints.setLayoutManager(new LinearLayoutManager(requireContext()));

        complaintArrList = new ArrayList<>();
        complaintArrList.add(new Complaint("Lab 2", "Pc 14", "Monitor not working", "resolved", "12/10/2025"));
        complaintArrList.add(new Complaint("Lab 1", "Pc 3", "Software crash", "pending", "10/10/2025"));
        complaintArrList.add(new Complaint("Lab 3", "Pc 8", "Network issue", "resolved", "23/10/2025"));
        ComplaintRecyclerViewAdapter adapter = new ComplaintRecyclerViewAdapter(requireContext(), complaintArrList);
        rvComplaints.setAdapter(adapter);

        return view;
    }
}