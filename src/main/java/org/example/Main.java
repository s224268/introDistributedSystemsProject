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



    public static void main(String[] argv) throws InterruptedException {
        SpaceRepository repository = new SpaceRepository();
        repository.addGate(GATE_URI);
        String[] spaces = {"Players", "Question", "GameState", "Answers",};
        String correctAnswer = "";

        for (String space : spaces) {
            repository.add(space, new SequentialSpace());
        }

        Space playerSpace = repository.get("Players");
        Space questionSpace = repository.get("Question");
        Space gameStateSpace = repository.get("GameState");
        Space AnswerSpace = repository.get("Answers");
        int countOfRounds = 0;


        while (true) {
            countOfRounds++;
            correctAnswer = getNewQnA(questionSpace);
            gameStateSpace.put("answering");

            // Here threading for waiting for answers

            // Process answers and give points
            updateScores();


            gameStateSpace.getAll(new FormalField(String.class));
            gameStateSpace.put("showing");

            //Wait here for showing

            if (countOfRounds == 10) {
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("final");

                //wait to show scoreboard here


                countOfRounds = 0;

            }

        }
    }

    private static void updateScores() {

    }



    private static String getNewQnA(Space question) throws InterruptedException {

        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("3");
        List<WordDefinition> response = api.callUrbanDictionaryAPI(params);

        if (response.size() < 3) {
            System.out.println("Not enough words retrieved from the API.");
            return "Not enough words retrieved from the API.";
        }

        question.getAll(new FormalField(Object.class), new FormalField(Object.class), new FormalField(Object.class), new FormalField(Object.class));
        String meaning = response.get(0).getMeaning();
        String word1 = response.get(0).getWord();
        String word2 = response.get(1).getWord();
        String word3 = response.get(2).getWord();
        question.put(meaning, word1, word2, word3);
        return word1;
    }
}