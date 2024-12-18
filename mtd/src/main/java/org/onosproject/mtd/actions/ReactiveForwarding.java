
package org.onosproject.mtd.actions;

import com.google.common.collect.ImmutableSet;
import org.onlab.packet.*;

import org.onlab.util.KryoNamespace;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.event.Event;
import org.onosproject.mtd.data.ReactiveForwardMetrics;
import org.onosproject.mtd.strategy.MtdHostsManage;
import org.onosproject.mtd.strategy.MtdMechanism;
import org.onosproject.net.*;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.*;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.MultiValuedTimestamp;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.FLOW_PRIORITY;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.FLOW_PRIORITY_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.FLOW_TIMEOUT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.FLOW_TIMEOUT_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.IGNORE_IPV4_MCAST_PACKETS_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.IPV6_FORWARDING;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.IPV6_FORWARDING_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_DST_MAC_ONLY;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_DST_MAC_ONLY_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_ICMP_FIELDS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_ICMP_FIELDS_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV4_ADDRESS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV4_ADDRESS_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV4_DSCP;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV4_DSCP_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV6_ADDRESS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV6_ADDRESS_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_IPV6_FLOW_LABEL_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_TCP_UDP_PORTS_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_VLAN_ID;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.MATCH_VLAN_ID_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.PACKET_OUT_OFPP_TABLE_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.PACKET_OUT_ONLY;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.PACKET_OUT_ONLY_DEFAULT;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.RECORD_METRICS;
import static org.onosproject.mtd.actions.OsgiPropertyConstants.RECORD_METRICS_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Sample reactive forwarding and mtd application.
 */
@Component(
        immediate = true,
        service = ReactiveForwarding.class,
        property = {
                PACKET_OUT_ONLY + ":Boolean=" + PACKET_OUT_ONLY_DEFAULT,
                PACKET_OUT_OFPP_TABLE + ":Boolean=" + PACKET_OUT_OFPP_TABLE_DEFAULT,
                FLOW_TIMEOUT + ":Integer=" + FLOW_TIMEOUT_DEFAULT,
                FLOW_PRIORITY  + ":Integer=" + FLOW_PRIORITY_DEFAULT,
                IPV6_FORWARDING + ":Boolean=" + IPV6_FORWARDING_DEFAULT,
                MATCH_DST_MAC_ONLY + ":Boolean=" + MATCH_DST_MAC_ONLY_DEFAULT,
                MATCH_VLAN_ID + ":Boolean=" + MATCH_VLAN_ID_DEFAULT,
                MATCH_IPV4_ADDRESS + ":Boolean=" + MATCH_IPV4_ADDRESS_DEFAULT,
                MATCH_IPV4_DSCP + ":Boolean=" + MATCH_IPV4_DSCP_DEFAULT,
                MATCH_IPV6_ADDRESS + ":Boolean=" + MATCH_IPV6_ADDRESS_DEFAULT,
                MATCH_IPV6_FLOW_LABEL + ":Boolean=" + MATCH_IPV6_FLOW_LABEL_DEFAULT,
                MATCH_TCP_UDP_PORTS + ":Boolean=" + MATCH_TCP_UDP_PORTS_DEFAULT,
                MATCH_ICMP_FIELDS + ":Boolean=" + MATCH_ICMP_FIELDS_DEFAULT,
                IGNORE_IPV4_MCAST_PACKETS + ":Boolean=" + IGNORE_IPV4_MCAST_PACKETS_DEFAULT,
                RECORD_METRICS + ":Boolean=" + RECORD_METRICS_DEFAULT
        }
)
public class ReactiveForwarding {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private  EventuallyConsistentMap<MacAddress, ReactiveForwardMetrics> metrics;

    private ApplicationId appId;

    /** 启用仅数据包输出转发；默认为 false。 */
    private boolean packetOutOnly = PACKET_OUT_ONLY_DEFAULT;

    /** 使用 OFPP_TABLE 端口启用第一个数据包转发，而不是使用实际端口的 PacketOut；默认为 false。 */
    private boolean packetOutOfppTable = PACKET_OUT_OFPP_TABLE_DEFAULT;

    /** 为已安装的流规则配置流超时；默认值为 10 秒。 */
    private int flowTimeout = FLOW_TIMEOUT_DEFAULT;

    /** 为已安装的流规则配置流优先级；默认值为 10。 */
    private int flowPriority = FLOW_PRIORITY_DEFAULT;

    /** 启用IPv6转发；默认为 false。*/
    private boolean ipv6Forwarding = IPV6_FORWARDING_DEFAULT;

    /** 仅启用匹配的 Dst Mac；默认为 false。*/
    private boolean matchDstMacOnly = MATCH_DST_MAC_ONLY_DEFAULT;

    /** 启用匹配Vlan ID；默认为 false。 */
    private boolean matchVlanId = MATCH_VLAN_ID_DEFAULT;

    /** 启用匹配 IPv4 地址；默认为 false,。 */
    private boolean matchIpv4Address = MATCH_IPV4_ADDRESS_DEFAULT;

    /** Enable matching IPv4 DSCP and ECN; default is false. */
    private boolean matchIpv4Dscp = MATCH_IPV4_DSCP_DEFAULT;

    /** Enable matching IPv6 Addresses; default is false. */
    private boolean matchIpv6Address = MATCH_IPV6_ADDRESS_DEFAULT;

    /** Enable matching IPv6 FlowLabel; default is false. */
    private boolean matchIpv6FlowLabel = MATCH_IPV6_FLOW_LABEL_DEFAULT;

    /** Enable matching TCP/UDP ports; default is false. */
    private boolean matchTcpUdpPorts = MATCH_TCP_UDP_PORTS_DEFAULT;

    /** Enable matching ICMPv4 and ICMPv6 fields; default is false. */
    private boolean matchIcmpFields = MATCH_ICMP_FIELDS_DEFAULT;

    /** Ignore (do not forward) IPv4 multicast packets; default is false. */
    private boolean ignoreIPv4Multicast = IGNORE_IPV4_MCAST_PACKETS_DEFAULT;

    /** Enable record metrics for reactive forwarding. */
    private boolean recordMetrics = RECORD_METRICS_DEFAULT;

    private final TopologyListener topologyListener = new InternalTopologyListener();
    private final HostListener hostListener =new InternalHostListener();

    //thread pool
    private ExecutorService blackHoleExecutor;

    private MtdHostsManage mtdHostsManage;
    Thread thread;

    @Activate
    public void activate(ComponentContext context) {

        KryoNamespace.Builder metricSerializer = KryoNamespace.newBuilder()
//                .register(KryoNamespaces.API)
                .register(ReactiveForwardMetrics.class)
                .register(MultiValuedTimestamp.class);
        metrics =  storageService.<MacAddress, ReactiveForwardMetrics>eventuallyConsistentMapBuilder()
                .withName("metrics-mtd")
                .withSerializer(metricSerializer)
                .withTimestampProvider((key, metricsData) -> new
                        MultiValuedTimestamp<>(new WallClockTimestamp(), System.nanoTime()))
                .build();


        //只用一个线程来执行任务，保证任务按FIFO顺序一个个执行。
        blackHoleExecutor = newSingleThreadExecutor(groupedThreads("onos/app/mtd",
                "black-hole-fixer",
                log));
        cfgService.registerProperties(getClass());
        appId = coreService.registerApplication("org.onosproject.mtd");

        packetService.addProcessor(processor, PacketProcessor.director(2));

        //handing link changes
        topologyService.addListener(topologyListener);
//        hostService.addListener(hostListener);

        mtdHostsManage = new MtdHostsManage();
        //host manage
        try {
            if (deviceService.getDevices().iterator().hasNext()) {
                Device device = deviceService.getDevices().iterator().next();
                if (device.hwVersion().equals("Open vSwitch")) { //Indicates that the current mode is ip.
                    mtdHostsManage.isPolymorphicMode = false;
                    mtdHostsManage.setHost(hostService.getHosts());
                } else {
                    mtdHostsManage.isPolymorphicMode = true;
                    mtdHostsManage.setHost(0);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        mtdHostsManage.getAllDevices(deviceService.getDevices());

        mtdHostsManage.startShift();
        mtdHostsManage.sign=true;
        thread = new Thread(mtdHostsManage);
        thread.start();

        readComponentConfiguration(context);
        requestIntercepts();

        log.info("Started", appId.id());
    }

    @Deactivate
    public void deactivate() {
        mtdHostsManage.sign=false;
        mtdHostsManage.rollbackAttackList();
        cfgService.unregisterProperties(getClass(), false);
        withdrawIntercepts();
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        topologyService.removeListener(topologyListener);
//        hostService.removeListener(hostListener);
        blackHoleExecutor.shutdown();
        blackHoleExecutor = null;
        processor = null;
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        readComponentConfiguration(context);
        requestIntercepts();
    }

    /**
     * 通过数据包服务请求数据包。
     */
    private void requestIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);

        selector.matchEthType(Ethernet.TYPE_IPV6);
        if (ipv6Forwarding) {
            packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);
        } else {
            packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        }
    }

    /**
     * 通过数据包服务取消数据包输入请求.
     */
    private void withdrawIntercepts() {
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(Ethernet.TYPE_IPV4);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
        selector.matchEthType(Ethernet.TYPE_IPV6);
        packetService.cancelPackets(selector.build(), PacketPriority.REACTIVE, appId);
    }

    /**
     * 从组件配置上下文中提取属性。
     *
     * @param context the component context
     */
    private void readComponentConfiguration(ComponentContext context) {
        Dictionary<?, ?> properties = context.getProperties();

        Boolean packetOutOnlyEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_ONLY);
        if (packetOutOnlyEnabled == null) {
            log.info("Packet-out is not configured, " +
                    "using current value of {}", packetOutOnly);
        } else {
            packetOutOnly = packetOutOnlyEnabled;
            log.info("Configured. Packet-out only forwarding is {}",
                    packetOutOnly ? "enabled" : "disabled");
        }

        Boolean packetOutOfppTableEnabled =
                Tools.isPropertyEnabled(properties, PACKET_OUT_OFPP_TABLE);
        if (packetOutOfppTableEnabled == null) {
            log.info("OFPP_TABLE port is not configured, " +
                    "using current value of {}", packetOutOfppTable);
        } else {
            packetOutOfppTable = packetOutOfppTableEnabled;
            log.info("Configured. Forwarding using OFPP_TABLE port is {}",
                    packetOutOfppTable ? "enabled" : "disabled");
        }

        Boolean ipv6ForwardingEnabled =
                Tools.isPropertyEnabled(properties, IPV6_FORWARDING);
        if (ipv6ForwardingEnabled == null) {
            log.info("IPv6 forwarding is not configured, " +
                    "using current value of {}", ipv6Forwarding);
        } else {
            ipv6Forwarding = ipv6ForwardingEnabled;
            log.info("Configured. IPv6 forwarding is {}",
                    ipv6Forwarding ? "enabled" : "disabled");
        }

        Boolean matchDstMacOnlyEnabled =
                Tools.isPropertyEnabled(properties, MATCH_DST_MAC_ONLY);
        if (matchDstMacOnlyEnabled == null) {
            log.info("Match Dst MAC is not configured, " +
                    "using current value of {}", matchDstMacOnly);
        } else {
            matchDstMacOnly = matchDstMacOnlyEnabled;
            log.info("Configured. Match Dst MAC Only is {}",
                    matchDstMacOnly ? "enabled" : "disabled");
        }

        Boolean matchVlanIdEnabled =
                Tools.isPropertyEnabled(properties, MATCH_VLAN_ID);
        if (matchVlanIdEnabled == null) {
            log.info("Matching Vlan ID is not configured, " +
                    "using current value of {}", matchVlanId);
        } else {
            matchVlanId = matchVlanIdEnabled;
            log.info("Configured. Matching Vlan ID is {}",
                    matchVlanId ? "enabled" : "disabled");
        }

        Boolean matchIpv4AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_ADDRESS);
        if (matchIpv4AddressEnabled == null) {
            log.info("Matching IPv4 Address is not configured, " +
                    "using current value of {}", matchIpv4Address);
        } else {
            matchIpv4Address = matchIpv4AddressEnabled;
            log.info("Configured. Matching IPv4 Addresses is {}",
                    matchIpv4Address ? "enabled" : "disabled");
        }

        Boolean matchIpv4DscpEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV4_DSCP);
        if (matchIpv4DscpEnabled == null) {
            log.info("Matching IPv4 DSCP and ECN is not configured, " +
                    "using current value of {}", matchIpv4Dscp);
        } else {
            matchIpv4Dscp = matchIpv4DscpEnabled;
            log.info("Configured. Matching IPv4 DSCP and ECN is {}",
                    matchIpv4Dscp ? "enabled" : "disabled");
        }

        Boolean matchIpv6AddressEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_ADDRESS);
        if (matchIpv6AddressEnabled == null) {
            log.info("Matching IPv6 Address is not configured, " +
                    "using current value of {}", matchIpv6Address);
        } else {
            matchIpv6Address = matchIpv6AddressEnabled;
            log.info("Configured. Matching IPv6 Addresses is {}",
                    matchIpv6Address ? "enabled" : "disabled");
        }

        Boolean matchIpv6FlowLabelEnabled =
                Tools.isPropertyEnabled(properties, MATCH_IPV6_FLOW_LABEL);
        if (matchIpv6FlowLabelEnabled == null) {
            log.info("Matching IPv6 FlowLabel is not configured, " +
                    "using current value of {}", matchIpv6FlowLabel);
        } else {
            matchIpv6FlowLabel = matchIpv6FlowLabelEnabled;
            log.info("Configured. Matching IPv6 FlowLabel is {}",
                    matchIpv6FlowLabel ? "enabled" : "disabled");
        }

        Boolean matchTcpUdpPortsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_TCP_UDP_PORTS);
        if (matchTcpUdpPortsEnabled == null) {
            log.info("Matching TCP/UDP fields is not configured, " +
                    "using current value of {}", matchTcpUdpPorts);
        } else {
            matchTcpUdpPorts = matchTcpUdpPortsEnabled;
            log.info("Configured. Matching TCP/UDP fields is {}",
                    matchTcpUdpPorts ? "enabled" : "disabled");
        }

        Boolean matchIcmpFieldsEnabled =
                Tools.isPropertyEnabled(properties, MATCH_ICMP_FIELDS);
        if (matchIcmpFieldsEnabled == null) {
            log.info("Matching ICMP (v4 and v6) fields is not configured, " +
                    "using current value of {}", matchIcmpFields);
        } else {
            matchIcmpFields = matchIcmpFieldsEnabled;
            log.info("Configured. Matching ICMP (v4 and v6) fields is {}",
                    matchIcmpFields ? "enabled" : "disabled");
        }

        Boolean ignoreIpv4McastPacketsEnabled =
                Tools.isPropertyEnabled(properties, IGNORE_IPV4_MCAST_PACKETS);
        if (ignoreIpv4McastPacketsEnabled == null) {
            log.info("Ignore IPv4 multi-cast packet is not configured, " +
                    "using current value of {}", ignoreIPv4Multicast);
        } else {
            ignoreIPv4Multicast = ignoreIpv4McastPacketsEnabled;
            log.info("Configured. Ignore IPv4 multicast packets is {}",
                    ignoreIPv4Multicast ? "enabled" : "disabled");
        }
        Boolean recordMetricsEnabled =
                Tools.isPropertyEnabled(properties, RECORD_METRICS);
        if (recordMetricsEnabled == null) {
            log.info("IConfigured. Ignore record metrics  is {} ," +
                    "using current value of {}", recordMetrics);
        } else {
            recordMetrics = recordMetricsEnabled;
            log.info("Configured. record metrics  is {}",
                    recordMetrics ? "enabled" : "disabled");
        }

        flowTimeout = Tools.getIntegerProperty(properties, FLOW_TIMEOUT, FLOW_TIMEOUT_DEFAULT);
        log.info("Configured. Flow Timeout is configured to {} seconds", flowTimeout);

        flowPriority = Tools.getIntegerProperty(properties, FLOW_PRIORITY, FLOW_PRIORITY_DEFAULT);
        log.info("Configured. Flow Priority is configured to {}", flowPriority);
    }


    private class ReactivePacketProcessor implements PacketProcessor {

        @Override
        public void process(PacketContext context) {

            if (context.isHandled()) {
                return;
            }

            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();


            if (ethPkt == null) {
                return;
            }

            MacAddress macAddress = ethPkt.getSourceMAC();
            ReactiveForwardMetrics macMetrics = null;
            macMetrics = createCounter(macAddress);
            inPacket(macMetrics);

            // 如果这被视为控制包，则保释。
            if (isControlPacket(ethPkt)) {
                droppedPacket(macMetrics);
                return;
            }

            // 禁用 IPv6 转发时跳过 IPv6 组播数据包。
            if (!ipv6Forwarding && isIpv6Multicast(ethPkt)) {
                droppedPacket(macMetrics);
                return;
            }

            HostId id = HostId.hostId(ethPkt.getDestinationMAC(), VlanId.vlanId(ethPkt.getVlanID()));
            HostId srcId= HostId.hostId(ethPkt.getSourceMAC(),VlanId.vlanId(ethPkt.getVlanID()));

            Host src=hostService.getHost(srcId);
            Host dst = hostService.getHost(id);

            //add host to host map
            if(!mtdHostsManage.hostIpAddressMap.containsKey(src) && src!=null){
                mtdHostsManage.addHost(src);
            }
            if(!mtdHostsManage.hostIpAddressMap.containsKey(dst) && dst!=null){
                mtdHostsManage.addHost(dst);
            }

            // 请勿以任何方式处理LLDP MAC地址。
            if (id.mac().isLldp()) {
                droppedPacket(macMetrics);
                return;
            }

            // 不要处理 IPv4 组播数据包，让 mfwd 处理它们
            if (ignoreIPv4Multicast && ethPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                if (id.mac().isMulticast()) {
                    return;
                }
            }

            // 我们知道这是给谁的吗？如果没有，则flood。
            if (dst == null) {
                flood(context, macMetrics);
                return;
            }

            // 我们是否在目的地所在的边缘交换机上？如果是这样，只需direct转发到目的地即可.
            if (pkt.receivedFrom().deviceId().equals(dst.location().deviceId())) {
                if (!context.inPacket().receivedFrom().port().equals(dst.location().port())) {
                    if((int)ethPkt.getEtherType()==2048) {
                        installRule(context, dst.location().port(), macMetrics);
                    }else{
                        reIPInstallRule(context, dst.location().port(), macMetrics,src,dst);
                    }

                }
                return;
            }

            topologyService.currentTopology();
            // 否则，获取一组从此处通向目标边缘交换机的路径。
            Set<Path> paths =
                    topologyService.getPaths(topologyService.currentTopology(),
                            pkt.receivedFrom().deviceId(),
                            dst.location().deviceId());
            if (paths.isEmpty()) {
                //如果没有路径，广播.
                flood(context, macMetrics);
                return;
            }

            List<Path> pathList = findForwardPathsIfPossible(paths, pkt.receivedFrom().port());

            // 选择一条不会回到原处的路；如果没有这样的路径，广播,
            int pathNum=0;
            if(MtdMechanism.pathMtdSign && (mtdHostsManage.pathTM.get(src)==Boolean.valueOf(true))){
                pathNum=(int) (Math.random()*pathList.size());
            }
            Path path=pathList.get(pathNum);
            if (path == null) {
                log.warn("Don't know where to go from here {} for {} -> {}",
                        pkt.receivedFrom(), ethPkt.getSourceMAC(), ethPkt.getDestinationMAC());
                flood(context, macMetrics);
                return;
            }
            int ege=ethPkt.getEtherType();
            byte[] serialize = ethPkt.serialize();
            if((int)ethPkt.getEtherType()==2048) {
                installRule(context, path.src().port(), macMetrics);
            }else{
                iPInstallRule(context, path.src().port(),macMetrics,src,dst);
            }



        }

    }

    // 如果允许，则广播定的数据包。
    private void flood(PacketContext context, ReactiveForwardMetrics macMetrics) {
        if (topologyService.isBroadcastPoint(topologyService.currentTopology(),
                context.inPacket().receivedFrom())) {
            packetOut(context, PortNumber.FLOOD, macMetrics);
        } else {
            context.block();
        }
    }

    // 从指定端口发送数据包.
    private void packetOut(PacketContext context, PortNumber portNumber, ReactiveForwardMetrics macMetrics) {

        replyPacket(macMetrics);
        context.treatmentBuilder().setOutput(portNumber);
        context.send();
    }

    // ip mtd mechanism (install rule to change real ip to virtual ip)
    private void iPInstallRule(PacketContext context, PortNumber portNumber, ReactiveForwardMetrics macMetrics,Host src,Host dst) {
        // （尚）不支持 Flow Service 中的缓冲区 ID，因此请先打包。
        Ethernet inPkt = context.inPacket().parsed();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        //int ipppp=inPkt.getEtherType();
        // If PacketOutOnly or ARP packet than forward directly to output port
        if (packetOutOnly || inPkt.getEtherType() == Ethernet.TYPE_ARP) {
            packetOut(context, portNumber, macMetrics);
            return;
        }

        //
        // If matchDstMacOnly
        //    Create flows matching dstMac only
        // Else
        //    Create flows with default matching and include configured fields
        //
        if (matchDstMacOnly) {
            selectorBuilder.matchEthDst(inPkt.getDestinationMAC());
        }
        else {
            selectorBuilder.matchInPort(context.inPacket().receivedFrom().port())
                    .matchEthSrc(inPkt.getSourceMAC())
                    .matchEthDst(inPkt.getDestinationMAC());

            // 如果配置了匹配 Vlan ID
            if (matchVlanId && inPkt.getVlanID() != Ethernet.VLAN_UNTAGGED) {
                selectorBuilder.matchVlanId(VlanId.vlanId(inPkt.getVlanID()));
            }

            //
            // If configured and EtherType is IPv4 - Match IPv4 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv4Address && inPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
                byte ipv4Protocol = ipv4Packet.getProtocol();
                Ip4Prefix matchIp4SrcPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                Ip4Prefix matchIp4DstPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix);

                if (matchIpv4Dscp) {
                    byte dscp = ipv4Packet.getDscp();
                    byte ecn = ipv4Packet.getEcn();
                    selectorBuilder.matchIPDscp(dscp).matchIPEcn(ecn);
                }

                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv4Protocol == IPv4.PROTOCOL_ICMP) {
                    ICMP icmpPacket = (ICMP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchIcmpType(icmpPacket.getIcmpType())
                            .matchIcmpCode(icmpPacket.getIcmpCode());
                }
            }

            //
            // If configured and EtherType is IPv6 - Match IPv6 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv6Address && inPkt.getEtherType() == Ethernet.TYPE_IPV6) {
                IPv6 ipv6Packet = (IPv6) inPkt.getPayload();
                byte ipv6NextHeader = ipv6Packet.getNextHeader();
                Ip6Prefix matchIp6SrcPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getSourceAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                Ip6Prefix matchIp6DstPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getDestinationAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV6)
                        .matchIPv6Src(matchIp6SrcPrefix)
                        .matchIPv6Dst(matchIp6DstPrefix);

                if (matchIpv6FlowLabel) {
                    selectorBuilder.matchIPv6FlowLabel(ipv6Packet.getFlowLabel());
                }

                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv6NextHeader == IPv6.PROTOCOL_ICMP6) {
                    ICMP6 icmp6Packet = (ICMP6) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchIcmpv6Type(icmp6Packet.getIcmpType())
                            .matchIcmpv6Code(icmp6Packet.getIcmpCode());
                }
            }
        }

//        //in order to change ip adress back on the last device
//        TrafficSelector.Builder selectorBuilder2 = DefaultTrafficSelector.builder();
//        selectorBuilder2.matchEthDst(inPkt.getDestinationMAC());
//        TrafficTreatment treatment2 = DefaultTrafficTreatment.builder()
//                .setIpDst(mtdHostsManage.hostIpAddressMap.get(dst))
//                .setIpSrc(mtdHostsManage.hostIpAddressMap.get(src))
////                .setOutput(path.dst().port())
//                .build();
        TrafficTreatment treatment;
        if(MtdMechanism.ipMtdSign && ((MtdMechanism.portMtdSign== false)||(mtdHostsManage.portTM.get(src) == Boolean.valueOf(false)))){
            treatment = DefaultTrafficTreatment.builder()
                    .setIpDst(mtdHostsManage.realVirtualIpMap.get(mtdHostsManage.hostIpAddressMap.get(dst)))
                    .setIpSrc(mtdHostsManage.realVirtualIpMap.get(mtdHostsManage.hostIpAddressMap.get(src)))
                    .setOutput(portNumber)
                    .build();
        }

        else if ((MtdMechanism.portMtdSign&&(mtdHostsManage.portTM.get(src) == Boolean.valueOf(true))) && (MtdMechanism.ipMtdSign==false)){
            treatment = DefaultTrafficTreatment.builder()
                    .setTcpDst(TpPort.tpPort((int)(Math.random()*6000+5)))
                    .setTcpSrc(TpPort.tpPort((int)(Math.random()*6000+5)))
                    .setOutput(portNumber)
                    .build();
        }

        else if((MtdMechanism.portMtdSign&&(mtdHostsManage.portTM.get(src) == Boolean.valueOf(true))) && MtdMechanism.ipMtdSign){
            treatment = DefaultTrafficTreatment.builder()
                    .setIpDst(mtdHostsManage.realVirtualIpMap.get(mtdHostsManage.hostIpAddressMap.get(dst)))
                    .setIpSrc(mtdHostsManage.realVirtualIpMap.get(mtdHostsManage.hostIpAddressMap.get(src)))
                    .setTcpDst(TpPort.tpPort((int)(Math.random()*6000+5)))
                    .setTcpSrc(TpPort.tpPort((int)(Math.random()*6000+5)))
                    .setOutput(portNumber)
                    .build();
        }
        else {
            treatment = DefaultTrafficTreatment.builder()
                    .setOutput(portNumber)
                    .build();
        }


        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makeTemporary(flowTimeout)
                .add();

        flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(),
                forwardingObjective);
        forwardPacket(macMetrics);
        //
        // If packetOutOfppTable
        //  Send packet back to the OpenFlow pipeline to match installed flow
        // Else
        //  Send packet direction on the appropriate port
        //
        if (packetOutOfppTable) {
            packetOut(context, PortNumber.TABLE, macMetrics);
        } else {
            packetOut(context, portNumber, macMetrics);
        }
    }
    // install rule to rechange back
    private void reIPInstallRule(PacketContext context, PortNumber portNumber, ReactiveForwardMetrics macMetrics,Host src,Host dst) {
        // （尚）不支持 Flow Service 中的缓冲区 ID，因此请先打包。
        Ethernet inPkt = context.inPacket().parsed();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        // If PacketOutOnly or ARP packet than forward directly to output port
        if (packetOutOnly || inPkt.getEtherType() == Ethernet.TYPE_ARP) {
            packetOut(context, portNumber, macMetrics);
            return;
        }

        //
        // If matchDstMacOnly
        //    Create flows matching dstMac only
        // Else
        //    Create flows with default matching and include configured fields
        //
        if (matchDstMacOnly) {
            selectorBuilder.matchEthDst(inPkt.getDestinationMAC());
        }
        else {
            selectorBuilder.matchInPort(context.inPacket().receivedFrom().port())
                    .matchEthSrc(inPkt.getSourceMAC())
                    .matchEthDst(inPkt.getDestinationMAC());

            // 如果配置了匹配 Vlan ID
            if (matchVlanId && inPkt.getVlanID() != Ethernet.VLAN_UNTAGGED) {
                selectorBuilder.matchVlanId(VlanId.vlanId(inPkt.getVlanID()));
            }

            //
            // If configured and EtherType is IPv4 - Match IPv4 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv4Address && inPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
                byte ipv4Protocol = ipv4Packet.getProtocol();
                Ip4Prefix matchIp4SrcPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                Ip4Prefix matchIp4DstPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix);

                if (matchIpv4Dscp) {
                    byte dscp = ipv4Packet.getDscp();
                    byte ecn = ipv4Packet.getEcn();
                    selectorBuilder.matchIPDscp(dscp).matchIPEcn(ecn);
                }

                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv4Protocol == IPv4.PROTOCOL_ICMP) {
                    ICMP icmpPacket = (ICMP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchIcmpType(icmpPacket.getIcmpType())
                            .matchIcmpCode(icmpPacket.getIcmpCode());
                }
            }

            //
            // If configured and EtherType is IPv6 - Match IPv6 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv6Address && inPkt.getEtherType() == Ethernet.TYPE_IPV6) {
                IPv6 ipv6Packet = (IPv6) inPkt.getPayload();
                byte ipv6NextHeader = ipv6Packet.getNextHeader();
                Ip6Prefix matchIp6SrcPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getSourceAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                Ip6Prefix matchIp6DstPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getDestinationAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV6)
                        .matchIPv6Src(matchIp6SrcPrefix)
                        .matchIPv6Dst(matchIp6DstPrefix);

                if (matchIpv6FlowLabel) {
                    selectorBuilder.matchIPv6FlowLabel(ipv6Packet.getFlowLabel());
                }

                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv6NextHeader == IPv6.PROTOCOL_ICMP6) {
                    ICMP6 icmp6Packet = (ICMP6) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchIcmpv6Type(icmp6Packet.getIcmpType())
                            .matchIcmpv6Code(icmp6Packet.getIcmpCode());
                }
            }
        }

        TrafficTreatment treatment;

        if(MtdMechanism.ipMtdSign){
            treatment = DefaultTrafficTreatment.builder()
                    .setIpDst(mtdHostsManage.hostIpAddressMap.get(dst))
                    .setIpSrc(mtdHostsManage.hostIpAddressMap.get(src))
                    .setOutput(portNumber)
                    .build();
        }

        else {
            treatment = DefaultTrafficTreatment.builder()
                    .setOutput(portNumber)
                    .build();
        }

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makeTemporary(flowTimeout)
                .add();

        flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(),
                forwardingObjective);
        forwardPacket(macMetrics);
        //
        // If packetOutOfppTable
        //  Send packet back to the OpenFlow pipeline to match installed flow
        // Else
        //  Send packet direction on the appropriate port
        //
        if (packetOutOfppTable) {
            packetOut(context, PortNumber.TABLE, macMetrics);
        } else {
            packetOut(context, portNumber, macMetrics);
        }
    }

    // Install a rule forwarding the packet to the specified port.
    private void installRule(PacketContext context, PortNumber portNumber, ReactiveForwardMetrics macMetrics) {
        //
        // We don't support (yet) buffer IDs in the Flow Service so
        // packet out first.
        //
        Ethernet inPkt = context.inPacket().parsed();
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();

        // If PacketOutOnly or ARP packet than forward directly to output port
        if (packetOutOnly || inPkt.getEtherType() == Ethernet.TYPE_ARP) {
            packetOut(context, portNumber, macMetrics);
            return;
        }

        //
        // If matchDstMacOnly
        //    Create flows matching dstMac only
        // Else
        //    Create flows with default matching and include configured fields
        //
        if (matchDstMacOnly) {
            selectorBuilder.matchEthDst(inPkt.getDestinationMAC());
        } else {
            selectorBuilder.matchInPort(context.inPacket().receivedFrom().port())
                    .matchEthSrc(inPkt.getSourceMAC())
                    .matchEthDst(inPkt.getDestinationMAC());

            // If configured Match Vlan ID
            if (matchVlanId && inPkt.getVlanID() != Ethernet.VLAN_UNTAGGED) {
                selectorBuilder.matchVlanId(VlanId.vlanId(inPkt.getVlanID()));
            }

            //
            // If configured and EtherType is IPv4 - Match IPv4 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv4Address && inPkt.getEtherType() == Ethernet.TYPE_IPV4) {
                IPv4 ipv4Packet = (IPv4) inPkt.getPayload();
                byte ipv4Protocol = ipv4Packet.getProtocol();
                Ip4Prefix matchIp4SrcPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getSourceAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                Ip4Prefix matchIp4DstPrefix =
                        Ip4Prefix.valueOf(ipv4Packet.getDestinationAddress(),
                                Ip4Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPSrc(matchIp4SrcPrefix)
                        .matchIPDst(matchIp4DstPrefix);

                if (matchIpv4Dscp) {
                    byte dscp = ipv4Packet.getDscp();
                    byte ecn = ipv4Packet.getEcn();
                    selectorBuilder.matchIPDscp(dscp).matchIPEcn(ecn);
                }

                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv4Protocol == IPv4.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv4Protocol == IPv4.PROTOCOL_ICMP) {
                    ICMP icmpPacket = (ICMP) ipv4Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv4Protocol)
                            .matchIcmpType(icmpPacket.getIcmpType())
                            .matchIcmpCode(icmpPacket.getIcmpCode());
                }
            }

            //
            // If configured and EtherType is IPv6 - Match IPv6 and
            // TCP/UDP/ICMP fields
            //
            if (matchIpv6Address && inPkt.getEtherType() == Ethernet.TYPE_IPV6) {
                IPv6 ipv6Packet = (IPv6) inPkt.getPayload();
                byte ipv6NextHeader = ipv6Packet.getNextHeader();
                Ip6Prefix matchIp6SrcPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getSourceAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                Ip6Prefix matchIp6DstPrefix =
                        Ip6Prefix.valueOf(ipv6Packet.getDestinationAddress(),
                                Ip6Prefix.MAX_MASK_LENGTH);
                selectorBuilder.matchEthType(Ethernet.TYPE_IPV6)
                        .matchIPv6Src(matchIp6SrcPrefix)
                        .matchIPv6Dst(matchIp6DstPrefix);

                if (matchIpv6FlowLabel) {
                    selectorBuilder.matchIPv6FlowLabel(ipv6Packet.getFlowLabel());
                }

                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_TCP) {
                    TCP tcpPacket = (TCP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchTcpSrc(TpPort.tpPort(tcpPacket.getSourcePort()))
                            .matchTcpDst(TpPort.tpPort(tcpPacket.getDestinationPort()));
                }
                if (matchTcpUdpPorts && ipv6NextHeader == IPv6.PROTOCOL_UDP) {
                    UDP udpPacket = (UDP) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchUdpSrc(TpPort.tpPort(udpPacket.getSourcePort()))
                            .matchUdpDst(TpPort.tpPort(udpPacket.getDestinationPort()));
                }
                if (matchIcmpFields && ipv6NextHeader == IPv6.PROTOCOL_ICMP6) {
                    ICMP6 icmp6Packet = (ICMP6) ipv6Packet.getPayload();
                    selectorBuilder.matchIPProtocol(ipv6NextHeader)
                            .matchIcmpv6Type(icmp6Packet.getIcmpType())
                            .matchIcmpv6Code(icmp6Packet.getIcmpCode());
                }
            }
        }
        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                .setOutput(portNumber)
                .build();

        ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                .withSelector(selectorBuilder.build())
                .withTreatment(treatment)
                .withPriority(flowPriority)
                .withFlag(ForwardingObjective.Flag.VERSATILE)
                .fromApp(appId)
                .makeTemporary(flowTimeout)
                .add();

        flowObjectiveService.forward(context.inPacket().receivedFrom().deviceId(),
                forwardingObjective);
        forwardPacket(macMetrics);
        //
        // If packetOutOfppTable
        //  Send packet back to the OpenFlow pipeline to match installed flow
        // Else
        //  Send packet direction on the appropriate port
        //
        if (packetOutOfppTable) {
            packetOut(context, PortNumber.TABLE, macMetrics);
        } else {
            packetOut(context, portNumber, macMetrics);
        }
    }

    // 指示这是否是控制数据包，例如LLDP、BDDP
    private boolean isControlPacket(Ethernet eth) {
        short type = eth.getEtherType();
        return type == Ethernet.TYPE_LLDP || type == Ethernet.TYPE_BSN;
    }

    // 指示这是否是 IPv6 组播数据包。
    private boolean isIpv6Multicast(Ethernet eth) {
        return eth.getEtherType() == Ethernet.TYPE_IPV6 && eth.isMulticast();
    }


    //identify all correct paths
    private List<Path> findForwardPathsIfPossible(Set<Path> paths, PortNumber notToPort) {
        List<Path>  pathList=new ArrayList<>();
        for (Path path : paths) {
            if (!path.src().port().equals(notToPort)) {
                pathList.add(path);
            }
        }
        return pathList;
    }

    private class InternalTopologyListener implements TopologyListener {
        @Override
        public void event(TopologyEvent event) {
            //返回触发拓扑更改的事件列表。
            List<Event> reasons = event.reasons();
            if (reasons != null) {
                reasons.forEach(re -> {
                    if (re instanceof LinkEvent) {
                        LinkEvent le = (LinkEvent) re;
                        if (le.type() == LinkEvent.Type.LINK_REMOVED && blackHoleExecutor != null) {
//                            subject:返回事件的主题。
                            //lambdad create thread,Runnable runnable2=()->System.out.println("Running from Lambda");
                            blackHoleExecutor.submit(() -> fixBlackhole(le.subject().src()));
                        }
                    }
                });
            }
        }
    }

    //host add or reduce event Listener
    private class InternalHostListener implements HostListener{
        @Override
        public void event(HostEvent event) {
            if (event.type()== HostEvent.Type.HOST_ADDED){
                mtdHostsManage.addHost(event.subject());
            }
            else if(event.type()== HostEvent.Type.HOST_REMOVED){
                mtdHostsManage.remoteHost(event.subject());
            }
            else {
                log.info(event.prevSubject()+"is change");
            }

        }
    }

    private void fixBlackhole(ConnectPoint egress) {
        Set<FlowEntry> rules = getFlowRulesFrom(egress);
        Set<SrcDstPair> pairs = findSrcDstPairs(rules);

        Map<DeviceId, Set<Path>> srcPaths = new HashMap<>();

        for (SrcDstPair sd : pairs) {
            // 获取 src 主机的边缘设备 ID
            Host srcHost = hostService.getHost(HostId.hostId(sd.src));
            Host dstHost = hostService.getHost(HostId.hostId(sd.dst));
            if (srcHost != null && dstHost != null) {
                DeviceId srcId = srcHost.location().deviceId();
                DeviceId dstId = dstHost.location().deviceId();
                log.trace("SRC ID is {}, DST ID is {}", srcId, dstId);

                cleanFlowRules(sd, egress.deviceId());

                Set<Path> shortestPaths = srcPaths.get(srcId);
                if (shortestPaths == null) {
                    shortestPaths = topologyService.getPaths(topologyService.currentTopology(),
                            egress.deviceId(), srcId);
                    srcPaths.put(srcId, shortestPaths);
                }
                backTrackBadNodes(shortestPaths, dstId, sd);
            }
        }
    }

    // 从链路断开事件回溯以删除导致黑洞的流量
    private void backTrackBadNodes(Set<Path> shortestPaths, DeviceId dstId, SrcDstPair sd) {
        for (Path p : shortestPaths) {
            List<Link> pathLinks = p.links();
            for (int i = 0; i < pathLinks.size(); i = i + 1) {
                Link curLink = pathLinks.get(i);
                DeviceId curDevice = curLink.src().deviceId();

                // skipping the first link because this link's src has already been pruned beforehand
                if (i != 0) {
                    cleanFlowRules(sd, curDevice);
                }

                Set<Path> pathsFromCurDevice =
                        topologyService.getPaths(topologyService.currentTopology(),
                                curDevice, dstId);
                if (findForwardPathsIfPossible(pathsFromCurDevice, curLink.src().port()) != null) {
                    break;
                } else {
                    if (i + 1 == pathLinks.size()) {
                        cleanFlowRules(sd, curLink.dst().deviceId());
                    }
                }
            }
        }
    }

    // 删除具有特定 SrcDstPair 的指定设备的流规则
    private void cleanFlowRules(SrcDstPair pair, DeviceId id) {
        log.trace("Searching for flow rules to remove from: {}", id);
        log.trace("Removing flows w/ SRC={}, DST={}", pair.src, pair.dst);
        for (FlowEntry r : flowRuleService.getFlowEntries(id)) {
            boolean matchesSrc = false, matchesDst = false;
            for (Instruction i : r.treatment().allInstructions()) {
                if (i.type() == Instruction.Type.OUTPUT) {
                    // if the flow has matching src and dst
                    for (Criterion cr : r.selector().criteria()) {
                        if (cr.type() == Criterion.Type.ETH_DST) {
                            if (((EthCriterion) cr).mac().equals(pair.dst)) {
                                matchesDst = true;
                            }
                        } else if (cr.type() == Criterion.Type.ETH_SRC) {
                            if (((EthCriterion) cr).mac().equals(pair.src)) {
                                matchesSrc = true;
                            }
                        }
                    }
                }
            }
            if (matchesDst && matchesSrc) {
                log.trace("Removed flow rule from device: {}", id);
                flowRuleService.removeFlowRules((FlowRule) r);
            }
        }
    }

    // 返回从指定的流条目集中提取的一组 src/dst MAC 对
    private Set<SrcDstPair> findSrcDstPairs(Set<FlowEntry> rules) {
        ImmutableSet.Builder<SrcDstPair> builder = ImmutableSet.builder();
        for (FlowEntry r : rules) {
            MacAddress src = null, dst = null;
            //criteria()以有序列表的形式返回选择条件。
            for (Criterion cr : r.selector().criteria()) {
                if (cr.type() == Criterion.Type.ETH_DST) {
                    dst = ((EthCriterion) cr).mac();
                } else if (cr.type() == Criterion.Type.ETH_SRC) {
                    src = ((EthCriterion) cr).mac();
                }
            }
            builder.add(new SrcDstPair(src, dst));
        }
        return builder.build();
    }

    //将连接点设备上面本应用下发的转发流表项添加到集合里面
    private Set<FlowEntry> getFlowRulesFrom(ConnectPoint egress) {
        ImmutableSet.Builder<FlowEntry> builder = ImmutableSet.builder();
        flowRuleService.getFlowEntries(egress.deviceId()).forEach(r -> {
            if (r.appId() == appId.id()) {
                r.treatment().allInstructions().forEach(i -> {
                    if (i.type() == Instruction.Type.OUTPUT) {
                        if (((Instructions.OutputInstruction) i).port().equals(egress.port())) {
                            builder.add(r);
                        }
                    }
                });
            }
        });

        return builder.build();
    }

    // 源和目标 MAC 地址对的包装类
    private final class SrcDstPair {
        final MacAddress src;
        final MacAddress dst;

        private SrcDstPair(MacAddress src, MacAddress dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SrcDstPair that = (SrcDstPair) o;
            return Objects.equals(src, that.src) &&
                    Objects.equals(dst, that.dst);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, dst);
        }
    }

    private ReactiveForwardMetrics createCounter(MacAddress macAddress) {
        ReactiveForwardMetrics macMetrics = null;
        if (recordMetrics) {
            macMetrics = metrics.compute(macAddress, (key, existingValue) -> {
                if (existingValue == null) {
                    return new ReactiveForwardMetrics(0L, 0L, 0L, 0L, macAddress);
                } else {
                    return existingValue;
                }
            });
        }
        return macMetrics;
    }

    private void  forwardPacket(ReactiveForwardMetrics macmetrics) {
        if (recordMetrics) {
            macmetrics.incrementForwardedPacket();
            metrics.put(macmetrics.getMacAddress(), macmetrics);
        }
    }

    private void inPacket(ReactiveForwardMetrics macmetrics) {
        if (recordMetrics) {
            macmetrics.incrementInPacket();
            metrics.put(macmetrics.getMacAddress(), macmetrics);
        }
    }

    private void replyPacket(ReactiveForwardMetrics macmetrics) {
        if (recordMetrics) {
            macmetrics.incremnetReplyPacket();
            metrics.put(macmetrics.getMacAddress(), macmetrics);
        }
    }

    private void droppedPacket(ReactiveForwardMetrics macmetrics) {
        if (recordMetrics) {
            macmetrics.incrementDroppedPacket();
            metrics.put(macmetrics.getMacAddress(), macmetrics);
        }
    }

    public EventuallyConsistentMap<MacAddress, ReactiveForwardMetrics> getMacAddress() {
        return metrics;
    }

    public void printMetric(MacAddress mac) {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println(" MACADDRESS \t\t\t\t\t\t Metrics");
        if (mac != null) {
            System.out.println(" " + mac + " \t\t\t " + metrics.get(mac));
        } else {
            for (MacAddress key : metrics.keySet()) {
                System.out.println(" " + key + " \t\t\t " + metrics.get(key));
            }
        }
    }


}
