package org.example;

import java.io.IOException;

import org.example.Services.API;
import org.example.Services.Params;
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

        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("5");
        var response = api.callUrbanDictionaryAPI(params);

        for (var wordDefinition : response) {
            System.out.println(wordDefinition);
        }
        repository.closeGates();
        System.exit(0);
    }


}