package com.lipisoft.toyshark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.lipisoft.toyshark.util.DatabaseHelper;

public class RequestDetailActivity extends AppCompatActivity {


    private TextView tvRequestDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        tvRequestDetail = findViewById(R.id.tvRequestDetail);
        if(id != -1)
        {
            String request = DatabaseHelper.getInstance(getApplicationContext()).getRequest(id);
            tvRequestDetail.setText(request);
        } else
        {
            tvRequestDetail.setText("Request not found!");
        }





    }
}