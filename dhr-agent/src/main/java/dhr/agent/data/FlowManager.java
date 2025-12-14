package dhr.agent.data;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowEntry;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.osgi.service.component.annotations.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dhr.agent.utility.Log.writeFlowLog;
import static dhr.agent.utility.Log.writeLog;
@Component(immediate = true, service = FlowManager.class)
public class FlowManager extends TimerTask {
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    public FlowManager() {
    }

    private Feature dataFeature;
    private Feature receivedData = null;
    private boolean isPolymorphicMode;

    private String pythonServer = "localhost";
//    private String pythonServer = "192.168.10.12";
    private int pythonServerPort = 13131;
    private Socket socket;
    ApplicationId coreId;

    public static String logFolderPath = System.getProperty("user.home") + "/dhr_log";
    public static String csvPath = logFolderPath + "/feature.csv";

    List<Feature> featureList;

    int sumPacket;
    int sumByte;
    private FeatureManager featureManager;

    // 存储所有特征key
    Set<String> featureKeys = new LinkedHashSet<>();
    List<LinkedHashMap<String, String>> featureMaps = new ArrayList<>();

//    public FlowInfoProcessor( ){
//        socket = null;
//    }
//
//    public FlowInfoProcessor(Socket socket){
//        this.socket = socket;
//    }

    @Activate
    public void activate() {
        dataFeature = new Feature();
        featureManager = new FeatureManager();
        featureList = new ArrayList<>();
        featureMaps = new ArrayList<>();
        coreId = coreService.getAppId("org.onosproject.core");
        // 初始化逻辑
//        new Thread(this).start();
    }

    @Deactivate
    public void deactivate() {
        // 清理资源
//        Thread.currentThread().interrupt();
        // 关闭socket
        try {
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setPolymorphicMode(boolean polymorphicMode) {
        isPolymorphicMode = polymorphicMode;
    }

    private void analysePolymorphicFlow(List<FlowEntry> flowEntriesListById){
        int flowListSize = flowEntriesListById.size();
        if (flowListSize != 0) {
            for (FlowEntry flowEntry : flowEntriesListById) {
                TrafficSelector selector = flowEntry.selector();
                // 获取源标识信息
                String srcInfo = featureManager.getSourceIdentification(selector);
                if (srcInfo != null) {
//                    srcIdentifications.add(srcInfo);
                }
                sumPacket += flowEntry.packets();
                sumByte += flowEntry.bytes();
            }
//            calculatePolymorphicFeature(deviceId, sumPacket, sumByte, flowListSize, srcIdentifications,  dataFeature);
//            srcIdentifications.clear();
            // TODO : 确认是否需要释放flowEntriesListById的内存，怎么释放
        } else {
            dataFeature.initFeature();
            return;
        }
    }

    private void analyseIpFlow(List<FlowEntry> flowEntriesListById) throws CloneNotSupportedException {
        int flowListSize = flowEntriesListById.size();
        if (flowListSize != 0) {
            for (FlowEntry flowEntry : flowEntriesListById) {
                featureManager.setFlowFeature(flowEntry);
                sumPacket += flowEntry.packets();
                sumByte += flowEntry.bytes();
                featureManager.setFlowsFeature(sumPacket, sumByte, flowListSize);
                //此处是引用拷贝，需要深拷贝
                Feature feature = (Feature) featureManager.getFeature().clone();
                featureList.add(feature);
                featureManager.initFeature();
            }
            featureManager.initContainer();
            // TODO : 确认是否需要释放flowEntriesListById的内存，怎么释放
        } else {
            dataFeature.initFeature();
            return;
        }
    }

    // 计算用于检测攻击的流量特征值
    // TODO ： 看看内存、速度等方面还有没有能优化的点
    private void getFlowInfo(DeviceId deviceId) throws CloneNotSupportedException {
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
//        writeFlowLog(flowEntriesListById);
        if (isPolymorphicMode) {
            analysePolymorphicFlow(flowEntriesListById);
        } else {
            analyseIpFlow(flowEntriesListById);
        }

    }


    public static String formatString(String s) {
        // 首先，将首字母大写
        String formatted = s.substring(0, 1).toUpperCase() + s.substring(1);

        // 使用正则表达式在每个大写字母前加空格（不包括第一个字母）
        formatted = formatted.replaceAll("([a-z])([A-Z])", "$1 $2");
        formatted = formatted.replace("Iat", "IAT");
        return formatted;
    }


    //     解析单条特征字符串并返回一个Map
    public LinkedHashMap<String, String> parseFeatureString(String featureStr) {
        LinkedHashMap<String, String> featureMap = new LinkedHashMap<>();
        // 使用正则表达式匹配键值对
        String regex = "(\\w+)='([^']*)'|([\\w]+)=([^,]+)";
        // 去掉 'Feature{' 和 '}'
        featureStr = featureStr.replace("Feature{", "").replace("}", "");
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(featureStr);

        while (matcher.find()) {
            // 如果捕获的是键=值对
            if (matcher.group(1) != null && matcher.group(2) != null) {
                featureMap.put(formatString(matcher.group(1)), matcher.group(2));
            } else if (matcher.group(3) != null && matcher.group(4) != null) {
                featureMap.put(formatString(matcher.group(3)), matcher.group(4));
            }
        }
        // 提取所有特征名称作为CSV头部
        if (featureKeys.isEmpty()){
            featureKeys.addAll(featureMap.keySet());
        }
        return featureMap;
    }

//    public static Map<String, String> parseFeatureString(String featureData) {
//        Map<String, String> featureMap = new HashMap<>();
//
//        // 去掉 'Feature{' 和 '}'
//        featureData = featureData.replace("Feature{", "").replace("}", "");
//
//        // 将特征字段按逗号分割
//        String[] fields = featureData.split(", ");
//        for (String field : fields) {
//            // 按 '=' 分割字段名和值
//            String[] keyValue = field.split("=");
//            if (keyValue.length == 2) {
//                String key = keyValue[0].trim();
//                String value = keyValue[1].trim().replace("'", ""); // 移除单引号
//                featureMap.put(key, value);
//            }
//        }
//
//        return featureMap;
//    }


    // 生成CSV文件,并开始请求流检测
    public void generateCSV(String outputFile) {
        File file = new File(outputFile);

        // 解析每一条特征字符串
        for (Feature feature : featureList) {
            LinkedHashMap<String, String> featureMap = parseFeatureString(feature.toString());
            // 请求流检测parseFeatureString
            featureMaps.add(featureMap);
        }

        boolean needHead = false;


        if (!file.exists() || file.length() == 0) {
            needHead = true;
        }

        BufferedWriter writer;
        // 写入CSV文件
        try {
            writer = new BufferedWriter(new FileWriter(outputFile, true));
            // 写入CSV头部
            if (needHead) {
                LinkedHashSet<String> headStrings = new LinkedHashSet<>();
                for (String feature : featureKeys) {
                    headStrings.add(feature);
                }
                writer.write(String.join(",", headStrings));
                writer.newLine();
            }
            // 写入每一行数据
            for (Map<String, String> featureMap : featureMaps) {
                List<String> rowData = new ArrayList<>();
                for (String key : featureKeys) {
                    // 如果特征Map中有该键，就添加值；否则添加空字符串
                    rowData.add(featureMap.getOrDefault(key, ""));
                }
                if (rowData.size() == featureKeys.size()) {
                    writer.write(String.join(",", rowData));
                    writer.newLine();
                } else {
                    writeLog("get wrong data:" + String.join(",", rowData));
                }

            }
//            writeLog("CSV file written successfully to " + outputFile);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestDetectionService() {
        if(!featureMaps.isEmpty()){
            for (Map<String, String> featureMap : featureMaps) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // 转为JSON格式
                    String jsonData = objectMapper.writeValueAsString(featureMap);
                    // log.info(jsonData);
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    InputStream in = socket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    out.print(jsonData);
                    out.flush();

                    this.receivedData = null;
                    // 接受服务器的回复,会阻塞等待
                    String receivedJsonData = br.readLine();

                    this.receivedData = objectMapper.readValue(receivedJsonData, Feature.class);

                } catch (IOException e) {
                    // log.info("发送发生了异常");
                    e.printStackTrace();
                }
            }

        }
    }

    // TODO : 接受消息处还需斟酌
    @Override
    public void run() {
        // 获得交换机，并遍历
//        writeLog("IDS run!");
        deviceService.getDevices().forEach(device -> {
            sumPacket = 0;
            sumByte = 0;
            DeviceId id = device.id();
//            writeLog(id);
            // 得到该交换机的几个特征
            try {
                getFlowInfo(id);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
/*            try {
                // 这里创建时发生了异常，直接走向了catch
                // 如果服务端未启动的话，就会出现异常
                if (socket == null || socket.isClosed()) {
                    socket = new Socket(pythonServer, pythonServerPort);
                }
            } catch (IOException e){
                writeLog("The program encountered a socket connection exception!");
                e.printStackTrace();
            }*/
            if (!featureList.isEmpty()) {
                generateCSV(csvPath);
                featureList.clear();
            }
            // 实时流检测
            if (!featureMaps.isEmpty()) {
/*                try {
                    requestDetectionService();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                featureMaps.clear();
            }

//            for (Feature feature : featureList) {
//                writeLog(feature.toString());
//            }

        });
    }
}