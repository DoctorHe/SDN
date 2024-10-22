/*
 * Copyright 2023-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nids.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.nids.app.utility.GetFeature;
import org.onlab.packet.*;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.*;
import org.onosproject.net.host.HostService;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.nids.app.data.DataFeature;
import org.nids.app.mitigation.ThreadedTraceBack;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;
import java.util.*;

import static org.nids.app.utility.Compute.*;
import static org.nids.app.utility.GetFeature.*;
import static org.nids.app.utility.Log.*;
import static org.onlab.util.Tools.get;

/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true,
        service = {SomeInterface.class},
        property = {
                "someProperty=Some Default String Value",
        })
public class AppComponent implements SomeInterface {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Some configurable property.
     */
    private String someProperty;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    public static final int FLOW_INFO_INTERVAL = 5000; // 收集流信息时间间隔,毫秒

    private ApplicationId appId;

    private InPacketProcessor inPacketProcessor = new InPacketProcessor();

    private Timer timer = new Timer();

    public static Hashtable<String, String> polymorphicHashtable = new Hashtable<>();

    private String pythonServer = "localhost";
    private int pythonServerPort = 13131;
    private Socket socket;

    private boolean isPolymorphicMode;

    @Activate
    protected void activate() {
        cfgService.registerProperties(getClass());
        appId = coreService.registerApplication("org.ids.app");

        // 注册包处理,包处理器将会添加到现有包处理器列表当中
        packetService.addProcessor(inPacketProcessor, PacketProcessor.director(0));

        // 填充模态信息
        initPolymorphicHashtable();

        log.info("Packet Processor registered");
        try {
            // 这里创建时发生了异常，直接走向了catch
            // 如果服务端未启动的话，就会出现异常
            socket = new Socket(pythonServer, pythonServerPort);
            // 定期运行FlowInfoProcessor
            timer.schedule(new FlowInfoProcessor(socket), 0, FLOW_INFO_INTERVAL);
        } catch (IOException e){
            log.error("The program encountered a socket connection exception!");
            writeLog("The program encountered a socket connection exception!");
            e.printStackTrace();
        }
        writeLog("ids activate!");
        log.info("Started DDoS Defend");

    }

    @Deactivate
    protected void deactivate() {
        // 移除包处理器
        packetService.removeProcessor(inPacketProcessor);

        // 取消定时任务
        timer.cancel();

        // 关闭socket
        try {
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        cfgService.unregisterProperties(getClass(), false);
        log.info("IDS Stopped");
        writeLog("IDS Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
        if (context != null) {
            someProperty = get(properties, "someProperty");
        }
        log.info("Reconfigured");
    }

    @Override
    public void someMethod() {
        log.info("Invoked");
    }



    // 包处理器
    private class InPacketProcessor implements PacketProcessor {
        public InPacketProcessor() {
        }

        @Override
        public void process(PacketContext context) {
            // pkt是正在处理的入站数据包
            InboundPacket pkt = context.inPacket();
            Ethernet ethernetPacket = pkt.parsed();
        }
    }

    private void initPolymorphicHashtable() {
        polymorphicHashtable.put("0x8624", "hdr.ndn.name_tlv.components[0].value=");
        polymorphicHashtable.put("0x800", "hdr.ipv4.srcAddr=");
        polymorphicHashtable.put("0x27c0", "hdr.mf.src_guid=");
        polymorphicHashtable.put("0x812", "hdr.id.srcIdentity=");
    }
    private class FlowInfoProcessor extends TimerTask {

        private DataFeature dataFeature = new DataFeature();
        private DataFeature receivedData = null;
        ApplicationId coreId = coreService.getAppId("org.onosproject.core");
        private final Socket socket;

        // 存放源ip地址
        Set<IpAddress> srcIpSet = new HashSet<>();
        Set<String> srcIdentifications = new HashSet<>();

        // 存放ip地址与端口的映射
        Map<IpAddress, List<Integer>> ipPortMap = new HashMap<>();

        public FlowInfoProcessor( ){
            socket = null;
        }

        public FlowInfoProcessor(Socket socket){
            this.socket = socket;
        }

        private void analysePolymorphicFlow(DeviceId deviceId, List<FlowEntry> flowEntriesListById,
                                   int flowListSize, int sumPacket, int sumByte){
            if (flowListSize != 0) {
                for (FlowEntry flowEntry : flowEntriesListById) {
                    TrafficSelector selector = flowEntry.selector();
                    // 获取源标识信息
                    String srcInfo = GetFeature.getSourceIdentification(selector);
                    if (srcInfo != null) {
                        srcIdentifications.add(srcInfo);
                    }
                    sumPacket += flowEntry.packets();
                    sumByte += flowEntry.bytes();
                }
                calculatePolymorphicFeature(deviceId, sumPacket, sumByte, flowListSize, srcIdentifications,  dataFeature);
//                writeLog(dataFeature.toString());
                srcIdentifications.clear();
                // TODO : 确认是否需要释放flowEntriesListById的内存，怎么释放
            } else {
                dataFeature.initFeature();
                return;
            }
        }

        private void analyseIpFlow(DeviceId deviceId, List<FlowEntry> flowEntriesListById,
                                 int flowListSize, int sumPacket, int sumByte){
            if (flowListSize != 0) {
                for (FlowEntry flowEntry : flowEntriesListById) {
                    TrafficSelector selector = flowEntry.selector();
                    // 获取源端口信息
                    Integer srcPort = getPort(selector, Criterion.Type.TCP_SRC);
                    if (srcPort == null) {
                        srcPort = getPort(selector, Criterion.Type.UDP_SRC);
                    }
                    // 获取目的端口信息
                    Integer dstPort = getPort(selector, Criterion.Type.TCP_DST);
                    if (dstPort == null) {
                        dstPort = getPort(selector, Criterion.Type.UDP_DST);
                    }
                    // 获取源IP地址信息
                    IpAddress srcIP = getIpAddress(selector, Criterion.Type.IPV4_SRC);
                    if (srcIP != null) {
                        srcIpSet.add(srcIP);
                        // 将源ip与源端口对应起来
                        addIpPort(srcIP, srcPort);
                    }
                    // 获取目的IP地址信息
                    IpAddress dstIP = getIpAddress(selector, Criterion.Type.IPV4_DST);
                    if (dstIP != null) {
                        // 将目的ip与目的端口对应起来
                        addIpPort(dstIP, dstPort);
                    }
                    sumPacket += flowEntry.packets();
                    sumByte += flowEntry.bytes();
                }
                calculateIpFeature(deviceId, sumPacket, sumByte, flowListSize, srcIpSet, ipPortMap, dataFeature);
                srcIpSet.clear();
                ipPortMap.clear();
                // TODO : 确认是否需要释放flowEntriesListById的内存，怎么释放
            } else {
                dataFeature.initFeature();
                return;
            }
        }

        // 计算用于检测攻击的流量特征值
        // TODO ： 看看内存、速度等方面还有没有能优化的点
        private void getFlowInfo(DeviceId deviceId) {
            // ArrayList可以动态扩容
            List<FlowEntry> flowEntriesListById = new ArrayList<>(flowRuleService.getFlowRuleCount(deviceId,
                    FlowEntry.FlowEntryState.ADDED));
            // 获得设备流表项并放入列表
            Iterable<FlowEntry> flowEntriesByState = flowRuleService.getFlowEntriesByState(deviceId,
                    FlowEntry.FlowEntryState.ADDED);
            // forEach的迭代器里不能随便删除，否则会引发异常
            flowEntriesByState.forEach(flowEntry -> {
                flowEntriesListById.add(flowEntry);
            });
            // 删除列表中固定流表项
            Iterator iterator = flowEntriesListById.iterator();
            while (iterator.hasNext()) {
                FlowEntry flowEntry = (FlowEntry) iterator.next();
                if (flowEntry.appId() == coreId.id() ||
                        !(flowEntry.state().equals(FlowEntry.FlowEntryState.ADDED))) {
                    iterator.remove();
                }
            }
            writeFlowLog(flowEntriesListById);
            int flowListSize = flowEntriesListById.size();
            int sumPacket = 0;
            int sumByte = 0;
            if (isPolymorphicMode) {
                analysePolymorphicFlow(deviceId, flowEntriesListById, flowListSize, sumPacket, sumByte);
            } else {
                analyseIpFlow(deviceId, flowEntriesListById, flowListSize, sumPacket, sumByte);
            }

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


        // TODO : 接受消息处还需斟酌
        @Override
        public void run() {
            // 获得交换机，并遍历
            writeLog("IDS run!");
            if (hostService.getHosts().iterator().hasNext()){  //Indicates that the current mode is ip.
                isPolymorphicMode = false;
            }else{
                isPolymorphicMode = true;
            }
            deviceService.getDevices().forEach(device -> {
                DeviceId id = device.id();
                // 得到该交换机的几个特征
                getFlowInfo(id);
                if(this.dataFeature.getId() != null){
                    // 发送
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        // 转为JSON格式
                        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
                        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                        String jsonData = objectMapper.writeValueAsString(this.dataFeature);
                        // 完成JSON格式转换
                        // log.info(jsonData);
                        PrintStream out = new PrintStream(socket.getOutputStream());
                        InputStream in = socket.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
                        out.print(jsonData);
                        out.flush();

                        this.receivedData = null;
                        // 接受服务器的回复,会阻塞等待
                        String receivedJsonData = br.readLine();

                        this.receivedData = objectMapper.readValue(receivedJsonData, DataFeature.class);

                        if(this.receivedData != null && this.receivedData.isAttack()){
                            log.info(this.dataFeature.getStringId()+ "attack");
                            // 调缓解，这个ID是遍历交换机时候得到的
                            ThreadedTraceBack.mitigation(id);
                        } else {
                            // ThreadedTraceBack.mitigation(id, this.dataFeature.getStringId());
                            log.info(this.dataFeature.getStringId()+ "no_attack");

                        }
                    } catch (IOException e) {
                        // log.info("发送发生了异常");
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
