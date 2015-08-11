package com.alibaba.zkconfter.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.zkconfter.client.util.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by pinian.lpn on 2015/8/7.
 */
public class ZkConfterTest {

    @Test
    public void testZkConfter() throws Exception {
        ZkConfter zkConfter = new ZkConfter("conf/zkconfter.properties");
        zkConfter.afterPropertiesSet();

        ZkClient zkClient = zkConfter.getZkClient();
        String zkDrmAttribute = "/zkconfter/zkconfter-client-test/drm/dev/com.alibaba.zkconfter.client.drm.DRMResourceTest/testInt";

        JSONObject dataAttribute = JSON.parseObject(zkClient.readData(zkDrmAttribute).toString());
        dataAttribute.put("value", 1);
        zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);


        TimeUnit.MINUTES.sleep(2);
    }
}
