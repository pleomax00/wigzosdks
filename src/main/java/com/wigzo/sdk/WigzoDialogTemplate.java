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
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wigzo.sdk.WigzoLayoutAttributes.WigzoLayoutProperties;
import com.wigzo.sdk.helpers.StringUtils;
import com.wigzo.sdk.helpers.WigzoTypeFace;

import java.util.HashMap;

/**
 * Created by wigzo on 17/2/17.
 */
@Keep
public class WigzoDialogTemplate extends Dialog implements View.OnClickListener {

    /*private TextView notification_title;*/
    private TextView notification_body;
    private TextView notification_title;

    private Button yes;
    private Button no;

    private ImageView notification_image;

    private Context context;

    private Bitmap remote_picture = null;
    private String body = "";
    private String title = "";
    private String layoutIdStr = "001";
    private HashMap<String, String> payload = new HashMap<>();
    private int layoutId = 0;
    private boolean hasImageView = false;
    private WigzoLayoutProperties wigzoLayoutProperties;
    private Typeface wigzoPrimaryTypeFace = null;
    private Typeface wigzoSecondayTypeFace = null;

    private Class<? extends AppCompatActivity> targetActivity = null;

    public WigzoDialogTemplate(Context context, String title, String body, HashMap<String,
            String> payload, Class<? extends AppCompatActivity> targetActivity) {
        super(context);
        this.context = context;
        this.title = title;
        this.body = body;
        this.targetActivity = targetActivity;
        this.payload = payload;
    }

    public WigzoDialogTemplate(Context context, String title, String body, HashMap<String,
            String> payload, Bitmap remote_picture, Class<? extends AppCompatActivity> targetActivity,
                               String layoutIdStr) {
        super(context);

        this.context = context;
        this.title = title;
        this.body = body;
        this.remote_picture = remote_picture;
        this.targetActivity = targetActivity;
        this.payload = payload;
        this.layoutIdStr = layoutIdStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        /*if (payload.containsKey("layoutId")) {
            layoutIdStr = payload.get("layoutId");
            layoutId = WigzoLayoutProperties.getWigzoLayoutId(layoutIdStr);
        } else {
            layoutId = R.layout.wigzo_dialog_template_1;
        }*/

        if (StringUtils.isNotEmpty(layoutIdStr)) {
            /*layoutIdStr = payload.get("layoutId");*/
            layoutId = WigzoLayoutProperties.getWigzoLayoutId(layoutIdStr);
        } else {
            layoutId = R.layout.wigzo_dialog_template_1;
        }

        setContentView(layoutId);

        wigzoLayoutProperties = WigzoLayoutProperties.getWigzoLayoutProperties(layoutId, context);

        hasImageView = wigzoLayoutProperties.hasImage;

        if (hasImageView) {
            notification_image = (ImageView) findViewById(R.id.notification_image);
        }

        if (null != remote_picture && hasImageView) {
                notification_image.setVisibility(View.VISIBLE);
                notification_image.setImageBitmap(remote_picture);
        }

        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);

        notification_title = (TextView) findViewById(R.id.notification_title);
        notification_body = (TextView) findViewById(R.id.notification_body);

        wigzoPrimaryTypeFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_regular);
        notification_title.setTypeface(wigzoPrimaryTypeFace);
        notification_title.setText(title);

        wigzoSecondayTypeFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_light);
        notification_body.setTypeface(wigzoSecondayTypeFace);
        notification_body.setText(body);

        yes.setTypeface(wigzoPrimaryTypeFace);
        no.setTypeface(wigzoPrimaryTypeFace);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_yes) {
            if (null != targetActivity) {
                Intent targetActivityIntent = new Intent(WigzoSDK.getInstance().getContext(), targetActivity);
                targetActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                WigzoSDK.getInstance().getContext().startActivity(targetActivityIntent);
            }
            dismiss();

        } else if (i == R.id.btn_no) {
            dismiss();

        } else {
        }
        dismiss();
    }
}
