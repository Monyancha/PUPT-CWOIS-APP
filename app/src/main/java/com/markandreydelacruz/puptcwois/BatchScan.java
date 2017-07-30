package com.markandreydelacruz.puptcwois;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.markandreydelacruz.puptcwois.Adapters.BatchScanAdapter;
import com.markandreydelacruz.puptcwois.Models.BatchScanModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class BatchScan extends AppCompatActivity {
    private String HOST;
    private String URL_ADD_SN, URL_GET_LIST, URL_DONE_SCANNING;
    private String locationName;
    private String serialNumber;
    private String report_id;
    ListView listViewBatchScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batchscan);
        report_id = getIntent().getExtras().getString("report_id");
        locationName = getIntent().getExtras().getString("locationName");
        HOST = getIntent().getExtras().getString("HOST");
        URL_ADD_SN = HOST + "api/addSerialNumber.php";
        URL_GET_LIST = HOST + "api/getReportList.php";
        URL_DONE_SCANNING = HOST + "api/doneScanning.php";
        setTitle("Batch Scan - ID:" + report_id);
        loadViews();
//        loadCurrentList();
    }

    private void loadViews() {
        TextView textViewTime = (TextView) findViewById(R.id.textViewLocation);
        textViewTime.setText("Current List: " + locationName);
        listViewBatchScan = (ListView) findViewById(R.id.listViewBatchScan);
        Button buttonScan = (Button) findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(BatchScan.this)
                        .setBeepEnabled(true)
                        .setPrompt("Place a QR Code inside the rectangle to scan it.")
                        .initiateScan();
            }
        });
        Button buttonDoneScanning = (Button) findViewById(R.id.buttonDoneScanning);
        buttonDoneScanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BatchScan.this);
                builder.setMessage("Finish scanning?").setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doneScanning();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.setTitle("Warning");
                alertDialog.show();
            }
        });
    }

    private void doneScanning() {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        RequestParams params = new RequestParams();
        params.put("report_id", report_id);
        asyncHttpClient.post(BatchScan.this, URL_DONE_SCANNING, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(BatchScan.this);
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
                            count++;
                        }
                        if (status != null) {
                            if(status.equals("success")){
                                AlertDialog.Builder builder = new AlertDialog.Builder(BatchScan.this);
                                builder.setCancelable(true)
                                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
//                                                Intent intent = new Intent(BatchScan.this, Home.class);
//                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.setTitle("Success");
                                alertDialog.show();
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

    private void loadCurrentList() {
//        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//        asyncHttpClient.setConnectTimeout(3000);
//        asyncHttpClient.setResponseTimeout(3000);
//        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
//        asyncHttpClient.get(BatchScan.this, URL_GET_LIST, new AsyncHttpResponseHandler() {

        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        RequestParams params = new RequestParams();
        params.put("report_id", report_id);
        asyncHttpClient.post(BatchScan.this, URL_GET_LIST, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(BatchScan.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Updating List...");
                dialog.setIndeterminate(false);
                dialog.setCancelable(false);
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

                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(new String(responseBody));

                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("items");
                    final List<BatchScanModel> batchScanModelList = new ArrayList<>();
                    Gson gson = new Gson();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject finalObject = jsonArray.getJSONObject(i);
                        BatchScanModel batchScanModel = gson.fromJson(finalObject.toString(), BatchScanModel.class); // a single line json parsing using Gson
                        batchScanModelList.add(batchScanModel);
                    }
                    BatchScanAdapter adapter = new BatchScanAdapter(getBaseContext(), R.layout.row_batchscan_item, batchScanModelList);
                    listViewBatchScan.setAdapter(adapter);
                    listViewBatchScan.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // list item click opens a new detailed activity
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            BatchScanModel allRecordsModel = batchScanModelList.get(position); // getting the model
//                        Intent intent = new Intent(AllRecords.this, AllRecordsDetails.class);
//                        intent.putExtra("allRecordsModel", new Gson().toJson(allRecordsModel)); // converting model json into string type and sending it via intent
//                        startActivity(intent);
//                            Toast.makeText(getApplicationContext(), new Gson().toJson(allRecordsModel), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                asyncHttpClient.cancelAllRequests(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
//                Snackbar snack = Snackbar.make(getWindow().getDecorView().getRootView(), "Connection Failed", Snackbar.LENGTH_INDEFINITE).setAction("Action", null);
//                ViewGroup group = (ViewGroup) snack.getView();
//                group.setBackgroundColor(Color.parseColor("#dd4e40"));
//                snack.show();
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

    //Getting the scan results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
//                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
//                finish();
            } else {
                serialNumber = result.getContents();
                addSerialNumber(serialNumber);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addSerialNumber(final String serialNumber) {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        RequestParams params = new RequestParams();
        params.put("report_id", report_id);
        params.put("serialNumber", serialNumber);
        asyncHttpClient.post(BatchScan.this, URL_ADD_SN, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(BatchScan.this);
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
                            count++;
                        }
                        if (status != null) {
                            if(status.equals("success")){
//                                listViewBatchScan.setAdapter(null);
                                loadCurrentList();
//                                Intent intent = new Intent(BatchScan.this, BatchScan.class);
//                                intent.putExtra("HOST", HOST);
//                                intent.putExtra("locationName", locationName);
//                                intent.putExtra("report_id", report_id);
//                                startActivity(intent);
//                                finish();
                                Toast.makeText(getApplicationContext(), "Scanned:" + serialNumber, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BatchScan.this);
        builder.setMessage("Are you sure you want to cancel the current process?").setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Process Canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Warning");
        alertDialog.show();
    }

}
