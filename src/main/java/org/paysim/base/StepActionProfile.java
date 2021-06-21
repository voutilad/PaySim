package org.paysim.base;

import org.paysim.output.Output;

import java.util.ArrayList;

public class StepActionProfile {
    private final String action;
    private final long step, month, day, hour, count;
    private final double totalSum, avgAmount, stdAmount;

    public StepActionProfile(long step, String action, long month, long day, long hour, long count, double totalSum, double avgAmount, double stdAmount) {
        this.step = step;
        this.action = action;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.count = count;
        this.totalSum = totalSum;
        this.avgAmount = avgAmount;
        this.stdAmount = stdAmount;
    }

    public long getCount() {
        return count;
    }

    public double getAvgAmount() {
        return avgAmount;
    }

    public double getStdAmount() {
        return stdAmount;
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();

        properties.add(action);
        properties.add(String.valueOf(month));
        properties.add(String.valueOf(day));
        properties.add(String.valueOf(hour));

        properties.add(String.valueOf(count));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, totalSum));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, avgAmount));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, stdAmount));

        properties.add(String.valueOf(step));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}