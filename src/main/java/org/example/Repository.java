package org.example;

import org.jspace.SequentialSpace;
import org.jspace.Space;
import org.jspace.SpaceRepository;

import java.io.IOException;

public class Repository {

    public final static String GATE_URI = "tcp://127.0.0.1:9001/?keep";

    private final Space playerSpace;
    private final Space questionSpace;
    private final Space gameStateSpace;
    private final Space answerSpace;

    public Repository() throws IOException {
        SpaceRepository spaceRepository = new SpaceRepository();
        spaceRepository.addGate(GATE_URI);
        String[] spaces = {"Players", "Question", "GameState", "Answers"};

        for (String space : spaces) {
            spaceRepository.add(space, new SequentialSpace());
        }

        playerSpace = spaceRepository.get("Players");
        questionSpace = spaceRepository.get("Question");
        gameStateSpace = spaceRepository.get("GameState");
        answerSpace = spaceRepository.get("Answers");
    }

    public Space getPlayerSpace() {
        return playerSpace;
    }

    public Space getQuestionSpace() {
        return questionSpace;
    }

    public Space getGameStateSpace() {
        return gameStateSpace;
    }

    public Space getAnswerSpace() {
        return answerSpace;
    }
}