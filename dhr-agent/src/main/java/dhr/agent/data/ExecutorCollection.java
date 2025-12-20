package dhr.agent.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 执行体集合
 * 管理同一类型的多个执行体实例
 */
public class ExecutorCollection {
    // 执行体集合类型
    private String type;
    
    // 执行体列表，按索引顺序存储
    private List<JumpStrategyExecutor> executors;
    
    /**
     * 构造函数
     * @param type 执行体集合类型
     */
    public ExecutorCollection(String type) {
        this.type = type;
        this.executors = new ArrayList<>();
    }
    
    /**
     * 添加执行体到集合中
     * @param executor 执行体实例
     */
    public void addExecutor(JumpStrategyExecutor executor) {
        if (executor != null) {
            executors.add(executor);
        }
    }
    
    /**
     * 根据索引获取执行体
     * @param index 执行体索引（从1开始）
     * @return 执行体实例，如果索引无效则返回null
     */
    public JumpStrategyExecutor getExecutorByIndex(int index) {
        // 索引从1开始，所以需要减1
        if (index >= 1 && index <= executors.size()) {
            return executors.get(index - 1);
        }
        return null;
    }
    
    /**
     * 获取集合中最高权重的执行体
     * @return 最高权重执行体实例，如果集合为空则返回null
     */
    public JumpStrategyExecutor getHighestWeightExecutor() {
        if (executors.isEmpty()) {
            return null;
        }
        
        JumpStrategyExecutor highestWeightExecutor = executors.get(0);
        for (JumpStrategyExecutor executor : executors) {
            if (executor.getWeight() > highestWeightExecutor.getWeight()) {
                highestWeightExecutor = executor;
            }
        }
        
        return highestWeightExecutor;
    }
    
    /**
     * 获取集合中所有执行体
     * @return 执行体集合
     */
    public Collection<JumpStrategyExecutor> getAllExecutors() {
        return new ArrayList<>(executors);
    }
    
    /**
     * 获取执行体集合类型
     * @return 执行体集合类型
     */
    public String getType() {
        return type;
    }
    
    /**
     * 获取执行体集合大小
     * @return 执行体数量
     */
    public int size() {
        return executors.size();
    }
    
    /**
     * 检查执行体集合是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return executors.isEmpty();
    }
}
