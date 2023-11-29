package com.truefmartin.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopWords {
    private static List<String> stopList;
    private static final StopWords INSTANCE = new StopWords();
    private StopWords(){
        stopList = loadStopWords("stoplist.txt", 102);
    }

    public static StopWords getInstance(){
        return INSTANCE;
    }

    private static List<String> loadStopWords(String fileName, int numWords) {
        final List<String> stopList = new ArrayList<>(numWords);
        try (FileReader fr = new FileReader(fileName); BufferedReader br = new BufferedReader(fr, numWords*50)) {
            String line;
            while ((line = br.readLine()) != null) {
                stopList.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stopList;
    }

    public List<String> getStopList() {
        return stopList;
    }

}
