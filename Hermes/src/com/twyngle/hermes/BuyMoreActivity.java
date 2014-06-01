package com.twyngle.hermes;

import java.math.BigDecimal;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class BuyMoreActivity extends Activity {

	private static final double TOKEN_BULK_COSTS[] = { 0, 3, 13.5, 27 };
	private static final String _CLIENT_ID_ = "AZ0I8hD-o3xE9MN-MpiWQ_0r8LLsunv1DUboaohZHoCN9Hx0hgS7n5RSfBZn";
	
	private static TextView tokenCost;
	private static TextView ticketCost;
	private static TextView donateCost;
	private static TextView totalCost;

	private static int tokensToBuy = 1;
	private static int buyMonthlyPass = 0;
	private static int buyForward = 0;
	private static double total = 0;

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
			.clientId(_CLIENT_ID_);

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_more_activity);

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		tokenCost = (TextView) this.findViewById(R.id.costTokens);
		tokenCost.setText(String.format(
				getResources().getString(R.string.buycost),
				TOKEN_BULK_COSTS[tokensToBuy]));

		ticketCost = (TextView) this.findViewById(R.id.costTickets);
		ticketCost.setText(String.format(
				getResources().getString(R.string.buycost),
				133.75 * buyMonthlyPass));

		donateCost = (TextView) this.findViewById(R.id.costDonateForward);
		donateCost.setText(String.format(
				getResources().getString(R.string.buycost), 3.0 * buyForward));

		totalCost = (TextView) this.findViewById(R.id.costTotal);
		total = TOKEN_BULK_COSTS[tokensToBuy] + 133.75 * buyMonthlyPass + 3.0
				* buyForward;
		totalCost.setText(String.format(
				getResources().getString(R.string.buycost),
				total));

		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);

	}

	@Override
	public void onDestroy() {
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}

	public void onBack(View view) {
		onBackPressed();
	}

	public void onBuy(View view) {
	    // PAYMENT_INTENT_SALE will cause the payment to complete immediately.
	    // Change PAYMENT_INTENT_SALE to PAYMENT_INTENT_AUTHORIZE to only authorize payment and 
	    // capture funds later.

	    PayPalPayment payment = new PayPalPayment(new BigDecimal(total),
	    		"CAD", "TTC payment",
	            PayPalPayment.PAYMENT_INTENT_SALE);

	    Intent intent = new Intent(this, PaymentActivity.class);

	    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

	    startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	    if (resultCode == Activity.RESULT_OK) {
	        PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
	        if (confirm != null) {
	            try {
	                Log.i("paymentExample", confirm.toJSONObject().toString(4));

	                // TODO: send 'confirm' to your server for verification.
	                // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
	                // for more details.

	        		onBackPressed();
	        		
	        		runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(
									getApplicationContext(),
									"Successfully completed payments",
									Toast.LENGTH_LONG).show();
						}
					});

	            } catch (JSONException e) {
	                Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
	            }
	        }
	    }
	    else if (resultCode == Activity.RESULT_CANCELED) {
	        Log.i("paymentExample", "The user canceled.");
	    }
	    else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
	        Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
	    }
	}

}
