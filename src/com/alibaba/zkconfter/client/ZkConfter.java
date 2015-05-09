package com.alibaba.zkconfter.client;

import com.alibaba.zkconfter.client.util.SysConstant;
import com.alibaba.zkconfter.client.util.ZkClient;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    private String includes;
    private String excludes;

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
        root = zkProps.getProperty(SysConstant.CONFIGS_ROOT);
        includes = zkProps.getProperty(SysConstant.CONFIGS_INCLUDES);
        excludes = zkProps.getProperty(SysConstant.CONFIGS_EXCLUDES, "");

        //验证配置项
        if (StringUtils.isEmpty(appName))
            throw new NullPointerException("Property zkconfter.appName cannot be null.");
        if (StringUtils.isEmpty(root))
            throw new NullPointerException("Property zkconfter.configs.root cannot be null.");
        if (StringUtils.isEmpty(includes))
            throw new NullPointerException("Property zkconfter.configs.includes cannot be null.");

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
        //获取本地配置文件信息
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resList = new ArrayList<Resource>();

        String[] incArray = includes.split(",");
        for (String inc : incArray) {
            Resource[] resources = resolver.getResources(getAppPath() + root + "/" + inc);
            resList.addAll(Arrays.asList(resources));
        }

        String[] excArray = excludes.split(",");
        for (String exc : excArray) {
            Resource[] resources = resolver.getResources(getAppPath() + root + "/" + exc);
            for (Iterator<Resource> it = resList.iterator(); it.hasNext(); ) {
                Resource r1 = it.next();
                for (Resource r2 : resources) {
                    if (r1.getFile().getPath().equals(r2.getFile().getPath()))
                        it.remove();
                }
            }
        }


        //上传文件到配置中心
        List<String> upPathList = new ArrayList<String>();
        for (Iterator<Resource> it = resList.iterator(); it.hasNext(); ) {
            Resource res = it.next();
            String path = ((FileSystemResource) res).getPath();
            path = path.replaceFirst(getAppPath().replaceFirst("file:/", "") + root, "");
            path = ZK_ROOT + appName + path;

            //zk存在这个节点，不上传文件
            if (zkClient.exists(path)) {
                it.remove();
                continue;
            }

            //上传配置文件
            byte[] data = new byte[(int) res.getFile().length()];
            InputStream in = null;
            try {
                in = res.getInputStream();
                in.read(data);
            } catch (Exception e) {
                throw e;
            } finally {
                if (in != null)
                    in.close();
            }
            zkClient.writeData(path, data, CreateMode.PERSISTENT);
            upPathList.add(path);
        }


        //从配置中心下载文件
        List<String> dwPathList = zkClient.getChildrenOfFullPathRecursive(ZK_ROOT + appName);
        dwPathList.removeAll(upPathList);
        for (Iterator<String> it = dwPathList.iterator(); it.hasNext(); ) {
            String dwPath = it.next();
            byte[] data = zkClient.readData(dwPath);

            //不是文件不下载
            if (data == null || data.length == 0) {
                it.remove();
                continue;
            }

            //下载配置文件
            String filename = getAppPath() + root + dwPath.replaceFirst(ZK_ROOT + appName, "");
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


        //记录zk中所有的配置文件的path
        this.filePathList = new ArrayList<String>();
        filePathList.addAll(upPathList);
        filePathList.addAll(dwPathList);
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
