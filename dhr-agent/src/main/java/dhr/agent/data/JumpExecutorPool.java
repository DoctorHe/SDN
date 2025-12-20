package dhr.agent.data;

/**
 * 跳变策略执行体池
 * 包含三个执行体集合：流检测、链路信息隐藏、特征选择
 */
public class JumpExecutorPool {
    // 流检测执行体集合
    private ExecutorCollection flowDetectionCollection;
    
    // 链路信息隐藏执行体集合
    private ExecutorCollection linkHidingCollection;
    
    // 特征选择执行体集合
    private ExecutorCollection featureSelectionCollection;
    
    /**
     * 构造函数
     */
    public JumpExecutorPool() {
        // 初始化三个执行体集合
        this.flowDetectionCollection = new ExecutorCollection("flow_detection");
        this.linkHidingCollection = new ExecutorCollection("link_hiding");
        this.featureSelectionCollection = new ExecutorCollection("feature_selection");
    }
    
    /**
     * 获取流检测执行体集合
     * @return 流检测执行体集合
     */
    public ExecutorCollection getFlowDetectionCollection() {
        return flowDetectionCollection;
    }
    
    /**
     * 获取链路信息隐藏执行体集合
     * @return 链路信息隐藏执行体集合
     */
    public ExecutorCollection getLinkHidingCollection() {
        return linkHidingCollection;
    }
    
    /**
     * 获取特征选择执行体集合
     * @return 特征选择执行体集合
     */
    public ExecutorCollection getFeatureSelectionCollection() {
        return featureSelectionCollection;
    }
    
    /**
     * 根据集合类型获取执行体集合
     * @param collectionType 集合类型
     * @return 执行体集合，如果类型不匹配则返回null
     */
    public ExecutorCollection getCollectionByType(String collectionType) {
        switch (collectionType) {
            case "flow_detection":
                return flowDetectionCollection;
            case "link_hiding":
                return linkHidingCollection;
            case "feature_selection":
                return featureSelectionCollection;
            default:
                return null;
        }
    }
}
