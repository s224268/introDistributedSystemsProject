package org.example;

import org.jspace.FormalField;
import org.jspace.Space;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;
public class AnswerGetter {
    private Space space;
    private CountDownLatch latch = new CountDownLatch(1);
    private List<UserAnswerWithTimestamp> answersWithTimestamps;

    public AnswerGetter(Space space) {
        this.answersWithTimestamps = new LinkedList<>();
        this.space = space;
    }

    public List<UserAnswerWithTimestamp> getAnswers(int waitTime, int maxPlayerCount) {
        InternalTimer internalTimer = new InternalTimer(waitTime, latch);
        AnswerCounter answerCounter = new AnswerCounter(maxPlayerCount, latch, space);

        internalTimer.start();
        answerCounter.start();

        try {
            latch.await();
            internalTimer.interrupt();
            answerCounter.interrupt();
            internalTimer.join(); //TODO: This works right?
            answerCounter.join();
            answersWithTimestamps = answerCounter.getAnswers();
        } catch (InterruptedException e) {
            System.out.println("IDK who interrupted my thing, but you need to stop");
            throw new Error("");
        }
        if (answersWithTimestamps == null) {
            throw new NullPointerException("Idk how this happened");
        }
        return answersWithTimestamps;
    }
}

class InternalTimer extends Thread {
    private int wait;
    private CountDownLatch latch;

    InternalTimer(int wait, CountDownLatch latch) {
        this.wait = wait;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            sleep(wait * 1000L);
        } catch (InterruptedException e) {
            System.out.println("Timer was stopped prematurely because all players answered. This is fine");
            return;
        }
        latch.countDown();
    }
}

class AnswerCounter extends Thread {
    private final int maxPlayerCount;
    private final List<UserAnswerWithTimestamp> answers;
    private final CountDownLatch latch;
    private Space space;
    private boolean done = false;

    AnswerCounter(int maxPlayerCount, CountDownLatch latch, Space space) {
        this.maxPlayerCount = maxPlayerCount;
        this.latch = latch;
        this.space = space;
        answers = new LinkedList<UserAnswerWithTimestamp>();
    }

    List<UserAnswerWithTimestamp> getAnswers() {
        if (!done) {
            throw new RuntimeException("Called getAnswers before thread was fully finished: Join before calling");
        }
        return answers;
    }

    @Override
    public void run() {
        if (space == null) {
            throw new NullPointerException("Class was not instantiated correctly, or space passed was null");
        }
        while (answers.size() < maxPlayerCount) {
            try {

                Object[] tuple = space.get(new FormalField(String.class), new FormalField(String.class), new FormalField(Long.class)); //TODO: Check if this pattern works
                String answerString = (String) tuple[0];
                String ID = (String) tuple[1];
                Long timeStamp = System.currentTimeMillis();
                UserAnswerWithTimestamp ans = new UserAnswerWithTimestamp();
                ans.answer = answerString;
                ans.ID = ID;
                ans.timeStamp = timeStamp;
                answers.add(ans); //Should be atomic

            } catch (InterruptedException e) {
                System.out.println("Answer count was not reached in time");
                break;
            }
        }
        done = true;
        latch.countDown();

    }
}