package org.example;

import static java.lang.Thread.sleep;

import org.example.Services.API;
import org.example.Services.Params;
import org.example.Services.WordDefinition;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RandomSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) throws Exception {
        SpaceRepository repository = new SpaceRepository();

        // ['NAME', 'UUID']
        SequentialSpace playerConnectionSpace = new SequentialSpace();
        // ['TEXT', Integer] - int denotes the type. 0 for wrong, 1 for right, 2 for meaning
        RandomSpace questionSpace = new RandomSpace();

        // ['TEXT', 'UUID'] - Answer from client
        // ['TEXT', 'UUID', Long] - Answer with timestampo added - Long is the time it takes to give an answer
        SequentialSpace answerSpace = new SequentialSpace();
        // [Integer, 'UUID']
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
        repository.addGate("tcp://0.0.0.0:9001/?keep");

        // Start threads
        new Thread(new PlayerConnectionThread(playerConnectionSpace, gameStateSpace, scoreboardSpace)).start();
        new Thread(new QuestionThread(questionSpace, gameStateSpace)).start();
        new Thread(new AnswerThread(answerSpace, playerConnectionSpace, gameStateSpace)).start();
        new Thread(new ScoreboardThread(scoreboardSpace, playerConnectionSpace, questionSpace, gameStateSpace, answerSpace, 10)).start();
        System.out.println("Repositories created and worker threads started, awaiting players");
    }
}

class PlayerConnectionThread implements Runnable {
    private final SequentialSpace playerConnectionSpace;
    private final SequentialSpace gameStateSpace;
    private final Space scoreboardSpace;

    public PlayerConnectionThread(SequentialSpace playerConnectionSpace, SequentialSpace gameStateSpace, Space scoreboardSpace) {
        this.playerConnectionSpace = playerConnectionSpace;
        this.gameStateSpace = gameStateSpace;
        this.scoreboardSpace = scoreboardSpace;
    }

    @Override
    public void run() {
        try {
            while (playerConnectionSpace.size() < 2) {
                sleep(200);
            }
            gameStateSpace.put("QUESTIONS");

            /*
            if (playerConnectionSpace.size()>1) {
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("SHOWING");
            }
            while (true) {
                if (playerConnectionSpace.size() > 1
                        && gameStateSpace.getp(new ActualField("STOP")) != null) {
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("QUESTIONS");
                    System.out.println("Setting game state to QUESTIONS");

                } else if (playerConnectionSpace.size() < 2) {
                    gameStateSpace.getAll(new FormalField(String.class));
                    gameStateSpace.put("STOP");
                    scoreboardSpace.getAll(new FormalField(Integer.class), new FormalField(String.class));
                    System.out.println("Setting game state to STOP; Game is paused");
                }
                sleep(1000); // Simulate delay
            }*/
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

    private static void getNewQnA(RandomSpace questionSpace) throws InterruptedException {
        var api = API.getInstance();
        Params params = new Params();
        params.setLimit("3");
        List<WordDefinition> response; // = api.callUrbanDictionaryAPI(params);
        String meaning = "";//response.get(0).getMeaning();
        String trueDef = "";//response.get(0).getWord();
        String word2 = "";//response.get(1).getWord();
        String word3 = "";//response.get(2).getWord();

        int tries = 0;
        do {
            tries++;
            response = api.callUrbanDictionaryAPI(params);
            questionSpace.getAll(new FormalField(String.class), new FormalField(Integer.class));
            meaning = response.get(0).getMeaning();
            trueDef = response.get(0).getWord();
            word2 = response.get(1).getWord();
            word3 = response.get(2).getWord();
            int i = 1;
            while (meaning.length() > 700 && i < 4) {
                System.out.println("Meaning wasn't approved, retrying.");
                meaning = response.get(i).getMeaning();
                i++;
            }
        } while (response.size() < 3 || (trueDef.length() > 200 || word2.length() > 200 || word3.length() > 200) && tries < 15);

        meaning = cleanMeaning(meaning, trueDef);

        questionSpace.put(trueDef, 1);
        questionSpace.put(word2, 0);
        questionSpace.put(word3, 0);
        questionSpace.put(meaning, 2);
    }

    private static String cleanMeaning(String stringToClean, String toCleanFor) {

        Pattern pattern = Pattern.compile(Pattern.quote(toCleanFor), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(stringToClean);

        // Replace all case-insensitive occurrences of `toCleanFor` with "________"
        return matcher.replaceAll("________");
    }

    @Override
    public void run() {

        try {
            while (true) {
                gameStateSpace.query(new ActualField("QUESTIONS"));
                getNewQnA(questionSpace);
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("ANSWERING");
                System.out.println("Setting game state to ANSWERING");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Thread 3: Answer Thread
class AnswerThread implements Runnable {
    private final SequentialSpace answerSpace;
    private final SequentialSpace playerConnectionSpace;
    private final SequentialSpace gameStateSpace;
    private final AnswerGetter answerGetter;

    public AnswerThread(SequentialSpace answerSpace, SequentialSpace playerConnectionSpace, SequentialSpace gameStateSpace) {
        this.answerSpace = answerSpace;
        this.playerConnectionSpace = playerConnectionSpace;
        this.gameStateSpace = gameStateSpace;
        this.answerGetter = new AnswerGetter(answerSpace, playerConnectionSpace, 30);
    }

    @Override
    public void run() {

        try {
            while (true) {

                gameStateSpace.query(new ActualField("ANSWERING"));
                Long currentTime = System.currentTimeMillis();
                List<UserAnswerWithTimestamp> answersWithTimestamps = answerGetter.getAnswers();
                for (UserAnswerWithTimestamp answerWithTimestamp : answersWithTimestamps) {
                    System.out.println("To answerSpace, adding: " + answerWithTimestamp.answer + " " + answerWithTimestamp.ID + " " + answerWithTimestamp.timeStamp);
                    answerSpace.put(answerWithTimestamp.answer, answerWithTimestamp.ID, (answerWithTimestamp.timeStamp - currentTime));
                }

                gameStateSpace.getAll(new ActualField("ANSWERING"));

                gameStateSpace.put("SHOWING"); //Creating a new thread for this seems overkill
                System.out.println("Setting game state to SHOWING");

                Thread.sleep(3000L);
                gameStateSpace.get(new ActualField("SHOWING"));
                gameStateSpace.put("SCOREBOARD");
                System.out.println("Setting game state to SCOREBOARD");

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
    private final int maxRounds;

    public ScoreboardThread(SequentialSpace scoreBoardSpace, SequentialSpace playerConnectionSpace, RandomSpace questionSpace, SequentialSpace gameStateSpace, SequentialSpace answerSpace, int maxRounds) {
        this.scoreBoardSpace = scoreBoardSpace;
        this.playerConnectionSpace = playerConnectionSpace;
        this.questionSpace = questionSpace;
        this.gameStateSpace = gameStateSpace;
        this.answerSpace = answerSpace;
        this.maxRounds = maxRounds;
    }

    private static Integer calculateScore(Long responseTime, Integer waitTime) {

        Long waitTimeInMillis = waitTime * 1000L;
        // Perform the calculation using floating-point division
        double score = Math.max(0, 100 - ((double) responseTime / waitTimeInMillis) * 100);
        // Cast to Integer and return
        return (int) score;
    }

    @Override
    public void run() {

        try {
            int round = 0;
            while (true) {
                round++;
                System.out.println("Current round: " + round);
                gameStateSpace.query(new ActualField("SCOREBOARD"));
                String correct_answer = (String) questionSpace.query(new FormalField(String.class), new ActualField(1))[0];

                List<Object[]> allAnswers = answerSpace.getAll(new FormalField(String.class), new FormalField(String.class), new FormalField(Long.class));

                for (Object[] _answer : allAnswers) {
                    String answerWord = (String) _answer[0];
                    String ID = (String) _answer[1];
                    Long timeTaken = (Long) _answer[2];
                    if (answerWord.equals(correct_answer)) {
                        System.out.print("Correct answer: " + correct_answer + " from: " + ID);
                        Object[] prevScore = scoreBoardSpace.getp(new FormalField(Integer.class), new ActualField(ID));
                        Integer scoreToAdd = calculateScore(timeTaken, 30);
                        System.out.print(", and therefore, their score is increased by " + scoreToAdd);
                        if (prevScore != null) {
                            scoreToAdd += (Integer) prevScore[0];
                        }
                        scoreBoardSpace.put(scoreToAdd, ID);
                        System.out.println(", now totalling " + scoreToAdd);
                    } else {
                        System.out.println("Incorrect answer: " + correct_answer + " from: " + ID);
                    }
                }


                Thread.sleep(3000);
                while (playerConnectionSpace.size() < 2) {
                    Thread.sleep(300);
                    scoreBoardSpace.getAll(new FormalField(Integer.class), new FormalField(String.class));
                }
                gameStateSpace.getAll(new FormalField(String.class));
                gameStateSpace.put("QUESTIONS");
                System.out.println("Setting game state to QUESTIONS");

                if (round == maxRounds) {
                    round = 0;
                    scoreBoardSpace.getAll(new FormalField(Integer.class), new FormalField(String.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}