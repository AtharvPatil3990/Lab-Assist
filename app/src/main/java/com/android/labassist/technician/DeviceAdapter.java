package com.android.labassist.technician;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.labassist.databinding.ItemDeviceBinding;
import com.android.labassist.database.entities.DeviceEntity; // Adjust to your entity!

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    private List<DeviceEntity> deviceList = new ArrayList<>();
    private final OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(String deviceId);
    }

    public DeviceAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public void setDevices(List<DeviceEntity> devices) {
        this.deviceList = devices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new DeviceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(deviceList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeviceBinding binding;

        public DeviceViewHolder(@NonNull ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DeviceEntity device, OnDeviceClickListener listener) {
            binding.tvDeviceName.setText(device.deviceName != null ? device.deviceName : "Unknown Device");

            // Assuming you have a model or type field in your database
            String model = device.deviceType != null ? device.deviceType : "Standard Equipment";
            binding.tvDeviceType.setText(model);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeviceClick(device.id);
                }
            });
        }
    }
}
