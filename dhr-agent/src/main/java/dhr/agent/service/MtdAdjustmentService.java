package dhr.agent.service;

import dhr.agent.data.MtdAdjustmentData;

/**
 * MTD调整服务接口
 * 用于DHR代理向MTD应用提供调整数据
 */
public interface MtdAdjustmentService {
    /**
     * 获取当前MTD调整数据
     * @return MTD调整数据
     */
    MtdAdjustmentData getAdjustmentData();
    
    /**
     * 设置MTD调整数据
     * @param data MTD调整数据
     */
    void setAdjustmentData(MtdAdjustmentData data);
}
