package kpn.data.assignment.process;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class EventProcess {

    public abstract void run(BufferedReader bufferedReader, BufferedOutputStream bufferedCustomerOutputStream,
                             BufferedOutputStream bufferedBusinessOutputStream) throws IOException, XMLStreamException, JAXBException, ParserConfigurationException, ExecutionException, InterruptedException, TimeoutException;
}
