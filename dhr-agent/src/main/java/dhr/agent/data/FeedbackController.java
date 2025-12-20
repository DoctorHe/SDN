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
    
    // 跳变策略执行体池映射表
    private Map<String, JumpExecutorPool> executorPools;
    
    // 当前安全等级
    private int currentSecurityLevel;
    
    // 上一轮状态码
    private String lastStatusCode;
    
    public FeedbackController() {
        initStateTransitionTable();
        initStrategySet();
        initSecurityLevelStrategySet();
        initOperationCodeMap();
        initExecutorPools();
        
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
        // 策略参数为三位，分别对应流检测、链路信息隐藏、特征选择执行体集合
        // 11表示启用池内第1个执行体，12表示启用池内第2个执行体，1x表示启用池内最高权重执行体
        securityLevelStrategySet.put("0", new SecurityLevelStrategy("初始化", "11 11 11"));
        securityLevelStrategySet.put("1", new SecurityLevelStrategy("DHR系统和网络稳定", "10 00 00"));
        securityLevelStrategySet.put("2", new SecurityLevelStrategy("系统和网络趋于稳定", "10 11 00"));
        securityLevelStrategySet.put("3", new SecurityLevelStrategy("网络开始趋于稳定", "1x 11 00"));
        securityLevelStrategySet.put("4", new SecurityLevelStrategy("系统可能受到影响/策略未生效", "1x 1x 00"));
        securityLevelStrategySet.put("5", new SecurityLevelStrategy("网络大概率受到攻击", "1x 1x 11"));
        securityLevelStrategySet.put("6", new SecurityLevelStrategy("系统与网络状态进一步恶化", "1x 1x 1x"));
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
     * 初始化跳变策略执行体池
     */
    private void initExecutorPools() {
        // 初始化单个执行体池，包含三个执行体集合
        JumpExecutorPool pool = new JumpExecutorPool();
        
        // 初始化流检测执行体集合
        ExecutorCollection flowCollection = pool.getFlowDetectionCollection();
        flowCollection.addExecutor(new FlowDetectionExecutor(15, "flow_detection_1")); // 权重15
        flowCollection.addExecutor(new SimpleDetectionExecutor(10, "simple_detection_1")); // 权重10
        flowCollection.addExecutor(new MachineLearningDetectionExecutor(20, "ml_detection_1")); // 权重20（最高）
        
        // 初始化链路信息隐藏执行体集合
        ExecutorCollection linkCollection = pool.getLinkHidingCollection();
        linkCollection.addExecutor(new ModalInfoExecutor(10, "modal_info_1")); // 权重10
        linkCollection.addExecutor(new PortExecutor(15, "port_1")); // 权重15
        linkCollection.addExecutor(new ForwardPathExecutor(12, "forward_path_1")); // 权重12
        linkCollection.addExecutor(new RequestServerExecutor(8, "request_server_1")); // 权重8
        
        // 初始化特征选择执行体集合
        ExecutorCollection featureCollection = pool.getFeatureSelectionCollection();
        featureCollection.addExecutor(new ExperienceSelectionExecutor(10, "experience_selection_1")); // 权重10
        featureCollection.addExecutor(new AnovaSelectionExecutor(15, "anova_selection_1")); // 权重15
        featureCollection.addExecutor(new RfeSelectionExecutor(18, "rfe_selection_1")); // 权重18（最高）
        
        // 将执行体池存储到映射表中
        executorPools = new HashMap<>();
        executorPools.put("main", pool);
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
     * 执行跳变策略
     * @param securityLevel 安全等级
     */
    public void executeJumpStrategy(int securityLevel) {
        // 获取当前安全等级对应的策略
        SecurityLevelStrategy strategy = securityLevelStrategySet.get(String.valueOf(securityLevel));
        if (strategy == null) {
            System.out.println("未找到对应安全等级的策略");
            return;
        }
        
        // 获取主执行体池
        JumpExecutorPool pool = executorPools.get("main");
        if (pool == null) {
            System.out.println("未找到主执行体池");
            return;
        }
        
        // 解析策略参数，执行相应的跳变策略
        String[] params = strategy.getStrategyParams().split(" ");
        
        // 策略参数格式为："11 11 11"，表示从三个执行体集合中各选1号执行体启用
        // 每一位参数对应一个执行体集合
        if (params.length == 3) {
            // 从流检测执行体集合中选择执行体
            executeFromCollection(pool.getFlowDetectionCollection(), "流检测", params[0]);
            // 从链路信息隐藏执行体集合中选择执行体
            executeFromCollection(pool.getLinkHidingCollection(), "链路信息隐藏", params[1]);
            // 从特征选择执行体集合中选择执行体
            executeFromCollection(pool.getFeatureSelectionCollection(), "特征选择", params[2]);
        }
    }
    
    /**
     * 从指定执行体集合中选择并执行执行体
     * @param collection 执行体集合
     * @param collectionName 执行体集合名称
     * @param operationCode 操作码
     */
    private void executeFromCollection(ExecutorCollection collection, String collectionName, String operationCode) {
        JumpStrategyExecutor executor = null;
        
        // 根据操作码选择执行体
        if (operationCode.equals("00")) {
            // 不启用
            System.out.println("不启用" + collectionName + "执行体集合");
            // 停止所有执行体
            for (JumpStrategyExecutor exec : collection.getAllExecutors()) {
                exec.setRunning(false);
            }
            return;
        } else if (operationCode.equals("10")) {
            // 启用，不替换执行体（使用当前执行体）
            System.out.println("启用" + collectionName + "执行体集合，不替换执行体");
            // 简化实现，使用第一个执行体
            executor = collection.getExecutorByIndex(1);
        } else if (operationCode.startsWith("1") && operationCode.length() == 2) {
            char secondChar = operationCode.charAt(1);
            if (secondChar == 'x') {
                // 1x：替换为最高权重执行体
                System.out.println("从" + collectionName + "执行体集合中选择最高权重执行体");
                executor = collection.getHighestWeightExecutor();
            } else if (Character.isDigit(secondChar)) {
                // 1n：替换为集合内第n个执行体
                int index = Character.getNumericValue(secondChar);
                System.out.println("从" + collectionName + "执行体集合中选择第" + index + "个执行体");
                executor = collection.getExecutorByIndex(index);
            }
        }
        
        // 执行选中的执行体
        if (executor != null) {
            executor.execute();
        } else {
            System.out.println("无法从" + collectionName + "执行体集合中找到合适的执行体，操作码: " + operationCode);
        }
    }
    
    /**
     * 获取跳变执行体状态
     * @param executorType 执行体类型
     * @return 执行体状态（只要有一个执行体在运行就返回true）
     */
    public boolean getExecutorStatus(String executorType) {
        // 简化实现，返回true表示运行中
        return true;
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
        /** 状态转移含义描述 */
        private String meaning;
        /** 是否符合一致性期望 */
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
        /** 执行器集合，如"流量检测"、"流量控制"、"链路信息隐藏执行"等 */
        private String executorCollection;
        /** 策略启动条件，如"无条件，持续运行"、"受到调度后启动"等 */
        private String startCondition;
        /** 执行器替换条件，如"状态码非11且不符合一致性期望"等 */
        private String executorReplacementCondition;
        
        /**
         * DhRStrategy构造函数
         * @param executorCollection 执行器集合
         * @param startCondition 策略启动条件
         * @param executorReplacementCondition 执行器替换条件
         */
        public DhRStrategy(String executorCollection, String startCondition, String executorReplacementCondition) {
            this.executorCollection = executorCollection;
            this.startCondition = startCondition;
            this.executorReplacementCondition = executorReplacementCondition;
        }
        
        /**
         * 获取执行器集合
         * @return 执行器集合
         */
        public String getExecutorCollection() {
            return executorCollection;
        }
        
        /**
         * 获取策略启动条件
         * @return 策略启动条件
         */
        public String getStartCondition() {
            return startCondition;
        }
        
        /**
         * 获取执行器替换条件
         * @return 执行器替换条件
         */
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
