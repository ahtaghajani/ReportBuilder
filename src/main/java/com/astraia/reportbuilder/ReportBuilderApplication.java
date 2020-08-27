package com.astraia.reportbuilder;

import com.astraia.reportbuilder.service.ReportFileWatcher;
import com.astraia.reportbuilder.service.XmlToWikiConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ReportBuilderApplication {

    private static Logger logger = LoggerFactory.getLogger(ReportBuilderApplication.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            usage();
        }

        ConfigurableApplicationContext applicationContext = SpringApplication.run(ReportBuilderApplication.class, args);

        int i = -1;
        Path inputDirectory = Paths.get(args[++i]);
        Path outputDirectory = Paths.get(args[++i]);

        validateDirectories(inputDirectory, outputDirectory);

        XmlToWikiConverter xmlToWikiConverter = applicationContext.getBean(XmlToWikiConverter.class);
        xmlToWikiConverter.convertOldXmlFiles(inputDirectory, outputDirectory);

        ReportFileWatcher reportFileWatcher = applicationContext.getBean(ReportFileWatcher.class);
        reportFileWatcher.watchAndConvertNewReports(inputDirectory, outputDirectory);
    }

    private static void validateDirectories(Path inputDirectory, Path outputDirectory) {
        if (!Files.exists(inputDirectory) || !Files.isDirectory(inputDirectory)) {
            String message = String.format("'%s' is not a valid directory", inputDirectory);
            logger.error(message);
            System.exit(1);
        }

        if (!Files.exists(outputDirectory)) {
            outputDirectory.toFile().mkdirs();
        } else if (!Files.isDirectory(outputDirectory)) {
            String message = String.format("'%s' is not a directory", outputDirectory);
            logger.error(message);
            System.exit(1);
        }
    }

    public static void usage() {
        System.err.println("usage: java -jar report-builder.jar inputDirectory outputDirectory");
        System.exit(-1);
    }

}
