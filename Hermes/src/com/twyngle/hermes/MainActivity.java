package com.twyngle.hermes;

import java.util.Collection;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MainActivity extends Activity implements IBeaconConsumer {

	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);
	
	private TextView textview;
	private static int numTokens = 20;
	private static int numMonthlyPass = 0;
	
	private static long timestamp = -1;
	
	private static final String _VENDOR_UUID_ = "282F191E-D981-48EA-A887-3E27A7D12316";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		textview = (TextView) this.findViewById(R.id.numTokensText);
		textview.setText(
				String.format(getResources().getString(R.string.count), numTokens));
		((TextView) this.findViewById(R.id.numMonthlyPass)).setText(
				String.format(getResources().getString(R.string.count), numMonthlyPass));

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
		
		if (timestamp > System.currentTimeMillis() - 15 * 1000) {
			runOnUiThread(new Runnable() {
				public void run() {
					TextView ready = (TextView) MainActivity.this
							.findViewById(R.id.readynow);
					ready.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(MainActivity.this,
									TicketInfoActivity.class);
							startActivity(intent);
						}
					});
					ready.setText("Open ticket");
				}
			});
		}
	}

	@Override
	public void onIBeaconServiceConnect() {
		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {
				if (iBeacons.size() > 0) {
					System.out.println("(pchan) last time was " + timestamp + " now:" + System.currentTimeMillis());
					boolean launch = false;
					for (IBeacon beacon : iBeacons) {
						if (beacon.getMinor() == 5) {
							if (beacon.getRssi() > -40) {
								if (numTokens > 0 &&
										timestamp < System.currentTimeMillis() - 15 * 1000) {
									--numTokens;
									timestamp = System.currentTimeMillis();
									launch = true;

									runOnUiThread(new Runnable() {
										public void run() {
											Toast.makeText(
													getApplicationContext(),
													"Successfully paid the TTC!",
													Toast.LENGTH_LONG).show();

											textview.setText(
													String.format(getResources().getString(R.string.count), numTokens));
										}
									});
								} else if (timestamp > System.currentTimeMillis() - 15 * 1000) {
									runOnUiThread(new Runnable() {
										public void run() {
											TextView ready = (TextView) MainActivity.this.findViewById(R.id.readynow);
											ready.setOnClickListener(new OnClickListener() {
													@Override
													public void onClick(View v) {
														Intent intent = new Intent(MainActivity.this,
																TicketInfoActivity.class);
														startActivity(intent);
													}
												});
											ready.setText("Open ticket");
										}
									});
								}
							}
							break;
						}
					}
					
					if (launch) {
						Intent intent = new Intent(MainActivity.this,
								TicketInfoActivity.class);
						startActivity(intent);
					}
				}
			}
		});

		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("TTC Vendor",
					_VENDOR_UUID_, null, null));
		} catch (RemoteException e) {
		}
	}

	public void buyMoreTokens(View view) {
		startActivity(new Intent(this, BuyMoreActivity.class));
	}

}
