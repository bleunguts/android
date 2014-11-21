package org.syncloud.android.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.acra.ACRA;
import org.syncloud.android.R;

public class CommunicationDialog extends AlertDialog {
    private ProgressBar progress;
    private Activity context;
    private TextView messageView;
    private CharSequence message;
    private Button reportBtn;
    private CharSequence title;

    public CommunicationDialog(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_communication, null);
        progress = (ProgressBar) view.findViewById(R.id.communication_progress);
        messageView = (TextView) view.findViewById(R.id.message);
        reportBtn = (Button) view.findViewById(R.id.report_button);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportError();
            }
        });
        setView(view);

        if (message != null)
            setMessage(message);
        setCancelable(false);
        progress.setVisibility(View.VISIBLE);
        messageView.setVisibility(View.INVISIBLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setMessage(CharSequence text) {
        message = text;
        if (messageView != null)
            messageView.setText(text);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        super.setTitle(title);
    }


    public void reportError() {
        ACRA.getErrorReporter().handleException(null);
    }


    private void setError(String error) {
        progress(error);
        reportBtn.setVisibility(View.VISIBLE);
        messageView.setVisibility(View.VISIBLE);
        setCancelable(true);
        progress.setVisibility(View.INVISIBLE);
    }

    public void start() {
        setTitle("Connecting to the device");
        setMessage("");
        setCancelable(false);
        show();
        messageView.setVisibility(View.INVISIBLE);
        reportBtn.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.VISIBLE);
    }

    public void stop() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        });
    }

    public void error(final String error) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setError(error);
            }
        });
    }

    public void title(final String message) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(message);
            }
        });
    }

    public void progress(final String progress) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMessage(message + "\n" + progress);
            }
        });
    }
}