package dhr.agent.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 反馈控制器
 */
public class FeedbackController {
    // 系统状态转移表
    private Map<String, SystemStateTransition> stateTransitionTable;
    
    // DHR系统策略集
    private Map<Integer, DhRStrategy> strategySet;
    
    // 引入安全等级和特征选择的策略集
    private Map<String, SecurityLevelStrategy> securityLevelStrategySet;
    
    // 操作码集合
    private Map<String, String> operationCodeMap;
    
    // 当前安全等级
    private int currentSecurityLevel;
    
    // 上一轮状态码
    private String lastStatusCode;
    
    public FeedbackController() {
        initStateTransitionTable();
        initStrategySet();
        initSecurityLevelStrategySet();
        initOperationCodeMap();
        
        this.currentSecurityLevel = 0;
        this.lastStatusCode = "11";
    }
    
    /**
     * 初始化系统状态转移表
     */
    private void initStateTransitionTable() {
        stateTransitionTable = new HashMap<>();
        
        // 表4：系统状态转移表
        stateTransitionTable.put("11_11", new SystemStateTransition("系统正常运行", true));
        stateTransitionTable.put("11_01", new SystemStateTransition("系统状态恶化", false));
        stateTransitionTable.put("11_00", new SystemStateTransition("系统状态急剧恶化", false));
        stateTransitionTable.put("01_11", new SystemStateTransition("系统恢复正常", true));
        stateTransitionTable.put("01_01", new SystemStateTransition("上轮策略未凑效", false));
        stateTransitionTable.put("01_00", new SystemStateTransition("系统状态恶化", true));
        stateTransitionTable.put("00_11", new SystemStateTransition("系统与网络恢复正常", true));
        stateTransitionTable.put("00_01", new SystemStateTransition("策略部分奏效", true));
        stateTransitionTable.put("00_00", new SystemStateTransition("上轮策略完全未奏效", false));
    }
    
    /**
     * 初始化DHR系统策略集
     */
    private void initStrategySet() {
        strategySet = new HashMap<>();
        
        // 表5：DHR系统策略集
        strategySet.put(1, new DhRStrategy("流量检测", "无条件，持续运行", "状态码非11且不符合一致性期望"));
        strategySet.put(2, new DhRStrategy("流量控制", "受到调度后启动", ""));
        strategySet.put(3, new DhRStrategy("链路信息隐藏执行", "无条件，持续运行", ""));
    }
    
    /**
     * 初始化引入安全等级和特征选择的策略集
     */
    private void initSecurityLevelStrategySet() {
        securityLevelStrategySet = new HashMap<>();
        
        // 表6：引入安全等级和特征选择的策略集
        securityLevelStrategySet.put("0_-_", new SecurityLevelStrategy("初始化", "1n 00 1n 1n"));
        securityLevelStrategySet.put("1_11->11", new SecurityLevelStrategy("DHR系统和网络稳定", "10 00 10 00"));
        securityLevelStrategySet.put("2_01/00->11", new SecurityLevelStrategy("DHR系统趋于稳定", "10 10 10 00"));
        securityLevelStrategySet.put("3_00->01", new SecurityLevelStrategy("网络开始趋于稳定", "1x 10 10 00"));
        securityLevelStrategySet.put("4_11/01->01", new SecurityLevelStrategy("系统可能受到影响/策略未生效", "1x 1x 1x 00"));
        securityLevelStrategySet.put("5_11->00", new SecurityLevelStrategy("系统与网络状态快速恶化", "1x 1x 1x 10"));
        securityLevelStrategySet.put("6_01/00->00", new SecurityLevelStrategy("系统与网络状态进一步恶化", "1x 1x 1x 1x"));
    }
    
    /**
     * 初始化操作码集合
     */
    private void initOperationCodeMap() {
        operationCodeMap = new HashMap<>();
        
        // 表7：操作码集合
        operationCodeMap.put("00", "不启用");
        operationCodeMap.put("10", "启用，不替换执行体");
        operationCodeMap.put("1n", "替换为执行体Pn");
        operationCodeMap.put("1x", "替换为最高权重执行体");
    }
    
    /**
     * 后向验证
     * @param currentStatusCode 当前状态码
     * @return 后向验证结果
     */
    public boolean backwardVerify(String currentStatusCode) {
        String transitionKey = lastStatusCode + "_" + currentStatusCode;
        SystemStateTransition transition = stateTransitionTable.get(transitionKey);
        
        boolean result = transition != null && transition.isConsistentExpectation();
        
        // 更新上一轮状态码
        this.lastStatusCode = currentStatusCode;
        
        return result;
    }
    
    /**
     * 策略选择
     * @param decisionParams 裁决参数
     * @return 选择的策略
     */
    public DhRStrategy selectStrategy(String decisionParams) {
        // 根据裁决参数在策略集中选择可以最快规避当前问题的策略
        // 简化实现，返回第一个策略
        return strategySet.get(1);
    }
    
    /**
     * 鲁棒控制
     * @param backwardVerifyResult 后向验证结果
     * @param currentStatusCode 当前状态码
     * @return 安全等级
     */
    public int robustControl(boolean backwardVerifyResult, String currentStatusCode) {
        // 根据后向验证结果及当前状态码决定当前系统安全等级
        if (backwardVerifyResult) {
            // 系统状态良好，降低安全等级
            currentSecurityLevel = Math.max(0, currentSecurityLevel - 1);
        } else {
            // 系统状态恶化，提高安全等级
            currentSecurityLevel = Math.min(6, currentSecurityLevel + 1);
        }
        
        return currentSecurityLevel;
    }
    
    /**
     * 获取安全等级策略
     * @param securityLevel 安全等级
     * @param stateTransition 状态转移
     * @return 安全等级策略
     */
    public SecurityLevelStrategy getSecurityLevelStrategy(int securityLevel, String stateTransition) {
        String key = securityLevel + "_" + stateTransition;
        return securityLevelStrategySet.get(key);
    }
    
    /**
     * 获取操作码含义
     * @param operationCode 操作码
     * @return 操作码含义
     */
    public String getOperationCodeMeaning(String operationCode) {
        return operationCodeMap.getOrDefault(operationCode, "未知操作码");
    }
    
    /**
     * 设置上一轮状态码
     * @param lastStatusCode 上一轮状态码
     */
    public void setLastStatusCode(String lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }
    
    /**
     * 获取上一轮状态码
     * @return 上一轮状态码
     */
    public String getLastStatusCode() {
        return lastStatusCode;
    }
    
    /**
     * 获取当前安全等级
     * @return 当前安全等级
     */
    public int getCurrentSecurityLevel() {
        return currentSecurityLevel;
    }
    
    // 系统状态转移内部类
    private static class SystemStateTransition {
        private String meaning;
        private boolean consistentExpectation;
        
        public SystemStateTransition(String meaning, boolean consistentExpectation) {
            this.meaning = meaning;
            this.consistentExpectation = consistentExpectation;
        }
        
        public String getMeaning() {
            return meaning;
        }
        
        public boolean isConsistentExpectation() {
            return consistentExpectation;
        }
    }
    
    // DHR策略内部类
    private static class DhRStrategy {
        private String executorCollection;
        private String startCondition;
        private String executorReplacementCondition;
        
        public DhRStrategy(String executorCollection, String startCondition, String executorReplacementCondition) {
            this.executorCollection = executorCollection;
            this.startCondition = startCondition;
            this.executorReplacementCondition = executorReplacementCondition;
        }
        
        public String getExecutorCollection() {
            return executorCollection;
        }
        
        public String getStartCondition() {
            return startCondition;
        }
        
        public String getExecutorReplacementCondition() {
            return executorReplacementCondition;
        }
    }
    
    // 安全等级策略内部类
    private static class SecurityLevelStrategy {
        private String meaning;
        private String strategyParams;
        
        public SecurityLevelStrategy(String meaning, String strategyParams) {
            this.meaning = meaning;
            this.strategyParams = strategyParams;
        }
        
        public String getMeaning() {
            return meaning;
        }
        
        public String getStrategyParams() {
            return strategyParams;
        }
    }
}
