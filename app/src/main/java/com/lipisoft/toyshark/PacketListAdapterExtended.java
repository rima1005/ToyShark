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

public class PacketListAdapterExtended extends RecyclerView.Adapter<PacketInfoViewHolderExtended> {
    private static final byte TCP = 6;
    private static final byte UDP = 17;

    @NonNull private final List<PacketInfoExtended> list;
    private OnRequestClickListener mOnRequestClickListener;

    PacketListAdapterExtended(@NonNull final List<PacketInfoExtended> list, OnRequestClickListener OnRequestClickListener) {
        this.list = list;
        this.mOnRequestClickListener = OnRequestClickListener;
    }

    @Override
    public PacketInfoViewHolderExtended onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent != null) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.packet_info_extended, parent, false);
            return new PacketInfoViewHolderExtended(view, mOnRequestClickListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(PacketInfoViewHolderExtended holder, int position) {
        if (holder != null) {
            final PacketInfoExtended packetInfo = list.get(position);
            final TextView protocol = holder.getProtocol();
            final TextView firstLine = holder.getFirstLine();

            final int protocolType = packetInfo.protocol;
            if (protocolType == TCP) {
                protocol.setText(R.string.tcp);
            } else if (protocolType == UDP) {
                protocol.setText(R.string.udp);
            }
            firstLine.setText(packetInfo.firstLine);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnRequestClickListener {
        void onRequestClick(int position);
    }
}
