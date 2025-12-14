package dhr.agent.data.executor.hide;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 模态标识信息跳变执行体
 */
public class ModalInfoJumpExecutor implements Executor {
    private static final String TYPE = "modal_info_jump";
    private static final int PRIORITY = 1;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现模态标识信息跳变逻辑
        // 动态改变流量的模态标识信息
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
