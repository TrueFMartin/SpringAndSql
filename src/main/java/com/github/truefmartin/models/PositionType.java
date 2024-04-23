package com.github.truefmartin.models;

import java.util.ArrayList;

public enum PositionType {
    quarterback,
    running_back,
    wide_receiver,
    tight_end,
    defensive_end,
    linebacker,
    cornerback,
    safety;

    private final String displayValue;

    PositionType() {
        this.displayValue = Inner.positionTypesDisplay.get(this.ordinal());
    }

    public String getDisplayValue() {
        return displayValue;
    }

    private static class Inner{
        static final ArrayList<String> positionTypesDisplay = new ArrayList<>() {
            {
                add("Quarterback");
                add("Running Back");
                add("Wide Receiver");
                add("Tight End");
                add("Defensive End");
                add("Linebacker");
                add("Cornerback");
                add("Safety");
            }
        };


    }

}

