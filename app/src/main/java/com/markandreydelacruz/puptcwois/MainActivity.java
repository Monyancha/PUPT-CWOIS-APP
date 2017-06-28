package com.markandreydelacruz.puptcwois;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static String HOST = "http://192.168.137.1/systems/CWOIS-2017/";
    private static final String URL_LOGIN = HOST + "api/login.php";
    private boolean doubleBackToExitPressedOnce = false;
    private MaterialEditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewCwoisSite;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setTitle("");
        initComp();
    }

    private void initComp() {
        editTextUsername= (MaterialEditText) findViewById(R.id.editTextUsername);
        editTextPassword = (MaterialEditText) findViewById(R.id.editTextPassword);
        textViewCwoisSite = (TextView) findViewById(R.id.textViewCwoisSite);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);

        String cwoisLink = "<a href=\"http://www.pup.edu.ph/CWOIS\">www.pup.edu.ph/CWOIS</a>";
        if (Build.VERSION.SDK_INT >= 24) {
            textViewCwoisSite.setText( Html.fromHtml(cwoisLink, Build.VERSION.SDK_INT)); // for 24 api and more
            textViewCwoisSite.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textViewCwoisSite.setText(Html.fromHtml(cwoisLink)); // or for older api
            textViewCwoisSite.setMovementMethod(LinkMovementMethod.getInstance());
        }

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(MainActivity.this);
                validate(URL_LOGIN);
            }
        });
    }

    private void validate(String URL_LOGIN) {
        username = editTextUsername.getText().toString();
        password = editTextPassword.getText().toString();
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        asyncHttpClient.post(MainActivity.this, URL_LOGIN, params, new AsyncHttpResponseHandler() {
            ProgressDialog dialog;
            @Override
            public void onStart() {
                super.onStart();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("Please wait...");
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
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
                if(responseBody == null) {
                    Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), "No Response. Try Again.", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getApplicationContext(), new String(responseBody), Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject jsonObject = new JSONObject(new String(responseBody));

                        int count = 0;
                        while(count < jsonObject.length()){
                            status = jsonObject.getString("status");
                            count++;
                        }
                        if(status.equals("success")){
                            Intent intent = new Intent(MainActivity.this, Home.class);
                            intent.putExtra("userDetails", new String(responseBody));
                            intent.putExtra("HOST", HOST);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_LONG).show();
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
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            }
        });
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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
