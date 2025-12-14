package dhr.agent.data.executor.flow;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 熵检测执行体
 */
public class EntropyDetectionExecutor implements Executor {
    private static final String TYPE = "entropy_detection";
    private static final int PRIORITY = 1;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现熵检测逻辑
        // 计算流量熵值，判断是否异常
        double entropy = calculateEntropy(feature);
        return entropy < 0.5; // 假设熵值小于0.5表示正常
    }
    
    private double calculateEntropy(Feature feature) {
        // 简化实现，实际应根据流量特征计算熵值
        double entropy = 0.0;
        // 基于源IP增长率、端口增长率等特征计算熵
        double ipGrowth = feature.getAvgSourceIPGrowth();
        double portGrowth = feature.getAvgPortGrowth();
        double flowGrowth = feature.getAvgFlowGrowth();
        
        // 简化的熵计算
        entropy = (ipGrowth + portGrowth + flowGrowth) / 3.0;
        return Math.min(1.0, Math.max(0.0, entropy));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
