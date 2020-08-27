package com.astraia.reportbuilder.parser;

import com.astraia.reportbuilder.schema.Report;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class XmlReportParser {

    private final Unmarshaller jaxbUnmarshaller;

    public XmlReportParser() throws JAXBException {
        jaxbUnmarshaller = JAXBContext.newInstance(Report.class).createUnmarshaller();
    }

    public Report parse(Path filePath) throws IOException, JAXBException {
        try (InputStream inputStream = Files.newInputStream(filePath);) {
            return (Report) jaxbUnmarshaller.unmarshal(inputStream);
        }
    }
}
