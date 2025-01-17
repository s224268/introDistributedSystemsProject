package org.example;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.ActualField;
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
        Space answerSpace = r.getAnswerSpace();

        AnswerGetter answerGetter = new AnswerGetter(answerSpace);
        int waitTime = 30;
        int countOfRounds = 0;


        while (true) {

            for (int i = 0; i < 2; i++) {
                playerSpace.query(new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
            }


            countOfRounds++;
            String correctAnswer = getNewQnA(questionSpace);
            gameStateSpace.put("ANSWERING");
            long startTimestamp = System.currentTimeMillis();

            // Here threading for waiting for answers

            List<UserAnswerWithTimestamp> answersWrapper = answerGetter.getAnswers(waitTime, playerSpace.size()); // ANTON HERE
            updateAllScores(answersWrapper, startTimestamp, correctAnswer, playerSpace, waitTime);


            gameStateSpace.getAll(new FormalField(String.class));
            gameStateSpace.put("SHOWING");

            Thread.sleep(5000);

            if (countOfRounds == 10) {
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("FINAL");
                Thread.sleep(5000);
                countOfRounds = 0;
            }
        }
    }


    private static void updateAllScores(List<UserAnswerWithTimestamp> answerWrapper, long startTimestamp, String correctAnswer, Space players, int waitTime) {
        for (UserAnswerWithTimestamp answer : answerWrapper) {
            int score = calculateScore(answer, startTimestamp, correctAnswer, waitTime);
            try {
                updateScore(players, answer.ID, score);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int calculateScore(UserAnswerWithTimestamp answerWrapper, long startTimestamp, String correctAnswer, int waitTime) {
        long responseTime = answerWrapper.timeStamp - startTimestamp;
        int points = 0;

        if (answerWrapper.answer.equalsIgnoreCase(correctAnswer)) {
            points = (int) Math.max(0, (100 - (responseTime / waitTime)) * 100);
        }
        return points;
    }

    private static void updateScore(Space players, String playerID, int score) throws InterruptedException {
        Object[] tuple = players.get(new FormalField(String.class), new ActualField(playerID), new FormalField(Integer.class));
        int newScore = (int) tuple[2] + score;
        players.put(tuple[0], tuple[1], newScore);
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

