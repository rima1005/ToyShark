package com.lipisoft.toyshark;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;

import com.lipisoft.toyshark.application.MyApplication;
import com.lipisoft.toyshark.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public enum HostManager {
    INSTANCE;

    public static final int HOST = 0;
    @NonNull private final List<Host> list = new ArrayList<>();
    private HostListAdapter adapter;
    @NonNull private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                if (msg.what == HostManager.HOST) {
                    updateList();
                    adapter.notifyDataSetChanged();
                }
            }
            super.handleMessage(msg);
        }
    };

    public void updateList() {
        list.clear();
        Cursor cursor = DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).getHosts();
        if(cursor == null || !cursor.moveToFirst())
            return;

        do {
            int ip = cursor.getInt(cursor.getColumnIndex("host"));
            String address = cursor.getString(cursor.getColumnIndex("hostname"));
            int port = cursor.getInt(cursor.getColumnIndex("port"));
            int accessCounter = cursor.getInt(cursor.getColumnIndex("queryCounter"));

            list.add(new Host(ip, address, port, accessCounter));

        } while (cursor.moveToNext());
    }

    @NonNull public List<Host> getList()
    {
        return this.list;
    }

    public void setAdapter(@NonNull HostListAdapter adapter) {
        this.adapter = adapter;
    }

    @NonNull public Handler getHandler() {
        return handler;
    }
}

