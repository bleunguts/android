package org.syncloud.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.syncloud.android.R;
import org.syncloud.android.ui.DeviceAppStoreActivity;
import org.syncloud.platform.sam.AppVersions;
import org.syncloud.platform.sam.Commands;

public class DeviceAppStoreAppsAdapter extends ArrayAdapter<AppVersions> {
    private DeviceAppStoreActivity activity;

    public DeviceAppStoreAppsAdapter(DeviceAppStoreActivity activity) {
        super(activity, R.layout.layout_app);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_app_in_store, null);
        TextView textView = (TextView) rowView.findViewById(R.id.app_name);
        final AppVersions appVersions = getItem(position);
        textView.setText(appVersions.app.name + " " + appVersions.current_version);

        ImageButton install = (ImageButton) rowView.findViewById(R.id.install_app);
        install.setVisibility(View.GONE);
        ImageButton remove = (ImageButton) rowView.findViewById(R.id.remove_app);
        remove.setVisibility(View.GONE);
        ImageButton upgrade = (ImageButton) rowView.findViewById(R.id.upgrade_app);
        upgrade.setVisibility(View.GONE);

        ImageButton appTypeUser = (ImageButton) rowView.findViewById(R.id.app_icon_user);
        ImageButton appTypeUtil = (ImageButton) rowView.findViewById(R.id.app_icon_util);

        if (appVersions.app.ui) {
            appTypeUser.setVisibility(View.VISIBLE);
            appTypeUtil.setVisibility(View.GONE);
        } else {
            appTypeUtil.setVisibility(View.VISIBLE);
            appTypeUser.setVisibility(View.GONE);
        }

        if (appVersions.installed()) {
            if (!appVersions.installed_version.equals(appVersions.current_version)) {
                upgrade.setVisibility(View.VISIBLE);
            }
            remove.setVisibility(View.VISIBLE);
        } else {
            install.setVisibility(View.VISIBLE);
        }

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.runSam(Commands.install, appVersions.app.id);
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.runSam(Commands.remove, appVersions.app.id);
            }
        });
        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {activity.runSam(Commands.upgrade, appVersions.app.id);
            }
        });

        return rowView;

    }
}
