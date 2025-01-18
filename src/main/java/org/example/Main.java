package org.example;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.io.IOException;
import java.util.*;


public class Main {

    private static Map<String, Integer> stalePlayers = new HashMap<String, Integer>();


    public static void main(String[] argv) throws InterruptedException, IOException {
        while (true) {
            Repository r = new Repository();
            Space playerSpace = r.getPlayerSpace();
            Space questionSpace = r.getQuestionSpace();
            Space gameStateSpace = r.getGameStateSpace();
            Space answerSpace = r.getAnswerSpace();

            AnswerGetter answerGetter = new AnswerGetter(answerSpace);

            int waitTime = 30;
            int countOfRounds = 0;
            playerSpace.put("Player", UUID.randomUUID().toString(), 0);

            System.out.println("waiting player 1");
            Object[] firstplayer = playerSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
            System.out.println("waiting player 2");
            playerSpace.query(new FormalField(String.class), new FormalField(String.class), new FormalField(Integer.class));
            playerSpace.put(firstplayer);

            while (true) {
                countOfRounds++;
                String correctAnswer = getNewQnA(questionSpace);
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("ANSWERING");
                long startTimestamp = System.currentTimeMillis();
                Object[] tuple = gameStateSpace.query(new FormalField(String.class));
                System.out.println(tuple[0]);

                List<UserAnswerWithTimestamp> answersWrapper = answerGetter.getAnswers(waitTime, playerSpace.size());
//                System.out.println(playerSpace.size());
//                checkForStalePlayers(answersWrapper, playerSpace);
//                System.out.println(playerSpace.size());

                updateAllScores(answersWrapper, startTimestamp, correctAnswer, playerSpace, waitTime);
                System.out.println("After score update");

                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("SHOWING");
                Object[] tuple1 = gameStateSpace.query(new FormalField(String.class));
                System.out.println(tuple1[0]);

                Thread.sleep(5000);

                if (countOfRounds == 10) {
                    System.out.println("Round 10");
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("FINAL");
                    Object[] tuple2 = gameStateSpace.query(new FormalField(String.class));
                    System.out.println(tuple2[0]);
                    Thread.sleep(5000);
                    countOfRounds = 0;
                    if (playerSpace.size() < 2) {
                        break;
                    }
                }
            }
        }
    }

    private static void checkForStalePlayers(List<UserAnswerWithTimestamp> answers, Space playerSpace) {
        Set<String> playerIDs = new HashSet<>();
        try {
            // Fetch all players currently in the playerSpace
            List<Object[]> rawData = playerSpace.queryAll();
            for (Object[] tuple : rawData) {
                playerIDs.add((String) tuple[1]);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e + " This shouldn't be interrupted");
        }

        // Create a set of IDs from players who answered this round
        Set<String> playersWhoAnswered = new HashSet<>();
        for (UserAnswerWithTimestamp answer : answers) {
            playersWhoAnswered.add(answer.ID);
        }

        // Update the stalePlayers map
        for (String playerID : playerIDs) {
            if (!playersWhoAnswered.contains(playerID)) {
                // Increment the count for players who did not answer
                stalePlayers.put(playerID, stalePlayers.getOrDefault(playerID, 0) + 1);
            } else {
                // Reset the count for players who did answer
                stalePlayers.put(playerID, 0);
            }
        }

        // Remove players who have been stale for 2 or more rounds
        for (Iterator<Map.Entry<String, Integer>> iterator = stalePlayers.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Integer> entry = iterator.next();
            if (entry.getValue() >= 2) {
                System.out.println("Player " + entry.getKey() + " has been removed for being inactive for 2 rounds.");
                try {
                    playerSpace.get(new FormalField(String.class), new ActualField(entry.getKey()), new FormalField(Integer.class));
                    System.out.printf("Player has been removed");
                } catch (InterruptedException e) {
                    throw new RuntimeException("Error removing stale player: " + e.getMessage());
                }
                iterator.remove();
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

