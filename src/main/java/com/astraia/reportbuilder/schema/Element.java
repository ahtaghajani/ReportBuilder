package com.astraia.reportbuilder.schema;

import com.astraia.reportbuilder.visitor.WikiWriterVisitor;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public interface Element {
    List<Object> getContent();

    void accept(WikiWriterVisitor wikiWriterVisitor, Writer writer) throws IOException;
}
