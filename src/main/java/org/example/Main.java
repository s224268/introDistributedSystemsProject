package org.example;

import java.util.function.Function;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

/**
 * A simple HelloWorld program.
 *
 *
 * @author Michele Loreti
 *
 */
public class Main {

    public static void main(String[] argv) throws InterruptedException {
        Space inbox = new SequentialSpace();

        inbox.put("Hello World!");
        Object[] tuple = inbox.get(new FormalField(String.class));
        System.out.println(tuple[0]);

    }

}