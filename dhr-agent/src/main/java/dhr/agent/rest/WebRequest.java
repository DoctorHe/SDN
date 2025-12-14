package dhr.agent.rest;


import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
public class WebRequest {
    private static String IP = "192.168.174.130";

    public WebRequest() {
    }
    public WebRequest(String ip) {
        IP = ip;
    }
    private static String getAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        return new String(Base64.getEncoder().encode(auth.getBytes()));
    }

    // 启动ONOS应用
    public static void activateApplication(String app_id) {
        try {
            String ONOS_REST_API_URL = String.format("http://%s:8181/onos/v1/applications/%s/active", IP, app_id);
            // 创建连接
            URL url = new URL(ONOS_REST_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法
            connection.setRequestMethod("POST");

            // 设置请求头 (例如 Basic Authentication)
//            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Basic " + getAuthHeader("onos", "rocks"));

            // 启用应用的参数数据
//            String data = "{\"appId\": \"" + APP_ID + "\"}";

            // 允许输出流
//            connection.setDoOutput(true);

            // 写入数据
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = data.getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }

            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Application " + app_id + " successfully enabled.");
            } else {
                System.out.println("Failed to enable application. HTTP response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deactivateApplication(String app_id) {
        try {
            String ONOS_REST_API_URL = String.format("http://%s:8181/onos/v1/applications/%s/active", IP, app_id);
            // 创建连接
            URL url = new URL(ONOS_REST_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法
            connection.setRequestMethod("DELETE");

            // 设置请求头 (例如 Basic Authentication)
            connection.setRequestProperty("Authorization", "Basic " + getAuthHeader("onos", "rocks"));


            // 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Application " + app_id + " successfully enabled.");
            } else {
                System.out.println("Failed to enable application. HTTP response code: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}