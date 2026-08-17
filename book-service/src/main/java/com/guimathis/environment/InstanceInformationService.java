package com.guimathis.environment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class InstanceInformationService implements ApplicationListener<WebServerInitializedEvent> {

    private String port;

    @Value("${HOSTNAME:LOCAL}")
    private String hostName;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        port = String.valueOf(event.getSource().getPort());
    }

    public String retrieveServerPort() {
        return port;
    }

    public String retrieveHostName() {
        return hostName.substring(hostName.length() - 5);
    }

}
