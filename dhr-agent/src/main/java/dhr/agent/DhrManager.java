package dhr.agent;

import dhr.agent.data.FlowManager;
import dhr.agent.data.FeedbackController;
import dhr.agent.data.DynamicScheduler;
import dhr.agent.data.OutputArbiter;
import dhr.agent.data.executor.ExecutorPool;
import dhr.agent.service.MtdAdjustmentService;
import dhr.agent.data.MtdAdjustmentData;
import dhr.agent.utility.Log;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.*;

import java.util.Timer;
@Component(immediate = true, service = {DhrManager.class, MtdAdjustmentService.class})
public class DhrManager implements Runnable, MtdAdjustmentService {
    private boolean isPolymorphicMode;
    public static final int FLOW_INFO_INTERVAL = 8000; // 收集流信息时间间隔,毫秒
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference
    protected FlowManager flowManager;
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;
    
    // 执行体集合池
    private ExecutorPool executorPool;
    
    // 动态调度器
    private DynamicScheduler dynamicScheduler;
    
    // 输出裁决器
    private OutputArbiter outputArbiter;
    
    // 反馈控制器
    private FeedbackController feedbackController;

    private Timer timer = new Timer();
    
    // 当前MTD调整数据
    private volatile MtdAdjustmentData currentAdjustmentData;
    
    // 应用ID
    private ApplicationId appId;
    
    // 发送数据的线程引用，用于应用关闭时中断
    private Thread sendThread;

    public boolean isPolymorphicMode() {
        return isPolymorphicMode;
    }

    public DhrManager() {
        // 初始化执行体集合池
        executorPool = new ExecutorPool();
        
        // 初始化动态调度器
        dynamicScheduler = new DynamicScheduler(executorPool);
        
        // 初始化输出裁决器
        outputArbiter = new OutputArbiter();
        
        // 初始化反馈控制器
        feedbackController = new FeedbackController();
        
        // 初始化MTD调整数据
        this.currentAdjustmentData = new MtdAdjustmentData();
    }
    
    @Override
    public MtdAdjustmentData getAdjustmentData() {
        return currentAdjustmentData;
    }
    
    @Override
    public void setAdjustmentData(MtdAdjustmentData data) {
        this.currentAdjustmentData = data;
        System.out.println("DHR Agent: MTD调整数据已更新 - " + data);
    }
    
    @Activate
    public void activate() {
        // 激活时创建并启动线程执行 run 方法
        flowManager.setPolymorphicMode(isPolymorphicMode);
        timer.schedule(flowManager, 0, FLOW_INFO_INTERVAL);
        
        // 初始化应用ID
        appId = coreService.registerApplication("dhr.agent");
        
        System.out.println("DHR Agent: 已激活");
        
        // 启动数据发送线程并保存引用
        sendThread = new Thread(this);
        sendThread.start();
    }
    
    /**
     * 生成示例数据，用于测试
     */
    private MtdAdjustmentData generateSampleData() {
        MtdAdjustmentData data = new MtdAdjustmentData();
        
        // 根据安全等级动态调整
        int securityLevel = (int) (Math.random() * 7) + 1; // 1-7级
        data.setSecurityLevel(securityLevel);
        
        // 安全等级越高，调整系数越大，跳变频率越高
        // 系数控制在1~8之间
        float factor = (7.0f/6.0f) * securityLevel - (1.0f/6.0f);
        // 确保系数在1~8之间
        factor = Math.max(1.0f, Math.min(8.0f, factor));
        data.setAdjustmentFactor(factor);
        
        // 根据安全等级调整机制开关
        data.setIpMtdEnabled(true);
        data.setPortMtdEnabled(securityLevel >= 2);
        data.setPathMtdEnabled(true);
        data.setHostMtdEnabled(securityLevel >= 4);
        
        // 调整概率分布
        float[] hostProbs = new float[4];
        float[] serverProbs = new float[4];
        float[] databaseProbs = new float[4];
        
        if (securityLevel <= 2) {
            // 低安全等级（1-2级）：更倾向于IP跳变
            hostProbs = new float[]{0.7f, 0.1f, 0.1f, 0.1f};
            serverProbs = new float[]{0.5f, 0.2f, 0.2f, 0.1f};
            databaseProbs = new float[]{0.5f, 0.2f, 0.2f, 0.1f};
        } else if (securityLevel <= 4) {
            // 中安全等级（3-4级）：均衡分布
            hostProbs = new float[]{0.4f, 0.3f, 0.2f, 0.1f};
            serverProbs = new float[]{0.3f, 0.3f, 0.2f, 0.2f};
            databaseProbs = new float[]{0.3f, 0.3f, 0.2f, 0.2f};
        } else {
            // 高安全等级（5-7级）：更倾向于多种跳变机制
            hostProbs = new float[]{0.3f, 0.3f, 0.2f, 0.2f};
            serverProbs = new float[]{0.2f, 0.4f, 0.2f, 0.2f};
            databaseProbs = new float[]{0.2f, 0.4f, 0.2f, 0.2f};
        }
        
        data.setHostMtdProbabilities(hostProbs);
        data.setServerMtdProbabilities(serverProbs);
        data.setDatabaseMtdProbabilities(databaseProbs);
        
        return data;
    }
    
    @Deactivate
    public void deactivate() {
        // 清理资源
        timer.cancel();
        
        // 中断数据发送线程
        if (sendThread != null && sendThread.isAlive()) {
            System.out.println("DHR Agent: 中断数据发送线程");
            sendThread.interrupt();
            try {
                // 等待线程结束，最多等待1秒
                sendThread.join(1000);
                System.out.println("DHR Agent: 数据发送线程已停止");
            } catch (InterruptedException e) {
                System.out.println("DHR Agent: 等待线程结束时被中断");
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("DHR Agent: 已停用");
    }
    
    public boolean getCurrentMode() {
        boolean currentMode = false;
        try {
            if (deviceService.getDevices().iterator().hasNext()) {
                Device device = deviceService.getDevices().iterator().next();
                if (device.hwVersion().equals("Open vSwitch")) { //Indicates that the current mode is ip.
                    currentMode = false;
                } else {
                    currentMode = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        isPolymorphicMode = currentMode;
        return currentMode;
    }

    @Override
    public void run() {
        // 定期更新MTD调整数据
        while (true) {
            try {
                // 生成示例数据
                MtdAdjustmentData data = generateSampleData();
                
                // 直接更新调整数据
                setAdjustmentData(data);
                
                // 每5秒更新一次数据
                Thread.sleep(5000 + (int)(Math.random() * 15000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
