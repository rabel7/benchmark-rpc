package cn.com.aperfect.motan;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MotanBenchmarkServer {

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[]{"classpath*:motan-benchmark-server.xml"});

        System.out.println("motan server running---");
        Thread.sleep(Long.MAX_VALUE);
    }

}
