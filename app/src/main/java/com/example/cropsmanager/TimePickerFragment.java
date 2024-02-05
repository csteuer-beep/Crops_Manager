package com.example.cropsmanager;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.NumberPicker;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {

    private TimePickerListener listener;

    public interface TimePickerListener {
        void onTimeSet(int minutes, int seconds);
    }

    public void setListener(TimePickerListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a custom dialog with NumberPickers for hours, minutes, and seconds
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_time_picker_dialog);

        // Initialize NumberPickers
        NumberPicker minutePicker = dialog.findViewById(R.id.minutePicker);
        NumberPicker secondPicker = dialog.findViewById(R.id.secondPicker);

        // Set the range for each NumberPicker

        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);

        // Set the values for each NumberPicker
        minutePicker.setValue(0);
        secondPicker.setValue(0);

        // Set a listener to handle the time set event
        dialog.findViewById(R.id.btnSetTime).setOnClickListener(v -> {
            int minutes = minutePicker.getValue();
            int seconds = secondPicker.getValue();

            if (listener != null) {
                listener.onTimeSet(minutes, seconds);
            }

            dialog.dismiss();
        });

        return dialog;
    }
}
