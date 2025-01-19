package org.example;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.*;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        SpaceRepository repository = new SpaceRepository();
        // ['NAME', 'UUID']
        SequentialSpace playerConnectionSpace = new SequentialSpace();
        // ['TEXT', int] - int denotes the type. 0 for wrong, 1 for right, 2 for meaning
        RandomSpace questionSpace = new RandomSpace();
        // ['TEXT', 'UUID', int] - int is the time it takes to give an answer - supplied by client
        SequentialSpace answerSpace = new SequentialSpace();
        // [int, 'UUID']
        SequentialSpace scoreboardSpace = new SequentialSpace();
        // ['STATE']
        SequentialSpace gameStateSpace = new SequentialSpace();

        // Add spaces to the repository
        repository.add("playerConnectionSpace", playerConnectionSpace);
        repository.add("questionSpace", questionSpace);
        repository.add("answerSpace", answerSpace);
        repository.add("scoreboardSpace", scoreboardSpace);
        repository.add("gameStateSpace", gameStateSpace);

        // Open a gate for remote connections
        repository.addGate("tcp://localhost:9001/?keep");

        // Start threads
        new Thread(new PlayerConnectionThread(playerConnectionSpace, gameStateSpace)).start();
        new Thread(new QuestionThread(questionSpace, gameStateSpace)).start();
        new Thread(new AnswerThread(answerSpace, playerConnectionSpace, gameStateSpace)).start();
        new Thread(new ScoreboardThread(scoreboardSpace, playerConnectionSpace, questionSpace, gameStateSpace, answerSpace)).start();
    }
}



class PlayerConnectionThread implements Runnable {
    private final SequentialSpace playerConnectionSpace;
    private final SequentialSpace gameStateSpace;

    public PlayerConnectionThread(SequentialSpace playerConnectionSpace, SequentialSpace gameStateSpace) {
        this.playerConnectionSpace = playerConnectionSpace;
        this.gameStateSpace = gameStateSpace;
    }

    @Override
    public void run() {
        try {
            // TODO: Implement heartbeat or something to auto remove inactive players
            while (true) {
                if (playerConnectionSpace.queryAll(new FormalField(String.class), new FormalField(String.class)).size() > 1
                        && gameStateSpace.getp(new ActualField("STOP")) != null) {
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("QUESTIONS");
                }
                else if (playerConnectionSpace.queryAll(new FormalField(String.class), new FormalField(String.class)).size() < 2) {
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("STOP");
                }

                Thread.sleep(1000); // Simulate delay
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class QuestionThread implements Runnable {
    private final RandomSpace questionSpace;
    private final SequentialSpace gameStateSpace;

    public QuestionThread(RandomSpace questionSpace, SequentialSpace gameStateSpace) {
        this.questionSpace = questionSpace;
        this.gameStateSpace = gameStateSpace;
    }

    @Override
    public void run() {
        int questionId = 0;
        int countOfRounds = 0;

        try {
            while (true) {
                // TODO: I made a mistake. We are showing scoreboard each round... If you don't think too hard about it, it kinda makes sense
                // If you fix this, implement a way to show the correct question on client.. Is easy.
                //if (countOfRounds == 10) {
                //    gameStateSpace.getAll(new FormalField(String.class));
                //    gameStateSpace.put("SCOREBOARD");
                //    countOfRounds = 0;
                //}
                gameStateSpace.query(new ActualField("QUESTIONS"));
                countOfRounds++;
                getNewQnA(questionSpace);
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("ANSWERING");

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getNewQnA(RandomSpace questionSpace) throws InterruptedException {

        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("3");
        List<WordDefinition> response = api.callUrbanDictionaryAPI(params);

        while (response.size() < 3) {
            response = api.callUrbanDictionaryAPI(params);
        }

        questionSpace.getAll(new FormalField(String.class), new FormalField(Integer.class));
        String meaning = response.get(0).getMeaning();
        String word1 = response.get(0).getWord();
        String word2 = response.get(1).getWord();
        String word3 = response.get(2).getWord();
        questionSpace.put(word1, 1);
        questionSpace.put(word2, 0);
        questionSpace.put(word3, 0);
        questionSpace.put(meaning, 2);
    }
}

// Thread 3: Answer Thread
class AnswerThread implements Runnable {
    private final SequentialSpace answerSpace;
    private final SequentialSpace playerConnectionSpace;
    private final SequentialSpace gameStateSpace;

    public AnswerThread(SequentialSpace answerSpace, SequentialSpace playerConnectionSpace, SequentialSpace gameStateSpace) {
        this.answerSpace = answerSpace;
        this.playerConnectionSpace = playerConnectionSpace;
        this.gameStateSpace = gameStateSpace;
    }

    @Override
    public void run() {
        int timer = 0;
        try {
            while (true) {
                // TODO: Fill in string fields player when u know
                gameStateSpace.query(new ActualField("ANSWERING"));
                // System.out.println(timer);
                boolean no_none_missing = true;
                for (Object[] player : playerConnectionSpace.queryAll(new FormalField(String.class), new FormalField(String.class))) {
                    var player_answered = answerSpace.queryp(new FormalField(String.class), new ActualField(player[1]), new FormalField(Integer.class));


                    if (player_answered == null) {
                        no_none_missing = false;
                    }
                }


                timer += 1;
                if (no_none_missing || timer > 30) {
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("SCOREBOARD");
                    timer = 0;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Thread 4: Scoreboard Thread
class ScoreboardThread implements Runnable {
    private final SequentialSpace scoreBoardSpace;
    private final SequentialSpace playerConnectionSpace;
    private final SequentialSpace gameStateSpace;
    private final SequentialSpace questionSpace;
    private final SequentialSpace answerSpace;

    public ScoreboardThread(SequentialSpace scoreBoardSpace, SequentialSpace playerConnectionSpace, RandomSpace questionSpace, SequentialSpace gameStateSpace, SequentialSpace answerSpace) {
        this.scoreBoardSpace = scoreBoardSpace;
        this.playerConnectionSpace = playerConnectionSpace;
        this.questionSpace = questionSpace;
        this.gameStateSpace = gameStateSpace;
        this.answerSpace = answerSpace;
    }

    @Override
    public void run() {
        Integer timer = 0;
        try {
            while (true) {
                gameStateSpace.query(new ActualField("SCOREBOARD"));
                String correct_answer = (String) questionSpace.query(new FormalField(String.class), new ActualField(1))[0];

                for (Object[] player : playerConnectionSpace.queryAll(new FormalField(String.class), new FormalField(String.class))) {
                    Object[] answer = answerSpace.get(new FormalField(String.class), new ActualField((String) player[1]), new FormalField(Integer.class));
                    if (Objects.equals((String) answer[0], correct_answer)) {
                        Object[] prevScore = scoreBoardSpace.getp(new FormalField(Integer.class), new ActualField((String) player[1]));

                        scoreBoardSpace.put((Integer) prevScore[0] + calculateScore((Integer) answer[2], 30), (String) player[1]);
                    }

                }
                while (timer < 5) {
                    timer += 1;
                    Thread.sleep(1000); // Update scoreboard periodically
                }
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("QUESTIONS");
                timer = 0;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Integer calculateScore(Integer responseTime, Integer waitTime) {
        return (Integer) Math.max(0, (100 - (responseTime / waitTime)) * 100);
    }
}