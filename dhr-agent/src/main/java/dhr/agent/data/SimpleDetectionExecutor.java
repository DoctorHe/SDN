package dhr.agent.data;

/**
 * 简检测执行体
 * 实现了简检测跳变策略
 */
public class SimpleDetectionExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "simple_detection";
    
    /**
     * 默认构造函数
     */
    public SimpleDetectionExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "simple_detection_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public SimpleDetectionExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现简检测逻辑
        System.out.println("执行简检测策略 - " + identifier);
        // 这里可以添加实际的简检测实现
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