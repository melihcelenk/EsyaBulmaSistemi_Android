package com.melihcelenk.seslekontrol.modeller;

public class NodeData {
    private int nodeId;
    private String ip;
    private String macAddress;

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString(){
        return "nodeId:"+getNodeId()+" ip: "+getIp()+ " macAddress: "+ getMacAddress();
    }
}
