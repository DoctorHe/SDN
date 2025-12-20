package dhr.agent.data;

/**
 * 流检测执行体
 * 实现了流检测跳变策略
 */
public class FlowDetectionExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "flow_detection";
    
    /**
     * 默认构造函数
     */
    public FlowDetectionExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "flow_detection_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public FlowDetectionExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现流检测逻辑
        System.out.println("执行流检测策略 - " + identifier);
        // 这里可以添加实际的流检测实现
        running = true;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }
    
    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    @Override
    public String getIdentifier() {
        return identifier;
    }
}