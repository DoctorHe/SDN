package org.onosproject.mtd.event;

import org.onlab.util.Identifier;
import org.onosproject.event.AbstractEvent;
import org.onosproject.mtd.data.MtdAdjustmentData;

/**
 * MTD调整事件，用于DHR代理向MTD应用发送调整数据.
 */
public class MtdAdjustmentEvent extends AbstractEvent<MtdAdjustmentEvent.Type, MtdAdjustmentData> {
    
    /**
     * 事件类型枚举.
     */
    public enum Type {
        /** MTD调整数据已更新. */
        MTD_ADJUSTMENT_UPDATED
    }
    
    /**
     * 创建一个新的MTD调整事件.
     * @param type 事件类型
     * @param subject 事件主体（MTD调整数据）
     */
    public MtdAdjustmentEvent(Type type, MtdAdjustmentData subject) {
        super(type, subject);
    }
    
    /**
     * 创建一个新的MTD调整事件，并指定事件时间.
     * @param type 事件类型
     * @param subject 事件主体（MTD调整数据）
     * @param time 事件时间戳
     */
    public MtdAdjustmentEvent(Type type, MtdAdjustmentData subject, long time) {
        super(type, subject, time);
    }
}
