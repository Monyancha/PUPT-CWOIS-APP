package com.markandreydelacruz.puptcwois;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class Home extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private String userDetails, username, lastname, firstname, HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home");
        userDetails = getIntent().getExtras().getString("userDetails");
        HOST = getIntent().getExtras().getString("HOST");
        initComp(userDetails);
    }

    private void initComp(String userDetails) {
        TextView textViewUsername = (TextView) findViewById(R.id.textViewUsername);
        TextView textViewName = (TextView) findViewById(R.id.textViewName);
        ImageButton imageButtonScanQrCode = (ImageButton) findViewById(R.id.imageButtonScanQrCode);
        imageButtonScanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, ScanQrCode.class);
                intent.putExtra("HOST", HOST);
                startActivity(intent);
            }
        });

        ImageButton imageButtonBatchScan = (ImageButton) findViewById(R.id.imageButtonBatchScan);
        imageButtonBatchScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDateAndTime("api/getDateAndTime.php");
            }
        });


        ImageButton imageButtonAvailableReports = (ImageButton) findViewById(R.id.imageButtonAvailableReports);
        imageButtonAvailableReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, AvailableReports.class);
                intent.putExtra("HOST", HOST);
                startActivity(intent);
            }
        });

        try {
            JSONObject jsonObject = new JSONObject(userDetails);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("userDetails");
            int count = 0;
            while(count < jsonArray.length()){
                jsonObject = jsonArray.getJSONObject(count);
                username = jsonObject.getString("username");
                lastname = jsonObject.getString("lastname");
                firstname= jsonObject.getString("firstname");
                count++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        textViewUsername.setText(username.toUpperCase());
        textViewName.setText("Welcome, " + firstname + " " + lastname);
    }

    private void getDateAndTime(String URL_GET_DATE_TIME) {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        asyncHttpClient.get(Home.this, HOST + URL_GET_DATE_TIME, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(Home.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Please Wait...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getApplicationContext(), "Canceled", Toast.LENGTH_LONG).show();
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
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                String date = null;
                String time = null;
                if(responseBody == null) {
                    Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(responseBody)).getJSONObject("data");
                        int count = 0;
                        while(count < jsonObject.length()){
                            date = jsonObject.getString("date");
                            time= jsonObject.getString("time");
                            count++;
                        }
                        if (date != null) {
                            if (date.trim().isEmpty() || time.trim().isEmpty()) {
                                Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(Home.this, ScanEquipments.class);
                                intent.putExtra("date", date);
                                intent.putExtra("time", time);
                                intent.putExtra("HOST", HOST);
                                startActivity(intent);
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
                Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_refresh) {
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
