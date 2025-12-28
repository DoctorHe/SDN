package org.onosproject.mtd.experiment;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 2SCEMA 核心处理器
 * 包含 SDC (算法逻辑) 和 SID (内部时钟类)
 */
public class TwoScemaProcessor {

    // --- 外部类成员变量 ---
    private final int delta;
    private final int muPlusRho;
    private final Map<String, Integer> hostToCriticalEdges = new ConcurrentHashMap<>();
    private final Set<String> criticalServerIds = ConcurrentHashMap.newKeySet();
    private int intervalCounter = 0;
    
    // 引用内部时钟类
    private final ShufflingFrequencyClock frequencyClock;
    
    // 运行状态
    private final AtomicReference<ScemaRealTimeStatus> currentStatus = new AtomicReference<>();
    private volatile boolean isRunning = true;

    /**
     * 初始化处理器
     * @param initialSigma 初始跳变间隔
     * @param delta Hard模式频率
     * @param muPlusRho 阈值
     */
    public TwoScemaProcessor(int initialSigma, int delta, int muPlusRho) {
        this.delta = delta;
        this.muPlusRho = muPlusRho;
        
        // 初始化内部时钟 (开启动态模拟: true)
        this.frequencyClock = new ShufflingFrequencyClock(initialSigma, true);
        
        // 初始化空状态
        this.currentStatus.set(new ScemaRealTimeStatus(0, initialSigma, false, Collections.emptySet()));
    }

    /**
     * 启动阻塞式处理线程 (对应您的策略循环需求)
     * 注意：在 SDN 控制器中，建议在独立线程运行此方法，避免阻塞主线程
     */
    public void start() {
        new Thread(() -> {
            System.out.println("2SCEMA 策略线程已启动...");
            while (isRunning) {
                // 1. 【关键】阻塞等待，直到频率时钟返回 (获取实时更新的 σ)
                double intervalUsed = frequencyClock.waitForNextTick();
                
                // 2. 执行一次算法周期
                runSidAlgorithm(intervalUsed);
            }
        }, "2SCEMA-Worker-Thread").start();
    }
    
    /**
     * 获取当前跳变间隔 (σ)
     * @return 当前跳变间隔，单位：秒
     */
    public double getCurrentInterval() {
        return frequencyClock.getCurrentSigma();
    }
    
    /**
     * 获取当前跳变频率对应的调整系数
     * 转换公式：adjustmentFactor = 15 / currentInterval
     * 间隔越小，调整系数越大，跳变频率越高
     * 确保最终跳变间隔控制在0.5s~15s以内
     * @return 调整系数，用于 MtdMechanism.adjustmentFactor
     */
    public float getAdjustmentFactor() {
        double currentInterval = frequencyClock.getCurrentSigma();
        
        // 确保currentInterval为正数
        if (currentInterval <= 0) {
            return 30.0f; // 当间隔为0或负数时，使用较大调整系数
        }
        
        // 调整系数公式：15 / currentInterval
        // 当currentInterval=0.5秒时：15/0.5 = 30 → 跳变频率较高
        // 当currentInterval=15秒时：15/15 = 1 → 跳变频率较低
        // 确保最终跳变间隔在0.5s~15s以内
        float adjustmentFactor = (float) (15 / currentInterval);
        
        // 确保调整系数范围：1.0f ~ 30.0f
        return Math.max(1.0f, Math.min(30.0f, adjustmentFactor));
    }
    
    /**
     * 直接将当前跳变频率应用到 MTD 系统
     * 更新 MtdMechanism.adjustmentFactor
     */
    public void applyToMtdSystem() {
        float factor = getAdjustmentFactor();
        // 导入并更新 MtdMechanism 的调整系数
        try {
            // 动态更新 MTD 系统的调整系数
            org.onosproject.mtd.strategy.MtdMechanism.updateAdjustmentFactor(factor);
            System.out.println((String.format("2SCEMA: 已更新 MTD 调整系数为 %.2f (当前间隔: %.1f 秒)", 
                    factor, frequencyClock.getCurrentSigma())));
        } catch (Exception e) {
            System.err.println("2SCEMA: 更新 MTD 调整系数失败: " + e.getMessage());
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    // 更新拓扑数据
    public void updateTopology(Map<String, Set<String>> hostEdgesMap, Set<String> serverIds) {
        criticalServerIds.clear();
        criticalServerIds.addAll(serverIds);
        hostToCriticalEdges.clear();
        for (Map.Entry<String, Set<String>> entry : hostEdgesMap.entrySet()) {
            String hostId = entry.getKey();
            if (serverIds.contains(hostId)) continue;
            int edgesToServers = 0;
            for (String neighbor : entry.getValue()) {
                if (serverIds.contains(neighbor)) {
                    edgesToServers++;
                }
            }
            hostToCriticalEdges.put(hostId, edgesToServers);
        }
    }

    public ScemaRealTimeStatus getCurrentStatus() {
        return currentStatus.get();
    }

    // 核心算法逻辑
    private void runSidAlgorithm(double currentSigma) {
        intervalCounter++;
        Map<String, Double> degrees = calculateDegrees();
        boolean isHardInterval = (intervalCounter % delta == 0);
        Set<String> mutatedHosts = selectHostsToMutate(degrees, isHardInterval);

        // 更新状态
        ScemaRealTimeStatus newStatus = new ScemaRealTimeStatus(
                intervalCounter, (int) Math.round(currentSigma), isHardInterval, mutatedHosts
        );
        currentStatus.set(newStatus);
        
        System.out.println(String.format(">>> 周期完成: 间隔=%.1fs, 模式=%s, 变异数=%d", 
                currentSigma, isHardInterval ? "HARD" : "SOFT", mutatedHosts.size()));
        
        // 自动将当前跳变频率应用到 MTD 系统
        applyToMtdSystem();
    }

    private Map<String, Double> calculateDegrees() {
        Map<String, Double> degrees = new HashMap<>();
        double totalNe = hostToCriticalEdges.values().stream().mapToInt(Integer::intValue).sum();
        if (totalNe == 0) return degrees;
        for (Map.Entry<String, Integer> entry : hostToCriticalEdges.entrySet()) {
            degrees.put(entry.getKey(), entry.getValue() / totalNe);
        }
        return degrees;
    }

    private Set<String> selectHostsToMutate(Map<String, Double> degrees, boolean isHard) {
        Set<String> targets = new HashSet<>();
        if (isHard) {
            targets = degrees.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(muPlusRho)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        } else {
            Random random = new Random();
            for (Map.Entry<String, Double> entry : degrees.entrySet()) {
                if (random.nextDouble() < entry.getValue()) {
                    targets.add(entry.getKey());
                }
            }
        }
        return targets;
    }

    // =========================================================
    //  👇 您要求的代码已转换为静态内部类 👇
    // =========================================================
    
    /**
     * 2SCEMA 频率(间隔)控制器
     * 对应论文中的 SID (Shuffling Interval Detector) 的计时逻辑
     */
    public static class ShufflingFrequencyClock {

        private static final Logger log = Logger.getLogger(ShufflingFrequencyClock.class.getName());

        // 当前的跳变间隔 σ (单位: 秒)
        private volatile double currentSigma;

        // 是否启用动态模拟
        private final boolean enableDynamicSimulation;
        
        // σ的最小值和最大值 (单位: 秒)
        private static final double MIN_SIGMA = 0.5;
        private static final double MAX_SIGMA = 15.0;

        /**
         * @param initialSigma 初始间隔 (秒)
         * @param enableDynamicSimulation 是否开启模拟自动更新频率
         */
        public ShufflingFrequencyClock(int initialSigma, boolean enableDynamicSimulation) {
            // 将初始值限制在0.5~15秒之间
            this.currentSigma = Math.max(MIN_SIGMA, Math.min(MAX_SIGMA, initialSigma));
            this.enableDynamicSimulation = enableDynamicSimulation;
        }

        /**
         * 【核心接口】等待下一个跳变时刻
         * 逻辑：
         * 1. 根据当前的 σ 进行阻塞等待
         * 2. (可选) 自动更新下一次的 σ 值
         * 3. 返回刚才使用的 σ 值
         */
        public synchronized double waitForNextTick() {
            double intervalUsed = currentSigma;

            try {
                // 模拟等待 σ 秒，转换为毫秒
                // log.info(">>> 内部时钟: 等待 " + intervalUsed + " 秒...");
                Thread.sleep((long) (intervalUsed * 1000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warning("时钟被中断");
            }

            // 如果开启了动态模拟，这里自动更新下一次的频率
            if (enableDynamicSimulation) {
                simulateAutoUpdate();
            }

            return intervalUsed;
        }

        /**
         * (可选) 模拟根据网络状况自动调整频率
         */
        private synchronized void simulateAutoUpdate() {
            // 简单模拟：在 [0.5, 15] 秒之间随机波动
            Random random = new Random();
            double newSigma = MIN_SIGMA + random.nextDouble() * (MAX_SIGMA - MIN_SIGMA);
            updateSigma(newSigma);
        }

        /**
         * 手动更新频率接口
         */
        public synchronized void updateSigma(double newSigma) {
            // 将新值限制在0.5~15秒之间
            double clampedSigma = Math.max(MIN_SIGMA, Math.min(MAX_SIGMA, newSigma));
            double old = this.currentSigma;
            this.currentSigma = clampedSigma;
            if (Math.abs(old - clampedSigma) > 0.001) {
                log.info(String.format("内部时钟: 频率已更新 σ=%.1fs", clampedSigma));
            }
        }

        public synchronized double getCurrentSigma() {
            return currentSigma;
        }
    }

    // =========================================================
    //  DTO (数据传输对象)
    // =========================================================
    public static class ScemaRealTimeStatus {
        public final int intervalIndex;
        public final int currentFrequency;
        public final boolean isHardInterval;
        public final Set<String> hostsToMutate;

        public ScemaRealTimeStatus(int idx, int freq, boolean isHard, Set<String> hosts) {
            this.intervalIndex = idx;
            this.currentFrequency = freq;
            this.isHardInterval = isHard;
            this.hostsToMutate = hosts;
        }
    }
    
    // 测试入口
    public static void main(String[] args) {
        // 初始间隔 2秒，方便观察
        TwoScemaProcessor processor = new TwoScemaProcessor(2, 3, 2);
        
        // 模拟拓扑
        Map<String, Set<String>> topo = new HashMap<>();
        topo.put("h1", new HashSet<>(Arrays.asList("s1")));
        processor.updateTopology(topo, new HashSet<>(Arrays.asList("s1")));
        
        // 启动处理器
        processor.start();
    }
}