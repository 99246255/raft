package com.emumaelish.config;

import com.emumaelish.service.RaftEngine;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 应用启动完成后执行
 **/
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        RaftEngine raftEngine = applicationContext.getBean(RaftEngine.class);
        raftEngine.start();
    }
}
