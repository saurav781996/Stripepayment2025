package com.example.stripepayment2024;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Context context;
    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;
    Button pay_now, btn_payment_intent;
    TextView tv_payment_intent , tv_status;
    EditText et_description, et_shipping_name, et_address_line1, et_postal_code, et_city, et_state, et_country, et_currency, et_payment_method_type, et_amount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;
        init();
        clicks();

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        PaymentConfiguration.init(getApplicationContext(), getResources().getString(R.string.publishable_key));
        tv_status.setText("Not Initiated");


    }


    private void init() {
        pay_now = (Button) findViewById(R.id.pay_now);
        btn_payment_intent = (Button) findViewById(R.id.btn_payment_intent);
        tv_payment_intent = (TextView) findViewById(R.id.tv_payment_intent);
        tv_status = (TextView) findViewById(R.id.tv_status);
        et_amount = (EditText) findViewById(R.id.et_amount);
        et_description = findViewById(R.id.et_description);
        et_shipping_name = findViewById(R.id.et_shipping_name);
        et_address_line1 = findViewById(R.id.et_address_line1);
        et_postal_code = findViewById(R.id.et_postal_code);
        et_city = findViewById(R.id.et_city);
        et_state = findViewById(R.id.et_state);
        et_country = findViewById(R.id.et_country);
        et_currency = findViewById(R.id.et_currency);
        et_payment_method_type = findViewById(R.id.et_payment_method_type);
    }

    private void clicks() {

        pay_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentIntentClientSecret != null) {
                    paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, new PaymentSheet.Configuration("Merchant Name", configuration));
                } else {
                    Toast.makeText(context, "API is loading....Payment intent not found ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_payment_intent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, String> params = new HashMap<>();
                params.put("description", et_description.getText().toString());
                params.put("shipping[name]", et_shipping_name.getText().toString());
                params.put("shipping[address][line1]", et_address_line1.getText().toString());
                params.put("shipping[address][postal_code]", et_postal_code.getText().toString());
                params.put("shipping[address][city]", et_city.getText().toString());
                params.put("shipping[address][state]", et_state.getText().toString());
                params.put("shipping[address][country]", et_country.getText().toString());
                params.put("currency", et_currency.getText().toString());
                params.put("payment_method_types[]", et_payment_method_type.getText().toString());
                params.put("amount", et_amount.getText().toString());


                stripePaymentIntentRequestAPI(params);
            }
        });

    }

    private void stripePaymentIntentRequestAPI(Map<String, String> params) {

        String URL = "https://api.stripe.com/v1/payment_intents";
        String STRIPE_SECRET_KEY = getResources().getString(R.string.secret_key);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject json = new JSONObject(response);
                            getStripeResponse(json);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error:\n" + error.toString(), Toast.LENGTH_LONG).show();
                        Log.e("StripeError", error.toString());
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "Basic " + Base64.encodeToString(STRIPE_SECRET_KEY.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                Log.d("AUTH_HEADER", auth); // Debug: check if Authorization is correct
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        requestQueue.add(stringRequest);
    }

    private void getStripeResponse(JSONObject json) throws JSONException {


        String id = json.optString("id");
        String object = json.optString("object");
        String amount = String.valueOf(json.optInt("amount"));
        String amount_capturable = String.valueOf(json.optInt("amount_capturable"));

        JSONObject amount_details = json.optJSONObject("amount_details");
        String tip = "";
        if (amount_details != null) {
            JSONObject tipObj = amount_details.optJSONObject("tip");
            tip = (tipObj != null) ? tipObj.toString() : "";
        }

        String amount_received = String.valueOf(json.optInt("amount_received"));
        String application = json.optString("application", null);
        String application_fee_amount = json.optString("application_fee_amount", null);
        String automatic_payment_methods = json.optString("automatic_payment_methods", null);
        String canceled_at = json.optString("canceled_at", null);
        String cancellation_reason = json.optString("cancellation_reason", null);
        String capture_method = json.optString("capture_method");
        String client_secret = json.optString("client_secret");
        String confirmation_method = json.optString("confirmation_method");
        String created = String.valueOf(json.optLong("created"));
        String currency = json.optString("currency");
        String customer = json.optString("customer", null);
        String description = json.optString("description");
        String last_payment_error = json.optString("last_payment_error", null);
        String latest_charge = json.optString("latest_charge", null);
        String livemode = String.valueOf(json.optBoolean("livemode"));

        JSONObject metadata = json.optJSONObject("metadata");
        String metadataStr = (metadata != null) ? metadata.toString() : "";

        String next_action = json.optString("next_action", null);
        String on_behalf_of = json.optString("on_behalf_of", null);
        String payment_method = json.optString("payment_method", null);

        JSONObject payment_method_configuration_details = json.optJSONObject("payment_method_configuration_details");
        String payment_method_configuration_detailsStr = (payment_method_configuration_details != null) ? payment_method_configuration_details.toString() : "";

        JSONObject payment_method_options = json.optJSONObject("payment_method_options");
        String request_three_d_secure = "";
        if (payment_method_options != null) {
            JSONObject card = payment_method_options.optJSONObject("card");
            if (card != null) {
                request_three_d_secure = card.optString("request_three_d_secure");
            }
        }

        // Get payment_method_types array as string list
        String payment_method_types = "";
        if (json.has("payment_method_types")) {
            payment_method_types = json.getJSONArray("payment_method_types").join(", ").replace("\"", "");
        }

        String processing = json.optString("processing", null);
        String receipt_email = json.optString("receipt_email", null);
        String review = json.optString("review", null);
        String setup_future_usage = json.optString("setup_future_usage", null);

        JSONObject shipping = json.optJSONObject("shipping");
        String shipping_name = "";
        String shipping_phone = "";
        String shipping_carrier = "";
        String shipping_tracking_number = "";
        String shipping_address_city = "";
        String shipping_address_country = "";
        String shipping_address_line1 = "";
        String shipping_address_line2 = "";
        String shipping_address_postal_code = "";
        String shipping_address_state = "";

        if (shipping != null) {
            shipping_name = shipping.optString("name");
            shipping_phone = shipping.optString("phone", null);
            shipping_carrier = shipping.optString("carrier", null);
            shipping_tracking_number = shipping.optString("tracking_number", null);

            JSONObject shipping_address = shipping.optJSONObject("address");
            if (shipping_address != null) {
                shipping_address_city = shipping_address.optString("city");
                shipping_address_country = shipping_address.optString("country");
                shipping_address_line1 = shipping_address.optString("line1");
                shipping_address_line2 = shipping_address.optString("line2", null);
                shipping_address_postal_code = shipping_address.optString("postal_code");
                shipping_address_state = shipping_address.optString("state");
            }
        }

        String source = json.optString("source", null);
        String statement_descriptor = json.optString("statement_descriptor", null);
        String statement_descriptor_suffix = json.optString("statement_descriptor_suffix", null);
        String status = json.optString("status");
        String transfer_data = json.optString("transfer_data", null);
        String transfer_group = json.optString("transfer_group", null);


        paymentIntentClientSecret = client_secret;
        tv_payment_intent.setText(paymentIntentClientSecret);
        tv_status.setText("client_secret: " + paymentIntentClientSecret);

    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(context, "Canceled ", Toast.LENGTH_SHORT).show();
            tv_status.setText("Payment: Canceled");
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(context, ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("TestingApp", "Payment Error: " + ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage());
            tv_status.setText("Payment: Error");
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(context, "Completed ", Toast.LENGTH_SHORT).show();
            tv_status.setText("Payment: Completed");
        }
    }

}










