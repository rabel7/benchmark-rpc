package cn.com.aperfect.hprose;

import cn.com.aperfect.base.AbstractBenchmarkClient;
import cn.com.aperfect.base.ClientRunnable;
import cn.com.aperfect.benchmark.BenchmarkService;
import hprose.client.HproseClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class HproseBenchmarkClient extends AbstractBenchmarkClient {


    private static BenchmarkService benchmarkService;

    /**
     * 测试入口函数,由main传参进入
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        int concurrents = 1000;
        int runtime = 90;
        String classname = "cn.com.aperfect.hprose.TestStringRunnable";
        String params = "1";

        if (args.length == 5) {
            concurrents = Integer.parseInt(args[0]);
            runtime = Integer.parseInt(args[1]);
            classname = args[2];
            params = args[3];
        }

        final HproseClient client = HproseClient.create("tcp://127.0.0.1:4321");
        benchmarkService = client.useService(BenchmarkService.class);

        new HproseBenchmarkClient().start(concurrents, runtime, classname, params);
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
