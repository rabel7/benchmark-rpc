package cn.com.aperfect.hrpose;

import cn.com.aperfect.benchmark.BenchmarkService;
import hprose.client.HproseClient;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by daive on 2016/9/4.
 */
public class TEST {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final HproseClient client = HproseClient.create("tcp://127.0.0.1:4321");
        BenchmarkService test = client.useService(BenchmarkService.class);
        System.out.println(test.echoService("1"));
    }
}
