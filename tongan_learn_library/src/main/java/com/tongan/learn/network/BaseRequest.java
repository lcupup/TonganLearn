package com.tongan.learn.network;

import android.text.TextUtils;
import android.util.Log;

import com.tongan.learn.TaConstant;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class BaseRequest {
    private static final String BOUNDARY = java.util.UUID.randomUUID().toString();
    private static final String TWO_HYPHENS = "--";
    private static final String LINE_END = "\r\n";


    /**
     * get请求
     */
    BaseResponse getData(String requestURL, Map<String, String> headerMap) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection(requestURL, "GET");
            if (headerMap != null) {
                setHeader(conn, headerMap);
            }
            conn.connect();
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptonResponse(conn, e);
        }
    }

    /**
     * post请求
     */
    BaseResponse postData(String requestURL, String body, String bodyType, Map<String, String> headerMap) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection(requestURL, "POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "text/plain");
            Map<String, String> commonHeader = new HashMap<>();
            commonHeader.put("Accept", "application/json");
            setHeader(conn, commonHeader);
            if (!TextUtils.isEmpty(bodyType)) {
                conn.setRequestProperty("Content-Type", bodyType);
            }
            if (headerMap != null) {
                setHeader(conn, headerMap);
            }
            conn.connect();
            if (!TextUtils.isEmpty(body)) {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                writer.write(body);
                writer.close();
            }
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptonResponse(conn, e);
        }
    }

    /**
     * 上传文件
     */
    BaseResponse uploadFile(String requestURL, File file, List<File> fileList, Map<String, File> fileMap, String fileKey, String fileType, Map<String, String> paramsMap, Map<String, String> headerMap, BaseCallBack callBack) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection(requestURL, "POST");
            setUpLoadConnection(conn);
            conn.setRequestProperty("Cookie", TaConstant.jsessionId);

            if (headerMap != null) {
                setHeader(conn, headerMap);
            }
            conn.connect();
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            if (paramsMap != null) {
                outputStream.write(getParamsString(paramsMap).getBytes());//上传参数
                outputStream.flush();
            }
            if (file != null) {
                //上传文件
                writeFile(file, fileKey, fileType, outputStream, callBack);
            } else if (fileList != null) {
                for (File f : fileList) {
                    writeFile(f, fileKey, fileType, outputStream, null);
                }
            } else if (fileMap != null) {
                for (String key : fileMap.keySet()) {
                    writeFile(fileMap.get(key), key, fileType, outputStream, null);
                }
            }
            byte[] endData = (LINE_END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END).getBytes();//写结束标记位
            outputStream.write(endData);
            outputStream.flush();
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptonResponse(conn, e);
        }
    }

    /**
     * 得到Connection对象，并进行一些设置
     */
    private HttpURLConnection getHttpURLConnection(String requestURL, String requestMethod) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10 * 1000);
        conn.setReadTimeout(15 * 1000);
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Cookie", TaConstant.jsessionId);
        return conn;
    }

    /**
     * 设置请求头
     */
    private void setHeader(HttpURLConnection conn, Map<String, String> headerMap) {
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                conn.setRequestProperty(key, headerMap.get(key));
            }
        }
    }

    /**
     * 上传文件时设置Connection参数
     */
    private void setConnection(HttpURLConnection conn) throws ProtocolException {
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Accept", "application/json");
    }

    /**
     * 上传文件时设置Connection参数
     */
    private void setUpLoadConnection(HttpURLConnection conn) throws ProtocolException {
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "text/plain");
//        conn.setRequestProperty("Connection","close");

    }

    /**
     * 上传文件时得到拼接的参数字符串
     */
    private String getParamsString(Map<String, String> paramsMap) {
        StringBuffer strBuf = new StringBuffer();
        for (String key : paramsMap.keySet()) {
            strBuf.append(TWO_HYPHENS);
            strBuf.append(BOUNDARY);
            strBuf.append(LINE_END);
            strBuf.append("Content-Disposition: form-data; name=\"" + key + "\"");
            strBuf.append(LINE_END);
            strBuf.append("Content-Type: " + "text/plain");
            strBuf.append(LINE_END);
            strBuf.append("Content-Lenght: " + paramsMap.get(key).length());
            strBuf.append(LINE_END);
            strBuf.append(LINE_END);
            strBuf.append(paramsMap.get(key));
            strBuf.append(LINE_END);
        }
        return strBuf.toString();
    }

    /**
     * 上传文件时写文件
     */
    private void writeFile(File file, String fileKey, String fileType, DataOutputStream outputStream, final BaseCallBack callBack) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        final long total = file.length();
        long sum = 0;
        byte[] buffer = new byte[1024 * 2];
        int length = -1;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
            sum = sum + length;
            if (callBack != null) {
                final long finalSum = sum;
                BaseCallBack.mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onProgress(finalSum * 100.0f / total, total);
                    }
                });
            }
        }
        outputStream.flush();
        inputStream.close();
    }

    /**
     * 上传文件时得到一定格式的拼接字符串
     */
    private String getFileParamsString(File file, String fileKey, String fileType) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(LINE_END);
        strBuf.append(TWO_HYPHENS);
        strBuf.append(BOUNDARY);
        strBuf.append(LINE_END);
        strBuf.append("Content-Disposition: form-data; name=\"" + fileKey + "\"; filename=\"" + file.getName() + "\"");
        strBuf.append(LINE_END);
        strBuf.append("Content-Type: " + fileType);
        strBuf.append(LINE_END);
        strBuf.append("Content-Lenght: " + file.length());
        strBuf.append(LINE_END);
        strBuf.append(LINE_END);
        return strBuf.toString();
    }

    /**
     * 当正常返回时，得到Response对象
     */
    private BaseResponse getRealResponse(HttpURLConnection conn) throws IOException {
        BaseResponse response = new BaseResponse();
        response.contentLength = conn.getContentLength();
        response.inputStream = conn.getInputStream();
        response.errorStream = conn.getErrorStream();
        response.code = conn.getResponseCode();
        return response;
    }

    /**
     * 当发生异常时，得到Response对象
     */
    private BaseResponse getExceptonResponse(HttpURLConnection conn, Exception e) {
        if (conn != null) {
            conn.disconnect();
        }
        e.printStackTrace();
        BaseResponse response = new BaseResponse();
        try {
            response.code = conn.getResponseCode();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        response.exception = e;
        return response;
    }

}
