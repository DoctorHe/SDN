package org.onosproject.mtd.strategy;

import org.apache.commons.lang3.tuple.Pair;
import org.onlab.packet.IpAddress;
import org.onosproject.mtd.data.PolymorphicHost;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;


public class MtdHostsManage implements Runnable{

    int count=0;
    private final Logger log = getLogger(getClass());
    public boolean sign;//control the termination of threads

    public boolean isPolymorphicMode;
    // log path
    private static String logFolderPath = System.getProperty("user.home") + "/mtd_log";
    private static String hostIpAddressMapPath = logFolderPath + "/hostIpAddressMap.log";
    private static String hostToServerPath = logFolderPath + "/hostToServer.log";
    private static String mtdLogPath = logFolderPath + "/mtd.log";
    private static String realVirtualIpLogPath = logFolderPath + "/realVirtualIpMap.log";
    private static String realVirtualLogPath = logFolderPath + "/realVirtualMap.log";
    private static String interceptedHostIpPath = logFolderPath + "/interceptedHostIp.log";
    private static String polymorphicInterceptedHostIpPath = System.getProperty("user.home") + "/work_space/p4/bmv2/home/interceptedHostIp.log";

    // save hosts in map
    public Map<Host,IpAddress> hostIpAddressMap = new HashMap<Host, IpAddress>() ;
    public Map<IpAddress,Host> IpAddressHostMap = new HashMap<IpAddress,Host>() ;

    //Save the mapping between the real and virtual addresses of the host
    public Map<IpAddress,IpAddress> realVirtualIpMap = new HashMap<IpAddress, IpAddress>();

    private ArrayList<PolymorphicHost> polymorphicHosts = new ArrayList<>();
    //Store polymorphic identification information before and after the change
    private Map<PolymorphicHost, PolymorphicHost> realVirtualMap = new HashMap<>();

    //true or false transformation judgment matrix;
    public Map<Host, Boolean> portTM = new HashMap<Host, Boolean>();
    public Map<Host, Boolean> pathTM = new HashMap<Host, Boolean>();
    public Map<Host, Boolean> hostTM = new HashMap<Host, Boolean>();
    public int vmNumber;
    //get all hosts

    public MtdHostsManage() {
        writeLog("Launch mtd management", "");
    }

    public MtdHostsManage(Iterable<Host> hosts) {
        beginGetAllHosts(hosts);
        writeLog("Launch mtd management in ip mode", hosts.toString());
    }
    public MtdHostsManage(int vmx){
        vmNumber = vmx;
        beginGetAllHosts(vmx);
        writeLog("Launch" +
                "" +
                "" +
                " mtd management in polymorphic mode!", polymorphicHosts.toString());
    }
    public void setHost(Iterable<Host> hosts) {
        beginGetAllHosts(hosts);
        writeLog("Launch mtd management in ip mode", hosts.toString());
    }
    public void setHost(int vmx) {
        vmNumber = vmx;
        beginGetAllHosts(vmx);
        writeLog("Launch" +
                "" +
                "" +
                " mtd management in polymorphic mode!", polymorphicHosts.toString());
    }
    public void getAllDevices(Iterable<Device> devices){
        int cnt = 0;
        for(Device device:devices){
            writeLog("device" + cnt, device.toString());
            cnt ++;
        }
    }
    public void beginGetAllHosts(int vmx){
        for (int i = 64; i < 101; i++) {
            PolymorphicHost polymorphicHost = new PolymorphicHost(vmx, i);
            polymorphicHosts.add(polymorphicHost);
            realVirtualMap.put(polymorphicHost, polymorphicHost);
        }
    }

    public void beginGetAllHosts(Iterable<Host> hosts){
        for(Host host:hosts){
            for(IpAddress ipAddress:host.ipAddresses()){
                hostIpAddressMap.put(host, ipAddress);
                IpAddressHostMap.put(ipAddress,host);
                portTM.put(host,false);
                pathTM.put(host,false);
                hostTM.put(host,false);
                realVirtualIpMap.put(ipAddress,ipAddress);
                writeHostIpAddressMap(hostIpAddressMap);
            }
            count++;
        }
        log.info("start,the net has hosts :" + count);
    }

    //add a host
    public void addHost(Host host){
        if (host!=null){
            if(!hostIpAddressMap.containsKey(host)){
                for(IpAddress ipAddress:host.ipAddresses()){
                    hostIpAddressMap.put(host, ipAddress);
                    writeLog(ipAddress, "add success hostIpAddressMap");
                    writeHostIpAddressMap(hostIpAddressMap);
                    log.info("add a host,the net has hosts:"+(++count));
                }
            }
            else {
                log.info("add fail,host is existed");
            }
            for(IpAddress ipAddress:host.ipAddresses()){
                if (!realVirtualIpMap.containsKey(ipAddress)){
                    realVirtualIpMap.put(ipAddress,IpAddress.valueOf(getRandomIp()));
                    writeLog(host,"add host success realVirtualIpMap");
                    writeRealVirtualIpMap(realVirtualIpMap);
                }
            }
        }
//        else
//            log.info("add is fail host cannot is null");

    }

    //remote a host
    public void remoteHost(Host host){
        if (host!=null){
            if (hostIpAddressMap.containsKey(host)){
                hostIpAddressMap.remove(host);
                writeLog(hostIpAddressMap.get(host), "remote host success");
                writeHostIpAddressMap(hostIpAddressMap);
                log.info("remote a host,the net has hosts:"+(--count));

            }else{
                log.info("remote fail");
            }

            for(IpAddress ipAddress:host.ipAddresses()){
                if (realVirtualIpMap.containsKey(ipAddress)){
                    realVirtualIpMap.remove(ipAddress);
                    writeLog(ipAddress,"remote host success");
                    writeRealVirtualIpMap(realVirtualIpMap);
                }
            }

        }
        else
            log.info("remote if fail, host cannot is null");

    }

    //shiftAddress
    public void startShift(){
        writeLog("start shift", null);
        for (Map.Entry<IpAddress,IpAddress> entry: realVirtualIpMap.entrySet()) {
            IpAddress virtualIp=IpAddress.valueOf(getRandomIp());
            realVirtualIpMap.put(entry.getKey(), virtualIp);
        }
        writeRealVirtualIpMap(realVirtualIpMap);
        writeAttack01(realVirtualIpMap);
    }

    //获取一个随机IP
    public String getRandomIp() {

        // 指定 IP 范围
        int[][] range = {
                {607649792, 608174079}, // 36.56.0.0-36.63.255.255
                {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
                {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
                {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
                {2078801920, 2079064063}, // 123.232.0.0-123.235.255.255
                {-1950089216, -1948778497}, // 139.196.0.0-139.215.255.255
                {-1425539072, -1425014785}, // 171.8.0.0-171.15.255.255
                {-1236271104, -1235419137}, // 182.80.0.0-182.92.255.255
                {-770113536, -768606209}, // 210.25.0.0-210.47.255.255
                {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
        };

        Random random = new Random();
        int index = random.nextInt(10);
        String ip = num2ip(range[index][0] + random.nextInt(range[index][1] - range[index][0]));
        return ip;
    }
    public Integer getRandomIdentity(){
        Random random = new Random();
        Integer identity = 202271720 + random.nextInt(8) * 100000 + random.nextInt(100) - 64;
        return identity;
    }
    public Integer getRandomMfID(){
        Random random = new Random();
        Integer mfID = 1 + random.nextInt(8) * 100 + random.nextInt(100) - 64;
        return mfID;
    }
    public Pair<Integer,Integer> getRandomGeoPosition(){
        Random random = new Random();
        Integer geoPosLat = random.nextInt(100) - 63;
        Integer geoPosLon = PolymorphicHost.float2CustomBin(-180 + random.nextInt(8) * 20 + (random.nextInt(100) - 64) * 0.4);
        Pair<Integer,Integer> geoPosition = Pair.of(geoPosLat, geoPosLon);
        return geoPosition;
    }

    public Integer getRandomNdnName(){
        Random random = new Random();
        Integer ndnName=202271720 + random.nextInt(8) * 100000 + random.nextInt(100) - 64;
        return ndnName;
    }

    // 将十进制转换成IP地址
    public String num2ip(int ip) {
        int[] b = new int[4];
        b[0] = (ip >> 24) & 0xff;
        b[1] = (ip >> 16) & 0xff;
        b[2] = (ip >> 8) & 0xff;
        b[3] = ip & 0xff;
        // 拼接 IP
        String x = b[0] + "." + b[1] + "." + b[2] + "." + b[3];
        return x;
    }

    public <T> void writeLog(T elem1,T elem2){

        long milliSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(milliSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        File folder = new File(logFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(mtdLogPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(mtdLogPath, true));
            writer.println("date:" + formattedDate + "\t" + "info:" + elem1 + "\t :" + elem2);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    private  void writeHostIpAddressMap(Map<Host,IpAddress> src) {

        long millisSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(millisSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(hostIpAddressMapPath, false));
            PrintWriter finalWriter = writer;
            src.forEach((host, ipAddress) -> finalWriter.println("date:" + formattedDate + "\t" + ipAddress + " info:" + host + ":::")
            );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    private  void writeRealVirtualMap(Map<PolymorphicHost,PolymorphicHost> host) {
        long millisSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(millisSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(realVirtualLogPath, false));
            PrintWriter finalWriter = writer;
            host.forEach((realHost, virtualHost) -> finalWriter.println("date:" + formattedDate + "\n" + "  real host:" + realHost + "\n  virtual host: " + virtualHost)
            );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    private  void writeRealVirtualIpMap(Map<IpAddress,IpAddress> src) {

        long millisSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(millisSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(realVirtualIpLogPath, false));
            PrintWriter finalWriter = writer;
            src.forEach((realIp, virtualIp) -> finalWriter.println("date:" + formattedDate + "\t" + " real ip:" + realIp + "::: virtual ip: " + virtualIp)
            );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }
    private  void hostToServer(Set<Integer> src, Set<Integer> src2, Set<Integer> src3) {
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(hostToServerPath, false));
            PrintWriter finalWriter = writer;
            finalWriter.println("Hosts included in the server1:");
            src.forEach((host) -> finalWriter.println(host));
            finalWriter.println("Hosts included in the server2:");
            src2.forEach((host) -> finalWriter.println(host));
            finalWriter.println("Hosts included in the server3:");
            src3.forEach((host) -> finalWriter.println(host));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    private  void writePolymorphicAttackList(Map<PolymorphicHost,PolymorphicHost> src) {
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(polymorphicInterceptedHostIpPath, false));
            PrintWriter finalWriter = writer;
            src.forEach((real, virtual) -> finalWriter.println(virtual.getIpAddress()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }
    private  void writeAttack01(Map<IpAddress,IpAddress> src) {
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(interceptedHostIpPath, false));
            PrintWriter finalWriter = writer;
            src.forEach((host, ipAddress) -> finalWriter.println(ipAddress));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    public void rollbackAttackList(){
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            if (isPolymorphicMode) {
                writer = new PrintWriter(new FileWriter(polymorphicInterceptedHostIpPath, false));
            } else {
                writer = new PrintWriter(new FileWriter(interceptedHostIpPath, false));
            }
            PrintWriter finalWriter = writer;
            if (isPolymorphicMode){
                realVirtualMap.forEach((real, virtual) -> finalWriter.println(real.getIpAddress()));
            }else{
                realVirtualIpMap.forEach((host, ipAddress) -> finalWriter.println(host));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

    public void ipModeAddressShuffle(IpAddress ip){
        writeLog(ip,"try to ip shuffle.");
        if (realVirtualIpMap.containsKey(ip)){
            realVirtualIpMap.put(ip,IpAddress.valueOf(getRandomIp()));
            writeAttack01(realVirtualIpMap);
            writeLog(ip,"Successful ip transformation, new ones are:" + realVirtualIpMap.get(ip));
            writeRealVirtualIpMap(realVirtualIpMap);
        }else {
            writeLog(ip,"The host has not joined the topology, please ping again!");
        }
    }

    public void polymorphicModeShuffle(IpAddress ip){
        writeLog(ip.toString(), "try to identification shuffle.");
        for (PolymorphicHost host : polymorphicHosts){

            if (host.getIpAddress().trim().equals(ip.toString().trim())){
                String virtualIp = getRandomIp();
                Integer virtualIdentity = getRandomIdentity();
                Integer virtualMfID = getRandomMfID();
                Integer virtualNdnName = getRandomNdnName();
                Pair<Integer,Integer> virtualGeoPosition = getRandomGeoPosition();
                Pair<Integer,Integer> virtualDis = Pair.of(0,0);
                Pair<Integer,Integer> virtualNdnInfo = Pair.of(virtualNdnName,0);
                PolymorphicHost virtualPolymorphicHost = new PolymorphicHost(
                        host.getMacAddress(),virtualIp,virtualIdentity,virtualMfID,virtualGeoPosition,virtualDis,virtualNdnInfo);
                realVirtualMap.put(host,virtualPolymorphicHost);
                writeRealVirtualMap(realVirtualMap);
                writePolymorphicAttackList(realVirtualMap);
                writeLog(ip,"Successful transformation, new ones are:" + virtualPolymorphicHost);
                break;
            }
        }
    }
    @Override
    public void run() {
        MtdMechanism mtdMechanism=new MtdMechanism();
        //ip mode 1-16 host  17-19 server  20-21 database
        //polymorphic mode 1-32 host 33-35 server 36-37 database
        mtdMechanism.export();
        MtdMechanism.initSHH();
        hostToServer(MtdMechanism.serverHasHosts1,MtdMechanism.serverHasHosts2,MtdMechanism.serverHasHosts3);
        while(sign){
            int[] host = chances(mtdMechanism.hfrMatrix,0);
            System.out.println("chance host:"+ (host[0]+1) +",    mtd mechanism:" + (host[1]+1));
            writeLog("****************","chance host:"+ (host[0]+1) +",    mtd mechanism:" + (host[1]+1));
            IpAddress ip;
            //Splicing Strings to form host Ip addresses
            String ip_str;
            Pair<IpAddress,IpAddress> ip_pair = null;
            if (isPolymorphicMode){
                Random random = new Random();
                ip_str = String.format("10.1.%d.%d", vmNumber + 1, 12 + host[0]);
                String ip_str2 = String.format("10.1.%d.%d", vmNumber + 1, Math.min(28 + host[0], 43));
                ip_pair = Pair.of(IpAddress.valueOf(ip_str), IpAddress.valueOf(ip_str2));
            }else{
                ip_str = (121 + ((host[0])/4)) + ".0.0." + (1 + ((host[0] % 4)));
            }

            ip= IpAddress.valueOf(ip_str);
            if (host[1]==0){
                if (isPolymorphicMode){
                    polymorphicModeShuffle(ip_pair.getLeft());
                    polymorphicModeShuffle(ip_pair.getRight());
                }else{
                    ipModeAddressShuffle(ip);
                }
            }else if(host[1]==1){//port transformation
                for(Map.Entry<Host,Boolean> entry: portTM.entrySet()){
                    portTM.put(entry.getKey(),false);
                }
                portTM.put(IpAddressHostMap.get(ip),true);
                writeLog(ip,"Port Successful transformation");

            }else if(host[1]==2){//path transformation
                for(Map.Entry<Host,Boolean> entry: pathTM.entrySet()){
                    pathTM.put(entry.getKey(),false);
                }
                pathTM.put(IpAddressHostMap.get(ip),true);
                writeLog(ip,"Path Successful transformation");
            }else {//host transformation
                if(MtdMechanism.hostMtdSign){
                    for(Map.Entry<Host,Boolean> entry: hostTM.entrySet()){
                        hostTM.put(entry.getKey(),false);
                    }
                    hostTM.put(IpAddressHostMap.get(ip),true);
                    writeLog(ip,"host Successful transformation");
                    //perform host mutation
                    if(MtdMechanism.serverHasHosts1.contains(host[0]+1)){
                        MtdMechanism.serverHasHosts1.remove(host[0]+1);
                        MtdMechanism.serverHasHosts2.add(host[0]+1);
                    }else if(MtdMechanism.serverHasHosts2.contains(host[0]+1)){
                        MtdMechanism.serverHasHosts2.remove(host[0]+1);
                        MtdMechanism.serverHasHosts3.add(host[0]+1);
                    }else{
                        MtdMechanism.serverHasHosts3.remove(host[0]+1);
                        MtdMechanism.serverHasHosts1.add(host[0]+1);
                    }
                    hostToServer(MtdMechanism.serverHasHosts1,MtdMechanism.serverHasHosts2,MtdMechanism.serverHasHosts3);
                }
            }


            int[] server = chances(mtdMechanism.hfrMatrix,16);
            System.out.println("chance server:"+ (server[0]+1) +",    mtd mechanism:" + (server[1]+1));
            writeLog("****************","chance server:"+ (server[0]+1) +",    mtd mechanism:" + (server[1]+1));
            //Splicing Strings to form host Ip addresses
            ip_str = (121 + ((server[0]) / 4)) + ".0.0." + (1 + (server[0] % 4));
            if (isPolymorphicMode){
                ip_str = String.format("10.1.%d.%d", vmNumber + 1, 28 + server[0]);
            }
            ip= IpAddress.valueOf(ip_str);
            if (server[1]==0){//ip transformation
                if (isPolymorphicMode){
                    polymorphicModeShuffle(ip);
                }else{
                    ipModeAddressShuffle(ip);
                }
            }else{ //port transformation
                for(Map.Entry<Host,Boolean> entry: portTM.entrySet()){
                    portTM.put(entry.getKey(),false);
                }
                portTM.put(IpAddressHostMap.get(ip),true);
                writeLog(ip,"Port is transformed successfully");
            }

            int[] database=chances(mtdMechanism.hfrMatrix,19);
            System.out.println("chance database:" + (database[0]+1) + ",    mtd mechanism:" + (database[1]+1));
            writeLog("****************","chance database:"+ (database[0]+1) + ",    mtd mechanism:" + (database[1]+1));
            //Splicing Strings to form host Ip addresses
            ip_str = (121 + ((database[0]) / 4)) + ".0.0." + (1 + (database[0] % 4));
            if (isPolymorphicMode){
                ip_str = String.format("10.1.%d.%d", vmNumber + 1, 47 + (database[0] + 1) % 4);
            }
            ip= IpAddress.valueOf(ip_str);
            if (database[1]==0){//ip transformation
                if (isPolymorphicMode){
                    polymorphicModeShuffle(ip);
                }else{
                    ipModeAddressShuffle(ip);
                }
            }
            else{ //port transformation
                for(Map.Entry<Host,Boolean> entry: portTM.entrySet()){
                    portTM.put(entry.getKey(),false);
                }
                portTM.put(IpAddressHostMap.get(ip),true);
                writeLog(ip,"Port Successful transformation");
            }
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //rhm,method that neither considers the host nor the mechanism
//    @Override
    public void runRhm() {

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(sign==true){

            startShift();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int[] chances(float[][] hfrMatrix,int p){
        float sh=0;
        float tempHost=0;
        int s=0;
        if(p==0){
            s=16;
        }
        else if(p==16){
            s=19;
        }
        else s=21;
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                sh+=hfrMatrix[i][j];
            }
        }
        float rh= (float) Math.random()*sh;
        boolean flag =false;//to exit two loop bodies
        for(int i=p;i<s;i++) {
            for (int j = 0; j < 4; j++) {
                tempHost+=hfrMatrix[i][j];
                if(tempHost>=rh){
                    flag=true;
                    return new int[]{i,j};
                }
            }
            if (flag==true){
                break;
            }
        }
        return null;
    }
    public static int[] polymorphicChances(float[][] hfrMatrix,int left){
        float sh=0;
        float tempHost=0;
        int right = 0;
        if(left == 0){
            right = 32;
        }
        else if(left == 32){
            right = 35;
        }
        else right = 37;
        for(int i = left; i < right; i++) {
            for (int j = 0; j < 4; j++) {
                sh+=hfrMatrix[i][j];
            }
        }
        float rh= (float) Math.random() * sh;
        boolean flag =false;//to exit two loop bodies
        for(int i=left; i < right;i++) {
            for (int j = 0; j < 4; j++) {
                tempHost+=hfrMatrix[i][j];
                if(tempHost>=rh){
                    flag=true;
                    return new int[]{i,j};
                }
            }
            if (flag){
                break;
            }
        }
        return null;
    }
}



