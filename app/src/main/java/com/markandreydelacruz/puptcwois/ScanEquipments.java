package com.markandreydelacruz.puptcwois;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class ScanEquipments extends AppCompatActivity implements  AdapterView.OnItemSelectedListener {
    private String HOST, date, time;
    private String URL_START_SCANNING;
    private int location_id;
    private String locationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanequipments);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Scan Equipments");
        HOST = getIntent().getExtras().getString("HOST");
        URL_START_SCANNING = HOST + "api/startScanning.php";
        date = getIntent().getExtras().getString("date");
        time = getIntent().getExtras().getString("time");

        TextView textViewDate = (TextView) findViewById(R.id.textViewDate);
        TextView textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewDate.setText(date);
        textViewTime.setText(time);

        Spinner spinnerLocation = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter  adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, LOCATION);
        spinnerLocation.setAdapter(adapter);
        spinnerLocation.setOnItemSelectedListener(this);

        Button buttonStartBatchScan = (Button) findViewById(R.id.buttonStartBatchScan);
        buttonStartBatchScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning(URL_START_SCANNING);
            }
        });
    }

    private void startScanning(String URL_START_SCANNING) {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        RequestParams params = new RequestParams();
        params.put("location_id", location_id);
        params.put("date", date);
        params.put("time", time);
        asyncHttpClient.post(ScanEquipments.this, URL_START_SCANNING, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(ScanEquipments.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Please wait...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getApplicationContext(), "Process Canceled", Toast.LENGTH_SHORT).show();
                        asyncHttpClient.cancelAllRequests(true);
                    }
                });
                dialog.show();
            }

            @Override
            public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
                super.onPreProcessResponse(instance, response);
                SystemClock.sleep(500);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, final byte[] responseBody) {
                String status = null;
                String report_id = null;
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
                            report_id = jsonObject.getString("report_id");
                            count++;
                        }
                        if (status != null) {
                            if(status.equals("success")){
                                Intent intent = new Intent(ScanEquipments.this, BatchScan.class);
                                intent.putExtra("HOST", HOST);
                                intent.putExtra("locationName", locationName);
                                intent.putExtra("report_id", report_id);
                                startActivity(intent);
//                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Unstable Connection. Try Again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_LONG).show();
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
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getApplicationContext(), "Location: " + ((TextView)view).getText(), Toast.LENGTH_SHORT).show();
        location_id = i+1;
        locationName = (String) ((TextView)view).getText();
//        Toast.makeText(getApplicationContext(), "Location ID: " + location_id, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private static final String[] LOCATION = new String[] {
            "DOSTLAB", "ABOITIZ", "REGISTRAR", "DIRECTOR", "HSS", "LIBRARY", "DENTAL", "MEDICAL", "SECURITY", "ACCOUNTING", "HAP", "CSC", "FACULTY", "GUIDANCE"
    };

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
