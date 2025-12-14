package dhr.agent.data.executor.hide;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 端口跳变执行体
 */
public class PortJumpExecutor implements Executor {
    private static final String TYPE = "port_jump";
    private static final int PRIORITY = 2;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现端口跳变逻辑
        // 动态改变流量的源端口或目的端口
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
