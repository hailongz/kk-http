package cn.kkmofang.http;

import android.content.Context;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by hailong11 on 2018/3/13.
 */

public class HttpOptions {


    public final static String TYPE_TEXT = "text";
    public final static String TYPE_JSON = "json";
    public final static String TYPE_DATA = "data";
    public final static String TYPE_URI = "uri";
    public final static String TYPE_IMAGE = "image";

    public final static String METHOD_GET = "GET";
    public final static String METHOD_POST = "POST";

    public String url;
    public String method;
    public Object data;
    public Map<String,Object> headers;
    public String type;
    public long timeout;

    public OnLoad onload;
    public OnFail onfail;
    public OnResponse onresponse;
    public OnProcess onprocess;

    private String _absoluteUrl;

    public String absoluteUrl() {
        if(_absoluteUrl == null && url != null) {

            if((TYPE_URI.equals(type) || TYPE_IMAGE.equals(type) || METHOD_GET.equals(method))
                    && (data != null && data instanceof Map)) {
                StringBuffer query = new StringBuffer();
                Map<String,Object> m = (Map<String,Object>) data;
                for(String key : m.keySet()) {
                    String v = stringValue(m.get(key),"");
                    if(query.length() !=0) {
                        query.append("&");
                    }
                    query.append(key);
                    query.append("=");
                    query.append(encodeURI(v));
                }
                if(url.endsWith("?")) {
                    _absoluteUrl = url + query.toString();
                } else if(url.contains("?")) {
                    _absoluteUrl = url + "&" + query.toString();
                } else {
                    _absoluteUrl = url + "?" + query.toString();
                }
            } else {
                _absoluteUrl = url;
            }

        }
        return _absoluteUrl;
    }

    private String _key;

    public String key() {
        if(_key == null && (TYPE_URI.equals(type) || TYPE_IMAGE.equals(type)) && url != null) {
            _key = cacheKey(absoluteUrl());
        }
        return _key;
    }


    public static interface OnLoad {
        void on(Object data,Exception error,Object weakObject);
    }

    public static interface OnFail {
        void on(Exception error,Object weakObject);
    }

    public static interface OnResponse {
        void on(int code,String status,Map<String,Object> headers,Object weakObject);
    }

    public static interface OnProcess {
        void on(long value,long maxValue,Object weakObject);
    }


    public static String stringValue(Object value ,String defaultValue) {

        if(value == null) {
            return defaultValue;
        }

        if(value instanceof String) {
            return (String) value;
        }

        return value.toString();
    }

    public static String encodeURI(String value) {
        try {
            return URLEncoder.encode(value,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String decodeURI(String value) {
        try {
            return URLDecoder.decode(value,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String path(Context context, String uri) {

        if(uri == null) {
            return null;
        }

        if(uri.startsWith("document://")) {
            return (new File(context.getDir("document",0777),uri.substring(11))).getAbsolutePath();
        }

        if(uri.startsWith("app://")) {
            return (new File(context.getDir("app",0777),uri.substring(6))).getAbsolutePath();
        }

        if(uri.startsWith("cache://")) {
            return (new File(context.getCacheDir(),uri.substring(6))).getAbsolutePath();
        }

        return uri;
    }

    public static String cacheKey(String uri) {

        if(uri == null) {
            return null;
        }

        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");

            MD5.update(uri.getBytes("UTF-8"));
            byte[] bytes = MD5.digest();

            StringBuffer sb = new StringBuffer();

            sb.append("kk-");

            for(int i=0;i<bytes.length;i++) {
                String v = Integer.toHexString(bytes[i]);
                if(v.length() == 1) {
                    sb.append("0").append(v);
                } else {
                    sb.append(v);
                }
            }

        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }

        return uri;
    }

    public static String cachePath(Context context,String uri) {
        return path(context,"cache://" + cacheKey(uri));
    }

    public static String cacheTmpPath(Context context,String uri) {
        return path(context,"cache://" + cacheKey(uri) + ".tmp");
    }


}
