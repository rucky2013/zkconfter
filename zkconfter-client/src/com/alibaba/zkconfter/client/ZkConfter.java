package com.alibaba.zkconfter.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.zkconfter.client.annotation.DRMAttribute;
import com.alibaba.zkconfter.client.annotation.DRMResource;
import com.alibaba.zkconfter.client.util.BeanUtils;
import com.alibaba.zkconfter.client.util.ZkClient;
import org.I0Itec.zkclient.IZkDataListener;
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
import java.lang.reflect.Field;
import java.util.*;


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
    private List<String> zkPathList;

    private static Map<String, Object> zkDrmPool = new HashMap<String, Object>();

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
        if (drm.equals("true")) {
            this.syncDrmZkConfter();
        }
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
        zkPathList = zkClient.getChildrenOfFullPathRecursive(zkPath);
    }

    /**
     * 同步配置中心
     *
     * @throws IOException
     */
    public void syncZkConfter() throws IOException {
        if (CollectionUtils.isEmpty(zkPathList)) {
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
            String zkPath = this.getZkPathByLcPath(lcPath);

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
        for (Iterator<String> it = zkPathList.iterator(); it.hasNext(); ) {
            String zkPath = it.next();
            byte[] data = zkClient.readData(zkPath);

            //如果不是文件节点，则不下载
            if (data == null || data.length == 0) {
                it.remove();
                continue;
            }

            //下载配置文件
            String lcPath = this.getLcPathByZkPath(zkPath);
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
     * 同步动态资源(DRM)
     */
    public void syncDrmZkConfter() throws IllegalAccessException, InstantiationException {
        List<Class<?>> list = BeanUtils.getClasses(drmPackage);
        for (Class<?> clazz : list) {
            DRMResource drmResource = clazz.getAnnotation(DRMResource.class);
            if (drmResource != null) {
                // 实例化
                final Object inst = clazz.newInstance();
                zkDrmPool.put(clazz.getCanonicalName(), inst);

                // 创建类节点
                String zkDrmResource = this.getZkDrmPath() + "/" + clazz.getCanonicalName();
                if (!zkClient.exists(zkDrmResource)) {
                    zkClient.create(zkDrmResource, CreateMode.PERSISTENT, true);
                }

                // 设置资源节点
                JSONObject dataResource = new JSONObject();
                dataResource.put("name", drmResource.name());
                dataResource.put("description", drmResource.description());
                zkClient.writeData(zkDrmResource, dataResource.toString(), CreateMode.PERSISTENT);

                // 创建属性节点
                Field[] fields = clazz.getDeclaredFields();
                for (final Field field : fields) {
                    DRMAttribute drmAttribute = field.getAnnotation(DRMAttribute.class);
                    if (drmAttribute != null) {
                        field.setAccessible(true);
                        String zkDrmAttribute = zkDrmResource + "/" + field.getName();
                        if (!zkClient.exists(zkDrmAttribute)) {
                            zkClient.create(zkDrmAttribute, CreateMode.PERSISTENT, true);

                            // 设置属性节点
                            JSONObject dataAttribute = new JSONObject();
                            dataAttribute.put("name", drmAttribute.name());
                            dataAttribute.put("description", drmAttribute.description());
                            dataAttribute.put("value", field.get(inst));
                            zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);
                        } else {
                            // 设置属性节点
                            JSONObject dataAttribute = JSON.parseObject(zkClient.readData(zkDrmAttribute).toString());
                            dataAttribute.put("name", drmAttribute.name());
                            dataAttribute.put("description", drmAttribute.description());
                            field.set(inst, dataAttribute.get("value"));
                            zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);
                        }


                        // 监听动态资源(DRM)
                        zkClient.subscribeDataChanges(zkDrmAttribute, new IZkDataListener() {
                            @Override
                            public void handleDataChange(String dataPath, Object data) throws Exception {
                                JSONObject dataAttribute = JSON.parseObject(data.toString());
                                Object value = dataAttribute.get("value");
                                field.set(inst, value);
                                logger.info("推送DRM属性" + dataPath + "，值为:" + value);
                            }

                            @Override
                            public void handleDataDeleted(String dataPath) throws Exception {
                                logger.info("DRM清除废弃属性:" + dataPath);
                            }
                        });
                    }
                }

                //清除已废弃的属性节点
                List<String> zkDrmAttributePaths = zkClient.getChildrenOfFullPathRecursive(zkDrmResource);
                for (String path : zkDrmAttributePaths) {
                    boolean flag = true;
                    for (Field field : fields) {
                        String zkDrmAttribute = zkDrmResource + "/" + field.getName();
                        if (path.equals(zkDrmAttribute)) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) {
                        zkClient.deleteRecursive(path);
                    }
                }
            }
        }
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

    private String getAppRoot() {
        return ZkConfter.class.getResource("/").toString().replaceFirst("file:/", "").replaceAll("WEB-INF/classes/", "");
    }

    private String getLcRoot() {
        return getAppRoot() + root;
    }

    private String getZkRoot() {
        return ZK_ROOT + appName + "/config";
    }

    private String getZkDrmRoot() {
        return ZK_ROOT + appName + "/drm";
    }

    private String getLcPath() {
        return getLcRoot() + (StringUtils.isEmpty(runtime) ? "" : "/" + runtime);
    }

    private String getZkPath() {
        return getZkRoot() + (StringUtils.isEmpty(runtime) ? "" : "/" + runtime);
    }

    private String getZkDrmPath() {
        return getZkDrmRoot() + (StringUtils.isEmpty(runtime) ? "" : "/" + runtime);
    }

    private String getLcPathByZkPath(String zkPath) {
        return getLcRoot() + zkPath.replaceFirst(getZkRoot(), "");
    }

    private String getZkPathByLcPath(String lcPath) {
        return getZkRoot() + lcPath.replaceFirst(getLcRoot(), "");
    }

    /**
     * 获取DRM对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T drm(Class<T> clazz) {
        return (T) zkDrmPool.get(clazz.getCanonicalName());
    }

}
