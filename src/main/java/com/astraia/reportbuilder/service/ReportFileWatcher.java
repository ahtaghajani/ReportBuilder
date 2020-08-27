package com.astraia.reportbuilder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

@Service
public class ReportFileWatcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final WatchService watcher;
    private final XmlToWikiConverter xmlToWikiConverter;

    @Autowired
    public ReportFileWatcher(XmlToWikiConverter xmlToWikiConverter) throws IOException {
        this.xmlToWikiConverter = xmlToWikiConverter;
        watcher = FileSystems.getDefault().newWatchService();
    }

    private void register(Path dir) throws IOException {
        dir.register(watcher, ENTRY_MODIFY);
    }

    public void watchAndConvertNewReports(Path inputDirectory, Path outputDirectory) throws InterruptedException, IOException {

        try {
            register(inputDirectory);
        } catch (IOException e) {
            logger.error(String.format("could not register watcher for '%s' directory", inputDirectory), e);
            throw e;
        }

        while (true) {
            WatchKey key;
            key = watcher.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                WatchEvent.Kind<?> kind = event.kind();
                if (logger.isInfoEnabled()) {
                    logger.info(String.format("event.kind: %s event.context: %s", ev.kind(), ev.context()));
                }
                if (kind != OVERFLOW) {
                    Path name = ev.context();
                    Path filePath = inputDirectory.resolve(name);
                    try {
                        checkFileIsNotStillInUse(filePath);
                        xmlToWikiConverter.convertXmlToWiki(filePath, outputDirectory);
                    } catch (IOException | JAXBException e) {
                        logger.error(String.format("could not convert '%s' to wiki. exception.class: %s, exception.message: %s",
                                filePath, e.getClass().getName(), e.getMessage()));
                    }
                }
            }

            key.reset();
        }
    }

    private void checkFileIsNotStillInUse(Path filePath) throws IOException, InterruptedException {
        try (InputStream inputStream = new FileInputStream(filePath.toFile())) {
            inputStream.read();
        } catch (FileNotFoundException e) {
            logger.info(String.format("'%s' file is still open in another process. we will try again in 1 second", filePath));
            Thread.sleep(500);
        }
    }

}
