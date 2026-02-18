package com.android.labassist.endUser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.labassist.R;
import com.android.labassist.SessionManager;
import com.android.labassist.databinding.FragmentUserHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        String[] titles = {"Monitor not working", "Software crash", "Network issue"};
        String[] subtitles = {"Lab 2 - PC 14", "Lab 1 - PC 3", "Lab 3 - PC 8"};

        ArrayList<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            Map<String, String> datum = new HashMap<>();
            datum.put("title", titles[i]);
            datum.put("subtitle", subtitles[i]);
            data.add(datum);
        }
        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                data,
                android.R.layout.simple_list_item_2, // 👈 two-line layout
                new String[]{"title", "subtitle"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        binding.lvRecentTask.setAdapter(adapter);
        displayNameDate();
        return binding.getRoot();
    }

    private void displayNameDate(){
        String userName = SessionManager.getUsername(requireContext());
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        String date = sdf.format(calendar.getTime());

        binding.tvDateHome.setText(date);
        binding.tvWelcomeText.setText("Welcome, " + userName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}