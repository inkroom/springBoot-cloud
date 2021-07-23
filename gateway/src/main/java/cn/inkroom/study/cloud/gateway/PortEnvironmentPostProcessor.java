package cn.inkroom.study.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.SocketUtils;

import java.util.Properties;

/**
 * @author inkbox
 * @date 2021/7/23
 */
public class PortEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        int availableTcpPort = SocketUtils.findAvailableTcpPort(6001, 12999);

        properties.put("management.server.port",availableTcpPort);
        properties.put("eureka.instance.metadata-map.management.port",availableTcpPort);
        PropertiesPropertySource source = new PropertiesPropertySource("CONSUME", properties);
        environment.getPropertySources().addFirst(source);
    }
}
