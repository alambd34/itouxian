package com.itouxian.android.util;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by chenjishi on 14-3-6.
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {

    @SuppressWarnings("NewApi")
    @Override
    public void transformPage(View view, float v) {
        int pageWidth = view.getWidth();
        if (v < -1) {
            view.setAlpha(0);
        } else if (v <= 0) {
            view.setAlpha(1);
            view.setTranslationX(0);
        } else if (v <= 1) {
            view.setAlpha(1 - v);
            view.setTranslationX(pageWidth * -v);
        } else {
            view.setAlpha(0);
        }
    }
}
