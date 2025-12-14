package dhr.agent.data;

import dhr.agent.utility.Log;
import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.criteria.TcpPortCriterion;
import org.onosproject.net.flow.criteria.UdpPortCriterion;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dhr.agent.DhrManager.FLOW_INFO_INTERVAL;

public class FeatureManager {
    private Feature feature;
    public static Hashtable<String, String> polymorphicHashtable = new Hashtable<>();
    public static Hashtable<String, String> ipHashtable = new Hashtable<>();

    // 单设备源Ip集合
    Set<IpAddress> srcIpSet;

    // 单设备源标识信息集合
    Set<String> srcIdentifications;

    // 单设备源Ip与源端口映射
    Map<IpAddress, List<Integer>> ipPortMap;

    ArrayList<Long> packetLengths;
    
    // 输入矩阵（流-特征矩阵）
    private List<List<Double>> inputMatrix;

    public FeatureManager() {
        srcIpSet = new HashSet<>();
        srcIdentifications = new HashSet<>();
        ipPortMap = new HashMap<>();
        feature = new Feature();
        packetLengths = new ArrayList<>();
        inputMatrix = new ArrayList<>();
        initPolymorphicHashtable();
        initIpHashtable();
    }

    public Feature getFeature() {
        return feature;
    }
    // 获得单条流规则的特征
    public void setFlowFeature(FlowEntry flowEntry) {
        TrafficSelector selector = flowEntry.selector();
        TrafficTreatment treatment = flowEntry.treatment();
        packetLengths.add(flowEntry.bytes());
        feature.setFlowId(flowEntry.id().toString());
        feature.setDeviceId(flowEntry.deviceId());
        IpAddress sourceIP = getIpAddress(selector, Criterion.Type.IPV4_SRC);
        srcIpSet.add(sourceIP);
        feature.setSourceIP(sourceIP);
        IpAddress destinationIP = getIpAddress(selector, Criterion.Type.IPV4_DST);
        feature.setDestinationIP(destinationIP);
        String protocol = null;

        // 获取in\out端口信息以及协议信息
        Integer srcPort = getPort(selector, Criterion.Type.TCP_SRC);
        if (srcPort == null) {
            srcPort = getPort(selector, Criterion.Type.UDP_SRC);
        } else {
            protocol = "tcp";
        }
        if (srcPort != null) {
            protocol = "udp";
        }
        if (srcPort == null) {
            srcPort = getPort(selector, treatment, true);
        }
        // 获取目的端口信息
        Integer dstPort = getPort(selector, Criterion.Type.TCP_DST);
        if (dstPort == null) {
            dstPort = getPort(selector, Criterion.Type.UDP_DST);
        }
        if (dstPort == null) {
            dstPort = getPort(selector, treatment, false);
        }

        feature.setSourcePort(srcPort);
        feature.setDestinationPort(dstPort);
        addIpPort(sourceIP, srcPort);
        feature.setProtocol(protocol);
        long timestamp = Long.parseLong(getAttribution(flowEntry.toString(), ipHashtable.get("created"))) / 1000; //转换为s
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss.SSS") // 定义输出格式
                .withZone(ZoneId.systemDefault()); // 使用系统默认时区
        String timestampString = formatter.format(Instant.ofEpochSecond(timestamp)); // 转换为格式化字符串
        long currentTimestamp = System.currentTimeMillis() / 1000;
        long flowDuration = currentTimestamp - timestamp;
        long lastSeen = flowEntry.lastSeen();
        feature.setTimestamp(timestampString);
        feature.setFlowDuration(flowDuration);

        feature.setSubflowFwdPackets(flowEntry.packets());
        feature.setSubflowFwdBytes(flowEntry.bytes());



        feature.setFwdPacketLengthMean(feature.getAvgFlowBytes());
        feature.setBwdPacketLengthMean(feature.getAvgFlowBytes());
        feature.setAvgFwdSegmentSize(feature.getAvgFlowBytes());
        double flowIATMean = flowEntry.packets() == 0 ? 0 : flowDuration / flowEntry.packets();
        feature.setFlowIatMax(flowDuration);
        feature.setFlowIatMean(flowIATMean);
        feature.setFlowIatMin(flowDuration);
        feature.setFwdIatTotal(lastSeen - timestamp * 1000);

        feature.setFwdIatMax(flowDuration);
        feature.setFwdIatMin(flowDuration);
        feature.setFwdIatMean(flowIATMean);

        feature.setIdleMax(feature.getFwdIatTotal());

    }

    // 获取该交互机上的总流量特征
    public void setFlowsFeature(int sumPacket, int sumByte, int flowListSize) {
        int secondTime = FLOW_INFO_INTERVAL / 1000;
        int portSum = 0;
        double avgPacket = 0.0d;
        double avgByte = 0.0d;
        double portChange = 0.0d;
        double flowChange = 0.0d;
        double ipChange = 0.0d;
        if (flowListSize != 0) {
            if (sumPacket != 0){
                avgPacket = (double) sumPacket / flowListSize;
            }

            if (sumByte != 0)
                avgByte = (double) sumByte / flowListSize;
            if(ipPortMap != null){
                for (IpAddress ipAddress : ipPortMap.keySet()) {
                    portSum += ipPortMap.get(ipAddress).size();
                }
                if (portSum != 0)
                    portChange = (double) portSum / secondTime;
            }
            flowChange = (double) flowListSize / secondTime;
            if(srcIpSet != null && srcIpSet.size() != 0)
                ipChange = (double) srcIpSet.size() / secondTime;
        }

        feature.setTotalFwdPackets(sumPacket);
        feature.setTotalLengthOfFwdPackets(sumByte);
        // 后续需要修改为后向
        feature.setTotalBwdPackets(sumPacket);
        feature.setTotalLengthOfBwdPackets(sumByte);

        feature.setAvgSourceIPGrowth(ipChange);
        feature.setAvgPacket(avgPacket);
        feature.setAvgByte(avgByte);
        feature.setAvgFlowGrowth(flowChange);
        feature.setAvgPortGrowth(portChange);
        feature.setAveragePacketSize((double) sumByte / sumPacket);

        feature.setAvgFlowBytes(avgByte);
        feature.setAvgFlowPackets(avgPacket);

        feature.setPacketLengthVariance(calculateVariance(packetLengths));
        // TODO : 需要加到判空里吗？

    }
    /**
     * 获取源、目的IPv4或IPv6地址
     *
     * @param selector 流表项的流量选择器
     * @param type     想要获取的类型，例如IPv4_SRC
     * @return ip地址
     */
    public static IpAddress getIpAddress(TrafficSelector selector, Criterion.Type type) {
        IpAddress ipAddress = null;
        if (type == Criterion.Type.IPV4_SRC
                || type == Criterion.Type.IPV4_DST) {
            Criterion criterion = selector.getCriterion(type);
            if (criterion instanceof IPCriterion) {
                IPCriterion IPv4Criterion = (IPCriterion) criterion;
                ipAddress = IPv4Criterion.ip().address();
            }
        } else if (type == Criterion.Type.IPV6_SRC
                || type == Criterion.Type.IPV6_DST) {
            Criterion criterion = selector.getCriterion(type);
            if (criterion instanceof IPCriterion) {
                IPCriterion IPv6Criterion = (IPCriterion) criterion;
                ipAddress = IPv6Criterion.ip().address();
            }
        }
        return ipAddress;
    }
    /**
     * 从流表项中获取TCP、UDP源端口或目的端口号，仅ip模态下可用
     *
     * @param selector 流表项的流量选择器
     * @param type     想要获取的类型，例如TCP_SRC
     * @return 端口号
     */

    public static Integer getPort(TrafficSelector selector, Criterion.Type type) {
        Integer port = null;
        if (type == Criterion.Type.TCP_SRC || type == Criterion.Type.TCP_DST) {
            Criterion criterion = selector.getCriterion(type);
            if (criterion instanceof TcpPortCriterion) {
                TcpPortCriterion tcpPortCriterion = (TcpPortCriterion) criterion;
                port = tcpPortCriterion.tcpPort().toInt();
            }
        } else if (type == Criterion.Type.UDP_SRC || type == Criterion.Type.UDP_DST) {
            Criterion criterion = selector.getCriterion(type);
            if (criterion instanceof UdpPortCriterion) {
                UdpPortCriterion udpPortCriterion = (UdpPortCriterion) criterion;
                port = udpPortCriterion.udpPort().toInt();
            }
        }
        return port;
    }

    public static Integer getPort(TrafficSelector selector, TrafficTreatment treatment, boolean isInPort) {
        Integer port = 0;
        if (isInPort) {
            port = Integer.valueOf(getAttribution(selector.toString(), "IN_PORT:"));
        } else {
            port = Integer.valueOf(getAttribution(treatment.toString(), "OUTPUT:"));
        }
        return port;
    }



    private void initPolymorphicHashtable() {
        polymorphicHashtable.put("0x8624", "hdr.ndn.name_tlv.components[0].value=");
        polymorphicHashtable.put("0x800", "hdr.ipv4.srcAddr=");
        polymorphicHashtable.put("0x27c0", "hdr.mf.src_guid=");
        polymorphicHashtable.put("0x812", "hdr.id.srcIdentity=");
    }
    private void initIpHashtable() {
        ipHashtable.put("created", "created=");
    }
    public static String getAttribution(String content, String matchContent) {
        int index = content.indexOf(matchContent) + matchContent.length();
        int index_end = index;
        String type = null;
        for (int i = index; i <= content.length(); ++i){
            if (content.charAt(i) == ',' || content.charAt(i) == '}'
                    || content.charAt(i) == ')' || content.charAt(i) == ']') {
                index_end = i;
                break;
            }
        }
        return content.substring(index, index_end);
    }

    public static String getType(TrafficSelector selector) {
        String typeMatchString = "hdr.ethernet.ether_type=";
        String selectorContent = selector.toString();
        String type = getAttribution(selectorContent, typeMatchString);
        return  type;
    }


    public static String getSourceIdentification(TrafficSelector selector) {
        String type = getType(selector);
        String matchString = polymorphicHashtable.get(type);
        String identification = getAttribution(selector.toString(), matchString);
        return identification;
    }


    public static void calculatePolymorphicFeature(DeviceId deviceId, int sumPacket, int sumByte, int flowListSize,
                                                   Set<String> srcInfo, Feature dataFeature) {

        int secondTime = FLOW_INFO_INTERVAL / 1000;
        double avgPacket = 0.0d;
        double avgByte = 0.0d;
        double portChange = 0.0d;
        double flowChange = 0.0d;
        double ipChange = 0.0d;
        if (flowListSize != 0) {
            if (sumPacket != 0)
                avgPacket = (double) sumPacket / flowListSize;
            if (sumByte != 0)
                avgByte = (double) sumByte / flowListSize;

            flowChange = (double) flowListSize / secondTime;
            if(srcInfo != null && srcInfo.size() != 0)
                ipChange = (double) srcInfo.size() / secondTime;
        }
        // TODO : 需要加到判空里吗？
        dataFeature.setFeature(deviceId, deviceId.toString(), avgPacket, avgByte,
                portChange, flowChange, ipChange);
    }
    public void initFeature() {
        this.feature.initFeature();
    }
    public void initContainer() {
        srcIpSet.clear();
        ipPortMap.clear();
        srcIdentifications.clear();
        packetLengths.clear();
    }
    
    /**
     * 输入代理：将流量数据转换为流-特征矩阵
     * @param flowData 流量数据
     */
    public void convertToFlowFeatureMatrix(Feature flowData) {
        // 将单个流量特征转换为特征向量
        List<Double> featureVector = new ArrayList<>();
        
        // 添加流量特征到特征向量
        featureVector.add((double) flowData.getTotalFwdPackets());
        featureVector.add((double) flowData.getTotalBwdPackets());
        featureVector.add((double) flowData.getTotalLengthOfFwdPackets());
        featureVector.add((double) flowData.getTotalLengthOfBwdPackets());
        featureVector.add(flowData.getAvgFlowBytes());
        featureVector.add(flowData.getFwdPacketLengthMean());
        featureVector.add(flowData.getBwdPacketLengthMean());
        featureVector.add(flowData.getAveragePacketSize());
        featureVector.add(flowData.getAvgFwdSegmentSize());
        featureVector.add((double) flowData.getSubflowFwdPackets());
        featureVector.add((double) flowData.getSubflowFwdBytes());
        featureVector.add(flowData.getPacketLengthVariance());
        featureVector.add(flowData.getFlowIatMean());
        featureVector.add((double) flowData.getFlowIatMax());
        featureVector.add((double) flowData.getFlowIatMin());
        featureVector.add((double) flowData.getFwdIatTotal());
        featureVector.add(flowData.getFwdIatMean());
        featureVector.add((double) flowData.getFwdIatMax());
        featureVector.add((double) flowData.getFwdIatMin());
        featureVector.add((double) flowData.getIdleMax());
        featureVector.add(flowData.getAvgFlowPackets());
        featureVector.add(flowData.getAvgPacket());
        featureVector.add(flowData.getAvgByte());
        featureVector.add(flowData.getAvgPortGrowth());
        featureVector.add(flowData.getAvgFlowGrowth());
        featureVector.add(flowData.getAvgSourceIPGrowth());
        
        // 将特征向量添加到输入矩阵
        inputMatrix.add(featureVector);
    }
    
    /**
     * 获取输入矩阵（流-特征矩阵）
     * @return 输入矩阵
     */
    public List<List<Double>> getInputMatrix() {
        return inputMatrix;
    }
    
    /**
     * 清空输入矩阵
     */
    public void clearInputMatrix() {
        inputMatrix.clear();
    }
    /**
     * 将ip与端口加入Map
     *
     * @param ipAddress ip
     * @param port      端口号
     */
    private void addIpPort(IpAddress ipAddress, Integer port) {
        if (ipAddress != null && port != null) {
            if (ipPortMap.containsKey(ipAddress)) {
                ipPortMap.get(ipAddress).add(port);
            } else {
                List<Integer> portList = new ArrayList<>();
                portList.add(port);
                ipPortMap.put(ipAddress, portList);
            }
        }
    }

    public static double calculateVariance(ArrayList<Long> numbers) {
        int n = numbers.size();

        if (n == 0) {
            return 0; // 如果列表为空，返回方差为0
        }

        // 计算平均值
        double mean = 0;
        for (Long num : numbers) {
            mean += num;
        }
        mean /= n;

        // 计算方差
        double sumSquaredDifferences = 0;
        for (Long num : numbers) {
            sumSquaredDifferences += Math.pow(num - mean, 2);
        }

        // 计算方差（无偏估计）
        return sumSquaredDifferences / n;  // 如果需要无偏估计可以使用 (n-1) 来代替 n
    }
}
