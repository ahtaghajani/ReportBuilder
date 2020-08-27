package com.astraia.reportbuilder.visitor;

import com.astraia.reportbuilder.schema.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

@Component
@Scope("prototype")
public class WikiWriterVisitorImpl implements WikiWriterVisitor {
    private int sectionLevel;

    private void removeWhiteSpaces(Element inputElement) {
        if (inputElement.getContent() != null) {
            Iterator<Object> iterator = inputElement.getContent().iterator();
            while (iterator.hasNext()) {
                Object nextElement = iterator.next();
                if (nextElement instanceof Element) {
                    Element element = (Element) nextElement;
                    removeWhiteSpaces(element);
                } else {
                    String trimmedContent = nextElement.toString().trim();
                    if (trimmedContent.isEmpty()) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public void visitReport(Report report, Writer writer) throws IOException {
        removeWhiteSpaces(report);

        if (report.getContent() != null) {
            for (int i = 0; i < report.getContent().size(); i++) {
                if (i > 0) {
                    newLine(writer);
                }
                if (report.getContent().get(i) instanceof Element) {
                    Element element = (Element) report.getContent().get(i);
                    element.accept(this, writer);
                } else {
                    String trimmedContent = report.getContent().get(i).toString().trim();
                    write(trimmedContent, writer);
                }
            }
        }
    }



    @Override
    public void visitSection(Section section, Writer writer) throws IOException {
        sectionLevel++;
        String formattedHeading = formatHeading(section.getHeading(), sectionLevel);
        writeln(formattedHeading, writer);

        List<Object> contentList = section.getContent();
        writeContentList(writer, contentList);
        sectionLevel--;
    }

    private void writeContentList(Writer writer, List<Object> contentList) throws IOException {
        if (contentList != null) {
            for (int i = 0; i < contentList.size(); i++) {
                Object currentItem = contentList.get(i);
                if (i > 0) {
                    writeSeparator(contentList, i, writer);
                }
                if (currentItem instanceof Element) {
                    Element element = (Element) currentItem;
                    element.accept(this, writer);
                } else {
                    String trimmedContent = currentItem.toString().trim();
                    write(trimmedContent, writer);
                }
            }
        }
    }

    private void writeSeparator(List<Object> contentList, int currentIndex, Writer writer) throws IOException {
        Object currentItem = contentList.get(currentIndex);
        if (currentItem instanceof Section) {
            newLine(writer);
        } else if (currentIndex == 2 && currentItem instanceof Bold && contentList.get(currentIndex - 1) instanceof String && contentList.get(currentIndex - 2) instanceof Bold) {
            newLine(writer);
        } else {
            write(" ", writer);
        }
    }

    @Override
    public void visitBold(Bold bold, Writer writer) throws IOException {
        write("'''", writer);
        writeContentList(writer, bold.getContent());
        write("'''", writer);
    }

    @Override
    public void visitItalic(Italic italic, Writer writer) throws IOException {
        write("''", writer);
        writeContentList(writer, italic.getContent());
        write("''", writer);
    }

    private void write(String string, Writer writer) throws IOException {
        writer.write(string);
    }

    private void newLine(Writer writer) throws IOException {
        writer.write("\n");
    }

    private void writeln(String string, Writer writer) throws IOException {
        write(string, writer);
        newLine(writer);
    }

    public static String formatHeading(String heading, int headingCounter) {
        headingCounter = headingCounter > 6 ? 6 : headingCounter;

        StringBuilder sb = new StringBuilder();

        append(sb, "=", headingCounter);
        sb.append(heading);
        append(sb, "=", headingCounter);

        return sb.toString();
    }

    private static void append(StringBuilder sb, String str, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
    }
}