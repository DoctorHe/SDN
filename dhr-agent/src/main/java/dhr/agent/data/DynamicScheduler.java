package dhr.agent.data;

import dhr.agent.data.executor.Executor;
import dhr.agent.data.executor.ExecutorPool;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态调度器
 */
public class DynamicScheduler {
    private ExecutorPool executorPool;
    
    // 当前策略参数
    private String strategyParams;
    
    public DynamicScheduler(ExecutorPool executorPool) {
        this.executorPool = executorPool;
        this.strategyParams = "1n 00 1n 1n";
    }
    
    /**
     * 根据策略参数调整执行体集合
     * @param strategyParams 策略参数
     * @return 调整后的执行体集合
     */
    public List<List<Executor>> adjustExecutors(String strategyParams) {
        this.strategyParams = strategyParams;
        
        List<List<Executor>> adjustedExecutors = new ArrayList<>();
        
        // 根据策略参数调整特征选择执行体集合
        List<Executor> featureSelectionExecutors = adjustFeatureSelectionExecutors();
        adjustedExecutors.add(featureSelectionExecutors);
        
        // 根据策略参数调整流量检测执行体集合
        List<Executor> flowDetectionExecutors = adjustFlowDetectionExecutors();
        adjustedExecutors.add(flowDetectionExecutors);
        
        // 根据策略参数调整流量控制执行体集合
        List<Executor> flowControlExecutors = adjustFlowControlExecutors();
        adjustedExecutors.add(flowControlExecutors);
        
        // 根据策略参数调整链路信息隐藏执行体集合
        List<Executor> linkInfoHideExecutors = adjustLinkInfoHideExecutors();
        adjustedExecutors.add(linkInfoHideExecutors);
        
        return adjustedExecutors;
    }
    
    private List<Executor> adjustFeatureSelectionExecutors() {
        // 根据策略参数调整特征选择执行体集合
        return executorPool.getFeatureSelectionExecutors();
    }
    
    private List<Executor> adjustFlowDetectionExecutors() {
        // 根据策略参数调整流量检测执行体集合
        String[] params = strategyParams.split(" ");
        if (params.length < 1) {
            return executorPool.getFlowDetectionExecutors();
        }
        
        String param = params[0];
        List<Executor> adjustedExecutors = new ArrayList<>();
        
        if (param.equals("00")) {
            // 不启用
            return adjustedExecutors;
        } else if (param.equals("10")) {
            // 启用，不替换执行体
            adjustedExecutors.addAll(executorPool.getFlowDetectionExecutors());
        } else if (param.startsWith("1n")) {
            // 替换为执行体Pn
            String executorType = getExecutorTypeByParam(param, "flow");
            Executor executor = executorPool.getExecutorByType(executorType);
            if (executor != null) {
                adjustedExecutors.add(executor);
            }
        } else if (param.equals("1x")) {
            // 替换为最高权重执行体
            Executor highestPriorityExecutor = executorPool.getHighestPriorityExecutor(
                    executorPool.getFlowDetectionExecutors());
            if (highestPriorityExecutor != null) {
                adjustedExecutors.add(highestPriorityExecutor);
            }
        }
        
        return adjustedExecutors;
    }
    
    private List<Executor> adjustFlowControlExecutors() {
        // 根据策略参数调整流量控制执行体集合
        String[] params = strategyParams.split(" ");
        if (params.length < 3) {
            return executorPool.getFlowControlExecutors();
        }
        
        String param = params[2];
        List<Executor> adjustedExecutors = new ArrayList<>();
        
        if (param.equals("00")) {
            // 不启用
            return adjustedExecutors;
        } else if (param.equals("10")) {
            // 启用，不替换执行体
            adjustedExecutors.addAll(executorPool.getFlowControlExecutors());
        } else if (param.startsWith("1n")) {
            // 替换为执行体Pn
            String executorType = getExecutorTypeByParam(param, "control");
            Executor executor = executorPool.getExecutorByType(executorType);
            if (executor != null) {
                adjustedExecutors.add(executor);
            }
        } else if (param.equals("1x")) {
            // 替换为最高权重执行体
            Executor highestPriorityExecutor = executorPool.getHighestPriorityExecutor(
                    executorPool.getFlowControlExecutors());
            if (highestPriorityExecutor != null) {
                adjustedExecutors.add(highestPriorityExecutor);
            }
        }
        
        return adjustedExecutors;
    }
    
    private List<Executor> adjustLinkInfoHideExecutors() {
        // 根据策略参数调整链路信息隐藏执行体集合
        String[] params = strategyParams.split(" ");
        if (params.length < 4) {
            return executorPool.getLinkInfoHideExecutors();
        }
        
        String param = params[3];
        List<Executor> adjustedExecutors = new ArrayList<>();
        
        if (param.equals("00")) {
            // 不启用
            return adjustedExecutors;
        } else if (param.equals("10")) {
            // 启用，不替换执行体
            adjustedExecutors.addAll(executorPool.getLinkInfoHideExecutors());
        } else if (param.startsWith("1n")) {
            // 替换为执行体Pn
            String executorType = getExecutorTypeByParam(param, "hide");
            Executor executor = executorPool.getExecutorByType(executorType);
            if (executor != null) {
                adjustedExecutors.add(executor);
            }
        } else if (param.equals("1x")) {
            // 替换为最高权重执行体
            Executor highestPriorityExecutor = executorPool.getHighestPriorityExecutor(
                    executorPool.getLinkInfoHideExecutors());
            if (highestPriorityExecutor != null) {
                adjustedExecutors.add(highestPriorityExecutor);
            }
        }
        
        return adjustedExecutors;
    }
    
    private String getExecutorTypeByParam(String param, String executorType) {
        // 根据参数获取执行体类型
        // 简化实现，实际应根据参数映射到具体执行体类型
        switch (executorType) {
            case "flow":
                return "entropy_detection";
            case "control":
                return "modal_info_block";
            case "hide":
                return "modal_info_jump";
            default:
                return null;
        }
    }
    
    public void setStrategyParams(String strategyParams) {
        this.strategyParams = strategyParams;
    }
    
    public String getStrategyParams() {
        return strategyParams;
    }
}
