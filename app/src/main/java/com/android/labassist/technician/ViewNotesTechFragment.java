package com.android.labassist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.labassist.databinding.FragmentViewNotesTechBinding;
import com.android.labassist.technician.TechComplaint;

public class ViewNotesTechFragment extends Fragment {
    TechComplaint techComplaint;
    FragmentViewNotesTechBinding binding;
    public static final String KEY_COMPLAINT_ID = "complaintID";
    private String complaint_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.complaint_id = getArguments().getString(KEY_COMPLAINT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_notes_tech, container, false);

        binding = FragmentViewNotesTechBinding.bind(view);

//        binding.toolbarViewNotes.setNavigationOnClickListener(v -> {
//            NavHostFragment.findNavController(ViewNotesTechFragment.this).navigateUp();
//        });


        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}