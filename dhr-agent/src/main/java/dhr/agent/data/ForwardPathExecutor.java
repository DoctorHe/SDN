package dhr.agent.data;

import java.util.Random;

/**
 * 转发路径执行体
 * 实现了转发路径跳变策略
 */
public class ForwardPathExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "forward_path";
    
    /**
     * 默认构造函数
     */
    public ForwardPathExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "forward_path_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public ForwardPathExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现转发路径跳变逻辑
        System.out.println("执行转发路径跳变策略 - " + identifier);
        // 模拟转发路径跳变过程
        String newPath = generateRandomPath();
        System.out.println("生成新转发路径: " + newPath);
        // 这里可以添加实际的转发路径跳变实现
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
     * 生成随机转发路径
     * @return 随机转发路径
     */
    private String generateRandomPath() {
        Random random = new Random();
        // 模拟路径节点
        String[] nodes = {"node1", "node2", "node3", "node4", "node5"};
        // 生成随机路径长度（3-5个节点）
        int pathLength = 3 + random.nextInt(3);
        StringBuilder path = new StringBuilder();
        
        for (int i = 0; i < pathLength; i++) {
            int nodeIndex = random.nextInt(nodes.length);
            if (i > 0) {
                path.append(" -> ");
            }
            path.append(nodes[nodeIndex]);
        }
        
        return path.toString();
    }
}