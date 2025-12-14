package dhr.agent.data.executor;

import dhr.agent.data.Feature;
import org.onosproject.net.DeviceId;

import java.util.List;

/**
 * 执行体接口，定义执行体的基本方法
 */
public interface Executor {
    /**
     * 执行体执行方法
     * @param feature 特征数据
     * @return 执行结果
     */
    boolean execute(Feature feature);
    
    /**
     * 获取执行体类型
     * @return 执行体类型
     */
    String getType();
    
    /**
     * 获取执行体优先级
     * @return 优先级
     */
    int getPriority();
}
