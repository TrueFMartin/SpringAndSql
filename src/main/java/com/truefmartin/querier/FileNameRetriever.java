package com.truefmartin.querier;

import com.truefmartin.inverter.InvertedFileReader;
import com.truefmartin.inverter.InvertedFileWriter;
import com.truefmartin.querier.accumulator.CompareType;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class FileNameRetriever {
    public static List<AbstractMap.SimpleEntry<String, Integer>> getFileNames (CompareType[] sortedDocIDs) {
        List<AbstractMap.SimpleEntry<String, Integer>> results;
        if(sortedDocIDs.length < 100) {
            results = new ArrayList<>(sortedDocIDs.length);
            try(var mapReader = new InvertedFileReader(InvertedFileWriter.FileType.MAP)) {
                for (int i = 0, sortedDocIDsLength = sortedDocIDs.length; i < sortedDocIDsLength; i++) {
                    var doc = sortedDocIDs[i];
                    var mapEntry = mapReader.readMapRecord(doc.docID);
                    if (mapEntry != null)
                        results.add(i, new AbstractMap.SimpleEntry<>(
                                mapEntry.fileName, sortedDocIDs[i].weight));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return results;
        } else {
            // TODO Implement parallel searching of Map for larger result sizes for next HW
            return null;
        }
    }

}
