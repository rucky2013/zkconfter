package com.alibaba.zkconfter.client.config;

import com.alibaba.zkconfter.client.ZkConfter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 集成配置中心ZkConfter
 * Spring导入properties文件，允许动态配置你要导入哪个目录下的配置文件。
 * @author Linpn
 */
public class ZkConfterPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private ZkConfter zkConfter;
	
	@Override
	public void afterPropertiesSet() throws Exception {
        super.setConfig(zkConfter.getConfig());
        super.afterPropertiesSet();
	}

    public ZkConfter getZkConfter() {
        return zkConfter;
    }

    public void setZkConfter(ZkConfter zkConfter) {
        this.zkConfter = zkConfter;
    }
}
