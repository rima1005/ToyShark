package com.lipisoft.toyshark;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lipisoft.toyshark.util.PacketUtil;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HostListAdapter extends RecyclerView.Adapter<HostViewHolder> {
    private static final byte TCP = 6;
    private static final byte UDP = 17;

    @NonNull private final List<Host> list;
    private OnHostListener mOnHostListener;

    HostListAdapter(@NonNull final List<Host> list, OnHostListener mOnHostListener) {
        this.list = list;
        this.mOnHostListener = mOnHostListener;
    }

    @Override
    public HostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent != null) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.host_info, parent, false);
            return new HostViewHolder(view, mOnHostListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(HostViewHolder holder, int position) {
        if (holder != null) {
            final Host host = list.get(position);
            final TextView accessCounter = holder.getAccessCounter();
            final TextView address = holder.getAddress();
            final TextView port = holder.getPort();

            accessCounter.setText(String.format(Locale.getDefault(), "%d", host.accessCounter));
            address.setText(host.address);
            port.setText(String.format(Locale.getDefault(), "%d", host.port));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnHostListener {
        void onHostClick(int position);
    }
}
