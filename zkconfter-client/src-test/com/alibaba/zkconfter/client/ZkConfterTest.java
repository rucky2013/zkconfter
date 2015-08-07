package com.alibaba.zkconfter.client;

import org.junit.Test;

/**
 * Created by pinian.lpn on 2015/8/7.
 */
public class ZkConfterTest {

    @Test
    public void testZkConfter() throws Exception {
        ZkConfter zkConfter = new ZkConfter("conf/zkconfter.properties");
        zkConfter.syncZkConfter();
    }
}
