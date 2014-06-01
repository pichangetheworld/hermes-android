package com.twyngle.hermes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TicketInfoActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		TextView timeIn = (TextView) this.findViewById(R.id.timeIn);
		timeIn.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault())
			.format(new Date()));
		
		TextView timeOut = (TextView) this.findViewById(R.id.timeOut);
		timeOut.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault())
			.format(new Date(System.currentTimeMillis() + 3 * 60 * 60 * 1000)));
    }
    
    public void onClick(View view) {
		onBackPressed();
    }
}
