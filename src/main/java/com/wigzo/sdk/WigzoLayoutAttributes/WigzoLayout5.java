package com.wigzo.sdk.WigzoLayoutAttributes;

import android.content.Context;

import com.wigzo.sdk.R;
import com.wigzo.sdk.helpers.WigzoTypeFace;

/**
 * Created by wigzo on 4/8/17.
 */

public class WigzoLayout5 extends WigzoLayoutProperties {
    private WigzoLayout5(Context context) {
        hasImage = false;
        wigzoTitleFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_bold);
        wigzoBodyFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_regular);
    }

    public static WigzoLayout5 getInstance(Context context) {
        return new WigzoLayout5(context);
    }
}
