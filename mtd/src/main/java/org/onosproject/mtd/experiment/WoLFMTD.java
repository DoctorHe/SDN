package org.onosproject.mtd.experiment;

import java.util.*;

/**
 * 基于 Multi-Agent WoLF-PHC 的 MTD 时间决策系统
 * 参考文献：Sun 等 - 2025 - Multi-Agent Reinforcement Learning for Moving Target Defense [cite: 6]
 */
public class WoLFMTD {

    // --- 1. 核心枚举定义 ---
    enum NetworkState { NORM, FRA, DAM, REC } // 正常、脆弱、受损、恢复 
    enum ActionLevel { LOW, MEDIUM, HIGH } // 动作等级 [cite: 182]

    // --- 2. 信念因子管理 (Belief Factor) ---
    static class BeliefFactor {
        double[] b = {0.1, 0.1, 0.1}; // 初始估计值 [cite: 196]
        double eta = 0.1; // 学习率 [cite: 199]

        // 更新估计值：b_i = b_i + eta * (theta_i - b_i) [cite: 196]
        void update(double[] actualTheta) {
            for (int i = 0; i < 3; i++) {
                b[i] += eta * (actualTheta[i] - b[i]);
            }
        }

        double getError(double[] actualTheta) {
            double error = 0;
            for (int i = 0; i < 3; i++) error += Math.abs(actualTheta[i] - b[i]);
            return error;
        }
    }

    // --- 3. WoLF-PHC 智能体 (Agent) ---
    static class WoLFAgent {
        double[][] pi;        // 策略函数 pi(s, a) [cite: 255]
        double[][] avgPi;     // 平均策略
        double[][] qTable;    // Q值表
        double alphaWin = 0.2;   // 获胜时的学习率 (Config 3) [cite: 382, 403]
        double alphaLearn = 0.1; // 学习时的学习率 [cite: 382]
        double beta = 0.1;       // 平均策略更新平滑参数 [cite: 382]

        WoLFAgent(int stateCount, int actionCount) {
            pi = new double[stateCount][actionCount];
            avgPi = new double[stateCount][actionCount];
            qTable = new double[stateCount][actionCount];
            for (int i = 0; i < stateCount; i++) {
                Arrays.fill(pi[i], 1.0 / actionCount);
                Arrays.fill(avgPi[i], 1.0 / actionCount);
            }
        }

        int selectAction(int state) {
            double r = Math.random();
            double cumulative = 0;
            for (int a = 0; a < pi[state].length; a++) {
                cumulative += pi[state][a];
                if (r <= cumulative) return a;
            }
            return pi[state].length - 1;
        }

        // 核心更新逻辑：判断获胜或落后并调整学习率 [cite: 259, 260]
        void learn(int s, int a, double reward, int nextS, double gamma) {
            // 更新 Q 值 (基础 Q-Learning)
            double maxNextQ = Arrays.stream(qTable[nextS]).max().orElse(0);
            qTable[s][a] += 0.1 * (reward + gamma * maxNextQ - qTable[s][a]);

            // 计算当前策略期望与平均策略期望，判断是否为 "Win" [cite: 264]
            double currentExp = 0, avgExp = 0;
            for (int i = 0; i < pi[s].length; i++) {
                currentExp += pi[s][i] * qTable[s][i];
                avgExp += avgPi[s][i] * qTable[s][i];
            }

            double alpha = (currentExp > avgExp) ? alphaWin : alphaLearn;

            // 更新策略 pi (简化 PHC 梯度上升)
            int bestA = 0;
            for (int i = 1; i < qTable[s].length; i++) if (qTable[s][i] > qTable[s][bestA]) bestA = i;
            for (int i = 0; i < pi[s].length; i++) {
                double delta = (i == bestA) ? alpha : -alpha / (pi[s].length - 1);
                pi[s][i] = Math.max(0, Math.min(1, pi[s][i] + delta));
            }
            
            // 更新平均策略 [cite: 303]
            for (int i = 0; i < avgPi[s].length; i++) {
                avgPi[s][i] += (1.0 / (1.0 + beta)) * (pi[s][i] - avgPi[s][i]);
            }
        }
    }

    // --- 4. 运行环境与主逻辑 ---
    private final double[] THETA = {0.5, 0.5, 0.5}; // 实际阈值向量 [cite: 340]
    private WoLFAgent defender = new WoLFAgent(4, 3);
    private BeliefFactor bd = new BeliefFactor(); // 防御者对攻击者的估计

    /**
     * 调用此函数获取实时的跳变频率
     * @param metrics [当前攻击频率, 当前负载, 当前暴露时间]
     */
    public double getNextHoppingFrequency(double[] metrics) {
        // 1. 判断当前状态 [cite: 170-173]
        NetworkState state = determineState(metrics);
        
        // 2. 更新信念因子 [cite: 196]
        bd.update(THETA);

        // 3. 选择动作级别 [cite: 182]
        int actionIdx = defender.selectAction(state.ordinal());
        
        // 4. 计算并返回物理频率 (根据 Table 5) [cite: 364]
        return mapToFrequency(actionIdx);
    }

    private NetworkState determineState(double[] m) {
        if (m[0] > THETA[0] && m[1] > THETA[1] && m[2] > THETA[2]) return NetworkState.DAM; // 受损 [cite: 172]
        if (m[0] > THETA[0]) return NetworkState.FRA; // 脆弱 [cite: 171]
        return NetworkState.NORM; // 正常 [cite: 170]
    }

    private double mapToFrequency(int level) {
        if (level == 2) return 1.0; // High: 1.0/s (对应1s间隔) [cite: 364]
        if (level == 1) return 0.2; // Medium: 0.2/s (对应5s间隔) [cite: 364]
        return 0.067; // Low: 0.067/s (对应15s间隔) [cite: 364]
    }
    
    /**
     * 将频率转换为调整系数
     * 转换公式：adjustmentFactor = 目标间隔的倒数 / 基础概率
     * 假设基础概率(phs[i] * pmh[j])为0.05，确保最终跳变间隔在0.5s~15s以内
     * @param frequency 跳变频率，单位：Hz
     * @return 调整系数，用于 MtdMechanism.adjustmentFactor
     */
    public float convertToAdjustmentFactor(double frequency) {
        // 目标跳变间隔 = 1 / frequency
        double targetInterval = 1 / frequency;
        
        // 假设基础概率(phs[i] * pmh[j])为0.05
        double baseProbability = 0.05;
        
        // 调整系数公式：(1 / (targetInterval * baseProbability))
        // 确保最终跳变间隔 = 1 / (phs[i] * pmh[j] * adjustmentFactor) ≈ targetInterval
        float adjustmentFactor = (float) (1 / (targetInterval * baseProbability));
        
        // 确保调整系数范围：2.0f ~ 40.0f
        // 对应跳变间隔：15s ~ 0.5s
        return Math.max(2.0f, Math.min(40.0f, adjustmentFactor));
    }
    
    /**
     * 获取当前跳变频率对应的调整系数
     * @param metrics [当前攻击频率, 当前负载, 当前暴露时间]
     * @return 调整系数，用于 MtdMechanism.adjustmentFactor
     */
    public float getAdjustmentFactor(double[] metrics) {
        double frequency = getNextHoppingFrequency(metrics);
        return convertToAdjustmentFactor(frequency);
    }
    
    /**
     * 直接将计算的调整系数应用到 MTD 系统
     * @param metrics [当前攻击频率, 当前负载, 当前暴露时间]
     */
    public void applyToMtdSystem(double[] metrics) {
        float factor = getAdjustmentFactor(metrics);
        
        // 动态更新 MTD 系统的调整系数
        try {
            org.onosproject.mtd.strategy.MtdMechanism.updateAdjustmentFactor(factor);
            System.out.println(String.format("WoLFMTD: 已更新 MTD 调整系数为 %.2f (对应频率: %.3f Hz)", 
                    factor, getNextHoppingFrequency(metrics)));
        } catch (Exception e) {
            System.err.println("WoLFMTD: 更新 MTD 调整系数失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}