package kpn.data.assignment.service;

public interface EventProcessorFactory extends AutoCloseable {
    StreamProcessor createProcessor();

    @Override
    default void close() {
    }
}
