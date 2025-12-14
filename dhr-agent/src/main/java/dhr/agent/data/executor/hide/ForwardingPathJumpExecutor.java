package dhr.agent.data.executor.hide;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 转发路径跳变执行体
 */
public class ForwardingPathJumpExecutor implements Executor {
    private static final String TYPE = "forwarding_path_jump";
    private static final int PRIORITY = 3;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现转发路径跳变逻辑
        // 动态改变流量的转发路径
        return true; // 简化实现，返回成功
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
