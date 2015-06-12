package com.alibaba.zkconfter.client;

import com.alibaba.zkconfter.client.util.SysConstant;
import com.alibaba.zkconfter.client.util.ZkClient;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;


/**
 * Created by pinian.lpn on 2015/1/15.
 */
public class ZkConfter implements InitializingBean {

    private final static Logger logger = Logger.getLogger(ZkConfter.class);

    private final static String DEFAULT_ZKCONFTER_FILE = "zkconfter.properties";
    private final static String ZK_ROOT = "/zkconfter/";

    private ZkClient zkClient;
    private Resource configLocation;
    private List<String> filePathList;

    private String appName;
    private String root;
    private String runtime;

    public ZkConfter() {

    }

    public ZkConfter(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public ZkConfter(String zkConfterFile) {
        try {
            this.configLocation = new ClassPathResource(zkConfterFile);

            if (!this.configLocation.exists()) {
                this.configLocation = new FileSystemResource(zkConfterFile);

                if (!this.configLocation.exists()) {
                    this.configLocation = new UrlResource(zkConfterFile);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //获取配置文件数据
        Properties zkProps = new Properties();
        if (configLocation == null || !configLocation.exists()) {
            zkProps.load(this.getClass().getClassLoader().getResourceAsStream(DEFAULT_ZKCONFTER_FILE));
        } else {
            zkProps.load(configLocation.getInputStream());
        }

        String zkServers = zkProps.getProperty(SysConstant.ZK_SERVERS, "127.0.0.1;:2181");
        appName = zkProps.getProperty(SysConstant.APP_NAME);
        root = zkProps.getProperty(SysConstant.ROOT);
        runtime = zkProps.getProperty(SysConstant.RUNTIME);

        //验证配置项
        if (StringUtils.isEmpty(appName))
            throw new NullPointerException("Property zkconfter.appName cannot be null.");
        if (StringUtils.isEmpty(root))
            throw new NullPointerException("Property zkconfter.configs.root cannot be null.");

        //创建ZkClient对象
        zkClient = new ZkClient(zkServers);
        if (!zkClient.exists(ZK_ROOT + appName)) {
            zkClient.create(ZK_ROOT + appName, CreateMode.PERSISTENT, true);
        }

        //同步配置中心
        this.syncZkConfter();

        //监听配置文件
        this.watchZkConfter();

    }


    /**
     * 同步配置中心
     */
    private void syncZkConfter() throws Exception {
        //从配置中心下载文件
        filePathList = zkClient.getChildrenOfFullPathRecursive(ZK_ROOT + appName);
        for (Iterator<String> it = filePathList.iterator(); it.hasNext(); ) {
            String path = it.next();
            byte[] data = zkClient.readData(path);

            //不是文件不下载
            if (data == null || data.length == 0) {
                it.remove();
                continue;
            }

            //下载配置文件
            String filename = getAppPath() + root + path.replaceFirst(ZK_ROOT + appName, "");
            File file = new File(filename);
            if (!file.exists())
                file.createNewFile();

            FileOutputStream on = null;
            try {
                on = new FileOutputStream(file);
                on.write(data);
            } catch (Exception e) {
                throw e;
            } finally {
                if (on != null)
                    on.close();
            }
        }
    }


    /**
     * 监听配置文件
     */
    private void watchZkConfter() {
//        zkClient.watchForChilds();
//        zkClient.watchForData();
//
//        zkClient.subscribeChildChanges();


    }


    public ZkClient getZkClient() {
        return zkClient;
    }

    public Resource getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * 获取系统的根目录
     */
    public static String getAppPath() {
        return ZkConfter.class.getResource("/").toString().replaceAll("WEB-INF/classes/", "");
    }

}
