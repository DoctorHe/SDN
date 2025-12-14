package dhr.agent.data.executor;

import dhr.agent.data.executor.control.*;
import dhr.agent.data.executor.flow.*;
import dhr.agent.data.executor.hide.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行体集合池
 */
public class ExecutorPool {
    // 特征选择执行体集合
    private List<Executor> featureSelectionExecutors;
    
    // 流量检测执行体集合
    private List<Executor> flowDetectionExecutors;
    
    // 流量控制执行体集合
    private List<Executor> flowControlExecutors;
    
    // 链路信息隐藏执行体集合
    private List<Executor> linkInfoHideExecutors;
    
    public ExecutorPool() {
        initExecutors();
    }
    
    private void initExecutors() {
        // 初始化特征选择执行体集合
        featureSelectionExecutors = new ArrayList<>();
        
        // 初始化流量检测执行体集合
        flowDetectionExecutors = new ArrayList<>();
        flowDetectionExecutors.add(new EntropyDetectionExecutor());
        flowDetectionExecutors.add(new SvmDetectionExecutor());
        flowDetectionExecutors.add(new LightGbmDetectionExecutor());
        
        // 初始化流量控制执行体集合
        flowControlExecutors = new ArrayList<>();
        flowControlExecutors.add(new ModalInfoBlockExecutor());
        flowControlExecutors.add(new SwitchGlobalLimitExecutor());
        flowControlExecutors.add(new SwitchPortLimitExecutor());
        
        // 初始化链路信息隐藏执行体集合
        linkInfoHideExecutors = new ArrayList<>();
        linkInfoHideExecutors.add(new ModalInfoJumpExecutor());
        linkInfoHideExecutors.add(new PortJumpExecutor());
        linkInfoHideExecutors.add(new ForwardingPathJumpExecutor());
        linkInfoHideExecutors.add(new RequestServerJumpExecutor());
    }
    
    public List<Executor> getFeatureSelectionExecutors() {
        return featureSelectionExecutors;
    }
    
    public List<Executor> getFlowDetectionExecutors() {
        return flowDetectionExecutors;
    }
    
    public List<Executor> getFlowControlExecutors() {
        return flowControlExecutors;
    }
    
    public List<Executor> getLinkInfoHideExecutors() {
        return linkInfoHideExecutors;
    }
    
    // 根据类型获取执行体
    public Executor getExecutorByType(String type) {
        for (Executor executor : flowDetectionExecutors) {
            if (executor.getType().equals(type)) {
                return executor;
            }
        }
        for (Executor executor : flowControlExecutors) {
            if (executor.getType().equals(type)) {
                return executor;
            }
        }
        for (Executor executor : linkInfoHideExecutors) {
            if (executor.getType().equals(type)) {
                return executor;
            }
        }
        return null;
    }
    
    // 获取最高优先级的执行体
    public Executor getHighestPriorityExecutor(List<Executor> executors) {
        if (executors == null || executors.isEmpty()) {
            return null;
        }
        
        Executor highestPriorityExecutor = executors.get(0);
        for (Executor executor : executors) {
            if (executor.getPriority() > highestPriorityExecutor.getPriority()) {
                highestPriorityExecutor = executor;
            }
        }
        return highestPriorityExecutor;
    }
}
