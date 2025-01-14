package org.example;

import java.io.IOException;
import java.util.List;

import org.example.Services.API;
import org.example.Services.Params;
import org.jspace.*;


public class Main {

    public final static String GATE_URI = "tcp://127.0.0.1:9001/?keep";
    public final static String NAMES_URI = "tcp://127.0.0.1:9001/Names?keep";
    public final static String QNA_URI = "tcp://127.0.0.1:9001/QnA?keep";



    public static void main(String[] argv) throws InterruptedException, IOException {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(GATE_URI);
        String[] spaces = {"Names", "QnA", "GameState"};

        for (String space : spaces) {
            repository.add(space, new SequentialSpace());
        }

        Space nameSpace = repository.get("Names");
        Space QnASpace = repository.get("QnA");
        Space gameStateSpace = repository.get("GameState");


        nameSpace.put("Jakob");
        nameSpace.put("Anton");

        List<Object[]> temp = nameSpace.queryAll(new FormalField(String.class));


        for (Object row : temp) {
            Object[] tuple = (Object[]) row;
            System.out.println(tuple[0]);
        }

        getNewQnA(QnASpace);



        repository.closeGates();
        System.exit(0);
    }




    /*
    Her skal vi vel have sådan at vi kun vælger en af ordene som det "rigtige" og sender "meaning" af det.
     */
    private static void getNewQnA(Space space) throws InterruptedException {
        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("3");
        var response = api.callUrbanDictionaryAPI(params);
        space.getAll(new FormalField(Object.class), new FormalField(Object.class));
        for (var word : response) {
            space.put(word.getWord(), word.getMeaning());
        }


        /* For testing purposes
        // Print the contents of the space
        List<Object[]> entries = space.queryAll(new FormalField(String.class), new FormalField(String.class));
        for (Object entry : entries) {
            Object[] tuple = (Object[]) entry;
            System.out.println("Word: " + tuple[0] + ", Meaning: " + tuple[1]);
        }
        */
    }
}