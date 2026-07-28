package com.guimathis.exchangeservice.environment;

import lombok.Getter;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class InstanceInformationService implements ApplicationListener<WebServerInitializedEvent> {

    private String port;

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        port = String.valueOf(event.getSource().getPort());
    }

    public String retrieveServerPort() {
        return port;
    }

}
