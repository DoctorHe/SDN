package org.nids.app.utility;

import org.nids.app.data.DataFeature;
import org.onlab.packet.IpAddress;
import org.onosproject.net.DeviceId;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.nids.app.AppComponent.FLOW_INFO_INTERVAL;

public class Compute {


    /**
     * 计算特征
     * @param deviceId 交换机ID
     * @param sumPacket 流表项发送的总包数
     * @param sumByte  流表项发送总字节数
     * @param flowListSize 交换机存放了几条流表项
     * @return
     */
    public static void calculateIpFeature(DeviceId deviceId, int sumPacket, int sumByte, int flowListSize,
                                  Set<IpAddress> srcIpSet, Map<IpAddress, List<Integer>> ipPortMap, DataFeature dataFeature) {

        int secondTime = FLOW_INFO_INTERVAL / 1000;
        int portSum = 0;
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
        // TODO : 需要加到判空里吗？
        dataFeature.setFeature(deviceId, deviceId.toString(), avgPacket, avgByte,
                portChange, flowChange, ipChange);
    }

    public static void calculatePolymorphicFeature(DeviceId deviceId, int sumPacket, int sumByte, int flowListSize,
                                          Set<String> srcInfo, DataFeature dataFeature) {

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
}
