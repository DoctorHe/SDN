package dhr.agent.data.executor.flow;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * SVM检测执行体
 */
public class SvmDetectionExecutor implements Executor {
    private static final String TYPE = "svm_detection";
    private static final int PRIORITY = 2;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现SVM检测逻辑
        // 使用SVM模型判断流量是否异常
        double svmScore = calculateSvmScore(feature);
        return svmScore > 0.0; // 假设SVM得分大于0表示正常
    }
    
    private double calculateSvmScore(Feature feature) {
        // 简化实现，实际应使用训练好的SVM模型
        double score = 0.0;
        // 基于流量特征计算SVM得分
        score += feature.getAvgFlowGrowth() * 0.3;
        score += feature.getAvgPortGrowth() * 0.2;
        score += feature.getAvgSourceIPGrowth() * 0.5;
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
