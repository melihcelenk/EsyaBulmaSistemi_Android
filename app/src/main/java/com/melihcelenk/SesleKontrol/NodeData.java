package com.melihcelenk.SesleKontrol;

public class NodeData {
    private int nodeId;
    private String ip;
    private String method;

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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString(){
        return "nodeId:"+getNodeId()+" ip: "+getIp()+ " method: "+getMethod();
    }
}
