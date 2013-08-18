/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dragoncurve.util;

/**
 * Cronometro de uso geral.
 *
 *
 */
public abstract class Timer {

    private long timeElapsed;
    private long tick;
    private long count;
    private long lastCount;
    private boolean paused = false;
    private boolean consumed = false;

    public Timer(long milis) {
        if (milis <= 0) {
            throw new IllegalArgumentException("milis <= 0");
        }
        tick = milis;
    }

    public Timer(long milis, boolean paused) {
        this(milis);
        this.paused = paused;
    }

    public synchronized void reset() {
        timeElapsed = 0;
        lastCount = 0;
        count = 0;
    }

    public void start() {
        paused = false;
    }

    public void pause(boolean state) {
        paused = state;
    }

    public boolean isPaused() {
        return paused;
    }

    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public synchronized boolean increase(long milis) {
        if (!paused) {
            timeElapsed += milis;
            lastCount = count;
            count = timeElapsed / tick;
            if (lastCount != count) {
                run();
            }
        }
        return consumed;
    }

    public synchronized long getTimeElapsed() {
        return timeElapsed;
    }

    public synchronized long getCount() {
        return count;
    }

    public abstract void run();
}
