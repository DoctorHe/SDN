package org.osdd.app.data;

import org.onosproject.net.DeviceId;

public class DataFeature {

    private boolean attack = false;
    private DeviceId id;
    private String stringId;
    // 平均包数
    private double avgPacket;
    // 平均字节数
    private double avgByte;
    // 端口增长率
    private double portChange;
    // 流增长率
    private double flowChange;
    // 源ip增长率
    private double srcIPChange;

    public DataFeature() {
    }

    public DataFeature(DeviceId id,String stringId, double avgPacket, double avgByte, double portChange, double flowChange, double srcIPChange) {
        this.id = id;
        this.stringId = stringId;
        this.avgPacket = avgPacket;
        this.avgByte = avgByte;
        this.portChange = portChange;
        this.flowChange = flowChange;
        this.srcIPChange = srcIPChange;
    }

    public DeviceId getId() {
        return id;
    }

    public void setId(DeviceId id) {
        this.id = id;
    }

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public double getAvgPacket() {
        return avgPacket;
    }

    public void setAvgPacket(double avgPacket) {
        this.avgPacket = avgPacket;
    }

    public double getAvgByte() {
        return avgByte;
    }

    public void setAvgByte(double avgByte) {
        this.avgByte = avgByte;
    }

    public double getPortChange() {
        return portChange;
    }

    public void setPortChange(double portChange) {
        this.portChange = portChange;
    }

    public double getFlowChange() {
        return flowChange;
    }

    public void setFlowChange(double flowChange) {
        this.flowChange = flowChange;
    }

    public double getSrcIPChange() {
        return srcIPChange;
    }

    public void setSrcIPChange(double srcIPChange) {
        this.srcIPChange = srcIPChange;
    }

    public boolean isAttack() {
        return attack;
    }

    public void setAttack(boolean attack) {
        this.attack = attack;
    }

    public void setFeature(DeviceId deviceId, String stringId,  double avgPacket, double avgByte, double portChange,
                           double flowChange, double srcIPChange){
        this.id = deviceId;
        this.stringId = stringId;
        this.avgPacket = avgPacket;
        this.avgByte = avgByte;
        this.portChange = portChange;
        this.flowChange = flowChange;
        this.srcIPChange = srcIPChange;
    }

    public void initFeature(){
        this.id = null;
        this.stringId = null;
        this.avgPacket = 0.0d;
        this.avgByte = 0.0d;
        this.portChange = 0.0d;
        this.flowChange = 0.0d;
        this.srcIPChange = 0.0d;
    }

    @Override
    public String toString() {
        return "DataFeature{" +
                "id=" + id +
                ", stringId='" + stringId + '\'' +
                ", avgPacket=" + avgPacket +
                ", avgByte=" + avgByte +
                ", portChange=" + portChange +
                ", flowChange=" + flowChange +
                ", srcIPChange=" + srcIPChange +
                '}';
    }

}
