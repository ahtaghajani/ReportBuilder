package com.astraia.reportbuilder;

import com.astraia.reportbuilder.parser.XmlReportParser;
import com.astraia.reportbuilder.service.ReportFileWatcher;
import com.astraia.reportbuilder.visitor.WikiWriterVisitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReportBuilderApplicationTests {

    @Autowired
    private ReportFileWatcher reportFileWatcher;

    @Autowired
    private WikiWriterVisitor wikiWriterVisitor;

    @Autowired
    private XmlReportParser xmlReportParser;
    private final Path sampleDirectory =Paths.get("src/test/resources/sample");

    @Test
    void testVisitor() throws IOException, JAXBException {
        String[] sampleNames = new String[]{"example1", "example2"};

        for (String sampleName : sampleNames) {
            StringWriter stringWriter = new StringWriter();
            wikiWriterVisitor.visitReport(xmlReportParser.parse(sampleDirectory.resolve(sampleName + ".xml")), stringWriter);

            String createdFileContent = stringWriter.toString();
            String expectedContent = readFile(sampleDirectory.resolve(sampleName + ".wiki")).trim();
            assertEquals(expectedContent, createdFileContent);
        }
    }

    @Test
    void testWatcher() throws IOException, InterruptedException {
        Path inputDirectory = sampleDirectory.resolve("inputDirectory");
        Path outputDirectory = sampleDirectory.resolve("outputDirectory");

        String nameWithoutExtension = "example1";
        Path sampleXmlFile = sampleDirectory.resolve(nameWithoutExtension + ".xml");
        Path desiredWikiFile = outputDirectory.resolve(nameWithoutExtension + ".wiki");

        deleteDirectoryContents(inputDirectory);
        deleteDirectoryContents(outputDirectory);

        new Thread(() -> {
            try {
                reportFileWatcher.watchAndConvertNewReports(inputDirectory, outputDirectory);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException("could not register watcher for input directory");
            }
        }).start();

        FileCopyUtils.copy(sampleXmlFile.toFile(), inputDirectory.resolve(sampleXmlFile.getFileName()).toFile());
        Thread.sleep(500);
        assertTrue(Files.exists(desiredWikiFile));

        String expectedContent = readFile(sampleDirectory.resolve(nameWithoutExtension+".wiki")).trim();
        String generateContent = readFile(desiredWikiFile).trim();
        assertEquals(expectedContent, generateContent);

        deleteDirectoryContents(inputDirectory);
        deleteDirectoryContents(outputDirectory);
    }

    private String readFile(Path path) throws IOException {
        File file = new File(String.valueOf(path));
        try (FileInputStream fis = new FileInputStream(file);) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private void deleteDirectoryContents(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    Files.delete(entry);
                }
            }
        }
    }

}

