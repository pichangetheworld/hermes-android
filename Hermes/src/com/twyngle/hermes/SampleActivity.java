package com.twyngle.hermes;

import java.math.BigDecimal;
import java.util.Collection;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

/**
 * Basic sample using the SDK to make a payment or consent to future payments.
 * 
 * For sample mobile backend interactions, see
 * https://github.com/paypal/rest-api
 * -sdk-python/tree/master/samples/mobile_backend
 */
public class SampleActivity extends Activity implements IBeaconConsumer {
	private static final String TAG = "paymentExample";

	/**
	 * - Set to PaymentActivity.ENVIRONMENT_PRODUCTION to move real money.
	 * 
	 * - Set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials
	 * from https://developer.paypal.com
	 * 
	 * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
	 * without communicating to PayPal's servers.
	 */

	private static final String _CONFIG_ENVIRONMENT_ = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
	private static final String _CONFIG_CLIENT_ID_ = "AcFhHBA99EPDraZXP-WeG4yq6joYVTeg7DPcghNMJWkjlWblM_2mvTkosi7f";

	// note that these credentials will differ between live & sandbox
	// environments.
	private static final int REQUEST_CODE_PAYMENT = 1;
	private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(_CONFIG_ENVIRONMENT_).clientId(_CONFIG_CLIENT_ID_)
			// The following are only used in PayPalFuturePaymentActivity.
			.merchantName("hermes")
			.merchantPrivacyPolicyUri(Uri.parse("http://hermes.ngrok.com/"))
			.merchantUserAgreementUri(Uri.parse("http://hermes.ngrok.com/"));
	
	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);
	
	private TextView textview;
	private LinearLayout layout;
	private static int numTokens = 200;
	
	private static long timestamp = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		layout = (LinearLayout) this.findViewById(R.id.wrapper);
		textview = (TextView) this.findViewById(R.id.numTokensText);
		textview.setText(getResources()
				.getQuantityString(
						R.plurals.num_tokens_remaining,
						numTokens,
						numTokens));

		iBeaconManager.bind(this);
		
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
	}

	private PayPalPayment getThingToBuy(String paymentIntent) {
		return new PayPalPayment(new BigDecimal("3.00"), "CAD", "TTC fare",
				paymentIntent);
	}

	public void onFuturePaymentPressed(View pressed) {
		Intent intent = new Intent(SampleActivity.this,
				PayPalFuturePaymentActivity.class);

		startActivityForResult(intent, REQUEST_CODE_FUTURE_PAYMENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PAYMENT) {
			if (resultCode == Activity.RESULT_OK) {
				PaymentConfirmation confirm = data
						.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
				if (confirm != null) {
					try {
						Log.i(TAG, confirm.toJSONObject().toString(4));
						Log.i(TAG, confirm.getPayment().toJSONObject()
								.toString(4));
						/**
						 * TODO: send 'confirm' (and possibly
						 * confirm.getPayment() to your server for verification
						 * or consent completion. See
						 * https://developer.paypal.com
						 * /webapps/developer/docs/integration
						 * /mobile/verify-mobile-payment/ for more details.
						 * 
						 * For sample mobile backend interactions, see
						 * https://github
						 * .com/paypal/rest-api-sdk-python/tree/master
						 * /samples/mobile_backend
						 */
						Toast.makeText(
								getApplicationContext(),
								"PaymentConfirmation info received from PayPal",
								Toast.LENGTH_LONG).show();

					} catch (JSONException e) {
						Log.e(TAG, "an extremely unlikely failure occurred: ",
								e);
					}
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Log.i(TAG, "The user canceled.");
			} else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
				Log.i(TAG,
						"An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
			}
		} else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
			if (resultCode == Activity.RESULT_OK) {
				PayPalAuthorization auth = data
						.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
				if (auth != null) {
					try {
						Log.i("FuturePaymentExample", auth.toJSONObject()
								.toString(4));

						String authorization_code = auth.getAuthorizationCode();
						Log.i("FuturePaymentExample", authorization_code);

						sendAuthorizationToServer(auth);
						Toast.makeText(getApplicationContext(),
								"Future Payment code received from PayPal",
								Toast.LENGTH_LONG).show();

					} catch (JSONException e) {
						Log.e("FuturePaymentExample",
								"an extremely unlikely failure occurred: ", e);
					}
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Log.i("FuturePaymentExample", "The user canceled.");
			} else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
				Log.i("FuturePaymentExample",
						"Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
			}
		}
	}

	private void sendAuthorizationToServer(PayPalAuthorization authorization) {

		/**
		 * TODO: Send the authorization response to your server, where it can
		 * exchange the authorization code for OAuth access and refresh tokens.
		 * 
		 * Your server must then store these tokens, so that your server code
		 * can execute payments for this user in the future.
		 * 
		 * A more complete example that includes the required app-server to
		 * PayPal-server integration is available from
		 * https://github.com/paypal/
		 * rest-api-sdk-python/tree/master/samples/mobile_backend
		 */
		System.out.println("(pchan) here's something about auth code:"
				+ authorization.getAuthorizationCode());

		RequestParams params = new RequestParams("auth_code",
				authorization.getAuthorizationCode());

		AsyncHttpClient client = new AsyncHttpClient();
		client.post("http://hermes.ngrok.com/paypal/consent", params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						// This should be a stringified Json object
						System.out.println(response);
					}
				});

	}

	public void onFuturePaymentPurchasePressed(View pressed) {
		// Get the Application Correlation ID from the SDK
		String correlationId = PayPalConfiguration
				.getApplicationCorrelationId(this);

		Log.i("FuturePaymentExample", "Application Correlation ID: "
				+ correlationId);

		// TODO: Send correlationId and transaction details to your server for
		// processing with PayPal...
		Toast.makeText(getApplicationContext(),
				"App Correlation ID received from SDK", Toast.LENGTH_LONG)
				.show();
	}

	@Override
	public void onDestroy() {
		// Stop service when done
		stopService(new Intent(this, PayPalService.class));
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
					
					if (iBeacons.iterator().next().getRssi() > -40) {
						if (numTokens > 0 && timestamp < System.currentTimeMillis() - 10*1000) {
							--numTokens;
							timestamp = System.currentTimeMillis();

							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(getApplicationContext(),
											"SUCCESSFULLY PAID THE IPAD", Toast.LENGTH_LONG)
											.show();

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
				}
				
				 for (IBeacon beacon : iBeacons) {
					 if (beacon.getMinor() == 1) {
						 // TODO: print something about current station
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
			iBeaconManager.startRangingBeaconsInRegion(new Region(
					"myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
		}
	}
}
