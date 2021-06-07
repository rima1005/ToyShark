package com.lipisoft.toyshark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.lipisoft.toyshark.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity implements PacketListAdapterExtended.OnRequestClickListener {

    private RecyclerView rvRequests;
    private List<PacketInfoExtended> packets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        Intent intent = getIntent();
        int ip = intent.getIntExtra("ip",0);
        int port = intent.getIntExtra("port",0);
        String keyword = intent.getStringExtra("keyword");
        boolean filter = intent.getBooleanExtra("filter", false);

        Cursor c;
        if(filter)
            c = DatabaseHelper.getInstance(getApplicationContext()).getFilteredPackets(ip, port, keyword);
        else
            c = DatabaseHelper.getInstance(getApplicationContext()).getPackets(ip, port);

        packets = new ArrayList<>();

        while(c.moveToNext())
        {
            int protocol = c.getInt(c.getColumnIndex("protocol"));
            int id = c.getInt(c.getColumnIndex("ID"));
            String request = c.getString(c.getColumnIndex("data"));
            String[] firstLine = request.split("\n", 2);

            packets.add(new PacketInfoExtended(protocol, firstLine[0], id));
        }

        rvRequests = findViewById(R.id.rvRequests);
        rvRequests.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvRequests.setHasFixedSize(true);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        PacketListAdapterExtended adapter = new PacketListAdapterExtended(packets, this);
        rvRequests.setAdapter(adapter);
    }

    @Override
    public void onRequestClick(int position) {
        PacketInfoExtended info = packets.get(position);
        Intent intent = new Intent(this, RequestDetailActivity.class);
        intent.putExtra("id", info.id);
        startActivity(intent);

    }
}