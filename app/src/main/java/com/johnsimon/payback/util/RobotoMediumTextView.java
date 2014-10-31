package com.johnsimon.payback.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.johnsimon.payback.util.FontCache;

public class RobotoMediumTextView extends TextView {

    public RobotoMediumTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		setTypeface(FontCache.get(getContext(), "robotomedium.ttf"));
    }

    public RobotoMediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotoMediumTextView(Context context) {
        super(context);
    }

}