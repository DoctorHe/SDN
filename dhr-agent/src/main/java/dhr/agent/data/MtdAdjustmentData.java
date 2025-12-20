package dhr.agent.data;

import java.io.Serializable;

/**
 * MTD调整数据模型
 * 用于dhr应用向mtd应用传递数据，调整跳变频率与策略
 */
public class MtdAdjustmentData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 跳变机制开关
    private boolean ipMtdEnabled;
    private boolean portMtdEnabled;
    private boolean pathMtdEnabled;
    private boolean hostMtdEnabled;
    
    // 主机上不同MTD机制的选择概率 (对应pmh数组)
    private float[] hostMtdProbabilities;
    
    // 服务器上不同MTD机制的选择概率 (对应pms数组)
    private float[] serverMtdProbabilities;
    
    // 数据库上不同MTD机制的选择概率 (对应pmd数组)
    private float[] databaseMtdProbabilities;
    
    // 调整系数，用于动态调整跳变频率
    private float adjustmentFactor;
    
    // 安全等级，用于快速调整跳变策略
    private int securityLevel;
    
    /**
     * 默认构造函数
     */
    public MtdAdjustmentData() {
        // 默认值
        this.ipMtdEnabled = true;
        this.portMtdEnabled = false;
        this.pathMtdEnabled = true;
        this.hostMtdEnabled = true;
        this.hostMtdProbabilities = new float[]{0.5f, 0.1f, 0.2f, 0.2f};
        this.serverMtdProbabilities = new float[]{0.2f, 0.4f, 0.2f, 0.2f};
        this.databaseMtdProbabilities = new float[]{0.2f, 0.4f, 0.2f, 0.2f};
        this.adjustmentFactor = 1.0f;
        this.securityLevel = 1;
    }
    
    // Getters and Setters
    public boolean isIpMtdEnabled() {
        return ipMtdEnabled;
    }
    
    public void setIpMtdEnabled(boolean ipMtdEnabled) {
        this.ipMtdEnabled = ipMtdEnabled;
    }
    
    public boolean isPortMtdEnabled() {
        return portMtdEnabled;
    }
    
    public void setPortMtdEnabled(boolean portMtdEnabled) {
        this.portMtdEnabled = portMtdEnabled;
    }
    
    public boolean isPathMtdEnabled() {
        return pathMtdEnabled;
    }
    
    public void setPathMtdEnabled(boolean pathMtdEnabled) {
        this.pathMtdEnabled = pathMtdEnabled;
    }
    
    public boolean isHostMtdEnabled() {
        return hostMtdEnabled;
    }
    
    public void setHostMtdEnabled(boolean hostMtdEnabled) {
        this.hostMtdEnabled = hostMtdEnabled;
    }
    
    public float[] getHostMtdProbabilities() {
        return hostMtdProbabilities;
    }
    
    public void setHostMtdProbabilities(float[] hostMtdProbabilities) {
        this.hostMtdProbabilities = hostMtdProbabilities;
    }
    
    public float[] getServerMtdProbabilities() {
        return serverMtdProbabilities;
    }
    
    public void setServerMtdProbabilities(float[] serverMtdProbabilities) {
        this.serverMtdProbabilities = serverMtdProbabilities;
    }
    
    public float[] getDatabaseMtdProbabilities() {
        return databaseMtdProbabilities;
    }
    
    public void setDatabaseMtdProbabilities(float[] databaseMtdProbabilities) {
        this.databaseMtdProbabilities = databaseMtdProbabilities;
    }
    
    public float getAdjustmentFactor() {
        return adjustmentFactor;
    }
    
    public void setAdjustmentFactor(float adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }
    
    public int getSecurityLevel() {
        return securityLevel;
    }
    
    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }
    
    @Override
    public String toString() {
        return "MtdAdjustmentData{" +
                "ipMtdEnabled=" + ipMtdEnabled +
                ", portMtdEnabled=" + portMtdEnabled +
                ", pathMtdEnabled=" + pathMtdEnabled +
                ", hostMtdEnabled=" + hostMtdEnabled +
                ", adjustmentFactor=" + adjustmentFactor +
                ", securityLevel=" + securityLevel +
                '}';
    }
}