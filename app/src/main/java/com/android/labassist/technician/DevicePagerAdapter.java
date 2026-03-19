package com.android.labassist.technician;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DevicePagerAdapter extends FragmentStateAdapter {

    private final String deviceId;


    public DevicePagerAdapter(@NonNull Fragment fragment, String deviceId, String labId) {
        super(fragment);
        this.deviceId = deviceId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // TAB 1: DEVICE COMPLAINTS
                // Pass null for labId, the valid deviceId, and the Device Action flag
                return TechComplaintsFragment.newInstance(
                        null,
                        deviceId,
                        TechComplaintsViewModel.ACTION_DEVICE_COMPLAINTS
                );

            case 1:
                // TAB 2: DEVICE NOTES
                // Pass null for labId, the valid deviceId, and the Device Notes flag
                return ViewTechNotesFragment.newInstance(deviceId, null, ViewTechNotesFragment.actionDeviceNotes, false);

            default:
                // Safe fallback in case of an unexpected index
                Log.e("DevicePagerAdapter", "Invalid position: " + position);
                return TechComplaintsFragment.newInstance(null, deviceId, TechComplaintsViewModel.ACTION_DEVICE_COMPLAINTS);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Kept perfectly lean with just Complaints and Notes!
    }
}