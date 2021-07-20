package cn.inkroom.study.cloud.eureka.server;

import com.netflix.appinfo.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author inkbox
 * @date 2021/7/20
 */
@Component
public class ShutdownTask extends Thread implements InitializingBean {

    private DelayQueue<DelayInstanceInfo> queue;//记录需要被关机的实例
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private InstanceFacade instanceFacade;
    @Value("${shutdown.delay}")
    private int delay;
    /**
     * 用于存放id列表，避免重复入队
     */
    private Set<String> ids;

    public ShutdownTask() {
        this.queue = new DelayQueue<>();
        ids = new HashSet<>();
    }

    /**
     * 用于将OUT_OF_SERVICE的实例关机
     *
     * @param event
     */
    @EventListener
    public void shutdown(EurekaInstanceRenewedEvent event) {
        if (event != null && event.getInstanceInfo() != null && event.getInstanceInfo().getStatus() == InstanceInfo.InstanceStatus.OUT_OF_SERVICE) {
            //这里就主动将其剔除，给对应机器发送关机指令
            this.offer(event.getInstanceInfo(), System.currentTimeMillis() + delay * 1000L);//开发环境暂定30秒后关机
        }
    }

    @Override
    public void run() {
        while (true) {
            DelayInstanceInfo instance = queue.poll();
            if (instance == null) continue;
            //首先检测该节点是否还存在
            if (instanceFacade.aliveAndOutOfService(instance.info) == Boolean.TRUE) {
                //可以关机了
                if (!instanceFacade.shutdown(instance.info)) {//关机失败，继续入队
                    instance.delay += delay * 1000L;
                    queue.offer(instance);
                    logger.debug("{}关机失败", instance.info);
                } else {
                    logger.debug("{}关机成功", instance.info);
                    ids.remove(instance.info.getInstanceId());
                }
            } else {
                logger.debug("{}不存在", instance.info);
            }
        }
    }

    public void offer(InstanceInfo instanceInfo, long delay) {
        if (!ids.contains(instanceInfo.getInstanceId())) {
            queue.offer(new DelayInstanceInfo(instanceInfo, delay));
            ids.add(instanceInfo.getInstanceId());

            logger.debug("{}入队，准备关机", instanceInfo.getInstanceId());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setName("shutdown-thread");
        this.start();
    }

    private class DelayInstanceInfo implements Delayed {
        private InstanceInfo info;
        private long delay;//执行任务的时间

        public DelayInstanceInfo(InstanceInfo info, long delay) {
            this.info = info;
            this.delay = delay;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(delay - System.currentTimeMillis(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) return 0;
            if (o instanceof DelayInstanceInfo) {
                if (this.delay > ((DelayInstanceInfo) o).delay) {
                    return 1;
                }
                return -1;
            }
            return 0;
        }
    }
}
