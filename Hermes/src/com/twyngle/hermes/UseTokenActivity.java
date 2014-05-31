package com.twyngle.hermes;

import java.util.Collection;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class UseTokenActivity extends Activity implements IBeaconConsumer {

	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);
	
	private TextView textview;
	private RelativeLayout layout;
	private static int numTokens = 200;
	
	private static long timestamp = -1;
	
	private static final String _VENDOR_UUID_ = "282F191E-D981-48EA-A887-3E27A7D12316";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tokencount);
		
		layout = (RelativeLayout) this.findViewById(R.id.wrapper);
		textview = (TextView) this.findViewById(R.id.numTokensText);
		textview.setText(getResources()
				.getQuantityString(
						R.plurals.num_tokens_remaining,
						numTokens,
						numTokens));

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
					
					for (IBeacon beacon : iBeacons) {
						if (beacon.getMinor() == 5) {
							// TODO: print something about current station
							if (beacon.getRssi() > -40) {
								if (numTokens > 0 &&
										timestamp < System.currentTimeMillis() - 10 * 1000) {
									--numTokens;
									timestamp = System.currentTimeMillis();

									runOnUiThread(new Runnable() {
										public void run() {
											Toast.makeText(
													getApplicationContext(),
													"SUCCESSFULLY PAID THE IPAD",
													Toast.LENGTH_LONG).show();

											layout.setBackgroundColor(Color.GREEN);

											textview.setText(getResources()
													.getQuantityString(
															R.plurals.num_tokens_remaining,
															numTokens,
															numTokens));
										}
									});
								}
							}
							break;
						}
					}
				}
				 
				if (timestamp < System.currentTimeMillis() - 1*1000) {
					runOnUiThread(new Runnable() {
						public void run() {
							layout.setBackgroundColor(Color.TRANSPARENT);
						}
					});
				}
			}
		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("TTC Vendor",
					_VENDOR_UUID_, null, null));
		} catch (RemoteException e) {
		}
	}
}