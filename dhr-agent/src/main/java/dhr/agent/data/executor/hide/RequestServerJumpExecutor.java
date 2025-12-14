package dhr.agent.data.executor.hide;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 请求服务器跳变执行体
 */
public class RequestServerJumpExecutor implements Executor {
    private static final String TYPE = "request_server_jump";
    private static final int PRIORITY = 4;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现请求服务器跳变逻辑
        // 动态改变请求的目标服务器
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
