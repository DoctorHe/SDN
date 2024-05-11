package org.onosproject.mtd.strategy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class test02 implements Runnable{
    @Override
    public void run() {
//        while (true){
//            writeLog(123,123);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
    private <T> void writeLog(T src,T bian){

        long miliseconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(miliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;

        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter("/home/wr/onos_projects/mtd-app/src/main/resources/mtd.log", false));
            writer.println("日期:"+formattedDate+"\t"+"frist:"+ src+"second:" +bian);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }

}
