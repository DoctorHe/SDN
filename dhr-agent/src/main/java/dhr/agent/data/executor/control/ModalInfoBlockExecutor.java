package dhr.agent.data.executor.control;

import dhr.agent.data.Feature;
import dhr.agent.data.executor.Executor;

/**
 * 模态标识信息禁流执行体
 */
public class ModalInfoBlockExecutor implements Executor {
    private static final String TYPE = "modal_info_block";
    private static final int PRIORITY = 1;
    
    @Override
    public boolean execute(Feature feature) {
        // 实现模态标识信息禁流逻辑
        // 禁止特定模态标识的流量
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
