package com.wigzo.sdk;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wigzo.sdk.helpers.WigzoTypeFace;

import java.util.HashMap;

/**
 * Created by wigzo on 17/2/17.
 */
@Keep
public class WigzoDialogTemplate extends Dialog implements View.OnClickListener {

    /*private TextView notification_title;*/
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

        setContentView(R.layout.wigzo_dialog_template_1);

        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        dialog_title = (TextView) findViewById(R.id.dialog_title);
        notification_body = (TextView) findViewById(R.id.notification_body);

        Typeface regularFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_regular);
        dialog_title.setTypeface(regularFace);
        dialog_title.setText(title);

        Typeface lightFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_light);
        notification_body.setTypeface(lightFace);
        notification_body.setText(body);

        yes.setTypeface(regularFace);
        no.setTypeface(regularFace);

        notificationImage = (ImageView) findViewById(R.id.notificationImage);

        if (null != remote_picture) {
            notificationImage.setVisibility(View.VISIBLE);
            notificationImage.setImageBitmap(remote_picture);
        }

        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_yes) {
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
