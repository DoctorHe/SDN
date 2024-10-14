package org.onosproject.mtd.data;

import org.apache.commons.lang3.tuple.Pair;
import org.onosproject.mtd.strategy.MtdMechanism;

public class PolymorphicHost {
    private String macAddress;
    private String ipAddress;
    private Integer identity;
    private Integer mfID;
    private Integer geoPosLat;
    private Integer geoPosLon;
    private Integer disA;
    private Integer disB;
    private Integer ndnName;
    private Integer ndnContent;

    public PolymorphicHost(){}
    public PolymorphicHost(Integer vmx, Integer index){
        this.macAddress = String.format("00:00:00:00:%02x:%02x", (vmx + 1) & 0xFF, index & 0xFF);
        this.ipAddress= String.format("10.1.%d.%d", vmx + 1, index - 64 + 12);
        this.identity=202271720 + vmx * 100000 + index - 64;
        this.mfID = 1 + vmx * 100 + index - 64;
        this.geoPosLat = index - 63;
        this.geoPosLon = float2CustomBin(-180 + vmx * 20 + (index - 64) * 0.4);
        this.disA = 0;
        this.disB = 0;
        this.ndnName=202271720 + vmx * 100000 + index - 64;
        this.ndnContent=2048 + vmx * 100 + index - 64;
    }

    public PolymorphicHost(String macAddress, String ipAddress, Integer identity, Integer mfID, Pair<Integer, Integer> geoPosition, Pair<Integer, Integer> dis, Pair<Integer, Integer> ndnInfo) {
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
        this.identity = identity;
        this.mfID = mfID;
        this.geoPosLat = geoPosition.getLeft();
        this.geoPosLon = geoPosition.getRight();
        this.disA = dis.getLeft();
        this.disB = dis.getRight();
        this.ndnName = ndnInfo.getLeft();
        this.ndnContent = ndnInfo.getRight();
    }

    public static Integer float2CustomBin(double number) {
        // 确定符号位并取绝对值
        String signBit = number < 0 ? "01" : "00";
        number = Math.abs(number);

        // 分离整数部分和小数部分
        long integerPart = (long) number;
        double fractionalPart = number - integerPart;

        // 将整数部分转换为 15 位二进制
        String integerBits = Long.toBinaryString(integerPart);
        while (integerBits.length() < 15) {
            integerBits = "0" + integerBits;
        }

        // 将小数部分转换为 15 位二进制
        StringBuilder fractionalBits = new StringBuilder();
        while (fractionalBits.length() < 15) {
            fractionalPart *= 2;
            long bit = (long) fractionalPart;
            fractionalBits.append(bit);
            fractionalPart -= bit;
        }

        // 拼接符号位、整数二进制和小数二进制
        StringBuilder binaryRepresentation = new StringBuilder();
        binaryRepresentation.append(signBit);
        binaryRepresentation.append(integerBits);
        binaryRepresentation.append(fractionalBits.toString());

        // 将二进制字符串转换为长整型
        Integer decimalRepresentation = Integer.parseInt(binaryRepresentation.toString(), 2);
        return decimalRepresentation;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getIdentity() {
        return identity;
    }

    public void setIdentity(Integer identity) {
        this.identity = identity;
    }

    public Integer getMfID() {
        return mfID;
    }

    public void setMfID(Integer mfID) {
        this.mfID = mfID;
    }

    public Integer getGeoPosLat() {
        return geoPosLat;
    }

    public void setGeoPosLat(Integer geoPosLat) {
        this.geoPosLat = geoPosLat;
    }

    public Integer getGeoPosLon() {
        return geoPosLon;
    }

    public void setGeoPosLon(Integer geoPosLon) {
        this.geoPosLon = geoPosLon;
    }

    public Integer getDisA() {
        return disA;
    }

    public void setDisA(Integer disA) {
        this.disA = disA;
    }

    public Integer getDisB() {
        return disB;
    }

    public void setDisB(Integer disB) {
        this.disB = disB;
    }

    public Integer getNdnName() {
        return ndnName;
    }

    public void setNdnName(Integer ndnName) {
        this.ndnName = ndnName;
    }

    public Integer getNdnContent() {
        return ndnContent;
    }

    public void setNdnContent(Integer ndnContent) {
        this.ndnContent = ndnContent;
    }

    @Override
    public String toString() {
        return "PolymorphicHost{" +
                "macAddress='" + macAddress + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", identity=" + identity +
                ", mfID=" + mfID +
                ", geoPosLat=" + geoPosLat +
                ", geoPosLon=" + geoPosLon +
                ", disA=" + disA +
                ", disB=" + disB +
                ", ndnName=" + ndnName +
                ", ndnContent=" + ndnContent +
                '}';
    }
}
