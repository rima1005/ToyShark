package com.lipisoft.toyshark;

public class Host {
    public String address;
    public int port;
    public int accessCounter;
    public int ip;

    public Host(int ip, String address, int port, int accessCounter)
    {
        this.ip = ip;
        this.address = address;
        this.port = port;
        this.accessCounter = accessCounter;
    }
}
