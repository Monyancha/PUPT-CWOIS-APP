package com.markandreydelacruz.puptcwois;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
