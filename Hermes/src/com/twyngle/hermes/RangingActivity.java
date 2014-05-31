package com.twyngle.hermes;

import java.util.Collection;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class RangingActivity extends Activity implements IBeaconConsumer {
	
	private static int numTokens = 5;
	
	protected static final String TAG = "RangingActivity";
	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranging);
		iBeaconManager.bind(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		iBeaconManager.unBind(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, false);
	}

	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				if (iBeacons.size() > 0) {
					IBeacon next = iBeacons.iterator().next();
					logToDisplay("The first iBeacon I see is about "
							+ next.getAccuracy()
							+ " meters away. rssi:" + next.getRssi()
							+ " txpower:" + next.getTxPower()
							+ " prox:" + next.getProximity()
							);
					
					--numTokens;
				}
				
				 for (IBeacon beacon : iBeacons) {
					 if (beacon.getMinor() == 1) {
						 // TODO: print something about current station
					 }
				 }
			}

		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region(
					"myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
		}
	}

	private void logToDisplay(final String line) {
		runOnUiThread(new Runnable() {
			public void run() {
				TextView tokens_remaining = (TextView) RangingActivity.this
						.findViewById(R.id.rangingText);
				tokens_remaining.setText(numTokens + " tokens remaining");
			}
		});
	}
}