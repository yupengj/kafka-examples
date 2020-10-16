package org.jiangyp.kafka.stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MergeTableStreamApplicationTest {

    MergeTableStreamApplication mergeTableStreamApplication = new MergeTableStreamApplication();


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void startApplication() {
        mergeTableStreamApplication.startApplication();
    }


    @Test
    public void shutdown() {
    }

    @Test
    public void monitor() {
    }
}
