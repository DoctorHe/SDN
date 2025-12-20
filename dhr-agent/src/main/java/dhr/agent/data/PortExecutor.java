package dhr.agent.data;

import java.util.Random;

/**
 * 端口执行体
 * 实现了端口跳变策略
 */
public class PortExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "port";
    
    /**
     * 默认构造函数
     */
    public PortExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "port_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public PortExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现端口跳变逻辑
        System.out.println("执行端口跳变策略 - " + identifier);
        // 模拟端口跳变过程
        int newPort = generateRandomPort();
        System.out.println("生成新端口: " + newPort);
        // 这里可以添加实际的端口跳变实现
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
    
    /**
     * 生成随机端口号
     * @return 随机端口号
     */
    private int generateRandomPort() {
        Random random = new Random();
        // 生成1024-65535之间的随机端口号
        return 1024 + random.nextInt(65535 - 1024 + 1);
    }
}