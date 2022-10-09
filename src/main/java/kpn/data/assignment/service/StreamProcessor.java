package kpn.data.assignment.service;

import javax.annotation.WillNotClose;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamProcessor extends AutoCloseable {
    @Override
    default void close() {
    }

    void process(@WillNotClose InputStream source, @WillNotClose OutputStream customerSink,
                 @WillNotClose OutputStream businessSink);
}
