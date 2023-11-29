package com.truefmartin.querier;

import com.truefmartin.inverter.InvertedFileReader;
import com.truefmartin.inverter.InvertedFileWriter;
import com.truefmartin.inverter.structs.DictData;

import java.util.concurrent.Callable;

public class ReadDictThread implements Callable<DictData> {
    private final String term;

    ReadDictThread(String term) {
        this.term = term;

    }
    @Override
    public DictData call() throws Exception {
        try (var dictReader = new InvertedFileReader(InvertedFileWriter.FileType.DICT)){
            return dictReader.findInDict(term, x -> Math.abs(x.hashCode()));
        }
    }
}
