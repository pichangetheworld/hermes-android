package com.twyngle.hermes;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * @author pchan
 */
public class WelcomeSplashActivity extends FragmentActivity
		implements OnClickListener {

	/**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity_splash);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
		Intent intent = new Intent(this, SigninActivity.class);

		startActivity(intent);
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
        	ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        	if (position == NUM_PAGES-1) {
        		fragment.setOnClickListener(WelcomeSplashActivity.this);
        	}
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

	@Override
	public void onClick(View view) {
		Intent intent = new Intent(this, SigninActivity.class);

		startActivity(intent);
	}

}