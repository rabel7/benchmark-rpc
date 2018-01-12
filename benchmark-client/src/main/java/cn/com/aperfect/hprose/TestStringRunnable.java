

package cn.com.aperfect.hprose;

import cn.com.aperfect.base.AbstractClientRunnable;
import cn.com.aperfect.benchmark.BenchmarkService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class TestStringRunnable extends AbstractClientRunnable {

    private String str;

    public TestStringRunnable(BenchmarkService service, String size, CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        super(service, barrier, latch, startTime, endTime);

        initString(Integer.parseInt(size));
    }

    /**
     * 根据size初始化字符串
     *
     * @param size size为1表示1kb的字符串
     */
    private void initString(int size) {
        int length = 1024 * size;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
        }

        this.str = builder.toString();
    }

    @Override
    protected Object call(BenchmarkService service) {
        Object result = null;
        try {
            result = service.echoService(str);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        int length = 1024 * 100;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
        }
        System.out.println(builder.toString());
    }
}
