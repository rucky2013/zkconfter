package com.google.code.zkconfter.client;

/**
 * Created by pinian.lpn on 2015/3/4.
 */
public final class ConfigConst {

    /**
     * Zk配置中心地址
     */
    public final static String ZK_SERVERS = "zkconfter.zkServers";

    /**
     * 系统名
     */
    public final static String APP_NAME = "zkconfter.appName";

    /**
     * 配置文件根目录
     */
    public final static String CONFIGS_ROOT = "zkconfter.configs.root";

    /**
     * 上传到配置中心的文件及目录
     */
    public final static String CONFIGS_INCLUDES = "zkconfter.configs.includes";

    /**
     * 从zkconfter.includes中排除目录或文件
     */
    public final static String CONFIGS_EXCLUDES = "zkconfter.configs.excludes";

}
