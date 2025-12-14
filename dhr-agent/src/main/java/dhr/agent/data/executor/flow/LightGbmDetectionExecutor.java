package dhr.agent.data.executor.flow;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * LightGBM检测执行体
 */
public class LightGbmDetectionExecutor implements Executor {
    private static final String TYPE = "lightgbm_detection";
    private static final int PRIORITY = 3;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现LightGBM检测逻辑
        // 使用LightGBM模型判断流量是否异常
        double lgbmScore = calculateLgbmScore(feature);
        return lgbmScore > 0.5; // 假设LightGBM得分大于0.5表示正常
    }
    
    private double calculateLgbmScore(Feature feature) {
        // 简化实现，实际应使用训练好的LightGBM模型
        double score = 0.0;
        // 基于流量特征计算LightGBM得分
        score += feature.getAvgFlowGrowth() * 0.25;
        score += feature.getAvgPortGrowth() * 0.25;
        score += feature.getAvgSourceIPGrowth() * 0.25;
        score += feature.getAvgPacket() * 0.25;
        return score;
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
