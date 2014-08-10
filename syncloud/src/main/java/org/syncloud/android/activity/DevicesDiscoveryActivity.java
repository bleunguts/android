package org.syncloud.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.adapter.DevicesDiscoveredAdapter;
import org.syncloud.android.discovery.AsyncDiscovery;
import org.syncloud.discovery.DeviceEndpointListener;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DeviceEndpoint;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DevicesDiscoveryActivity extends Activity {

    private AsyncDiscovery asyncDiscovery;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private ImageButton refreshBtn;
    private ProgressBar progressBar;
    private DevicesDiscoveredAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_discovery);
        final ListView listview = (ListView) findViewById(R.id.devices_discovered);
        progressBar = (ProgressBar) findViewById(R.id.discovery_progress);
        refreshBtn = (ImageButton) findViewById(R.id.discovery_refresh_btn);
        listAdapter = new DevicesDiscoveredAdapter(this);
        listview.setAdapter(listAdapter);

        DeviceEndpointListener deviceEndpointListener = new DeviceEndpointListener() {
            @Override
            public void added(final DeviceEndpoint endpoint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.add(endpoint);
                    }
                });
            }

            @Override
            public void removed(final DeviceEndpoint endpoint) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.remove(endpoint);
                    }
                });
            }
        };

        asyncDiscovery = new AsyncDiscovery(
                (WifiManager) getSystemService(Context.WIFI_SERVICE),
                deviceEndpointListener);

        discoveryStart();
    }

    private void discoveryStart() {
        listAdapter.clear();
        refreshBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        asyncDiscovery.start();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        discoveryStop();
                    }
                });
            }
        }, 10, TimeUnit.SECONDS );
    }

    private void discoveryStop() {
        asyncDiscovery.stop();
        progressBar.setVisibility(View.GONE);
        refreshBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.discovery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void open(final DeviceEndpoint endpoint) {
        Intent intent = new Intent(this, DeviceActivateActivity.class);
        intent.putExtra(SyncloudApplication.DEVICE_ENDPOINT, endpoint);
        startActivity(intent);
        setResult(Activity.RESULT_OK, new Intent(this, DevicesSavedActivity.class));
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        asyncDiscovery.stop();
    }

    public void refresh(View view) {
        discoveryStart();
    }
}
