package org.syncloud.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.support.model.PortMapping;
import org.syncloud.android.R;
import org.syncloud.android.SyncloudApplication;
import org.syncloud.android.network.Network;
import org.syncloud.common.upnp.igd.Router;
import org.syncloud.common.upnp.UPnP;

import java.util.List;

import static com.google.common.base.Optional.of;
import static java.lang.String.format;

public class EnvironmentCheckActivity extends ActionBarActivity {

    private static Logger logger = Logger.getLogger(EnvironmentCheckActivity.class.getName());

    private Optional<UPnP> upnp = Optional.absent();

    private int checksInFlight = 0;
    private static final int TOTAL_CHECKS = 4;

    private SyncloudApplication application;

    private Button checkBtn;
    private ImageButton sendbtn;

    //TODO: How do I do a widget?
    private TextView routerText;
    private ProgressBar routerProgress;
    private ImageView routerStatusGood;
    private ImageView routerStatusBad;

    //TODO: Widget
    private TextView ipText;
    private ProgressBar ipProgress;
    private ImageView ipStatusGood;
    private ImageView ipStatusBad;

    //TODO: Widget
    private TextView portsText;
    private ProgressBar portsProgress;
    private ImageView portsStatusGood;
    private ImageView portsStatusBad;

    //TODO: Widget
    private TextView manipulationText;
    private ProgressBar manipulationProgress;
    private ImageView manipulationStatusGood;
    private ImageView manipulationStatusBad;
    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_environment_check);

        application = (SyncloudApplication) getApplication();
        network = new Network((WifiManager) getSystemService(Context.WIFI_SERVICE));

        routerText = (TextView) findViewById(R.id.upnp_router_status);
        routerProgress = (ProgressBar) findViewById(R.id.upnp_router_progress);
        routerStatusGood = (ImageView) findViewById(R.id.upnp_router_good);
        routerStatusBad = (ImageView) findViewById(R.id.upnp_router_bad);

        ipText = (TextView) findViewById(R.id.upnp_ip_status);
        ipProgress = (ProgressBar) findViewById(R.id.upnp_ip_progress);
        ipStatusGood = (ImageView) findViewById(R.id.upnp_ip_good);
        ipStatusBad = (ImageView) findViewById(R.id.upnp_ip_bad);

        portsText = (TextView) findViewById(R.id.upnp_ports_status);
        portsProgress = (ProgressBar) findViewById(R.id.upnp_ports_progress);
        portsStatusGood = (ImageView) findViewById(R.id.upnp_ports_good);
        portsStatusBad = (ImageView) findViewById(R.id.upnp_ports_bad);

        manipulationText = (TextView) findViewById(R.id.upnp_manipulation_status);
        manipulationProgress = (ProgressBar) findViewById(R.id.upnp_manipulation_progress);
        manipulationStatusGood = (ImageView) findViewById(R.id.upnp_manipulation_good);
        manipulationStatusBad = (ImageView) findViewById(R.id.upnp_manipulation_bad);

        checkBtn = (Button) findViewById(R.id.upnp_check_btn);
        sendbtn = (ImageButton) findViewById(R.id.upnp_send_btn);

        check();
    }

    private void check() {
        reset();
        new RouterTask().execute((Void) null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_environment_check, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), 2);
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendReport(View view) {
        application.reportError();
    }

    public void onCheck(View view) {
        check();
    }

    private void mainControllsEnabled(boolean enabled) {
        checkBtn.setEnabled(enabled);
        sendbtn.setEnabled(enabled);
    }

    private void reset() {

        logger.info("reset");
        checksInFlight = TOTAL_CHECKS;

        routerText.setText("");
        routerStatusBad.setVisibility(View.GONE);
        routerStatusGood.setVisibility(View.GONE);
        routerProgress.setVisibility(View.GONE);

        ipText.setText("");
        ipStatusBad.setVisibility(View.GONE);
        ipStatusGood.setVisibility(View.GONE);
        ipProgress.setVisibility(View.GONE);

        portsText.setText("");
        portsStatusBad.setVisibility(View.GONE);
        portsStatusGood.setVisibility(View.GONE);
        portsProgress.setVisibility(View.GONE);

        manipulationText.setText("");
        manipulationStatusBad.setVisibility(View.GONE);
        manipulationStatusGood.setVisibility(View.GONE);
        manipulationProgress.setVisibility(View.GONE);

        mainControllsEnabled(false);

    }

    private void done(int checks) {
        logger.info("done: " + checks + "/" + checksInFlight);
        checksInFlight -= checks;
        if (checksInFlight == 0) {
            if (upnp.isPresent())
                upnp.get().shutdown();
            mainControllsEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnp.isPresent())
            upnp.get().shutdown();
    }

    public class RouterTask extends AsyncTask<Void, Void, Optional<Router>> {

        @Override
        protected void onPreExecute() {
            routerText.setText("Checking ...");
            routerStatusBad.setVisibility(View.GONE);
            routerStatusGood.setVisibility(View.GONE);
            routerProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Optional<Router> doInBackground(Void... voids) {
            upnp = of(new UPnP(new AndroidUpnpServiceConfiguration()));
            return upnp.get().start().find();
        }

        @Override
        protected void onPostExecute(Optional<Router> result) {
            if (result.isPresent()) {
                Router router = result.get();
                routerText.setText(router.getName());
                routerStatusGood.setVisibility(View.VISIBLE);
                done(1);
                new IPTask().execute(router);
                new PortsTask().execute(router);
                new ManipulationTask().execute(router);
            } else {
                routerText.setText("Not able to find UPnP router");
                routerStatusBad.setVisibility(View.VISIBLE);
                done(checksInFlight);
            }
            routerProgress.setVisibility(View.GONE);
        }
    }

    public class IPTask extends AsyncTask<Router, Void, Optional<String>> {

        @Override
        protected void onPreExecute() {
            ipText.setText("Checking ...");
            ipStatusBad.setVisibility(View.GONE);
            ipStatusGood.setVisibility(View.GONE);
            ipProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Optional<String> doInBackground(Router... routers) {
            return routers[0].getExternalIP();
        }

        @Override
        protected void onPostExecute(Optional<String> ip) {
            if (ip.isPresent()) {
                ipText.setText(ip.get());
                ipStatusGood.setVisibility(View.VISIBLE);
            } else {
                ipText.setText("Not able to find IP");
                ipStatusBad.setVisibility(View.VISIBLE);
            }
            ipProgress.setVisibility(View.GONE);
            done(1);
        }
    }

    public class PortsTask extends AsyncTask<Router, Void, List<PortMapping>> {

        @Override
        protected void onPreExecute() {
            portsText.setText("Checking ...");
            portsStatusBad.setVisibility(View.GONE);
            portsStatusGood.setVisibility(View.GONE);
            portsProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<PortMapping> doInBackground(Router... routers) {
            return routers[0].getPortMappings();
        }

        @Override
        protected void onPostExecute(List<PortMapping> ports) {
            if (!ports.isEmpty()) {
                portsText.setText(format("%s mapped ports", ports.size()));
//                ipStatusGood.setVisibility(View.VISIBLE);
            } else {
                ipText.setText("No mapped ports, may be fine");
//                ipStatusBad.setVisibility(View.VISIBLE);
            }
            portsStatusGood.setVisibility(View.VISIBLE);
            portsProgress.setVisibility(View.GONE);
            done(1);
        }
    }

    public class ManipulationTask extends AsyncTask<Router, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            manipulationText.setText("Checking ...");
            manipulationStatusBad.setVisibility(View.GONE);
            manipulationStatusGood.setVisibility(View.GONE);
            manipulationProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Router... routers) {
            Router router = routers[0];

            Optional<String> ip = network.hostname();
            if (!ip.isPresent())
                return false;


            return router.canToManipulatePorts(ip.get());
        }

        @Override
        protected void onPostExecute(Boolean works) {
            if (works) {
                manipulationText.setText("Can modify port mappings");
                manipulationStatusGood.setVisibility(View.VISIBLE);
            } else {
                manipulationText.setText("Unable to modify port mappings");
                manipulationStatusBad.setVisibility(View.VISIBLE);
            }
            manipulationProgress.setVisibility(View.GONE);
            done(1);
        }
    }
}
