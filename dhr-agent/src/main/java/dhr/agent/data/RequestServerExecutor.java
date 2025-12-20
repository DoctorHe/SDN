package dhr.agent.data;

/**
 * 请求服务器执行体
 * 实现了请求服务器跳变策略
 */
public class RequestServerExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "request_server";
    
    /**
     * 默认构造函数
     */
    public RequestServerExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "request_server_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public RequestServerExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现请求服务器跳变逻辑
        System.out.println("执行请求服务器跳变策略 - " + identifier);
        // 这里可以添加实际的请求服务器跳变实现
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