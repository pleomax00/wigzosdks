package com.wigzo.sdk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by wigzo on 17/2/17.
 */

public class WigzoDialogTemplate extends Dialog implements View.OnClickListener {

    View v;
    private TextView notification_title;
    private TextView notification_body;

    private Button yes;
    private Button no;

    private Context context;

    private String body = "";
    private String title = "";

    public WigzoDialogTemplate(Context context, String title, String body, HashMap<String, String> payload) {
        super(context);
        this.context = context;
        this.title = title;
        this.body = body;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.wigzo_dialog_template);

        setTitle("Wigzo Dialog");

        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);

        notification_title = (TextView) findViewById(R.id.notification_title);
        notification_body = (TextView) findViewById(R.id.notification_body);

        notification_title.setText(title);
        notification_body.setText(body);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_yes) {
            Toast.makeText(context, "Ok Clicked", Toast.LENGTH_SHORT).show();
            dismiss();

        } else if (i == R.id.btn_no) {
            dismiss();

        } else {
        }
        dismiss();
    }
}
