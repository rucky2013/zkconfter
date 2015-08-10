package com.alibaba.zkconfter.client;

import com.alibaba.zkconfter.client.util.BeanUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * ZkConfter 主操作对象，可以配置在Spring中
 * <p/>
 * Created by pinian.lpn on 2015/1/15.
 */
public class ZkConfter implements InitializingBean {

    private final static Logger logger = Logger.getLogger(ZkConfter.class);

    private final static String DEFAULT_ZKCONFTER_FILE = "zkconfter.properties";
    private final static String ZK_ROOT = "/zkconfter/";

    private Resource config;

    private String zkServers;
    private String appName;
    private String root;
    private String runtime;
    private String drm;
    private String drmPackage;

    private ZkClient zkClient;
    private List<String> filePathList;

    /**
     * 构造函数
     */
    public ZkConfter() {
    }

    /**
     * 构造函数
     */
    public ZkConfter(Resource config) {
        try {
            this.config = config;
            this.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造函数
     */
    public ZkConfter(String zkConfterFile) {
        try {
            this.config = new ClassPathResource(zkConfterFile);

            if (!this.config.exists()) {
                this.config = new FileSystemResource(zkConfterFile);

                if (!this.config.exists()) {
                    this.config = new UrlResource(zkConfterFile);
                }
            }
            this.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在BeanFactory载入后执行
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
        this.syncZkConfter();
        this.syncDrmZkConfter();
    }

    /**
     * 初始化配置参数
     *
     * @throws IOException
     */
    public void init() throws IOException {
        //获取zkconfter配置
        Properties zkProps = new Properties();
        if (config == null || !config.exists()) {
            zkProps.load(this.getClass().getClassLoader().getResourceAsStream(DEFAULT_ZKCONFTER_FILE));
        } else {
            zkProps.load(config.getInputStream());
        }

        zkServers = zkProps.getProperty(SysConstant.ZK_SERVERS);
        appName = zkProps.getProperty(SysConstant.APP_NAME);
        root = zkProps.getProperty(SysConstant.ROOT, "");
        runtime = zkProps.getProperty(SysConstant.RUNTIME, "");
        drm = zkProps.getProperty(SysConstant.DRM, "");
        drmPackage = zkProps.getProperty(SysConstant.DRM_PACKAGE, "");

        //验证配置项
        if (StringUtils.isEmpty(zkServers))
            throw new NullPointerException("Property zkconfter.zkServers cannot be null.");
        if (StringUtils.isEmpty(appName))
            throw new NullPointerException("Property zkconfter.appName cannot be null.");

        //创建ZkClient对象
        zkClient = new ZkClient(zkServers);

        //配置中心当前目录
        String zkPath = this.getZkPath();
        if (!zkClient.exists(zkPath)) {
            zkClient.create(zkPath, CreateMode.PERSISTENT, true);
        }

        //获取配置中心文件列表
        filePathList = zkClient.getChildrenOfFullPathRecursive(zkPath);
    }

    /**
     * 同步配置中心
     *
     * @throws IOException
     */
    public void syncZkConfter() throws IOException {
        if (CollectionUtils.isEmpty(filePathList)) {
            //如果目录为空，则上传初始化配置文件
            this.uploadZkConfter();
        } else {
            //如果不为空，则直接下载并覆盖本地文件
            this.downloadZkConfter();
        }
    }

    /**
     * 上传本地文件至配置中心
     *
     * @throws IOException
     */
    public void uploadZkConfter() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("file:/" + this.getLcPath() + "/**");
        List<Resource> resList = Arrays.asList(resources);

        //上传文件到配置中心
        for (Iterator<Resource> it = resList.iterator(); it.hasNext(); ) {
            Resource res = it.next();
            String lcPath = ((FileSystemResource) res).getPath();
            String zkPath = this.getZkPath(lcPath);

            //上传配置文件
            byte[] data = new byte[(int) res.getFile().length()];
            InputStream in = null;
            try {
                in = res.getInputStream();
                in.read(data);
            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null)
                    in.close();
            }

            zkClient.writeData(zkPath, data, CreateMode.PERSISTENT);
            logger.info("上传文件:" + zkPath);
        }
    }

    /**
     * 下载配置中心文件至本地
     *
     * @throws IOException
     */
    public void downloadZkConfter() throws IOException {
        //从配置中心下载文件
        for (Iterator<String> it = filePathList.iterator(); it.hasNext(); ) {
            String zkPath = it.next();
            byte[] data = zkClient.readData(zkPath);

            //如果不是文件节点，则不下载
            if (data == null || data.length == 0) {
                it.remove();
                continue;
            }

            //下载配置文件
            String lcPath = this.getLcPath(zkPath);
            File file = new File(lcPath);
            if (!file.exists())
                file.createNewFile();

            FileOutputStream on = null;
            try {
                on = new FileOutputStream(file);
                on.write(data);
            } catch (IOException e) {
                throw e;
            } finally {
                if (on != null)
                    on.close();
            }

            logger.info("下载文件:" + zkPath);
        }
    }

    /**
     * 监听动态资源(DRM)
     */
    public void syncDrmZkConfter() {
        BeanUtils.getClasses("");

    }

    public void updateDrmZkConfter() {

    }

    public void downloadDrmZkConfter() {

    }





    public Resource getConfig() {
        return config;
    }

    public void setConfig(Resource config) {
        this.config = config;
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    private String getZkPath() {
        return ZK_ROOT + appName + (StringUtils.isEmpty(runtime) ? "" : "/" + runtime);
    }

    private String getZkPath(String lcPath) {
        return ZK_ROOT + appName + lcPath.replaceFirst(getAppPath() + root, "");
    }

    private String getLcPath() {
        return getAppPath() + root + (StringUtils.isEmpty(runtime) ? "" : "/" + runtime);
    }

    private String getLcPath(String zkPath) {
        return getAppPath() + root + zkPath.replaceFirst(ZK_ROOT + appName, "");
    }

    /**
     * 获取系统的根目录
     */
    private String getAppPath() {
        return ZkConfter.class.getResource("/").toString().replaceFirst("file:/", "").replaceAll("WEB-INF/classes/", "");
    }

}
