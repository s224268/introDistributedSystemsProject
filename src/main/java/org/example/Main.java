package org.example;

import java.io.IOException;
import java.util.List;

import org.jspace.*;


public class Main {

    public final static String GATE_URI = "tcp://127.0.0.1:9001/?keep";
    public final static String REMOTE_URI = "tcp://127.0.0.1:9001/Names?keep";



    public static void main(String[] argv) throws InterruptedException, IOException {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(GATE_URI);
        repository.add("Names", new SequentialSpace());
        repository.add("QNA", new SequentialSpace());
        repository.add("GameState", new SequentialSpace());
        repository.put("Names", "Jakob");
        repository.put("Names", "Anton");
        repository.put("Names", "Lucas");

        Space remoteSpace = new RemoteSpace(REMOTE_URI);
        List<Object[]> temp = remoteSpace.queryAll(new FormalField(String.class));

        repository.put("Names", "Lucas");



        for (Object[] row : temp) {
            System.out.println(row[0]);
        }

    }


}