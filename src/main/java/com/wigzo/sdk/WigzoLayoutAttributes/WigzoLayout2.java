package com.wigzo.sdk.WigzoLayoutAttributes;

import android.content.Context;

import com.wigzo.sdk.R;
import com.wigzo.sdk.helpers.WigzoTypeFace;

/**
 * Created by wigzo on 4/8/17.
 */

public class WigzoLayout2 extends WigzoLayoutProperties {
    private WigzoLayout2(Context context) {
        hasImage = true;
        wigzoTitleFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_regular);
        wigzoBodyFace = WigzoTypeFace.getFontFromRes(context, R.raw.wigzo_brown_light);
    }

    public static WigzoLayout2 getInstance(Context context) {
        return new WigzoLayout2(context);
    }
}
