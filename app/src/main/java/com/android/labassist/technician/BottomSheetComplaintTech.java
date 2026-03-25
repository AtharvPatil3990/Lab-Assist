package com.android.labassist.technician;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.android.labassist.ComplaintStatus;
import com.android.labassist.R;
import com.android.labassist.auth.SessionManager;
import com.android.labassist.database.AppDatabase;
import com.android.labassist.database.entities.ComplaintEntity;
import com.android.labassist.databinding.BottomSheetComplaintTechBinding;
import com.android.labassist.network.ApiController;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

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
    private boolean isAdmin;

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

        isAdmin = SessionManager.getInstance(requireContext()).getRole().equals(SessionManager.ROLE_ADMIN);

        // 3. Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(BottomSheetComplaintTechViewModel.class);
        AppDatabase db = AppDatabase.getInstance(requireContext());
        viewModel.init(db.labAssistDao(), ApiController.getInstance(requireContext()), currentComplaintId);

        // 4. Observe the data from Room
        viewModel.getComplaint().observe(getViewLifecycleOwner(), complaint -> {
            if (complaint != null) {
                // Save the latest instance to our local variable
                this.techComplaint = complaint;
                // Now populate the UI!
                populateUI();
                setBottomButtonStateOngoing();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                // Show the dark overlay and block all touches
                binding.loadingOverlay.setVisibility(View.VISIBLE);

                // Optional: Prevent them from swiping the bottom sheet down to close it
                // while the API call is running!
                if(getDialog() != null)
                    getDialog().setCancelable(false);
            } else {
                // Hide the overlay and restore functionality
                binding.loadingOverlay.setVisibility(View.GONE);
                if(getDialog() != null)
                    getDialog().setCancelable(true);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            // Check if there is actually an error to show
            if (errorMessage != null && !errorMessage.isEmpty()) {

                // Create and show a Material Snackbar
                Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG)
                        // Optional: Make it look like an error by setting a red background
                        // .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.design_default_color_error))
                        .show();

                // VERY IMPORTANT: Clear the error after showing it!
                viewModel.clearError();
            }
        });

    }

//    @SuppressLint("SetTextI18n")
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

        if(isAdmin){
            binding.btnAcceptComplaint.setVisibility(View.GONE);
            binding.btnReassignComplaint.setVisibility(View.GONE);
            binding.btnStartWork.setVisibility(View.GONE);
            binding.layoutStatusButtons.setVisibility(View.GONE);

            binding.ivOverflowMenu.setVisibility(View.GONE);
            return;
        }

        setBottomButtonSelectedStatus(techComplaint.getStatus());


        // Get the current status safely
        String currentStatus = techComplaint.getStatus() != null ? techComplaint.getStatus().toUpperCase() : "OPEN";

        // 1. Reset everything to hidden by default
        binding.btnAcceptComplaint.setVisibility(View.GONE);
        binding.btnReassignComplaint.setVisibility(View.GONE);
        binding.btnStartWork.setVisibility(View.GONE);
        binding.layoutStatusButtons.setVisibility(View.GONE);

        // 2. The State Machine Routing
        switch (currentStatus) {
            case "OPEN":
                // Standard Accept Button
                binding.btnAcceptComplaint.setVisibility(View.VISIBLE);
                binding.btnAcceptComplaint.setText("Accept Complaint");
                binding.btnAcceptComplaint.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_btn_bg));
                binding.btnAcceptComplaint.setEnabled(true);

                binding.btnAcceptComplaint.setOnClickListener(v -> {
                    viewModel.updateComplaintStatus(techComplaint.id, "ASSIGNED");
                });

                showReassignButton();

                break;

            case "QUEUED":
                // Warning Accept Button
                binding.btnAcceptComplaint.setVisibility(View.VISIBLE);
                binding.btnAcceptComplaint.setText("Override Queue & Accept");

                // Set the dynamic warning background
                binding.btnAcceptComplaint.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.warning_color)
                ));

                // Set the dynamic text color so it's always readable
                binding.btnAcceptComplaint.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.warning_text_color)
                );

                // You can even tint the icon to match the text!
                binding.btnAcceptComplaint.setIconTint(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.warning_text_color)
                ));

                binding.btnAcceptComplaint.setEnabled(true);
                binding.btnAcceptComplaint.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Override System Queue?")
                            .setMessage("This complaint is queued because you have other pending tasks.\nAre you sure you want to prioritize this one?")
                            .setPositiveButton("Accept Complaint", (dialog, which) -> {
                                viewModel.updateComplaintStatus(techComplaint.getId(), "ASSIGNED");
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                });

                showReassignButton();

                break;

            case "ASSIGNED":
                // STATE 2: Accepted but not started - Show only the Start Work button
                binding.btnStartWork.setVisibility(View.VISIBLE);

                binding.btnStartWork.setOnClickListener(v -> {
                    viewModel.updateComplaintStatus(techComplaint.getId(), "IN_PROGRESS");
                });
                break;

            case "IN_PROGRESS":
            case "ON_HOLD":
                // STATE 3: Actively Working - Show the 3-button grid
                binding.layoutStatusButtons.setVisibility(View.VISIBLE);

                // Attach click listeners to your 3 custom chips
                binding.layoutButtonOngoingState.setOnClickListener(v -> {
                    viewModel.updateComplaintStatus(techComplaint.getId(), "IN_PROGRESS");
//                    setBottomButtonStateOngoing();

                });

                binding.layoutButtonCompletedState.setOnClickListener(v -> {
//                    setBottomButtonStateComplete();
//                     TODO: Add an image path while calling API updateComplaintStatus
                    viewModel.updateComplaintStatusWithImagePath(techComplaint.getId(), "RESOLVED", "tempPath");
                });

                binding.layoutButtonCancelState.setOnClickListener(v -> {

                    // 1. Inflate our custom XML layout
                    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_reason, null);
                    TextInputEditText etReason = dialogView.findViewById(R.id.etCancelReason);

                    // 2. Build the Material Dialog
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Reject Complaint")
                            .setMessage("Please explain why this ticket is being cancelled or rejected.")
                            .setView(dialogView)
                            // We set the positive listener to null for now. We will override it below!
                            .setPositiveButton("Submit", null)
                            .setNegativeButton("Back", (dialog, which) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    // 3. THE SENIOR DEV TRICK: Override the positive button
                    // Standard dialogs close immediately when you click "Submit".
                    // By overriding it here, we stop it from closing if the box is empty!
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(buttonView -> {

                        String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

                        if (reason.isEmpty()) {
                            // Show an error state on the text box and DO NOT close the dialog
                            etReason.setError("A reason is required to reject a ticket.");
                            etReason.requestFocus();
                        } else {
                            // 4. SUCCESS! We have a valid reason.

                            // TODO: Call your ViewModel, but you will need to pass the 'reason' string now!
                            viewModel.updateComplaintStatusWithReason(techComplaint.getId(), "CANCELLED", reason);

                            dialog.dismiss(); // Now we can safely close the dialog
                        }
                    });
                });
                break;

            case "RESOLVED":
            case "CLOSED":
            case "CANCELLED":
                // STATE 4: Terminal States - Leave everything GONE!
                // The technician can only read the ticket, they cannot change it anymore.
                break;

            default:
                // Fallback just in case a weird status string gets into the database
                binding.btnAcceptComplaint.setVisibility(View.VISIBLE);
                showReassignButton();
                break;
        }


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
    }

    private void showReassignButton() {
        binding.btnReassignComplaint.setVisibility(View.VISIBLE);
        binding.btnReassignComplaint.setEnabled(true);
        binding.btnReassignComplaint.setOnClickListener(v -> {
            // 1. Inflate your custom text input layout
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cancel_reason, null);
            TextInputEditText etReason = dialogView.findViewById(R.id.etCancelReason);

            // 2. Build the Material Dialog
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reassign Complaint")
                    .setMessage("This Complaint will be reassigned to another technician,\nPlease provide a reason for reassigning")
                    .setView(dialogView)
                    .setPositiveButton("Reassign", null) // We override this below
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            // 3. The "Senior Dev Trick" to prevent empty submissions
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(buttonView -> {
                String reason = etReason.getText() != null ? etReason.getText().toString().trim() : "";

                if (reason.isEmpty()) {
                    etReason.setError("A reason is required.");
                    etReason.requestFocus();
                } else {
                    // SUCCESS! Call the ViewModel.
                    // The dialog closes, the dark overlay appears, and the Edge Function fires!
                    viewModel.rerouteAssignedComplaint(techComplaint.getId(), reason);
                    dialog.dismiss();
                }
            });
            BottomSheetComplaintTech.this.dismiss();
        });
    }

    private void setBottomButtonSelectedStatus(String status){
        switch(status){
            case "IN_PROGRESS":
                    setBottomButtonStateOngoing();
                    break;
            case "RESOLVED":
                    setBottomButtonStateComplete();
                    break;
            case "CANCELLED":
                    setBottomButtonStateCancel();
                    break;
        }
    }


    private void setBottomButtonStateOngoing(){
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

        binding.layoutButtonOngoingState.setAlpha(1);
        binding.layoutButtonCompletedState.setAlpha(0.8f);
        binding.layoutButtonCancelState.setAlpha(0.8f);


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
        binding.layoutButtonOngoingState.setAlpha(0.8f);
        binding.layoutButtonCancelState.setAlpha(0.8f);


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
        binding.layoutButtonCompletedState.setAlpha(0.8f);
        binding.layoutButtonOngoingState.setAlpha(0.8f);

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
            case "IN_PROGRESS":
                button = binding.layoutButtonOngoingState;
                bg = R.color.chip_ongoing_status_bg;
                stroke = R.color.chip_ongoing_status_stroke_color;
                content = R.color.ongoing_status;
                tv = binding.tvBottomOngoingStatusText;
                iv = binding.ivBottomOngoingStatusIcon;
                break;
            case "RESOLVED":
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