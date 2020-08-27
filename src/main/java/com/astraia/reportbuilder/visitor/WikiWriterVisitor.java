package com.astraia.reportbuilder.visitor;

import com.astraia.reportbuilder.schema.Bold;
import com.astraia.reportbuilder.schema.Italic;
import com.astraia.reportbuilder.schema.Report;
import com.astraia.reportbuilder.schema.Section;

import java.io.IOException;
import java.io.Writer;

public interface WikiWriterVisitor {
    void visitSection(Section section, Writer writer) throws IOException;
    void visitBold(Bold bold, Writer writer) throws IOException;
    void visitItalic(Italic italic, Writer writer) throws IOException;
    void visitReport(Report report, Writer writer) throws IOException;
}
