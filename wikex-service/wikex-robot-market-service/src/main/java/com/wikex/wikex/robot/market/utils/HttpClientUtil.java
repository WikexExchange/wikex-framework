package com.wikex.wikex.robot.market.utils;


import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {
    
    private static String charSet = "UTF-8";
    private static CloseableHttpClient httpClient = null;
    private static CloseableHttpResponse response = null;
    
    static PoolingHttpClientConnectionManager manager = null;
    
    public static synchronized CloseableHttpClient getHttpClient(){

        if(httpClient == null){

            
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", SSLConnectionSocketFactory.getSystemSocketFactory())
                    .build();

            
            HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connectionFactory
              = new ManagedHttpClientConnectionFactory(DefaultHttpRequestWriterFactory.INSTANCE, 
                    DefaultHttpResponseParserFactory.INSTANCE);

            
            DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;

            
            manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry,connectionFactory,dnsResolver);

            
            SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            manager.setDefaultSocketConfig(defaultSocketConfig);

            manager.setMaxTotal(300); 
            
            
            manager.setDefaultMaxPerRoute(200);
            
            manager.setValidateAfterInactivity(5*1000);

            
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectTimeout(2*1000) 
                    .setSocketTimeout(5*1000) 
                    .setConnectionRequestTimeout(2000) 
                    .build();

            
            httpClient = HttpClients.custom()
                    .setConnectionManager(manager)
                    .setConnectionManagerShared(false) 
                    .evictIdleConnections(60, TimeUnit.SECONDS) 
                    .evictExpiredConnections()
                    .setConnectionTimeToLive(60, TimeUnit.SECONDS) 
                    .setDefaultRequestConfig(defaultRequestConfig) 
                    .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE) 
                    .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE) 
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)) 
                    .build();

            
            Runtime.getRuntime().addShutdownHook(new Thread(){
                @Override
                public void run() {
                    try{
                        if(httpClient !=null){
                            httpClient.close();
                        }
                    }catch(IOException e){
                    }
                }
            });
        }
        return httpClient;
    }
    
    public static String doHttpsPost(String url, String jsonStr, Map<String,String> headerPram) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        try {
            httpClient = SSLClient.createSSLClientDefault();
            HttpPost httpPost = new HttpPost(url);
            if(headerPram != null && !headerPram.isEmpty()) {
            	for(Map.Entry<String, String> entry:headerPram.entrySet()){
            		httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            
            StringEntity se = new StringEntity(jsonStr);
            se.setContentType("text/json");
            se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
            httpPost.setEntity(se);
            
            response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    return EntityUtils.toString(resEntity, charSet);
                }
            }
        }finally {
            if(httpClient != null){
                 httpClient.close();
            }
            if(response != null){
                  response.close();
            }
        }
        return null;
    }
    
    public static String doHttpsPost(String url,Map<String,String> param, Map<String,String> headerPram) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException{
        try {
            
            httpClient = SSLClient.createSSLClientDefault();
            
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            
            for(Map.Entry<String, String> entry:param.entrySet()){
                postParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            
            
            HttpPost post = new HttpPost(url);
            if(headerPram != null && !headerPram.isEmpty()) {
            	for(Map.Entry<String, String> entry:headerPram.entrySet()){
            		post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpEntity paramEntity = new UrlEncodedFormEntity(postParams,charSet);
            post.setEntity(paramEntity);
            response = httpClient.execute(post);
            StatusLine status = response.getStatusLine();  
            int state = status.getStatusCode();  
            if (state == HttpStatus.SC_OK) {  
                HttpEntity valueEntity = response.getEntity();
                String content = EntityUtils.toString(valueEntity);
                
                return content;
            }
        }finally{
            if(httpClient != null){
                httpClient.close();
            }
            if(response != null){
                response.close();
            }
        }
        return null;
    }
    
    public static String doHttpPost(String url,Map<String,String> param, Map<String,String> headerPram) throws ClientProtocolException, IOException{
        try {
            
            httpClient = HttpClients.createDefault();
            
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            
            for(Map.Entry<String, String> entry:param.entrySet()){
                postParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            
            
            HttpPost post = new HttpPost(url);
            if(headerPram != null && !headerPram.isEmpty()) {
            	for(Map.Entry<String, String> entry:headerPram.entrySet()){
            		post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            HttpEntity paramEntity = new UrlEncodedFormEntity(postParams,charSet);
            post.setEntity(paramEntity);
            response = httpClient.execute(post);
            StatusLine status = response.getStatusLine();  
            int state = status.getStatusCode();  
            if (state == HttpStatus.SC_OK) {
                HttpEntity valueEntity = response.getEntity();
                String content = EntityUtils.toString(valueEntity);
                return content;
            }else {
            	return null;
            }
        }finally{
            if(httpClient != null){
                httpClient.close();
            }
            if(response != null){
                response.close();
            }
        }
    }
    
     /** 
     * @param url 
     * @param params 
     * @return 
     * @throws IOException 
     * @throws ClientProtocolException 
     */  
    public static String doHttpPost(String url, String jsonStr, Map<String,String> headerPram) throws ClientProtocolException, IOException {  
        try {
            httpClient = HttpClients.createDefault();
          
            
            HttpPost httpPost = new HttpPost(url);     
            if(headerPram != null && !headerPram.isEmpty()) {
            	for(Map.Entry<String, String> entry:headerPram.entrySet()){
            		httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
              
            StringEntity entity = new StringEntity(jsonStr, charSet);  
            entity.setContentType("text/json");
            entity.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
            httpPost.setEntity(entity);          
            
            response = httpClient.execute(httpPost);  
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
                HttpEntity responseEntity = response.getEntity();  
                String jsonString = EntityUtils.toString(responseEntity);  
                return jsonString;  
            }
        }finally {
            if(httpClient != null){
                httpClient.close();
            }
            if(response != null){
                response.close();
            }
        }  
        return null;  
    }  
    
    
    public static String doHttpGet(String url, Map<String,String> param, Map<String,String> headerPram) throws ClientProtocolException, IOException {
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        
        httpclient = getHttpClient();
        if(param != null && !param.isEmpty()) {
            
            List<NameValuePair> getParams = new ArrayList<NameValuePair>();
            for(Map.Entry<String, String> entry:param.entrySet()){
                getParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            url +="?"+EntityUtils.toString(new UrlEncodedFormEntity(getParams), "UTF-8");
        }
        
        HttpGet httpGet = new HttpGet(url);
        RequestConfig rconfig = RequestConfig.custom()
        		.setConnectionRequestTimeout(5000)
        		.setSocketTimeout(10000)
        		.setConnectTimeout(5000)
        		.build();
        httpGet.setConfig(rconfig);
        if(headerPram != null && !headerPram.isEmpty()) {
        	for(Map.Entry<String, String> entry:headerPram.entrySet()){
        		httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        response = httpclient.execute(httpGet);  
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
            return EntityUtils.toString(response.getEntity());  
        }
        return null;
    }
    
    public static String doHttpsGet(String url, Map<String,String> param, Map<String,String> headerPram) throws ClientProtocolException, IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        httpClient = getHttpClient();
        if(param != null && !param.isEmpty()) {
            
            List<NameValuePair> getParams = new ArrayList<NameValuePair>();
            for(Map.Entry<String, String> entry:param.entrySet()){
                getParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            url +="?"+EntityUtils.toString(new UrlEncodedFormEntity(getParams), "UTF-8");
        }
        HttpGet httpGet = new HttpGet(url);
        RequestConfig rconfig = RequestConfig.custom()

        		.setConnectionRequestTimeout(2000)
        		.setSocketTimeout(4000)
        		.setConnectTimeout(3000)
        		.build();
        httpGet.setConfig(rconfig);
        if(headerPram != null && !headerPram.isEmpty()) {
        	for(Map.Entry<String, String> entry:headerPram.entrySet()){
        		httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        response = httpClient.execute(httpGet);
        if (response != null) {
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                return EntityUtils.toString(resEntity, charSet);
            }
        }
        return null;
    }
}