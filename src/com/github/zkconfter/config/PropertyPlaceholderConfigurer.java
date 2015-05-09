package com.github.zkconfter.config;

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
	private Resource profile;
	private String[] configs;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(configs==null)
			return;

		List<Resource> resources = new LinkedList<Resource>();
		resources.add(profile);
			
		Properties propsConf = new Properties();
		propsConf.load(profile.getInputStream());		
		String regex = "\\$\\{(.+)\\}";
		
		for(String config : configs){
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(config);
			for(int i=1;m.find();i++){
				if(config.startsWith("file://") || config.startsWith("classpath:") || config.startsWith("classpath*:")){
					config = config.replaceFirst(regex, propsConf.getProperty(m.group(i)));
				}else{
					config = contextPath + config.replaceFirst(regex, propsConf.getProperty(m.group(i)));
				}
			}
			
			Resource[] a = this.resourcePatternResolver.getResources(config);
			if(a!=null){
				for(Resource r : a){
					resources.add(r);
				}
			}
		}
		
		super.setLocations(resources.toArray(new Resource[0]));
	}


	public void setProfile(Resource profile) {
		this.profile = profile;
	}
	
	public void setConfigs(String[] configs){
		this.configs = configs;
	}
	
}
