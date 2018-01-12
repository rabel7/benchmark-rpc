package cn.com.aperfect.base;

import java.text.MessageFormat;
import java.util.List;

/**
 * 客户端的统计,由每个线程的clientStatistics统计而来
 */
public class ClientStatistics {
    public int statisticTime;
    public long above0sum;      // [0,1]
    public long above1sum;      // (1,5]
    public long above5sum;      // (5,10]
    public long above10sum;     // (10,50]
    public long above50sum;     // (50,100]
    public long above100sum;    // (100,500]
    public long above500sum;    // (500,1000]
    public long above1000sum;   // > 1000

    public long maxTPS = 0;
    public long minTPS = 0;
    public long successTPS = 0;
    public long successRT = 0;
    public long errTPS = 0;
    public long errRT = 0;
    public long allTPS = 0;
    public long allRT = 0;

    public List<RunnableStatistics> statistics;

    public ClientStatistics(List<RunnableStatistics> statistics) {
        this.statistics = statistics;
        statisticTime = statistics.get(0).statisticTime;
    }

    /**
     * 汇总计算所有线程的统计结果
     */
    public void collectStatistics() {
        for (RunnableStatistics statistic : statistics) {
            above0sum += statistic.above0sum;
            above1sum += statistic.above1sum;
            above5sum += statistic.above5sum;
            above10sum += statistic.above10sum;
            above50sum += statistic.above50sum;
            above100sum += statistic.above100sum;
            above500sum += statistic.above500sum;
            above1000sum += statistic.above1000sum;

            long runnableTPS = 0;
            for (int i = 0; i < statistic.statisticTime; i++) {
                runnableTPS += (statistic.TPS[i] + statistic.errTPS[i]);
                successTPS += statistic.TPS[i];
                successRT += statistic.RT[i];
                errTPS += statistic.errTPS[i];
                errRT += statistic.errRT[i];
            }
            if (runnableTPS > maxTPS) {
                maxTPS = runnableTPS;
            }
            if (runnableTPS < minTPS || minTPS == 0) {
                minTPS = runnableTPS;
            }
        }
        allTPS = successTPS + errTPS;
        allRT = successRT + errRT;
    }

    /**
     * 打印最终的统计信息
     */
    public void printStatistics() {
        System.out.println("Benchmark Run Time: " + statisticTime);
        System.out.println(MessageFormat.format("Requests: {0}, Success: {1}%({2}), Error: {3}%({4})", allTPS, successTPS * 100 / allTPS, successTPS, errTPS * 100 / allTPS, errTPS));
        System.out.println(MessageFormat.format("Avg TPS: {0}, Max TPS: {1}, Min TPS: {2}", (allTPS / statisticTime), maxTPS, minTPS));
        System.out.println(MessageFormat.format("Avg ResponseTime: {0}ms", allRT / allTPS / 1000f));

        System.out.println(MessageFormat.format("RT [0,1]: {0}% {1}/{2}", above0sum * 100 / allTPS, above0sum, allTPS));
        System.out.println(MessageFormat.format("RT (1,5]: {0}% {1}/{2}", above1sum * 100 / allTPS, above1sum, allTPS));
        System.out.println(MessageFormat.format("RT (5,10]: {0}% {1}/{2}", above5sum * 100 / allTPS, above5sum, allTPS));
        System.out.println(MessageFormat.format("RT (10,50]: {0}% {1}/{2}", above10sum * 100 / allTPS, above10sum, allTPS));
        System.out.println(MessageFormat.format("RT (50,100]: {0}% {1}/{2}", above50sum * 100 / allTPS, above50sum, allTPS));
        System.out.println(MessageFormat.format("RT (100,500]: {0}% {1}/{2}", above100sum * 100 / allTPS, above100sum, allTPS));
        System.out.println(MessageFormat.format("RT (500,1000]: {0}% {1}/{2}", above500sum * 100 / allTPS, above500sum, allTPS));
        System.out.println(MessageFormat.format("RT >1000: {0}% {1}/{2}", above1000sum * 100 / allTPS, above1000sum, allTPS));
    }
}
