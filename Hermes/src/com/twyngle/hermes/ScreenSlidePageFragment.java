package com.twyngle.hermes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class ScreenSlidePageFragment extends Fragment {
	private static OnClickListener onClickListener = null;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        return rootView;
    }

    public void setOnClickListener(OnClickListener listener) {
    	onClickListener = listener;
    }
    
    public void onClick(View view) {
    	if (onClickListener != null) {
    		onClickListener.onClick(view);
    	}
    }
}
