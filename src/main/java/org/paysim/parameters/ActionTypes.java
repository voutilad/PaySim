package org.paysim.parameters;

import org.paysim.utils.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ActionTypes {
    private static final int COLUMN_ACTION = 0, COLUMN_OCCURRENCES = 1;
    private static final Logger logger = LoggerFactory.getLogger(ActionTypes.class);
    private static Set<String> actions = new TreeSet<>();
    private static Map<String, Integer> maxOccurrencesPerAction = new HashMap<>();

    public static void loadActionTypes(String filename) {
        List<String[]> parameters = CSVReader.read(filename);

        for (String[] paramLine : parameters) {
            String action = paramLine[COLUMN_ACTION];
            actions.add(action);
        }
    }

    public static void loadMaxOccurrencesPerClient(String filename) {
        List<String[]> parameters = CSVReader.read(filename);
        int loaded = 0;
        for (String[] paramLine : parameters) {
            if (isValidAction(paramLine[COLUMN_ACTION])) {
                maxOccurrencesPerAction.put(paramLine[COLUMN_ACTION], Integer.parseInt(paramLine[COLUMN_OCCURRENCES]));
                loaded++;
            }
        }
        if (loaded != actions.size()) {
            logger.warn(String.format("missing action in %s", filename));
        }
    }

    public static int getMaxOccurrenceGivenAction(String action) {
        return maxOccurrencesPerAction.get(action);
    }

    public static boolean isValidAction(String name) {
        return actions.contains(name);
    }

    public static Set<String> getActions() {
        return actions;
    }

}
