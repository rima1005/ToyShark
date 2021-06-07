package com.lipisoft.toyshark;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.net.InetAddresses;
import com.lipisoft.toyshark.util.DatabaseHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TlsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tls);
        Intent intent = getIntent();
        int port = intent.getIntExtra("port", 0);
        int ip = intent.getIntExtra("ip", 0);

        int hostId = -1;
        String defaultCipher = "";
        String defaultProtocol = "";
        String hostname = "";

        Cursor dnslookup = DatabaseHelper.getInstance(getApplicationContext()).getHost(ip, port);
        if(dnslookup.moveToFirst())
            hostname = dnslookup.getString(dnslookup.getColumnIndex("hostname"));

        Objects.requireNonNull(getSupportActionBar()).setTitle(hostname);
        if(!InetAddresses.fromInteger(ip).getHostAddress().equals(hostname))
            Objects.requireNonNull(getSupportActionBar()).setSubtitle(InetAddresses.fromInteger(ip).getHostAddress());

        Cursor host = DatabaseHelper.getInstance(getApplicationContext()).getHost(ip, port);
        if(host.moveToFirst())
        {
            hostId = host.getInt(host.getColumnIndex("ID"));
            defaultCipher = host.getString(host.getColumnIndex("suite"));
            defaultProtocol = host.getString(host.getColumnIndex("protocol"));
        }

        TextView handshakeDetails = findViewById(R.id.tvHandshakeDetails);
        if(defaultCipher == null || defaultCipher == "" && defaultProtocol == null || defaultProtocol == "")
        {
            handshakeDetails.setText("The server seems to have certificate pinning or refused the TLS connection for other reasions!");
        }
        else
        {
            handshakeDetails.setText(defaultProtocol + " with " + defaultCipher);
        }


        Cursor suits = null;
        Cursor protocols = null;

        if(hostId != -1)
        {
            suits = DatabaseHelper.getInstance(getApplicationContext()).getSupportedSuites(hostId);
            protocols = DatabaseHelper.getInstance(getApplicationContext()).getSupportedProtocols(hostId);
        }
        List<String> supportedSuits = new ArrayList<>();
        List<String> supportedProtocols = new ArrayList<>();

        if(suits != null && suits.moveToFirst())
        {
            do {
                supportedSuits.add(suits.getString(suits.getColumnIndex("name")));
            } while(suits.moveToNext());

            suits.close();
        }

        if(protocols != null && protocols.moveToFirst())
        {
            do {
                supportedProtocols.add(protocols.getString(protocols.getColumnIndex("name")));
            } while(protocols.moveToNext());

            protocols.close();
        }

        ListView lvSuits = findViewById(R.id.lvDetailsCipherSuits);
        ListView lvProtocols = findViewById(R.id.lvDetailsProtocols);

        ArrayAdapter<String> suitsAdapter = new ArrayAdapter<>(this, R.layout.simple_list_view, supportedSuits);
        ArrayAdapter<String> protocolsAdapter = new ArrayAdapter<>(this, R.layout.simple_list_view, supportedProtocols);

        lvSuits.setAdapter(suitsAdapter);
        lvProtocols.setAdapter(protocolsAdapter);

        lvSuits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent webInfo = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ciphersuite.info/cs/" + parent.getItemAtPosition(position).toString()));
                startActivity(webInfo);
            }
        });
    }
}