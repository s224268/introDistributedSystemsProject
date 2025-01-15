package org.example;

import java.io.IOException;
import java.util.List;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.*;


public class Main {

    public final static String GATE_URI = "tcp://127.0.0.1:9001/?keep";
    public final static String NAMES_URI = "tcp://127.0.0.1:9001/Names?keep";
    public final static String QNA_URI = "tcp://127.0.0.1:9001/QnA?keep";



    public static void main(String[] argv) throws InterruptedException, IOException {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(GATE_URI);
        String[] spaces = {"Names", "Question", "GameState", "CorrectAnswer"};

        for (String space : spaces) {
            repository.add(space, new SequentialSpace());
        }

        Space nameSpace = repository.get("Names");
        Space questionSpace = repository.get("Question");
        Space gameStateSpace = repository.get("GameState");
        Space correctAnswerSpace = repository.get("CorrectAnswer");


        nameSpace.put("Jakob");
        nameSpace.put("Anton");

        List<Object[]> temp = nameSpace.queryAll(new FormalField(String.class));


        for (Object row : temp) {
            Object[] tuple = (Object[]) row;
            System.out.println(tuple[0]);
        }

        getNewQnA(questionSpace, correctAnswerSpace);



        repository.closeGates();
        repository.shutDown();
    }


    private static void getNewQnA(Space question, Space answer) throws InterruptedException {
        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("3");
        List<WordDefinition> response = api.callUrbanDictionaryAPI(params);

        if (response.size() < 3) {
            System.out.println("Not enough words retrieved from the API.");
            return;
        }

        question.getAll(new FormalField(Object.class), new FormalField(Object.class), new FormalField(Object.class), new FormalField(Object.class));
        String meaning = response.get(0).getMeaning();
        String word1 = response.get(0).getWord();
        String word2 = response.get(1).getWord();
        String word3 = response.get(2).getWord();
        question.put(meaning, word1, word2, word3);

        answer.getAll(new FormalField(Object.class));
        answer.put(word1);

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