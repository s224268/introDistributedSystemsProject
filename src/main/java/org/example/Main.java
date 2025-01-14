package org.example;

import java.io.IOException;
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

        repository.put("Names", "Jakob");
        repository.put("Names", "Anton");
        repository.put("Names", "Lucas");

        repository.put("GameState", GameState.ANSWERING);

        Space nameSpace = new RemoteSpace(NAMES_URI);
        Object[] temp = nameSpace.queryp(new FormalField(String.class));

        repository.put("Names", "Lucas");

        for (Object row : temp) {
            System.out.println(row);
        }
        Space QnASpace = new RemoteSpace(QNA_URI);
        QnASpace.put("Word: TEST", "Meaning: TEST");
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
    }
}