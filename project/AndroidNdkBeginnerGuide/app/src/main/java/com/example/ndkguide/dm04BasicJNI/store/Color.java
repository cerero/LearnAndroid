package com.example.ndkguide.dm04BasicJNI.store;

import android.text.TextUtils;

public class Color {
    private int mColor;

    public Color(String pColor) {
        if (TextUtils.isEmpty(pColor)) {
            throw new IllegalArgumentException();
        }
        mColor = android.graphics.Color.parseColor(pColor);
    }

    @Override
    public String toString() {
        return String.format("#%06X", mColor);
    }
}
