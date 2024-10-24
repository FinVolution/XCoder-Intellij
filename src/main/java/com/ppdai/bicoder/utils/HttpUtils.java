package com.ppdai.bicoder.utils;

import com.ppdai.bicoder.config.PluginStaticConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * http工具类
 *
 */
public class HttpUtils {

    public static String doPost(String url, String body, Map<String, Object> header) {
        CloseableHttpClient httpClient;
        CloseableHttpResponse httpResponse = null;
        String result;

        // 创建httpClient实例
        httpClient = HttpClients.createDefault();

        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        // 设置连接主机服务超时时间
        requestBuilder.setConnectTimeout(PluginStaticConfig.REQUEST_CONNECT_TIMEOUT)
                // 设置读取数据连接超时时间
                .setSocketTimeout(PluginStaticConfig.REQUEST_SOCKET_TIMEOUT);
        RequestConfig requestConfig = requestBuilder.build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        if (header != null && header.size() > 0) {
            header.forEach((k, v) -> httpPost.setHeader(k, v.toString()));
        }
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        // 封装post请求参数
        try {
            BiCoderLoggerUtils.getInstance(HttpUtils.class).info("doPost, url:" + url + ", body:" + body);
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (IOException e) {
            BiCoderLoggerUtils.getInstance(HttpUtils.class).warn("doPost error, url:" + url + ", error info: " + e);
            return null;
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    BiCoderLoggerUtils.getInstance(HttpUtils.class).warn("close httpResponse error, url:" + url + ", error info: " + e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    BiCoderLoggerUtils.getInstance(HttpUtils.class).warn("close httpClient error, url:" + url + ", error info: " + e);
                }
            }
        }
        return result;
    }
}
