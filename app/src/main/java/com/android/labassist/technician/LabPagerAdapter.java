package com.android.labassist.technician;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LabPagerAdapter extends FragmentStateAdapter {

    private final String labId;

    public LabPagerAdapter(@NonNull Fragment fragment, String labId) {
        super(fragment);
        this.labId = labId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a bundle to pass the labId to the child fragments
        Bundle args = new Bundle();
        args.putString("LAB_ID", labId);

        switch (position) {
            case 0:
                TechLabDevicesFragment devicesFragment = new TechLabDevicesFragment();
                devicesFragment.setArguments(args);
                return devicesFragment;

            case 1:
                return TechComplaintsFragment.newInstance(labId, null, TechComplaintsViewModel.ACTION_LAB_COMPLAINTS);

            case 2:
                // This is the Notes Fragment we already built!
                ViewTechNotesFragment notesFragment = new ViewTechNotesFragment();
                args.putInt("action", ViewTechNotesFragment.actionLabNotes);
                args.putBoolean("SHOW_TITLE_BAR", false);
                notesFragment.setArguments(args);
                return notesFragment;

            default:
                Log.e("LabPagerAdapter", "Invalid position: " + position);
                args.putInt("action", ViewTechNotesFragment.actionLabNotes);
                ViewTechNotesFragment notes = new ViewTechNotesFragment();
                notes.setArguments(args);
                return notes;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // We have exactly 3 tabs
    }
}
