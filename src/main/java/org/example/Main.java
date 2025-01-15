package org.example;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.List;


public class Main {

    public static void main(String[] argv) throws InterruptedException, IOException {
        Repository r = new Repository();
        Space playerSpace = r.getPlayerSpace();
        Space questionSpace = r.getQuestionSpace();
        Space gameStateSpace = r.getGameStateSpace();
        Space AnswerSpace = r.getAnswerSpace();
        int countOfRounds = 0;


        while (true) {
            countOfRounds++;
            String correctAnswer = getNewQnA(questionSpace);
            gameStateSpace.put("answering");

            // Here threading for waiting for answers

            // Process answers and give points
            updateScore();


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

    private static void updateScore() {

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