package com.markandreydelacruz.puptcwois;

import android.content.Intent;
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

public class AvailableReports extends AppCompatActivity implements  AdapterView.OnItemSelectedListener{

    private int location_id;
    private String locationName;
    private String HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availablereports);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Available Reports");
        HOST = getIntent().getExtras().getString("HOST");

        Spinner spinnerLocation = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, LOCATION);
        spinnerLocation.setAdapter(adapter);
        spinnerLocation.setOnItemSelectedListener(this);

        Button buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AvailableReports.this, LocationReportsList.class);
                intent.putExtra("location_id", location_id);
                intent.putExtra("locationName", locationName);
                intent.putExtra("HOST", HOST);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(getApplicationContext(), "Location: " + ((TextView)view).getText(), Toast.LENGTH_SHORT).show();
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
