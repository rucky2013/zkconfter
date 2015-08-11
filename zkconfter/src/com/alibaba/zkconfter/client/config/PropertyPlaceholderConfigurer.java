package com.alibaba.zkconfter.client.config;

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
 * Spring导入properties文件，允许动态配置你要导入哪个目录下的配置文件。
 * @author Linpn
 */
public class PropertyPlaceholderConfigurer extends
		org.springframework.beans.factory.config.PropertyPlaceholderConfigurer implements InitializingBean {
	
	private String contextPath = this.getClass().getResource("/").toString().replaceAll("WEB-INF/classes/", "");
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private Resource config;
	private String[] locations;
	
	@Override
	public void afterPropertiesSet() throws Exception {
        if(config ==null)
            return;
		if(locations ==null)
			return;

		List<Resource> resources = new LinkedList<Resource>();
		resources.add(config);
			
		Properties propsConf = new Properties();
		propsConf.load(config.getInputStream());
		String regex = "\\$\\{(.+)\\}";
		
		for(String location : locations){
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(location);
			for(int i=1;m.find();i++){
				if(location.startsWith("file://") || location.startsWith("classpath:") || location.startsWith("classpath*:")){
					location = location.replaceFirst(regex, propsConf.getProperty(m.group(i)));
				}else{
					location = contextPath + location.replaceFirst(regex, propsConf.getProperty(m.group(i)));
				}
			}
			
			Resource[] a = this.resourcePatternResolver.getResources(location);
			if(a!=null){
				for(Resource r : a){
					resources.add(r);
				}
			}
		}
		
		super.setLocations(resources.toArray(new Resource[0]));
	}


    public Resource getConfig() {
        return config;
    }

    public void setConfig(Resource config) {
        this.config = config;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }
}
