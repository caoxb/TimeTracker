package com.example.data;

import android.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

public class ABTabListener implements ActionBar.TabListener {
    private ViewPager mViewPager;
    
    public ABTabListener(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        int pos = tab.getPosition();
        mViewPager.setCurrentItem(pos);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }
}
