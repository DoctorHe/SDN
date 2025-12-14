package dhr.agent;

import dhr.agent.data.FlowManager;
import dhr.agent.data.FeedbackController;
import dhr.agent.data.DynamicScheduler;
import dhr.agent.data.OutputArbiter;
import dhr.agent.data.executor.ExecutorPool;
import dhr.agent.utility.Log;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
@Component(immediate = true, service = DhrManager.class)
public class DhrManager implements Runnable{
    private boolean isPolymorphicMode;
    public static final int FLOW_INFO_INTERVAL = 8000; // 收集流信息时间间隔,毫秒
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
    @Reference
    protected FlowManager flowManager;
    
    // 执行体集合池
    private ExecutorPool executorPool;
    
    // 动态调度器
    private DynamicScheduler dynamicScheduler;
    
    // 输出裁决器
    private OutputArbiter outputArbiter;
    
    // 反馈控制器
    private FeedbackController feedbackController;

    private Timer timer = new Timer();

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
    }
    @Activate
    public void activate() {
        // 激活时创建并启动线程执行 run 方法
        flowManager.setPolymorphicMode(isPolymorphicMode);
        timer.schedule(flowManager, 0, FLOW_INFO_INTERVAL);
//        new Thread(this).start();
    }
    @Deactivate
    public void deactivate() {
        // 清理资源
        timer.cancel();
//        Thread.currentThread().interrupt();
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
        // 这里创建时发生了异常，直接走向了catch
        // 如果服务端未启动的话，就会出现异常
//            socket = new Socket(pythonServer, pythonServerPort);
        // 定期运行FlowInfoProcessor


    }
}
