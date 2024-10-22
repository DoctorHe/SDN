package org.nids.app.utility;

import org.nids.app.AppComponent;
import org.onlab.packet.IpAddress;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.IPCriterion;
import org.onosproject.net.flow.criteria.TcpPortCriterion;
import org.onosproject.net.flow.criteria.UdpPortCriterion;

import java.util.Hashtable;

public class GetFeature {

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

    public static String getAttribution(String content, String matchContent) {
        int index = content.indexOf(matchContent) + matchContent.length();
        int index_end = index;
        String type = null;
        for (int i = index; i <= content.length(); ++i){
            if (content.charAt(i) == ',') {
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
        String matchString = AppComponent.polymorphicHashtable.get(type);
        String identification = getAttribution(selector.toString(), matchString);
        return identification;
    }
}
