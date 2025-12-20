package dhr.agent.data;

import java.util.Random;

/**
 * IP地址跳变执行体
 * 实现了IP地址跳变策略
 */
public class IpJumpExecutor implements JumpStrategyExecutor {
    private boolean running;
    private int weight;
    private String identifier;
    private static final String TYPE = "ip";
    
    /**
     * 默认构造函数
     */
    public IpJumpExecutor() {
        this.weight = 10; // 默认权重
        this.identifier = "ip_" + System.currentTimeMillis();
    }
    
    /**
     * 带参数构造函数
     * @param weight 权重
     * @param identifier 标识
     */
    public IpJumpExecutor(int weight, String identifier) {
        this.weight = weight;
        this.identifier = identifier;
    }
    
    @Override
    public void execute() {
        // 实现IP地址跳变逻辑
        System.out.println("执行IP地址跳变策略 - " + identifier);
        // 模拟IP跳变过程
        String newIp = generateRandomIp();
        System.out.println("生成新IP地址: " + newIp);
        // 这里可以添加实际的IP跳变实现
        running = true;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
    
    @Override
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }
    
    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    @Override
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * 生成随机IP地址
     * @return 随机IP地址
     */
    private String generateRandomIp() {
        Random random = new Random();
        // 指定IP范围
        int[][] range = {
                {607649792, 608174079}, // 36.56.0.0-36.63.255.255
                {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
                {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
                {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
                {2078801920, 2079064063}  // 123.232.0.0-123.235.255.255
        };
        
        int index = random.nextInt(range.length);
        int ipNum = range[index][0] + random.nextInt(range[index][1] - range[index][0]);
        return num2ip(ipNum);
    }
    
    /**
     * 将十进制转换为IP地址
     * @param ip 十进制IP
     * @return IP地址字符串
     */
    private String num2ip(int ip) {
        int[] b = new int[4];
        b[0] = (ip >> 24) & 0xff;
        b[1] = (ip >> 16) & 0xff;
        b[2] = (ip >> 8) & 0xff;
        b[3] = ip & 0xff;
        return b[0] + "." + b[1] + "." + b[2] + "." + b[3];
    }
}
