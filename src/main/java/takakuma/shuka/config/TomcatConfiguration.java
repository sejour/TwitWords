package takakuma.shuka.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfiguration {

	@Value("${tomcat.ajp.port}")
	int ajpPort;

	@Value("${tomcat.ajp.enabled}")
	boolean tomcatAjpEnabled;

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {

	    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
	    if (this.tomcatAjpEnabled) {
	        Connector ajpConnector = new Connector("AJP/1.3");
	        ajpConnector.setProtocol("AJP/1.3");
	        ajpConnector.setPort(this.ajpPort);
	        ajpConnector.setSecure(false);
	        ajpConnector.setAllowTrace(false);
	        ajpConnector.setScheme("ajp");
	        tomcat.addAdditionalTomcatConnectors(ajpConnector);
	    }

	    return tomcat;
	}

}
