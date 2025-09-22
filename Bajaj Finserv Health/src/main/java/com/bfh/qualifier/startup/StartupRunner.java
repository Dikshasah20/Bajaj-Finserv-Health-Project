package com.bfh.qualifier.startup;

import com.bfh.qualifier.service.QualifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);
    private final QualifierService qualifierService;

    public StartupRunner(QualifierService qualifierService) {
        this.qualifierService = qualifierService;
    }

    @Override
    public void run(String... args) {
        log.info("Starting qualifier flow");
        qualifierService.runFlow();
    }
}

