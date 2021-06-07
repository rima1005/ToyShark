package com.lipisoft.toyshark.util;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.common.net.InetAddresses;
import com.lipisoft.toyshark.HostManager;
import com.lipisoft.toyshark.application.MyApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import static com.lipisoft.toyshark.HostManager.HOST;

public final class DbHandler extends Handler {
    public int queue = 0;
    private static final int MAX_QUEUE = 1000;
    public static final int MSG_HOST = 1;
    public static final int MSG_TLS = 2;
    private static final String TAG = "ToyShark.Util.DbHandler";

    public DbHandler(Looper looper) {super(looper);}

    public void queueHost(int addr, int port)
    {
        Message msg = obtainMessage();
        msg.obj = null;
        msg.what = MSG_HOST;
        msg.arg1 = addr;
        msg.arg2 = port;

        synchronized (this)
        {
            if (queue > MAX_QUEUE)
            {
                Log.w(TAG, "Log queue full");
                return;
            }

            sendMessage(msg);
            queue++;
        }

    }

    private void queueTLS(int addr, int port)
    {
        Message msg = obtainMessage();
        msg.obj = null;
        msg.what = MSG_TLS;
        msg.arg1 = addr;
        msg.arg2 = port;

        synchronized (this)
        {
            if (queue > MAX_QUEUE)
            {
                Log.w(TAG, "Log queue full");
                return;
            }

            sendMessage(msg);
            queue++;
        }

    }

    @Override
    public void handleMessage(Message msg)
    {
        try{
            switch (msg.what)
            {
                case MSG_HOST:
                    if(!DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).hostexists(msg.arg1, msg.arg2))
                    {
                        InetAddress address = InetAddresses.fromInteger(msg.arg1);
                        DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).insertHost(msg.arg1, msg.arg2, address.getHostName());
                        queueTLS(msg.arg1, msg.arg2);
                    }
                    else
                    {
                        DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).increaseCounter(msg.arg1, msg.arg2);
                    }
                    HostManager.INSTANCE.getHandler().obtainMessage(HOST).sendToTarget();
                    break;
                case MSG_TLS:
                    boolean success = getHandshakeDetails(msg.arg1, msg.arg2);
                    if(success)
                    {
                        getSupportedCipherSuits(msg.arg1, msg.arg2);
                        getSupportedProtocols(msg.arg1, msg.arg2);
                    }
                    break;

                default:
                    Log.e(TAG, "Unknown log message=" + msg.what);
            }

            synchronized (this)
            {
                queue--;
            }


        }catch (Throwable ex)
        {
            Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
        }
    }

    private boolean getHandshakeDetails(int addr, int port)
    {
        InetAddress host = null;
        SSLSocket sslSocket = null;

        host = InetAddresses.fromInteger(addr);

        if (host.isLoopbackAddress() || host.isAnyLocalAddress())
            return false;

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try
        {
            sslSocket = (SSLSocket) factory.createSocket(host, port);
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to create SSL Socket for " + host.getHostAddress() + "::" + port);
            return false;
        }

        sslSocket.setEnabledProtocols(sslSocket.getSupportedProtocols());
        sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

        try
        {
            sslSocket.startHandshake();
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to start Handshake " + host.getHostAddress() + "::" + port);
            return false;
        }

        SSLSession session = sslSocket.getSession();
        String ciphersuite = session.getCipherSuite();
        String protocol = session.getProtocol();
        try
        {
            if (!sslSocket.isClosed())
                sslSocket.close();
        } catch (IOException e)
        {
            Log.e(TAG, "Failed to close Socket for " + host.getHostAddress() + "::" + port);
        }

        DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).updateHost(addr, port, ciphersuite, protocol);
        return true;
    }


    private void getSupportedCipherSuits(int addr, int port)
    {
        InetAddress host = null;
        SSLSocket sslSocket = null;

        host = InetAddresses.fromInteger(addr);

        if (host.isLoopbackAddress() || host.isAnyLocalAddress())
            return;


        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try
        {
            sslSocket = (SSLSocket) factory.createSocket(host, port);
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to create SSL Socket for " + host.getHostAddress() + "::" + port);
            return;
        }

        String[] supportedCipherSuits = sslSocket.getSupportedCipherSuites();
        for(String suit : supportedCipherSuits)
        {
            if(!DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).suiteExists(suit))
            {
                DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).insertSuite(suit);
            }
        }

        for(String suit : supportedCipherSuits)
        {
            try
            {
                sslSocket = (SSLSocket)factory.createSocket(host, port);
                sslSocket.addHandshakeCompletedListener(new MyHandshakeCompleteListener(suit));
                String[] enabledSuites = {suit};
                sslSocket.setEnabledCipherSuites(enabledSuites);
                Log.i(TAG, "Starting Handshake with Suite " + suit + ": for host: " + host.getHostAddress());
                sslSocket.startHandshake();
            } catch (IOException e)
            {
                Log.e(TAG, "Failed to connect to Socket or did not pass Handshake");
            } finally {
                try
                {
                    if(!sslSocket.isClosed())
                        sslSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Cursor c = DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).getHost(addr, port);
        int hostId = -1;
        if(c.moveToFirst())
            hostId = c.getInt(c.getColumnIndex("ID"));
        c.close();

        for(String suit : MyHandshakeCompleteListener.getHandshakeList())
        {
            Cursor suitCursor = DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).getSuite(suit);
            int suitId = -1;
            if(suitCursor.moveToFirst())
                suitId = suitCursor.getInt(suitCursor.getColumnIndex("ID"));
            suitCursor.close();
            if(suitId == -1 || hostId == -1)
                continue;
            DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).insertSupportedSuite(hostId, suitId);
        }

        MyHandshakeCompleteListener.clearHandshakeList();
    }

    private void getSupportedProtocols(int addr, int port)
    {
        InetAddress host = null;
        SSLSocket sslSocket = null;

        host = InetAddresses.fromInteger(addr);

        if (host.isLoopbackAddress() || host.isAnyLocalAddress())
            return;


        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try
        {
            sslSocket = (SSLSocket) factory.createSocket(host, port);
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to create SSL Socket for " + host.getHostAddress() + "::" + port);
            return;
        }

        String[] supportedProtocols = sslSocket.getSupportedProtocols();
        for(String protocol : supportedProtocols)
        {
            if(!DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).protocolExists(protocol))
            {
                DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).insertProtocol(protocol);
            }
        }

        for(String protocol : supportedProtocols)
        {
            try
            {
                sslSocket = (SSLSocket)factory.createSocket(host, port);
                sslSocket.addHandshakeCompletedListener(new MyHandshakeCompleteListener(protocol));
                String[] enabledProtocols = {protocol};
                sslSocket.setEnabledProtocols(enabledProtocols);
                Log.i(TAG, "Starting Handshake with Protocol " + protocol + " for host " + host.getHostAddress());
                sslSocket.startHandshake();
            } catch (IOException e)
            {
                Log.e(TAG, "Failed to connect to Socket or did not pass Handshake");
            } finally {
                try
                {
                    if(!sslSocket.isClosed())
                        sslSocket.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Cursor c = DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).getHost(addr, port);
        int hostId = -1;
        if(c.moveToFirst())
            hostId = c.getInt(c.getColumnIndex("ID"));
        c.close();

        for(String suit : MyHandshakeCompleteListener.getHandshakeList())
        {
            Cursor protocolCursor = DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).getProtocol(suit);
            int protocolId = -1;
            if(protocolCursor.moveToFirst())
                protocolId = protocolCursor.getInt(protocolCursor.getColumnIndex("ID"));
            protocolCursor.close();
            if(protocolId == -1 || hostId == -1)
                continue;
            DatabaseHelper.getInstance(MyApplication.instance.getApplicationContext()).insertSupportedProtocol(hostId, protocolId);
        }
        MyHandshakeCompleteListener.clearHandshakeList();
    }
}
