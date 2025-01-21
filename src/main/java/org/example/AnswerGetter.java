package org.example;

import org.jspace.FormalField;
import org.jspace.Space;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.lang.Thread.sleep;
public class AnswerGetter {
    private Space answerSpace;
    private Space playerConnectionSpace;
    private CountDownLatch latch = new CountDownLatch(1);
    private List<UserAnswerWithTimestamp> answersWithTimestamps;
    private int waitTime;

    public AnswerGetter(Space answerSpace, Space playerSpace, int waitTime) {
        this.answersWithTimestamps = new LinkedList<>();
        this.answerSpace = answerSpace;
        this.playerConnectionSpace = playerSpace;
        this.waitTime = waitTime;
    }

    public List<UserAnswerWithTimestamp> getAnswers() {
        latch = new CountDownLatch(1); // Reset latch yo

        InternalTimer internalTimer = new InternalTimer(waitTime, latch);
        AnswerCounter answerCounter = new AnswerCounter(playerConnectionSpace, latch, answerSpace);



        internalTimer.start();
        answerCounter.start();

        try {
            latch.await();

            if (internalTimer.isAlive()) internalTimer.interrupt();
            if (answerCounter.isAlive()) answerCounter.interrupt();

            internalTimer.join();
            answerCounter.join();

            answersWithTimestamps = answerCounter.getAnswers();
        } catch (InterruptedException e) {
            System.out.println("Main thread was interrupted during answer collection.");
            throw new Error(e);
        }

        if (answersWithTimestamps == null) {
            throw new NullPointerException("Answers list was null. Something went wrong.");
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
    private final Space playerSpace;
    private final List<UserAnswerWithTimestamp> answers;
    private final CountDownLatch latch;
    private final Space answerSpace;
    private boolean done = false;

    AnswerCounter(Space playerSpace, CountDownLatch latch, Space answerSpace) {
        this.playerSpace = playerSpace;
        this.latch = latch;
        this.answerSpace = answerSpace;
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
        if (answerSpace == null) {
            throw new NullPointerException("Class was not instantiated correctly, or space passed was null");
        }
        while (answers.size() < playerSpace.size()) {
            System.out.println("Answers size is:" + answers.size());
            try {
                Object[] tuple = answerSpace.get(new FormalField(String.class), new FormalField(String.class)); //TODO: Check if this pattern works
                String answerString = (String) tuple[0];
                String ID = (String) tuple[1];
                long timeStamp = System.currentTimeMillis();
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