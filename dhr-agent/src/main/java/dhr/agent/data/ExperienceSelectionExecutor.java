package dhr.agent.data;

/**
 * 经验选择执行体
 * 实现了经验选择跳变策略
 */
public class ExperienceSelectionExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "experience_selection";
    
    /**
     * 默认构造函数
     */
    public ExperienceSelectionExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "experience_selection_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public ExperienceSelectionExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现经验选择逻辑
        System.out.println("执行经验选择策略 - " + identifier);
        // 这里可以添加实际的经验选择实现
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