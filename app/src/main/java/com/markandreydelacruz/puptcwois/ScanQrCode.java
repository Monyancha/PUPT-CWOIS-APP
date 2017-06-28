package com.markandreydelacruz.puptcwois;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cz.msebera.android.httpclient.Header;

public class ScanQrCode extends AppCompatActivity {

    private String equip_id;
    private String name;
    private String serialNumber;
    private String description;
    private String originalLocation;
    private String firstname;
    private String lastname;
    private String HOST;
    private String status;
    MaterialBetterSpinner spinnerStatus, spinnerLocation;
    LinearLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanqrcode);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Scan Results");

        HOST = getIntent().getExtras().getString("HOST");

        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);

        new IntentIntegrator(ScanQrCode.this)
                .setBeepEnabled(true)
                .setPrompt("Place a QR Code inside the rectangle to scan it.")
                .initiateScan();

        spinnerStatus = (MaterialBetterSpinner) findViewById(R.id.spinnerStatus);
        ArrayAdapter<String> spinnerStatusArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, STATUS);
        spinnerStatus.setAdapter(spinnerStatusArrayAdapter);

        spinnerLocation = (MaterialBetterSpinner) findViewById(R.id.spinnerLocation);
        ArrayAdapter<String> spinnerLocationArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, LOCATION);
        spinnerLocation.setAdapter(spinnerLocationArrayAdapter);


        final Button buttonScanAgain = (Button) findViewById(R.id.buttonScanAgain);
        buttonScanAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(ScanQrCode.this)
                        .setBeepEnabled(true)
                        .setPrompt("Place a QR Code inside the rectangle to scan it.")
                        .initiateScan();
            }
        });

        Button buttonSaveUpdate = (Button) findViewById(R.id.buttonSaveUpdate);
        buttonSaveUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdate();
            }
        });
    }

    private void saveUpdate() {
        String status, location;
        status = spinnerStatus.getText().toString();
        location = spinnerLocation.getText().toString();

        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("status", status);
        params.put("location", location);
        params.put("equip_id", equip_id);
        asyncHttpClient.post(ScanQrCode.this, HOST + "api/updateScanDetails.php", params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(ScanQrCode.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Saving...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getApplicationContext(), "Process Canceled", Toast.LENGTH_SHORT).show();
                        asyncHttpClient.cancelAllRequests(true);
                    }
                });
                dialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                String status = null;
                String message = null;
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                if(responseBody == null) {
                    Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(responseBody));

                        int count = 0;
                        while(count < jsonObject.length()){
                            status = jsonObject.getString("status");
                            message = jsonObject.getString("message");
                            count++;
                        }
                        if(status.equals("success")){
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Connection failed. Try again.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                new BackgroundTaskScanQrCode().execute();
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            }
        });
    }

    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
//                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
                finish();
            } else {
                serialNumber = result.getContents();
                new BackgroundTaskScanQrCode().execute();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public class BackgroundTaskScanQrCode extends AsyncTask<Void, Void, String> {
        String jsonUrl;
        String jsonString;
        ProgressDialog dialog;
        private JSONObject jsonObject;
        private JSONArray jsonArray;

        @Override
        protected void onPreExecute() {
            jsonUrl = HOST + "api/scanItem.php?serialNumber=\""+serialNumber+"\"";
            super.onPreExecute();
            dialog = new ProgressDialog(ScanQrCode.this);
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(jsonUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setConnectTimeout(5_000);
                httpURLConnection.setReadTimeout(5_000);
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                while((jsonString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(jsonString + "\n");
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                SystemClock.sleep(500);

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if(jsonString == null) {
                Toast.makeText(getApplicationContext(), "Connection Failed. Try Again.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                try {
                    jsonObject = new JSONObject(jsonString);
                    jsonArray = jsonObject.getJSONObject("data").getJSONArray("itemDetails");
                    int count = 0;
                    while(count < jsonArray.length()){
                        JSONObject jsonObject = jsonArray.getJSONObject(count);
                        equip_id = jsonObject.getString("equipId");
                        name = jsonObject.getString("name");
                        serialNumber = jsonObject.getString("serialNumber");
                        description = jsonObject.getString("description");
                        originalLocation = jsonObject.getString("username");
                        firstname = jsonObject.getString("firstname");
                        lastname = jsonObject.getString("lastname");
                        status = jsonObject.getString("status");
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Item Not Found.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                setUpUIViews();
            }
        }
    }

    private static final String[] STATUS = new String[] {
            "Working", "Good Condition", "Defective", "Damaged", "Missing", "Transferred", "Under Maintenance", "For Testing", "Condemned"
    };

    private static final String[] LOCATION = new String[] {
            "DOSTLAB", "ABOITIZ", "REGISTRAR", "DIRECTOR", "HSS", "LIBRARY", "DENTAL", "MEDICAL", "SECURITY", "ACCOUNTING", "HAP", "CSC", "FACULTY", "GUIDANCE"
    };

    private void setUpUIViews() {
        TextView tx_item_id = (TextView) findViewById(R.id.tx_equip_id);
        tx_item_id.setText(equip_id);
        TextView tx_description = (TextView) findViewById(R.id.tx_name);
        tx_description.setText(name);
        TextView tx_partNumber = (TextView) findViewById(R.id.tx_serialNumber);
        tx_partNumber.setText(serialNumber);
        TextView tx_boxNumber = (TextView) findViewById(R.id.tx_description);
        tx_boxNumber.setText(description);
        TextView tx_orderPoint = (TextView) findViewById(R.id.tx_originalLocation);
        tx_orderPoint.setText(originalLocation);
        TextView tx_stockOnHand = (TextView) findViewById(R.id.tx_personInCharge);
        tx_stockOnHand.setText(firstname + " " + lastname);
        emptyLayout.requestFocus();
        spinnerStatus.setText(status);
        spinnerLocation.setText(originalLocation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
