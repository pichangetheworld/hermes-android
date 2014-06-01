package com.twyngle.hermes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class TicketInfoActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		SharedPreferences prefs = getSharedPreferences("hermes", MODE_PRIVATE);
		long last = prefs.getLong("lastTimestamp", -1);
		if (last < 0) {
			last = System.currentTimeMillis();
		}

		TextView timeIn = (TextView) this.findViewById(R.id.timeIn);
		timeIn.setText(new SimpleDateFormat("h:mm a", Locale.getDefault())
			.format(new Date(last)));
		
		TextView timeOut = (TextView) this.findViewById(R.id.timeOut);
		timeOut.setText(new SimpleDateFormat("h:mm a", Locale.getDefault())
			.format(new Date(last + 3 * 60 * 60 * 1000)));
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.get("http://myttc.ca/queens_park_station.json",
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						// This should be a stringified Json object
						try {
							JSONObject obj = new JSONObject(response);
							String nextTrainTime = obj.getJSONArray("stops")
			                 .getJSONObject(6)
			                 .getJSONArray("routes")
			                 .getJSONObject(0)
			                 .getJSONArray("stop_times")
			                 .getJSONObject(1)
			                 .getString("departure_time");
							
							TextView timeIn = 
									(TextView) TicketInfoActivity.this.findViewById(R.id.nextTrain);
							timeIn.setText(nextTrainTime);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
    }
    
    public void onClick(View view) {
		onBackPressed();
    }
}
