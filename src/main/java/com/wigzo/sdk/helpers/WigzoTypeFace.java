package com.wigzo.sdk.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wigzo on 25/7/17.
 */

public class WigzoTypeFace {
    public static Typeface getFontFromRes(Context context, int resource)
    {
        Typeface tf = null;
        InputStream is = null;
        try {
            is = context.getResources().openRawResource(resource);
        }
        catch(Resources.NotFoundException e) {
            Log.e("WIGZO", "Could not find font in resources!");
        }

        String outPath = context.getCacheDir() + "/tmp" + System.currentTimeMillis() + ".raw";

        try
        {
            byte[] buffer = new byte[is.available()];
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outPath));

            int l = 0;
            while((l = is.read(buffer)) > 0)
                bos.write(buffer, 0, l);

            bos.close();

            tf = Typeface.createFromFile(outPath);

            // clean up
            new File(outPath).delete();
        }
        catch (IOException e)
        {
            Log.e("WIGZO", "Error reading in font!");
            return null;
        }

        Log.d("WIGZO", "Successfully loaded font.");

        return tf;
    }
}
