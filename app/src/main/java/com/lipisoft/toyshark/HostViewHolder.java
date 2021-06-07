package com.lipisoft.toyshark;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class HostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView accessCounter;
    private final TextView address;
    private final TextView port;
    HostListAdapter.OnHostListener onHostListener;

    HostViewHolder(View itemView, HostListAdapter.OnHostListener onHostListener) {
        super(itemView);
        accessCounter = itemView.findViewById(R.id.accessCounter);
        address = itemView.findViewById(R.id.address);
        port = itemView.findViewById(R.id.port);
        this.onHostListener = onHostListener;
        itemView.setOnClickListener(this);
    }

    public TextView getAccessCounter() {
        return accessCounter;
    }

    public TextView getAddress() {
        return address;
    }

    public TextView getPort() {
        return port;
    }

    @Override
    public void onClick(View view) {
        onHostListener.onHostClick(getAdapterPosition());
    }
}
