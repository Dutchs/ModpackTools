package com.dutchs.modpacktools.debug;

public class GCTimer {
    public static final int LOGGING_LENGTH = 240;
    private final long[] loggedTimes = new long[240];
    private int logStart;
    private int logLength;
    private int logEnd;
    private final String name;

    public GCTimer(String name) {
        this.name = name;
    }

    public void logGCDuration(long pRunningTime) {
        this.loggedTimes[this.logEnd] = pRunningTime;
        ++this.logEnd;
        if (this.logEnd == 240) {
            this.logEnd = 0;
        }

        if (this.logLength < 240) {
            this.logStart = 0;
            ++this.logLength;
        } else {
            this.logStart = this.wrapIndex(this.logEnd + 1);
        }

    }

    public int scaleSampleTo(long pValue, int pScale, int pDivisor) {
        double d0 = (double) pValue / (double) (1000L / (long) pDivisor);
        return (int) (d0 * (double) pScale);
    }

    /**
     * Return the last index used when 240 frames have been set
     */
    public int getLogStart() {
        return this.logStart;
    }

    /**
     * Return the index of the next frame in the array
     */
    public int getLogEnd() {
        return this.logEnd;
    }

    /**
     * Change 240 to 0
     */
    public int wrapIndex(int pRawIndex) {
        return pRawIndex % 240;
    }

    /**
     * Return the array of frames
     */
    public long[] getLog() {
        return this.loggedTimes;
    }

    public String getName() {
        return name;
    }
}