package com.android.labassist.technician;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.animation.ArgbEvaluator;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.BottomSheetComplaintTechBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BottomSheetComplaintTech extends BottomSheetDialogFragment {
    private BottomSheetComplaintTechBinding binding;
    private final ComplaintEntity techComplaint;
    private boolean isNotesOpen;

    public  BottomSheetComplaintTech(ComplaintEntity techComplaint){
        this.techComplaint = techComplaint;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = BottomSheetComplaintTechBinding.bind(inflater.inflate(R.layout.bottom_sheet_complaint_tech, container, false));
        binding.tvComplaintTitle.setText(techComplaint.getTitle());
        binding.tvLabLocation.setText(techComplaint.getLabName());
        binding.tvAppointedDate.setText(setDate(techComplaint.getCreatedAt()));
        String description = techComplaint.getDescription();
        if(description == null || description.isBlank()) {
            binding.tvProblemDescription.setText("No description set");
        }
        else {
            binding.tvProblemDescription.setText(techComplaint.getDescription());
        }

        setBottomButtonSelectedStatus(techComplaint.getStatus());

        binding.layoutButtonPendingState.setOnClickListener(v->{
            setBottomButtonStatePending();
            techComplaint.setStatus("Pending");
        });

        binding.layoutButtonOngoingState.setOnClickListener(v->{
            setBottomButtonStateOngoing();
            techComplaint.setStatus("Ongoing");
        });

        binding.layoutButtonCompletedState.setOnClickListener(v->{
            setBottomButtonStateComplete();
            techComplaint.setStatus("Resolved");
        });

        binding.layoutButtonCancelState.setOnClickListener(v->{
            setBottomButtonStateCancel();
            techComplaint.setStatus("Cancelled");
        });

//        TODO: Load notes with real data
//        setting notes recycler view adapter

        binding.ivOverflowMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), v);
            popupMenu.inflate(R.menu.menu_advance_options_tech);
            try {
                Field field = popupMenu.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                Object menuPopupHelper = field.get(popupMenu);
                assert menuPopupHelper != null;
                menuPopupHelper.getClass()
                        .getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(menuPopupHelper, true);
            }catch (Exception ignored){}

//            popupMenu.setOnMenuItemClickListener(item -> {
//                int itemID = item.getItemId();
////                todo: check for id
//
//            });
            popupMenu.show();
        });

//        Assigning Last updated date / completed date
        String lastUpdateDateLabel;
        switch (techComplaint.getStatus()){
            case "Resolved":
                    lastUpdateDateLabel = "Resolved on";
                    binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.completed_date_icon);
                    break;
            case "Cancelled":
                    lastUpdateDateLabel = "Rejected on";
                    binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.reject_icon);
                    break;
            default: lastUpdateDateLabel = "Status Updated On";
                     binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.last_update_date_icon);
                     break;
        }
        binding.tvLastUpdatedDateLabel.setText(lastUpdateDateLabel);

        return binding.getRoot();
    }

    private void setBottomButtonSelectedStatus(String status){
        switch(status){
            case "Pending":
                    setBottomButtonStatePending();
                    break;
            case "Ongoing":
                    setBottomButtonStateOngoing();
                    break;
            case "Resolved":
                    setBottomButtonStateComplete();
                    break;
            case "Cancelled":
                    setBottomButtonStateCancel();
                    break;
        }
    }

    private void setBottomButtonStatePending(){
        resetButtonInstant(
                techComplaint.getStatus());

        animateStatusChange(binding.layoutButtonPendingState,
                R.color.chip_pending_status_bg,
                R.color.pending_status_selected_bg,
                R.color.chip_pending_status_stroke_color,
                R.color.pending_status_selected_stroke,
                R.color.white,
                binding.tvBottomPendingStatusText,
                binding.ivBottomPendingStatusIcon);

//        binding.layoutButtonPendingState.setClickable(false);
//        binding.layoutButtonOngoingState.setClickable(true);
//        binding.layoutButtonCompletedState.setClickable(true);
//        binding.layoutButtonCancelState.setClickable(true);

        binding.layoutButtonPendingState.setAlpha(1);
        binding.layoutButtonOngoingState.setAlpha(0.4f);
        binding.layoutButtonCompletedState.setAlpha(0.4f);
        binding.layoutButtonCancelState.setAlpha(0.4f);

    }

    private void setBottomButtonStateOngoing(){
//        setting color of pending layout button
//        binding.layoutButtonOngoingState.setBackgroundResource(R.drawable.button_bg_status_ongoing_selected);
//        binding.ivBottomPendingStatusIcon.setImageTintList(ColorStateList.valueOf(R.color.ongoing_status_selected_text));
//        binding.tvBottomPendingStatusText.setBackgroundTintList(ColorStateList.valueOf(R.color.ongoing_status_selected_text));

        resetButtonInstant(
                techComplaint.getStatus());

        animateStatusChange(binding.layoutButtonOngoingState,
                R.color.chip_ongoing_status_bg,
                R.color.ongoing_status_selected_bg,
                R.color.chip_ongoing_status_stroke_color,
                R.color.ongoing_status_selected_stroke,
                R.color.white,
                binding.tvBottomOngoingStatusText,
                binding.ivBottomOngoingStatusIcon);

//        binding.layoutButtonOngoingState.setClickable(false);
//        binding.layoutButtonCompletedState.setClickable(true);
//        binding.layoutButtonPendingState.setClickable(true);
//        binding.layoutButtonCancelState.setClickable(true);

        binding.layoutButtonOngoingState.setAlpha(1);
        binding.layoutButtonCompletedState.setAlpha(0.4f);
        binding.layoutButtonPendingState.setAlpha(0.4f);
        binding.layoutButtonCancelState.setAlpha(0.4f);


    }
    private void setBottomButtonStateComplete(){
        resetButtonInstant(
                techComplaint.getStatus());

        animateStatusChange(binding.layoutButtonCompletedState,
                R.color.chip_completed_status_bg,
                R.color.completed_status_selected_bg,
                R.color.chip_completed_status_stroke_color,
                R.color.completed_status_selected_stroke,
                R.color.white,
                binding.tvBottomCompleteStatusText,
                binding.ivBottomCompleteStatus);

        binding.layoutButtonCompletedState.setAlpha(1);
        binding.layoutButtonOngoingState.setAlpha(0.4f);
        binding.layoutButtonPendingState.setAlpha(0.4f);
        binding.layoutButtonCancelState.setAlpha(0.4f);


    }
    private void setBottomButtonStateCancel(){
        resetButtonInstant(
                techComplaint.getStatus());

        animateStatusChange(binding.layoutButtonCancelState,
                R.color.chip_cancelled_status_bg,
                R.color.cancelled_status_selected_bg,
                R.color.chip_cancelled_status_stroke_color,
                R.color.cancelled_status_selected_stroke,
                R.color.white,
                binding.tvBottomCancelStatusText,
                binding.ivBottomCancelStatus);

        binding.layoutButtonCancelState.setAlpha(1);
        binding.layoutButtonCompletedState.setAlpha(0.4f);
        binding.layoutButtonOngoingState.setAlpha(0.4f);
        binding.layoutButtonPendingState.setAlpha(0.4f);

    }

    private String setDate(long dateInMills){
        String date;
        LocalDateTime noteDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateInMills), ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter;
        if(DateUtils.isToday(dateInMills)){
            formatter = DateTimeFormatter.ofPattern("hh:mm a");
            date = "Today • " + noteDate.format(formatter);
        }
        else if(noteDate.getYear() == now.getYear()){
            formatter = DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a");
            date = noteDate.format(formatter);
        }
        else{
            formatter = DateTimeFormatter.ofPattern("dd MMM • hh:mm a");
            date = noteDate.format(formatter);
        }
        return date;
    }


    private void resetButtonInstant(String state) {
        View button;
        @ColorRes int bg;
        @ColorRes int stroke;
        @ColorRes int content;
        TextView tv;
        ImageView iv;

        switch(state){
            case "Ongoing":
                button = binding.layoutButtonOngoingState;
                bg = R.color.chip_ongoing_status_bg;
                stroke = R.color.chip_ongoing_status_stroke_color;
                content = R.color.ongoing_status;
                tv = binding.tvBottomOngoingStatusText;
                iv = binding.ivBottomOngoingStatusIcon;
                break;
            case "Pending":
                button = binding.layoutButtonPendingState;
                bg = R.color.chip_pending_status_bg;
                stroke = R.color.chip_pending_status_stroke_color;
                content = R.color.pending_status;
                tv = binding.tvBottomPendingStatusText;
                iv = binding.ivBottomPendingStatusIcon;
                break;
            case "Resolved":
                button = binding.layoutButtonCompletedState;
                bg = R.color.chip_completed_status_bg;
                stroke = R.color.chip_completed_status_stroke_color;
                content = R.color.completed_status;
                tv = binding.tvBottomCompleteStatusText;
                iv = binding.ivBottomCompleteStatus;
                break;
            default:
                button = binding.layoutButtonCancelState;
                bg = R.color.chip_cancelled_status_bg;
                stroke = R.color.chip_cancelled_status_stroke_color;
                content = R.color.cancelled_status;
                tv = binding.tvBottomCancelStatusText;
                iv = binding.ivBottomCancelStatus;
                break;
        }

        GradientDrawable d = (GradientDrawable) button.getBackground().mutate();
        d.setColor(ContextCompat.getColor(requireContext(), bg));
        d.setStroke(1, ContextCompat.getColor(requireContext(), stroke));
        tv.setTextColor(ContextCompat.getColor(requireContext(), content));
        iv.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), content)
        ));
    }

    private void animateStatusChange(
            View button,
            @ColorRes int fromBg,
            @ColorRes int toBg,
            @ColorRes int fromStroke,
            @ColorRes int toStroke,
            @ColorRes int content,
            TextView tv,
            ImageView iv
    ) {
        GradientDrawable bg = (GradientDrawable) button.getBackground().mutate();

        int startBg = ContextCompat.getColor(requireContext(), fromBg);
        int endBg = ContextCompat.getColor(requireContext(), toBg);

        int startStroke = ContextCompat.getColor(requireContext(), fromStroke);
        int endStroke = ContextCompat.getColor(requireContext(), toStroke);

        int textColor = ContextCompat.getColor(requireContext(), content);

        button.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() ->
                        button.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                ).start();

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(200);

        ArgbEvaluator evaluator = new ArgbEvaluator();

        anim.addUpdateListener(a -> {
            float f = a.getAnimatedFraction();
            bg.setColor((int) evaluator.evaluate(f, startBg, endBg));
            bg.setStroke(1, (int) evaluator.evaluate(f, startStroke, endStroke));
            tv.setTextColor(textColor);
            iv.setImageTintList(ColorStateList.valueOf(textColor));
        });

        anim.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}