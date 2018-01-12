package cn.com.aperfect.motan;

import cn.com.aperfect.base.AbstractBenchmarkClient;
import cn.com.aperfect.base.ClientRunnable;
import cn.com.aperfect.benchmark.BenchmarkService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MotanBenchmarkClient extends AbstractBenchmarkClient {

    /**
     * 并发的Runable线程，是否使用相同的client进行调用。
     * true：并发线程只使用一个client（bean实例）调用服务。
     * false: 每个并发线程使用不同的Client调用服务
     */
    private static BenchmarkService benchmarkService;

    /**
     * 测试入口函数,由main传参进入
     *
     * @param args
     */
    public static void main(String[] args) {
        int concurrents = 1000;
        int runtime = 90;
        String classname = "cn.com.aperfect.motan.TestStringRunnable";
        String params = "1";

        if (args.length == 5) {
            concurrents = Integer.parseInt(args[0]);
            runtime = Integer.parseInt(args[1]);
            classname = args[2];
            params = args[3];
        }

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[]{"classpath*:motan-benchmark-client.xml"});
        benchmarkService = (BenchmarkService) applicationContext.getBean("motanBenchmarkReferer");

        //start test
        new MotanBenchmarkClient().start(concurrents, runtime, classname, params);
    }

    @Override
    public ClientRunnable getClientRunnable(String classname, String params, CyclicBarrier barrier,
                                            CountDownLatch latch, long startTime, long endTime) {

        Class[] parameterTypes = new Class[]{BenchmarkService.class, String.class, CyclicBarrier.class,
                CountDownLatch.class, long.class, long.class};

        Object[] parameters = new Object[]{benchmarkService, params, barrier, latch, startTime, endTime};

        ClientRunnable clientRunnable = null;
        try {
            clientRunnable = (ClientRunnable) Class.forName(classname).getConstructor(parameterTypes).newInstance(parameters);
        } catch (InstantiationException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getTargetException();
        }

        return clientRunnable;
    }
}
