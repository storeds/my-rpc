package com.rpc.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @program: My-Rpc
 * @author: cx
 * @create: 2022-02-28 21:30
 * @description: 创建 ThreadPool(线程池) 的工具类
 **/
public class ThreadPoolFactory {

    /**
     * 线程池参数
     */
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);
    private static Map<String, ExecutorService> threadPollsMap = new ConcurrentHashMap<>();

    private ThreadPoolFactory() {
    }

    /**
     * 创建默认线程池
     * @param threadNamePrefix
     * @return
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    /**
     * 线程池的判断
     * @param threadNamePrefix
     * @param daemon
     * @return
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        ExecutorService pool = threadPollsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));

        // 判断线程池的状态，如果是处于关闭和终止状态就移除这个map的值
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPollsMap.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPollsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }


    /**
     * 真正创建线程池
     * @param threadNamePrefix
     * @param daemon
     * @return
     */
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 设置阻塞队列的长度，这里选择的是ArrayBlockingQueue有界阻塞队列
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);

        // 返回创建好的线程池
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建线程工厂
     * @param threadNamePrefix
     * @param daemon
     * @return
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {

        // 带有线程名前缀，返回工厂
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        // 如果没有线程名前缀就使用默认的线程工厂
        return Executors.defaultThreadFactory();
    }

    /**
     * 关闭线程池
     */
    public static void shutDownAll() {
        logger.info("关闭所有线程池...");
        threadPollsMap.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            logger.info("关闭线程池 [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ie) {
                logger.error("关闭线程池失败！");
                executorService.shutdownNow();
            }
        });
    }

}
