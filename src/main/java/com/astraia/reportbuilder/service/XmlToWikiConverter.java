package com.astraia.reportbuilder.service;

import com.astraia.reportbuilder.parser.XmlReportParser;
import com.astraia.reportbuilder.schema.Report;
import com.astraia.reportbuilder.visitor.WikiWriterVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class XmlToWikiConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ApplicationContext applicationContext;
    private final XmlReportParser xmlReportParser;

    @Autowired
    public XmlToWikiConverter(ApplicationContext applicationContext, XmlReportParser xmlReportParser) {
        this.applicationContext = applicationContext;
        this.xmlReportParser = xmlReportParser;
    }

    public void convertXmlToWiki(Path filePath, Path outputDirectory) throws IOException, JAXBException {
        Report report = xmlReportParser.parse(filePath);
        convertReportToWiki(report, filePath, outputDirectory);
    }

    private void convertReportToWiki(Report report, Path filePath, Path outputDirectory) throws IOException {
        String fileName = filePath.getFileName().toString().split(Pattern.quote("."))[0];
        FileWriter fileWriter = new FileWriter(outputDirectory + "/" + fileName + ".wiki");
        try (BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);) {
            //WikiWriterVisitor is a stateful bean and it uses prototype scope so we should get a new bean each time
            WikiWriterVisitor visitor = applicationContext.getBean(WikiWriterVisitor.class);
            report.accept(visitor, bufferedWriter);
        }
    }

    /**
     * convert xml files that wer generate when this service was not up
     * @param inputDirectory xml files directory
     * @param outputDirectory generated wiki files directory
     */
    public void convertOldXmlFiles(Path inputDirectory, Path outputDirectory) {
        List<String> outputDirectoryFileNames = Stream.of(outputDirectory.toFile().listFiles())
                .map(File::getName).collect(Collectors.toList());

        List<Path> unconvertedXmlFiles = Stream.of(inputDirectory.toFile().listFiles())
                .filter(inputFile -> inputFile.getName().endsWith(".xml"))
                .filter(inputFile ->
                        outputDirectoryFileNames.stream().noneMatch(outputFileName ->
                                (inputFile.getName().split(Pattern.quote("."))[0] + ".wiki").equals(outputFileName)))
                .map(file -> file.toPath())
                .collect(Collectors.toList());

        for (Path filePath : unconvertedXmlFiles) {
            try {
                convertXmlToWiki(filePath, outputDirectory);
            } catch (IOException | JAXBException e) {
                logger.error(String.format("could not convert '%s' to wiki", filePath), e);
            }
        }
    }
}
