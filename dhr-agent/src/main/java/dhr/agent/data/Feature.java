package dhr.agent.data;

import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;

public class Feature implements Cloneable{

    //    唯一标识网络流的一组标志符
    private String flowId;

    //     设备ID
    private DeviceId deviceId;

    //    源IP地址，表示流量的发起方。
    private IpAddress sourceIP;

    //    源端口号，用于标识流量来源的特定应用程序或服务。
    private Integer sourcePort;

    //    目的IP地址，表示流量的接收方。
    private IpAddress destinationIP;

    //    目的端口号，用于标识目标主机上的特定应用程序或服务。
    private Integer destinationPort;

    //    协议类型（如TCP、UDP、ICMP等），定义了通信的规则。
    private String protocol;

    //    记录流量开始的时间戳。
    private String timestamp;

    //    流的持续时间，通常以毫秒为单位。
    private long flowDuration;

    //    通过设备的平均包数
    private double avgPacket;

    //    通过设备的平均字节数
    private double avgByte;

    //    端口增长率
    private double avgPortGrowth;

    //    流增长率
    private double avgFlowGrowth;

    //    源ip增长率
    private double avgSourceIPGrowth;

    //    总的前向（源到目的）数据包数。
    private long totalFwdPackets;

    //    总的后向（目的到源）数据包数。
    private long totalBwdPackets;

    //    所有前向数据包的总字节数。
    private long totalLengthOfFwdPackets;

    //    所有后向数据包的总字节数。
    private long totalLengthOfBwdPackets;

    //    流的总字节数除以流持续时间，表示字节传输速率。
    private double avgFlowBytes;

    //    前向数据包的平均长度。
    private double fwdPacketLengthMean;

    //    后向数据包的平均长度。
    private double bwdPacketLengthMean;

    //    每个数据包的平均大小
    private double averagePacketSize;

    //    前向段的平均大小
    private double avgFwdSegmentSize;

    //    前向子流包数
    private long subflowFwdPackets;

    //    前向子流比特数
    private long subflowFwdBytes;

    //    包长方差
    private double packetLengthVariance;

    //    流内任意两个连续数据包之间的平均时间间隔（Inter-Arrival Time, IAT）。
    private double flowIatMean;

    //    流内任意两个连续数据包之间的最大时间间隔。
    private long flowIatMax;

    //    流内任意两个连续数据包之间的最小时间间隔。
    private long flowIatMin;

    //    前向数据包之间的时间间隔总和。
    private long fwdIatTotal;

    //    前向数据包之间的平均时间间隔。
    private double fwdIatMean;

    //    前向数据包之间的最大时间间隔。
    private long fwdIatMax;

    //    前向数据包之间的最小时间间隔。
    private long fwdIatMin;

    //    流最大空闲时间
    private long  idleMax;

    public void setIdleMax(long idleMax) {
        this.idleMax = idleMax;
    }

    public void setSubflowFwdPackets(long subflowFwdPackets) {
        this.subflowFwdPackets = subflowFwdPackets;
    }

    public void setPacketLengthVariance(double packetLengthVariance) {
        this.packetLengthVariance = packetLengthVariance;
    }
    public void setSubflowFwdBytes(long subflowFwdBytes) {
        this.subflowFwdBytes = subflowFwdBytes;
    }

    public void setFlowIatMean(double flowIatMean) {
        this.flowIatMean = flowIatMean;
    }

    public void setFlowIatMax(long flowIatMax) {
        this.flowIatMax = flowIatMax;
    }

    public void setFlowIatMin(long flowIatMin) {
        this.flowIatMin = flowIatMin;
    }

    public void setFwdIatTotal(long fwdIatTotal) {
        this.fwdIatTotal = fwdIatTotal;
    }

    public long getFwdIatTotal() {
        return fwdIatTotal;
    }

    public void setFwdIatMean(double fwdIatMean) {
        this.fwdIatMean = fwdIatMean;
    }

    public void setFwdIatMax(long fwdIatMax) {
        this.fwdIatMax = fwdIatMax;
    }

    public void setFwdIatMin(long fwdIatMin) {
        this.fwdIatMin = fwdIatMin;
    }
    
    // Getter methods for missing properties
    public long getIdleMax() {
        return idleMax;
    }
    
    public long getSubflowFwdPackets() {
        return subflowFwdPackets;
    }
    
    public double getPacketLengthVariance() {
        return packetLengthVariance;
    }
    
    public long getSubflowFwdBytes() {
        return subflowFwdBytes;
    }
    
    public double getFlowIatMean() {
        return flowIatMean;
    }
    
    public long getFlowIatMax() {
        return flowIatMax;
    }
    
    public long getFlowIatMin() {
        return flowIatMin;
    }
    
    public double getFwdIatMean() {
        return fwdIatMean;
    }
    
    public long getFwdIatMax() {
        return fwdIatMax;
    }
    
    public long getFwdIatMin() {
        return fwdIatMin;
    }
    
    public double getFwdPacketLengthMean() {
        return fwdPacketLengthMean;
    }
    
    public double getBwdPacketLengthMean() {
        return bwdPacketLengthMean;
    }
    
    public double getAveragePacketSize() {
        return averagePacketSize;
    }
    
    public double getAvgFwdSegmentSize() {
        return avgFwdSegmentSize;
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

    public double getAvgPortGrowth() {
        return avgPortGrowth;
    }

    public void setAvgPortGrowth(double avgPortGrowth) {
        this.avgPortGrowth = avgPortGrowth;
    }

    public double getAvgFlowGrowth() {
        return avgFlowGrowth;
    }

    public void setAvgFlowGrowth(double avgFlowGrowth) {
        this.avgFlowGrowth = avgFlowGrowth;
    }

    public double getAvgSourceIPGrowth() {
        return avgSourceIPGrowth;
    }

    public void setAvgSourceIPGrowth(double avgSourceIPGrowth) {
        this.avgSourceIPGrowth = avgSourceIPGrowth;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public IpAddress getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(IpAddress sourceIP) {
        this.sourceIP = sourceIP;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public IpAddress getDestinationIP() {
        return destinationIP;
    }

    public void setDestinationIP(IpAddress destinationIP) {
        this.destinationIP = destinationIP;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public long getFlowDuration() {
        return flowDuration;
    }

    public void setFlowDuration(long flowDuration) {
        this.flowDuration = flowDuration;
    }

    public long getTotalFwdPackets() {
        return totalFwdPackets;
    }

    public void setTotalFwdPackets(long totalFwdPackets) {
        this.totalFwdPackets = totalFwdPackets;
    }

    public long getTotalBwdPackets() {
        return totalBwdPackets;
    }

    public void setTotalBwdPackets(long totalBwdPackets) {
        this.totalBwdPackets = totalBwdPackets;
    }

    public long getTotalLengthOfFwdPackets() {
        return totalLengthOfFwdPackets;
    }

    public void setTotalLengthOfFwdPackets(long totalLengthOfFwdPackets) {
        this.totalLengthOfFwdPackets = totalLengthOfFwdPackets;
    }

    public long getTotalLengthOfBwdPackets() {
        return totalLengthOfBwdPackets;
    }

    public void setTotalLengthOfBwdPackets(long totalLengthOfBwdPackets) {
        this.totalLengthOfBwdPackets = totalLengthOfBwdPackets;
    }

    public double getAvgFlowBytes() {
        return avgFlowBytes;
    }

    public void setAvgFlowBytes(double avgFlowBytes) {
        this.avgFlowBytes = avgFlowBytes;
    }

    public double getAvgFlowPackets() {
        return avgFlowPackets;
    }

    public void setFwdPacketLengthMean(double fwdPacketLengthMean) {
        this.fwdPacketLengthMean = fwdPacketLengthMean;
    }

    public void setBwdPacketLengthMean(double bwdPacketLengthMean) {
        this.bwdPacketLengthMean = bwdPacketLengthMean;
    }

    public void setAveragePacketSize(double averagePacketSize) {
        this.averagePacketSize = averagePacketSize;
    }

    public void setAvgFwdSegmentSize(double avgFwdSegmentSize) {
        this.avgFwdSegmentSize = avgFwdSegmentSize;
    }

    public void setAvgFlowPackets(double avgFlowPackets) {
        this.avgFlowPackets = avgFlowPackets;
    }

    //    流的总数据包数除以流持续时间，表示数据包传输速率。
    private double avgFlowPackets;


    public Feature() {
    }


    public Feature(DeviceId id, String flowId, double avgPacket, double avgByte, double avgPortGrowth, double flowChange, double srcIPChange) {
        this.deviceId = id;
        this.flowId = flowId;
        this.avgPacket = avgPacket;
        this.avgByte = avgByte;
        this.avgPortGrowth = avgPortGrowth;
        this.avgFlowGrowth = flowChange;
        this.avgSourceIPGrowth = srcIPChange;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public void setFeature(DeviceId deviceId, String flowId,  double avgPacket, double avgByte, double avgPortGrowth,
                           double flowChange, double srcIPChange){
        this.deviceId = deviceId;
        this.flowId = flowId;
        this.avgPacket = avgPacket;
        this.avgByte = avgByte;
        this.avgPortGrowth = avgPortGrowth;
        this.avgFlowGrowth = flowChange;
        this.avgSourceIPGrowth = srcIPChange;
    }

    public void initFeature(){
        this.flowId = null;
        //     设备ID
        DeviceId deviceId = null;

        //     平均包数
        this.avgPacket = 0;

        //    平均字节数
        this.avgByte = 0;

        //    端口增长率
        this.avgPortGrowth = 0;

        //    流增长率
        this.avgFlowGrowth = 0;

        //    源ip增长率
        this.avgSourceIPGrowth = 0;

        //    源IP地址，表示流量的发起方。
        this.sourceIP = null;

        //    源端口号，用于标识流量来源的特定应用程序或服务。
        this.sourcePort = 0;

        //    目的IP地址，表示流量的接收方。
        this.destinationIP = null;

        //    目的端口号，用于标识目标主机上的特定应用程序或服务。
        this.destinationPort = 0;

        //    协议类型（如TCP、UDP、ICMP等），定义了通信的规则。
        this.protocol = null;

        //    记录流量开始的时间戳。
        this.timestamp = null;

        //    流的持续时间，通常以毫秒为单位。
        this.flowDuration = 0;

        //    总的前向（源到目的）数据包数。
        this.totalFwdPackets = 0;

        //    总的后向（目的到源）数据包数。
        this.totalBwdPackets = 0;

        //    所有前向数据包的总字节数。
        this.totalLengthOfFwdPackets = 0;

        //    所有后向数据包的总字节数。
        this.totalLengthOfBwdPackets = 0;

        //    流的总字节数除以流持续时间，表示字节传输速率。
        this.avgFlowBytes = 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Feature{" +
                "flowId='" + flowId + '\'' +
                ", deviceId=" + deviceId +
                ", sourceIP=" + sourceIP +
                ", sourcePort=" + sourcePort +
                ", destinationIP=" + destinationIP +
                ", destinationPort=" + destinationPort +
                ", protocol='" + protocol + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", flowDuration=" + flowDuration +
                ", avgPacket=" + avgPacket +
                ", avgByte=" + avgByte +
                ", avgPortGrowth=" + avgPortGrowth +
                ", avgFlowGrowth=" + avgFlowGrowth +
                ", avgSourceIPGrowth=" + avgSourceIPGrowth +
                ", totalFwdPackets=" + totalFwdPackets +
                ", totalBwdPackets=" + totalBwdPackets +
                ", totalLengthOfFwdPackets=" + totalLengthOfFwdPackets +
                ", totalLengthOfBwdPackets=" + totalLengthOfBwdPackets +
                ", avgFlowBytes=" + avgFlowBytes +
                ", fwdPacketLengthMean=" + fwdPacketLengthMean +
                ", bwdPacketLengthMean=" + bwdPacketLengthMean +
                ", averagePacketSize=" + averagePacketSize +
                ", avgFwdSegmentSize=" + avgFwdSegmentSize +
                ", subflowFwdPackets=" + subflowFwdPackets +
                ", subflowFwdBytes=" + subflowFwdBytes +
                ", packetLengthVariance=" + packetLengthVariance +
                ", flowIatMean=" + flowIatMean +
                ", flowIatMax=" + flowIatMax +
                ", flowIatMin=" + flowIatMin +
                ", fwdIatTotal=" + fwdIatTotal +
                ", fwdIatMean=" + fwdIatMean +
                ", fwdIatMax=" + fwdIatMax +
                ", fwdIatMin=" + fwdIatMin +
                ", idleMax=" + idleMax +
                ", avgFlowPackets=" + avgFlowPackets +
                '}';
    }
}
