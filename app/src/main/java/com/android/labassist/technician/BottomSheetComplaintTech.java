package com.android.labassist.technician;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.BottomSheetComplaintTechBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BottomSheetComplaintTech extends BottomSheetDialogFragment {

    private BottomSheetComplaintTechBinding binding;

    // The currently loaded complaint object (Not final anymore!)
    private ComplaintEntity techComplaint;

    private String currentComplaintId;
    private BottomSheetComplaintTechViewModel viewModel;

    private static final String ARG_COMPLAINT_ID = "COMPLAINT_ID";

    // 1. Empty constructor required for Fragments
    public BottomSheetComplaintTech() {}

    // 2. The Factory Method passing only the ID
    public static BottomSheetComplaintTech newInstance(String complaintId) {
        BottomSheetComplaintTech bottomSheet = new BottomSheetComplaintTech();
        Bundle args = new Bundle();
        args.putString(ARG_COMPLAINT_ID, complaintId);
        bottomSheet.setArguments(args);
        return bottomSheet;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // This forces the sheet to animate to its fully expanded state instantly
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Unpack the ID
        if (getArguments() != null) {
            currentComplaintId = getArguments().getString(ARG_COMPLAINT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BottomSheetComplaintTechBinding.bind(inflater.inflate(R.layout.bottom_sheet_complaint_tech, container, false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(BottomSheetComplaintTechViewModel.class);
        AppDatabase db = AppDatabase.getInstance(requireContext());
        viewModel.init(db.labAssistDao(), currentComplaintId);

        // 4. Observe the data from Room
        viewModel.getComplaint().observe(getViewLifecycleOwner(), complaint -> {
            if (complaint != null) {
                // Save the latest instance to our local variable
                this.techComplaint = complaint;

                // Now populate the UI!
                populateUI();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void populateUI() {
        // All your original onCreateView logic goes here!
        binding.tvComplaintTitle.setText(techComplaint.getTitle());
        binding.tvLabLocation.setText(techComplaint.getLabName());
        binding.tvAppointedDate.setText(parseDate(techComplaint.getCreatedAt()));

        String description = techComplaint.getDescription();
        binding.tvDeviceCode.setText(techComplaint.deviceName + " • " + techComplaint.deviceCode);

        if (description == null || description.isBlank()) {
            binding.tvProblemDescription.setText("No description set");
        } else {
            binding.tvProblemDescription.setText(techComplaint.getDescription());
        }

        setBottomButtonSelectedStatus(techComplaint.getStatus());

        // TODO: Add change complaint state via calling RestAPI function and update Room DB!
        // NOTE: Make sure your ViewModel handles saving these changes back to the database!

        binding.layoutButtonPendingState.setOnClickListener(v -> {
            setBottomButtonStatePending();
            techComplaint.setStatus("Pending");
        });

        binding.layoutButtonOngoingState.setOnClickListener(v -> {
            setBottomButtonStateOngoing();
            techComplaint.setStatus("Ongoing");
        });

        binding.layoutButtonCompletedState.setOnClickListener(v -> {
            setBottomButtonStateComplete();
            techComplaint.setStatus("Resolved");
        });

        binding.layoutButtonCancelState.setOnClickListener(v -> {
            setBottomButtonStateCancel();
            techComplaint.setStatus("Cancelled");
        });

        // Overflow Menu for Notes
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
            } catch (Exception ignored) {}

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemID = item.getItemId();
                if (itemID == R.id.menu_view_notes_of_complaint) {
                    Bundle bundle = new Bundle();
                    bundle.putString("COMPLAINT_ID", techComplaint.getId());
                    bundle.putString("DEVICE_ID", techComplaint.getDeviceId());
                    bundle.putString("LAB_ID", techComplaint.getLabId());

                    if (techComplaint.deviceId == null || techComplaint.deviceId.isEmpty())
                        bundle.putInt("action", ViewTechNotesFragment.actionLabNotes);
                    else
                        bundle.putInt("action", ViewTechNotesFragment.actionDeviceNotes);

                    Log.d("BottomSheet", "Complaint ID: " + techComplaint.getId());
                    Log.d("BottomSheet", "Device ID: " + techComplaint.getDeviceId());
                    Log.d("BottomSheet", "Lab ID: " + techComplaint.getLabId());

                    NavHostFragment
                            .findNavController(this)
                            .navigate(R.id.action_global_view_notes, bundle);

                    dismiss();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        // Assigning Last updated date / completed date
        String lastUpdateDateLabel;
        if (techComplaint.getStatus().equals(ComplaintStatus.RESOLVED)) {
            lastUpdateDateLabel = "Resolved on";
            binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.completed_date_icon);
        } else if (techComplaint.getStatus().equals(ComplaintStatus.CANCELLED)) {
            lastUpdateDateLabel = "Rejected on";
            binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.reject_icon);
        } else {
            lastUpdateDateLabel = "Status Updated On";
            binding.ivLastUpdatedDateIcon.setImageResource(R.drawable.last_update_date_icon);
        }
        binding.tvLastUpdatedDateLabel.setText(lastUpdateDateLabel);
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

    private String parseDate(long dateInMills) {
        // 1. Safety check for invalid dates
        if (dateInMills <= 0) {
            return "Unknown Date";
        }

        Date dateObj = new Date(dateInMills);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // 2. Is it Today? (e.g., "Today, 10:30 AM")
        if (DateUtils.isToday(dateInMills)) {
            return "Today • " + timeFormat.format(dateObj);
        }

        // Setup Calendars to check for Yesterday and the Year
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(dateInMills);

        // 3. Is it Yesterday? (e.g., "Yesterday, 10:30 AM")
        now.add(Calendar.DAY_OF_YEAR, -1);
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday • " + timeFormat.format(dateObj);
        }

        // Reset the 'now' calendar back to today
        now = Calendar.getInstance();

        // 4. Is it from this current year? (e.g., "Oct 24 • 10:30 AM")
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
            SimpleDateFormat sameYearFormat = new SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault());
            return sameYearFormat.format(dateObj);
        }
        // 5. It must be from a previous year (e.g., "24 Oct 2023 • 10:30 AM")
        else {
            SimpleDateFormat diffYearFormat = new SimpleDateFormat("dd MMM yyyy • h:mm a", Locale.getDefault());
            return diffYearFormat.format(dateObj);
        }
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