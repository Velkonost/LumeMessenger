package com.velkonost.lume;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author Velkonost
 */

public class FontsOverride {
    public static void setDefaultFont(Context context,
                                      String staticTypefaceFieldName) {
        final Typeface regular =  Typeface.createFromAsset(
                context.getAssets(),
                String.format(Locale.US, "fonts/%s", "OpenSans-Light.ttf"));

        replaceFont(staticTypefaceFieldName, regular);
    }

    protected static void replaceFont(String staticTypefaceFieldName,
                                      final Typeface newTypeface) {
        try {
            final Field staticField = Typeface.class
                    .getDeclaredField(staticTypefaceFieldName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeface);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
