package dhr.agent.data;

/**
 * 跳变策略执行体接口
 * 定义了跳变策略的执行方法和状态管理
 */
public interface JumpStrategyExecutor {
    /**
     * 执行跳变策略
     */
    void execute();
    
    /**
     * 获取执行体类型
     * @return 执行体类型
     */
    String getType();
    
    /**
     * 检查执行体是否正在运行
     * @return 运行状态
     */
    boolean isRunning();
    
    /**
     * 设置执行体运行状态
     * @param running 运行状态
     */
    void setRunning(boolean running);
    
    /**
     * 获取执行体权重
     * @return 执行体权重
     */
    int getWeight();
    
    /**
     * 设置执行体权重
     * @param weight 执行体权重
     */
    void setWeight(int weight);
    
    /**
     * 获取执行体标识
     * @return 执行体标识
     */
    String getIdentifier();
}
