package kpn.data.assignment;

import kpn.data.assignment.service.EventProcessorFactory;
import kpn.data.assignment.service.StreamProcessor;

import java.io.*;
import java.util.Iterator;
import java.util.ServiceLoader;

public class StreamApplication {
    public static void main(String[] args) {
        processStream();
    }

    private static void processStream() {
        Iterator<EventProcessorFactory> factories =
                ServiceLoader.load(EventProcessorFactory.class).iterator();
        if (!factories.hasNext()) {
            throw new IllegalStateException("No EventProcessorFactory found");
        }

        try (EventProcessorFactory factory = factories.next();
             StreamProcessor processor = factory.createProcessor()) {
            processor.process(getInputStream("src/main/resources/input/outages.xml"), getOutputStream("customer_outages.json"), getOutputStream("business_outages.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getInputStream(String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    private static OutputStream getOutputStream(String fileName) throws IOException {
        String outputPath = "target/output/";
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File outputFile = new File(outputPath + fileName);
        outputFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(outputFile, false);
        return outputStream;
    }
}
