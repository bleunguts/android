package org.syncloud.android.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.syncloud.android.Preferences;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.ui.DevicesSavedActivity;
import org.syncloud.ssh.model.Device;

public class DevicesSavedAdapter extends ArrayAdapter<Device> {
    private final Preferences preferences;
    private DevicesSavedActivity activity;

    public DevicesSavedAdapter(DevicesSavedActivity activity) {
        super(activity, R.layout.layout_device_saved);
        this.activity = activity;
        preferences = ((SyncloudApplication) activity.getApplication()).getPreferences();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_device_saved, null);

        TextView txtDeviceTitle = (TextView) rowView.findViewById(R.id.txt_device_title);
        TextView txtDomainName = (TextView) rowView.findViewById(R.id.txt_domain_name);
        TextView txtMacAddress = (TextView) rowView.findViewById(R.id.txt_mac_address);
        ImageButton btnDeleteDevice = (ImageButton) rowView.findViewById(R.id.delete_device_btn);
        ImageButton btnShareDevice = (ImageButton) rowView.findViewById(R.id.share_device_btn);

        final Device device = getItem(position);

        txtDeviceTitle.setText(device.id().title);
        txtDomainName.setText(device.userDomain());
        txtMacAddress.setText(device.macAddress());

        txtMacAddress.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);
        btnDeleteDevice.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);
        btnDeleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db().remove(device);
                refresh();
            }
        });
        btnShareDevice.setVisibility(preferences.isDebug() ? View.VISIBLE : View.GONE);
        btnShareDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.shareDevice(device);
            }
        });

        return rowView;

    }

}
