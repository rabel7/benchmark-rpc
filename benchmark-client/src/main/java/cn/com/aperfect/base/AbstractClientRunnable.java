package cn.com.aperfect.base;

import cn.com.aperfect.benchmark.BenchmarkService;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public abstract class AbstractClientRunnable implements ClientRunnable {

    RunnableStatistics statistics;
    private CyclicBarrier cyclicBarrier;
    private CountDownLatch countDownLatch;
    private long startTime;
    private long endTime;
    private int statisticTime;
    private BenchmarkService benchmarkService;

    /**
     * @param benchmarkService
     * @param barrier
     * @param latch
     * @param startTime        总的开始时间
     * @param endTime          总的结束时间
     */
    public AbstractClientRunnable(BenchmarkService benchmarkService, CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        this.cyclicBarrier = barrier;
        this.countDownLatch = latch;
        this.startTime = startTime;
        this.endTime = endTime;
        this.benchmarkService = benchmarkService;

        statisticTime = (int) ((endTime - startTime) / 1000000);
        statistics = new RunnableStatistics(statisticTime);
    }

    @Override
    public RunnableStatistics getStatistics() {
        return statistics;
    }

    @Override
    public void run() {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        callService();
        countDownLatch.countDown();
    }

    /**
     * 1.先预热直到startTime
     * 2.开始执行测试并计算执行时间（80-85行）判断是否执行成功，如果失败算入失败数
     */
    private void callService() {
        long beginTime = System.nanoTime() / 1000L;
        while (beginTime <= startTime) {
            // warm up
            beginTime = System.nanoTime() / 1000L;
            call(benchmarkService);
        }
        while (beginTime <= endTime) {
            beginTime = System.nanoTime() / 1000L;
            Object result = call(benchmarkService);
            long responseTime = System.nanoTime() / 1000L - beginTime;

            collectResponseTimeDistribution(responseTime);

            int currTime = (int) ((beginTime - startTime) / 1000000L);
            if (currTime >= statisticTime) {
                continue;
            }
            if (result != null) {  //success
                statistics.TPS[currTime]++;
                statistics.RT[currTime] += responseTime;
            } else {
                statistics.errTPS[currTime]++;
                statistics.errRT[currTime] += responseTime;
            }
        }
    }


    /**
     * 计算返回时间区间
     *
     * @param time 执行时间
     */
    private void collectResponseTimeDistribution(long time) {
        double responseTime = (double) (time / 1000L);
        if (responseTime >= 0 && responseTime <= 1) {  // [0,1]
            statistics.above0sum++;
        } else if (responseTime > 1 && responseTime <= 5) {  // [1,5]
            statistics.above1sum++;
        } else if (responseTime > 5 && responseTime <= 10) {  // (5,10]
            statistics.above5sum++;
        } else if (responseTime > 10 && responseTime <= 50) {  // (10,50]
            statistics.above10sum++;
        } else if (responseTime > 50 && responseTime <= 100) {  // (50,100]
            statistics.above50sum++;
        } else if (responseTime > 100 && responseTime <= 500) { // (100,500]
            statistics.above100sum++;
        } else if (responseTime > 500 && responseTime <= 1000) { // (500,1000]
            statistics.above500sum++;
        } else if (responseTime > 1000) { // > 1000
            statistics.above1000sum++;
        }
    }

    /**
     * 由具体的实现类实现具体的调用
     *
     * @param benchmarkService
     * @return
     */
    protected abstract Object call(BenchmarkService benchmarkService);

}
