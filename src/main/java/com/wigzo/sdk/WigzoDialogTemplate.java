package com.wigzo.sdk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by wigzo on 17/2/17.
 */
@Keep
public class WigzoDialogTemplate extends Dialog implements View.OnClickListener {

    View v;
    private TextView notification_title;
    private TextView notification_body;
    private TextView dialog_title;

    private Button yes;
    private Button no;

    private ImageView notificationImage;

    private Context context;

    private String body = "";
    private String title = "";
    private Bitmap remote_picture = null;

    private Class<? extends AppCompatActivity> targetActivity = null;

    public WigzoDialogTemplate(Context context, String title, String body, HashMap<String, String> payload, Class<? extends AppCompatActivity> targetActivity) {
        super(context);
        this.context = context;
        this.title = title;
        this.body = body;
        this.targetActivity = targetActivity;
    }

    public WigzoDialogTemplate(Context context, String title, String body, HashMap<String, String> payload, Bitmap remote_picture, Class<? extends AppCompatActivity> targetActivity) {
        super(context);

        this.context = context;
        this.title = title;
        this.body = body;
        this.remote_picture = remote_picture;
        this.targetActivity = targetActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.wigzo_dialog_template);

        /*setTitle(title);*/

        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);

        notification_title = (TextView) findViewById(R.id.notification_title);
        notification_body = (TextView) findViewById(R.id.notification_body);
        dialog_title = (TextView) findViewById(R.id.dialog_title);

        notificationImage = (ImageView) findViewById(R.id.notificationImage);

        notificationImage.setImageBitmap(remote_picture);

        /*notification_title.setText(title);*/
        dialog_title.setText(title);
        notification_title.setVisibility(View.GONE);
        notification_body.setText(body);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_yes) {
            Toast.makeText(context, "Ok Clicked", Toast.LENGTH_SHORT).show();

            /*Intent targetActivityIntent = new Intent(WigzoSDK.getInstance().getContext(), targetActivity);*/
            Intent targetActivityIntent = new Intent(WigzoApplication.getAppContext(), targetActivity);
            WigzoApplication.getAppContext().startActivity(targetActivityIntent);
            dismiss();

        } else if (i == R.id.btn_no) {
            dismiss();

        } else {
        }
        dismiss();
    }
}
