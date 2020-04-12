package com.example.xyzreader.transform;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.example.xyzreader.util.Constants;

public final class CubeOutPageTransformer implements ViewPager.PageTransformer {

    private static final int ROTATION_NINETY = 90;
    private static final int ROTATION_NINETY_N = -90;

    @Override
    public void transformPage(@NonNull View view, float position) {
        if (position < -1) {    // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(Constants.ALPHA_ZERO);

        } else if (position <= 0) {    // [-1,0]
            view.setAlpha(Constants.ALPHA_ONE);
            view.setPivotX(view.getWidth());
            view.setRotationY(ROTATION_NINETY_N * Math.abs(position));

        } else if (position <= 1) {    // (0,1]
            view.setAlpha(Constants.ALPHA_ONE);
            view.setPivotX(Constants.PIVOT_X);
            view.setRotationY(ROTATION_NINETY * Math.abs(position));

        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(Constants.ALPHA_ZERO);
        }
    }
}