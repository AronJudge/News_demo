package com.xiangxue.network.dns;

import android.content.Context;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

public class AlibabaDns implements Dns {

    private HttpDnsService httpdns;

    public AlibabaDns(Context context) {
        httpdns = HttpDns.getService(context, "169929");
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        //通过异步解析接口获取ip
        String ip = httpdns.getIpByHostAsync(hostname);
        if (ip != null) {
            //如果ip不为null，直接使用该ip进行网络请求
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            return inetAddresses;
        }
        //如果返回null，走系统DNS服务解析域名
        return Dns.SYSTEM.lookup(hostname);
    }
}
