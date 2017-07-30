package com.markandreydelacruz.puptcwois;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.markandreydelacruz.puptcwois.Adapters.BatchScanAdapter;
import com.markandreydelacruz.puptcwois.Adapters.LocationReportsListAdapter;
import com.markandreydelacruz.puptcwois.Models.BatchScanModel;
import com.markandreydelacruz.puptcwois.Models.LocationReportsListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;

public class LocationReportsList extends AppCompatActivity {

    private int location_id;
    private String locationName;
    private String HOST;
    private String URL_GET_LOCATION_REPORTS;
    ListView listViewLocationReportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationreportslist);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        location_id = getIntent().getExtras().getInt("location_id");
        locationName = getIntent().getExtras().getString("locationName");
        HOST = getIntent().getExtras().getString("HOST");
        URL_GET_LOCATION_REPORTS = HOST + "api/getLocationReportsList.php";
        setTitle(locationName + " Reports");
        listViewLocationReportsList = (ListView) findViewById(R.id.listViewLocationReportsList);
//        Toast.makeText(getApplicationContext(), "location ID:" + location_id, Toast.LENGTH_LONG).show();
        loadLocationReports();
    }

    private void loadLocationReports() {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setConnectTimeout(3000);
        asyncHttpClient.setResponseTimeout(3000);
        asyncHttpClient.setMaxRetriesAndTimeout(0, 0);
        RequestParams params = new RequestParams();
        params.put("location_id", location_id);
        asyncHttpClient.post(LocationReportsList.this, URL_GET_LOCATION_REPORTS, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(LocationReportsList.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Requesting Reports...");
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
                    if(jsonObject.getString("status").equals("No Result")) {
                        Toast.makeText(getApplicationContext(), jsonObject.getString("status"), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("reports");
                    final List<LocationReportsListModel> locationReportsListModelList = new ArrayList<>();
                    Gson gson = new Gson();
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject finalObject = jsonArray.getJSONObject(i);
                        LocationReportsListModel locationReportsListModel = gson.fromJson(finalObject.toString(), LocationReportsListModel.class); // a single line json parsing using Gson
                        locationReportsListModelList.add(locationReportsListModel);
                    }
//                    Toast.makeText(getApplicationContext(), locationReportsListModelList.get(0).getTime(), Toast.LENGTH_SHORT).show();
                    LocationReportsListAdapter adapter = new LocationReportsListAdapter(getBaseContext(), R.layout.row_locationreportslist_item, locationReportsListModelList);
                    listViewLocationReportsList.setAdapter(adapter);
                    listViewLocationReportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {  // list item click opens a new detailed activity
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            LocationReportsListModel locationReportsListModel = locationReportsListModelList.get(position); // getting the model
//                        Intent intent = new Intent(AllRecords.this, AllRecordsDetails.class);
//                        intent.putExtra("allRecordsModel", new Gson().toJson(allRecordsModel)); // converting model json into string type and sending it via intent
//                        startActivity(intent);

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(HOST + "api/" + locationReportsListModelList.get(position).getUrl()));
                            startActivity(intent);
//                            Toast.makeText(getApplicationContext(), locationReportsListModelList.get(position).getUrl(), Toast.LENGTH_SHORT).show();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(LocationReportsList.this);
//                            builder.setMessage("Do you want to open the report?").setCancelable(true)
//                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            Toast.makeText(getApplicationContext(), "this will open the browser and view the report", Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(Intent.ACTION_VIEW, HOST + "api/" + locationReportsListModelList.get(position).getUrl());
//                                            startActivity(intent);
//                                        }
//                                    })
//                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            dialogInterface.cancel();
//                                        }
//                                    });
//
//                            AlertDialog alertDialog = builder.create();
//                            alertDialog.setTitle("Generate Report");
//                            alertDialog.show();
//                            if (Build.VERSION.SDK_INT >= 24) {
//                                Html.fromHtml(HOST + "api/" + locationReportsListModelList.get(position).getUrl(), Build.VERSION.SDK_INT); // for 24 api and more
//                                LinkMovementMethod.getInstance();
//                            } else {
//                                Html.fromHtml(HOST + "api/" + locationReportsListModelList.get(position).getUrl()); // or for older api
//                                LinkMovementMethod.getInstance();
//                            }
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
                finish();
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
