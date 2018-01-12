package cn.com.aperfect.hrpose;

import cn.com.aperfect.motan.BenchmarkServiceImpl;
import hprose.server.HproseTcpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HproseBenchmarkServer {

    public static void main(String[] args) throws Exception {
        HproseTcpServer server = new HproseTcpServer("tcp://localhost:4321");
        server.add("echoService", new BenchmarkServiceImpl());
        server.start();
        System.out.println("hprose server running---");
        Thread.sleep(Long.MAX_VALUE);
    }

}
