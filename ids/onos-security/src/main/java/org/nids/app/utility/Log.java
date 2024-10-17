package org.nids.app.utility;

import org.onosproject.net.flow.FlowEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Log {

    public static String logFolderPath = System.getProperty("user.home") + "/ids_log";
    public static String logPath = logFolderPath + "/ids.log";
    public static void writeFlowLog(List<FlowEntry> src) {

        long millisSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(millisSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(logPath, true));
            PrintWriter finalWriter = writer;
            src.forEach((flowEntry) -> finalWriter.println("date:" + formattedDate + "\t" + " flows:" + flowEntry.toString())
            );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                // 关闭写入流
                writer.close();
            }
        }
    }
    public static <T> void writeLog(T elem1){

        long milliSeconds = System.currentTimeMillis(); // 获取事件发生时间
        Date d1 = new Date(milliSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(d1);
        PrintWriter writer = null;
        File folder = new File(logFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(logPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            // 创建日志文件的PrintWriter对象
            writer = new PrintWriter(new FileWriter(logPath, true));
            writer.println("date:" + formattedDate + "\t" + "info:" + elem1);
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
