package kpn.data.assignment.process;

import kpn.data.assignment.model.Outage;
import org.apache.commons.lang3.StringUtils;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RSSProcess extends EventProcess {
    Jsonb jsonb = JsonbBuilder.create();
    List<Outage> businessOutageList = new ArrayList<>();
    List<Outage> customerOutageList = new ArrayList<>();
    public static final Logger LOGGER = LoggerFactory.getLogger(RSSProcess.class);

    @Override
    public void run(BufferedReader bufferedReader, BufferedOutputStream bufferedCustomerOS,
                    BufferedOutputStream bufferedBusinessOS) {

        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        Future<?> future = executorService.submit(() -> {
            try {
                XMLParser(bufferedReader);
                writeToBufferStream(bufferedBusinessOS, businessOutageList);
                writeToBufferStream(bufferedCustomerOS, customerOutageList);
            } catch (XMLStreamException e) {
                LOGGER.error("ERROR - Exception in XML Stream :" + e.getMessage());
            } catch (IOException e) {
                LOGGER.error("ERROR - I/O Exception occurred:" + e.getMessage());
            }
        });

        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        try {
            String result = (String) future.get(200, TimeUnit.MILLISECONDS);
            LOGGER.info("INFO - Stream is processed successfully");
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("ERROR - Thread is interrupted:" + e.getMessage());
        } catch (TimeoutException e) {
            LOGGER.error("ERROR - Timeout occurred in thread execution:" + e.getMessage());
        }
    }

    private void XMLParser(BufferedReader bufferedReader) throws XMLStreamException, IOException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(bufferedReader);
        Outage outage = new Outage();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                switch (startElement.getName().getLocalPart()) {
                    case "item":
                        outage = new Outage();
                        break;
                    case "title":
                        nextEvent = reader.nextEvent();
                        try {
                            outage.setTitle(nextEvent.asCharacters().getData());
                        } catch (ClassCastException ce) {
                        }
                        break;
                    case "postalCodes":
                        nextEvent = reader.nextEvent();
                        try {
                            outage.setPostalCodes(nextEvent.asCharacters().getData());
                        } catch (ClassCastException ce) {
                        }
                        break;
                    case "locations":
                        nextEvent = reader.nextEvent();
                        try {
                            outage.setLocations(nextEvent.asCharacters().getData());
                        } catch (ClassCastException ce) {
                        }
                        break;
                    case "description":
                        nextEvent = reader.nextEvent();
                        try {
                            String description = nextEvent.asCharacters().getData();
                            outage.setDescription(description);

                            String startDateContent = StringUtils.substringBetween(description, "Starttijd:", "Eindtijd:");
                            String startDate = parseDate(startDateContent);
                            outage.setStartDate(startDate);

                            String endDateContent = StringUtils.substringBetween(description, "Eindtijd:", "&");
                            String endDate = parseDate(endDateContent);
                            outage.setEndDate(endDate);

                            outage.setStatus(getStatus(startDate, endDate));
                        } catch (ClassCastException ce) {
                        } catch (ParseException e) {
                            LOGGER.error("ERROR - Parsing exception: " + e.getMessage());
                        }
                        break;
                }
            } else if (nextEvent.isEndElement()) {
                EndElement endElement = nextEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("item")) {
                    if (isBusinessOutage(outage.getLocations()))
                        businessOutageList.add(outage);
                    else
                        customerOutageList.add(outage);
                }
            }
        }


    }

    private String parseDate(String dateContent) {
        if (dateContent != null) {
            Pattern patternStartDate = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
            Matcher matcherStartDate = patternStartDate.matcher(dateContent);

            if (matcherStartDate.find()) {
                return matcherStartDate.group(0);
            }
        }
        return "onbekend";
    }

    private String getStatus(String startDateVal, String endDateVal) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        String status = "Actueel";
        Date today = new Date();
        if (!endDateVal.equals("onbekend")) {
            Date endDate = dateFormat.parse(endDateVal);
            if (today.after(endDate))
                status = "Opgelost";
        }
        if (!startDateVal.equals("onbekend")) {
            Date startDate = dateFormat.parse(startDateVal);
            if (today.before(startDate))
                status = "Gepland";
        }
        return status;
    }

    private boolean isBusinessOutage(String locations) {
        return locations != null && (locations.contains("ZMST") || locations.contains("ZMOH"));
    }

    private void writeToBufferStream(BufferedOutputStream bufferedOS, List<Outage> outageList) throws IOException {
        bufferedOS.write(jsonb.toJson(outageList, Outage.class).getBytes(StandardCharsets.UTF_8));
        bufferedOS.flush();
        bufferedOS.close();
    }
}
