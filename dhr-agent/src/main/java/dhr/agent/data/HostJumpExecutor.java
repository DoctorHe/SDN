package dhr.agent.data;

import java.util.Random;

/**
 * 主机跳变执行体
 * 实现了主机跳变策略
 */
public class HostJumpExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "host";
    
    /**
     * 默认构造函数
     */
    public HostJumpExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "host_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public HostJumpExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现主机跳变逻辑
        System.out.println("执行主机跳变策略 - " + identifier);
        // 模拟主机跳变过程
        String newHost = generateRandomHost();
        System.out.println("生成新主机标识: " + newHost);
        // 这里可以添加实际的主机跳变实现
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
     * 生成随机主机标识
     * @return 随机主机标识
     */
    private String generateRandomHost() {
        Random random = new Random();
        // 模拟主机标识
        String[] hostPrefixes = {"host", "server", "client", "node", "device"};
        int hostIndex = random.nextInt(hostPrefixes.length);
        int hostId = 1 + random.nextInt(20);
        
        return hostPrefixes[hostIndex] + hostId;
    }
}
