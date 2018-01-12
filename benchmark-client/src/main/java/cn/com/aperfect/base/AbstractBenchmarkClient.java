package cn.com.aperfect.base;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractBenchmarkClient {

    //预热时间30秒
    private static final int WARMUPTIME = 30;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int concurrents;
    private int runTime;
    private String classname;
    private String params;
    private ClientStatistics statistics;

    /**
     * @param concurrents 并发线程数
     * @param runtime     benchmark实际运行时间
     * @param classname   测试的类名
     * @param params      测试String时，指String的size，单位为k
     */
    public void start(int concurrents, int runtime, String classname, String params) {
        this.concurrents = concurrents;
        this.runTime = runtime;
        this.classname = classname;
        this.params = params;

        printStartInfo();

        // prepare runnables
        long currentTime = System.nanoTime() / 1000L;
        long startTime = currentTime + WARMUPTIME * 1000 * 1000L;
        long endTime = currentTime + runTime * 1000 * 1000L;

        List<ClientRunnable> runnables = new ArrayList<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(this.concurrents);
        CountDownLatch countDownLatch = new CountDownLatch(this.concurrents);
        for (int i = 0; i < this.concurrents; i++) {
            ClientRunnable runnable = getClientRunnable(classname, params, cyclicBarrier, countDownLatch, startTime, endTime);
            runnables.add(runnable);
            Thread thread = new Thread(runnable, "benchmarkclient-" + i);
            thread.start();
        }

        //直到所有的并发线程结束后才会唤醒接下来的活动
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //将所有线程的统计数据放到集合中
        List<RunnableStatistics> runnableStatisticses = new ArrayList<>();
        for (ClientRunnable runnable : runnables) {
            runnableStatisticses.add(runnable.getStatistics());
        }
        statistics = new ClientStatistics(runnableStatisticses);
        statistics.collectStatistics();

        printStatistics();

        System.exit(0);
    }

    /**
     * 初始化打印开始信息
     */
    private void printStartInfo() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.SECOND, runTime);
        Date finishDate = calendar.getTime();

        StringBuilder startInfo = new StringBuilder(dateFormat.format(currentDate));
        startInfo.append(" ready to start client benchmark");
        startInfo.append(", concurrent num is ").append(concurrents);
        startInfo.append(", the benchmark will end at ").append(dateFormat.format(finishDate));

        System.out.println(startInfo.toString());
    }

    /**
     * 打印最终的统计结果
     */
    private void printStatistics() {
        System.out.println("----------Benchmark Statistics--------------");
        System.out.println("Concurrents: " + concurrents);
        System.out.println("Runtime: " + runTime + " seconds");
        System.out.println("ClassName: " + classname);
        System.out.println("Params: " + params);
        statistics.printStatistics();
    }

    /**
     * 不同的rpc实现都要重写此方法
     * 比如MotanBenchmarkClient 要通过classname+params获取最终的测试类比如：TestStringRunnable
     * 因为每个rpc实现获取的客户端方式不一样
     *
     * @param classname
     * @param params
     * @param barrier
     * @param latch
     * @param startTime
     * @param endTime
     * @return
     */
    public abstract ClientRunnable getClientRunnable(String classname, String params, CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime);
}
