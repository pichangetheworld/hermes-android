package com.twyngle.hermes;

import java.math.BigDecimal;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
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

	private static int tokensToBuy;
	private static int buyMonthlyPass = 0;
	private static int buyForward = 0;
	private static double total = 0;
	
	private static RadioGroup tokenCountGroup;
	private static RadioGroup ticketCountGroup;
	private static RadioGroup forwardCountGroup;

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
			.clientId(_CLIENT_ID_);

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_more_activity);

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		tokenCountGroup = (RadioGroup) this.findViewById(R.id.radioGroup1);
		ticketCountGroup = (RadioGroup) this.findViewById(R.id.buytickets);
		forwardCountGroup = (RadioGroup) this.findViewById(R.id.buyforward);
		OnCheckedChangeListener listener = new OnCheckedChangeListener() 
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	    		updateTotal();
	        }
	    };
		tokenCountGroup.setOnCheckedChangeListener(listener);
		ticketCountGroup.setOnCheckedChangeListener(listener);
		forwardCountGroup.setOnCheckedChangeListener(listener);
		tokenCost = (TextView) this.findViewById(R.id.costTokens);
		ticketCost = (TextView) this.findViewById(R.id.costTickets);
		donateCost = (TextView) this.findViewById(R.id.costDonateForward);
		totalCost = (TextView) this.findViewById(R.id.costTotal);

		updateTotal();
		
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
	}
	
	private int buttonIdToIndex(int buttonId) {
		switch (buttonId) {
		case R.id.radioButton0:
			return 0;
		case R.id.radioButton1:
			return 1;
		case R.id.radioButton5:
			return 2;
		case R.id.radioButton10:
			return 3;
		case R.id.ticketsNo:
			return 0;
		case R.id.ticketsYes:
			return 1;
		case R.id.forwardNo:
			return 0;
		case R.id.forwardYes:
			return 1;
		default:
			return 1;
		}
	}
	
	private void updateTotal() {
		tokensToBuy = buttonIdToIndex(tokenCountGroup.getCheckedRadioButtonId());
		buyMonthlyPass = buttonIdToIndex(ticketCountGroup.getCheckedRadioButtonId());
		buyForward = buttonIdToIndex(forwardCountGroup.getCheckedRadioButtonId());

		tokenCost.setText(String.format(
				getResources().getString(R.string.buycost),
				TOKEN_BULK_COSTS[tokensToBuy]));

		ticketCost.setText(String.format(
				getResources().getString(R.string.buycost),
				133.75 * buyMonthlyPass));

		donateCost.setText(String.format(
				getResources().getString(R.string.buycost), 3.0 * buyForward));

		total = TOKEN_BULK_COSTS[tokensToBuy] + 133.75 * buyMonthlyPass + 3.0
				* buyForward;
		totalCost.setText(String.format(
				getResources().getString(R.string.buycost),
				total));
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

	        		SharedPreferences prefs = getSharedPreferences("hermes", MODE_PRIVATE);
	        		int t = prefs.getInt("numTokens", 0);
	        		prefs.edit().putInt("numTokens", t + tokensToBuy).commit();
	        		System.out.println("pchan: had " + t + " tokens now have " + (t + tokensToBuy));
	                
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
