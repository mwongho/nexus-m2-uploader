package org.sonatype.nexus.repository.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NexusImporterApp implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(NexusImporterApp.class);
  
    @Autowired
    private ImporterService importerService;
    
    @Autowired
    private ExporterService exporterService;
    
    public static void main(String[] args) {
        logger.info("MAIN !!!!!!!!!");
        SpringApplication app = new SpringApplication(NexusImporterApp.class);
        app.run(args);
    }
 
    @Override
    public void run(String... args) throws Exception {
        logger.info("RUN !!!!!!!!!");
        if(args.length > 0) {
            String option = args[0];
            logger.info("Option :{}"+option);
            switch (option) {
            case "import":
                this.importerService.importArtifacts();
                break;
            case "export":
                this.exporterService.exportArtifacts();
                break;
            default:
                break;
            }
            
        }
        logger.info("RAN !!!!!!!!!");
    }
    
}
