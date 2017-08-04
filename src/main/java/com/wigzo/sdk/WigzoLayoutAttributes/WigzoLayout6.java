package com.wigzo.sdk.WigzoLayoutAttributes;

import android.content.Context;

import com.wigzo.sdk.R;
import com.wigzo.sdk.helpers.WigzoTypeFace;

/**
 * Created by wigzo on 4/8/17.
 */

public class WigzoLayout6 extends WigzoLayoutProperties {
    private WigzoLayout6(Context context) {
        hasImage = false;
        wigzoTitleFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_bold);
        wigzoBodyFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_light);
    }

    public static WigzoLayout6 getInstance(Context context) {
        return new WigzoLayout6(context);
    }
}
