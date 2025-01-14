package org.example;

import java.io.IOException;
import java.util.List;

import org.example.Services.API;
import org.jspace.*;


public class Main {

    public final static String GATE_URI = "tcp://127.0.0.1:9001/?keep";
    public final static String REMOTE_URI = "tcp://127.0.0.1:9001/Names?keep";



    public static void main(String[] argv) throws InterruptedException, IOException {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(GATE_URI);
        String[] spaces = {"Names", "QNA", "GameState"};

        for (String space : spaces) {
            repository.add(space, new SequentialSpace());
        }

        repository.put("Names", "Jakob");
        repository.put("Names", "Anton");
        repository.put("Names", "Lucas");

        repository.put("GameState", GameState.ANSWERING);

        Space remoteSpace = new RemoteSpace(REMOTE_URI);
        Object[] temp = remoteSpace.queryp(new FormalField(String.class));

        repository.put("Names", "Lucas");

        for (Object row : temp) {
            System.out.println(row);
        }

        var api = new API();
        var response = api.callUrbanDictionaryAPI();

        for (var wordDefinition : response) {
            System.out.println(wordDefinition);
        }
        repository.closeGates();
        System.exit(0);
    }


}