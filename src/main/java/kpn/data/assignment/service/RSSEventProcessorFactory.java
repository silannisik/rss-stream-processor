package kpn.data.assignment.service;

import com.google.auto.service.AutoService;
import kpn.data.assignment.process.RSSProcess;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@AutoService(EventProcessorFactory.class)
public final class RSSEventProcessorFactory implements EventProcessorFactory {
    @Override
    public StreamProcessor createProcessor() {
        RSSProcess rssProcess = new RSSProcess();
        return (source, customerSink, businessSink) -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source));
            BufferedOutputStream bufferedCustomerOutputStream = new BufferedOutputStream(customerSink);
            BufferedOutputStream bufferedBusinessOutputStream = new BufferedOutputStream(businessSink);
            rssProcess.run(bufferedReader, bufferedCustomerOutputStream, bufferedBusinessOutputStream);
        };
    }
}