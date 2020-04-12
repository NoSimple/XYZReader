package com.example.xyzreader.transform;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public final class PopPageTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(@NonNull View view, float position) {

        view.setTranslationX(-position * view.getWidth());
        if (Math.abs(position) < 0.5) {
            view.setVisibility(View.VISIBLE);
            view.setScaleX(1 - Math.abs(position));
            view.setScaleY(1 - Math.abs(position));
        } else if (Math.abs(position) > 0.5) {
            view.setVisibility(View.GONE);
        }
    }
}