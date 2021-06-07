package com.lipisoft.toyshark.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

public class MyHandshakeCompleteListener implements HandshakeCompletedListener {
    public static String TAG = "ToyShark.Service.HandshakeCompleteListener";

    private String suit;

    public MyHandshakeCompleteListener(String Suit)
    {
        suit = Suit;
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
        Log.i(TAG, "Finished Handshake with " + suit);
        successfulHandshakes.add(suit);

    }

    private static List<String> successfulHandshakes = new ArrayList<>();

    public static void clearHandshakeList() {successfulHandshakes.clear();}
    public static List<String> getHandshakeList()
    {
        return successfulHandshakes;
    }
}
