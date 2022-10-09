package kpn.data.assignment.service;

import org.xml.sax.SAXException;

import javax.annotation.WillNotClose;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface StreamProcessor extends AutoCloseable {
    @Override
    default void close() {
    }

    void process(@WillNotClose InputStream source, @WillNotClose OutputStream customerSink,
                 @WillNotClose OutputStream businessSink)
            throws IOException, XMLStreamException, ParserConfigurationException, SAXException, ExecutionException, InterruptedException, TimeoutException;
}
