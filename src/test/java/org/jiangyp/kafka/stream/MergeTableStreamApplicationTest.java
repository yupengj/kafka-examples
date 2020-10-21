package org.jiangyp.kafka.stream;

import org.jiangyp.kafka.KafkaConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;


public class MergeTableStreamApplicationTest {

    MergeTableStreamApplication mergeTableStreamApplication = new MergeTableStreamApplication();

    @Before
    public void setUp() throws Exception {
        mergeTableStreamApplication.setBootstrapServer(KafkaConfig.BOOTSTRAP_SERVERS_CONFIG);
    }


    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void startStreamApplication() {
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                mergeTableStreamApplication.closeStreamApplication();
                latch.countDown();
            }
        });
        try {
            mergeTableStreamApplication.startStreamApplication();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    @Test
    public void closeStreamApplication() {
    }

    @Test
    public void monitor() {
    }
}
