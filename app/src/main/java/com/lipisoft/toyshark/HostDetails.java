package com.lipisoft.toyshark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.net.InetAddresses;
import com.lipisoft.toyshark.util.DatabaseHelper;

import java.util.Objects;

public class HostDetails extends AppCompatActivity {

    Button tlsproperties;
    Button requests;
    Button filter;
    TextView filterWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_details);
        Intent intent = getIntent();
        int ip = intent.getIntExtra("ip", 0);
        int port = intent.getIntExtra("port", 0);

        String hostname = "";

        Cursor dnslookup = DatabaseHelper.getInstance(getApplicationContext()).getHost(ip, port);
        if(dnslookup.moveToFirst())
            hostname = dnslookup.getString(dnslookup.getColumnIndex("hostname"));

        Objects.requireNonNull(getSupportActionBar()).setTitle(hostname);
        if(!InetAddresses.fromInteger(ip).getHostAddress().equals(hostname))
            Objects.requireNonNull(getSupportActionBar()).setSubtitle(InetAddresses.fromInteger(ip).getHostAddress() + " Port: " + port);
        else
            Objects.requireNonNull(getSupportActionBar()).setSubtitle("Port: " + port);

        tlsproperties = findViewById(R.id.btnTLS);
        requests = findViewById(R.id.btnRequests);
        filter = findViewById(R.id.btnFilter);
        filterWord = findViewById(R.id.etFilter);

        tlsproperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TlsActivity.class);
                intent.putExtra("ip", ip);
                intent.putExtra("port", port);
                startActivity(intent);

            }
        });

        requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RequestActivity.class);
                intent.putExtra("ip", ip);
                intent.putExtra("port", port);
                intent.putExtra("filter", false);
                startActivity(intent);
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RequestActivity.class);
                intent.putExtra("ip", ip);
                intent.putExtra("port", port);
                intent.putExtra("keyword", filterWord.getText().toString());
                intent.putExtra("filter", true);
                startActivity(intent);
            }
        });




    }
}