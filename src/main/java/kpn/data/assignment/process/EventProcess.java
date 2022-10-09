package kpn.data.assignment.process;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;

public abstract class EventProcess {

    public abstract void run(BufferedReader bufferedReader, BufferedOutputStream bufferedCustomerOutputStream,
                             BufferedOutputStream bufferedBusinessOutputStream);
}
