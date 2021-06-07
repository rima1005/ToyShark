package com.lipisoft.toyshark;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PacketInfoViewHolderExtended extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView protocol;
    private final TextView firstLine;
    PacketListAdapterExtended.OnRequestClickListener onRequestClickListener;


    PacketInfoViewHolderExtended(View itemView, PacketListAdapterExtended.OnRequestClickListener onRequestClickListener) {
        super(itemView);
        protocol = itemView.findViewById(R.id.protocol);
        firstLine = itemView.findViewById(R.id.firstLine);
        this.onRequestClickListener = onRequestClickListener;
        itemView.setOnClickListener(this);

    }


    public TextView getProtocol() {
        return protocol;
    }

    public TextView getFirstLine() {
        return firstLine;
    }

    @Override
    public void onClick(View view) {
        onRequestClickListener.onRequestClick(getAdapterPosition());
    }
}