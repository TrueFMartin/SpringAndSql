package com.truefmartin.inverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Holds the buffered reader and the current entry. One SortedBuffer for each temporary sorted file
public class SortedBuffer {
    private final int BUFFERED_READER_SIZE;
    BufferedReader reader;

    FileEntry entry;

    boolean isClosed = true;

    boolean termUsed = false;
    Path path;
    public SortedBuffer(String fileName, int docID, int bufferSize) {
        this.path = Path.of(fileName);
        this.entry = new FileEntry(docID);
        this.BUFFERED_READER_SIZE = bufferSize;
    }

    public void open(){
        InputStreamReader stream = null;
        try {
            stream = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.reader = new BufferedReader(stream, BUFFERED_READER_SIZE);
        this.isClosed = false;
        // Load first 'entry'
        next();
    }

    // Progress to next line
    public void next() {
        String line = null;
        try {
            line = reader.readLine();
            if (line != null) {
                String[] splitLine = line.split(" ");
                this.entry.term = splitLine[0];
                this.entry.freq = Double.parseDouble(splitLine[1]);
            } else {
                reader.close();
                this.isClosed = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Handle odd errors, get the posting # so that the tokenizer can be modified to accommodate.
            throw new ArrayIndexOutOfBoundsException("Error in Next: " + line + " " +
                    "with " + this.entry.docID);
        }
    }
    public FileEntry getEntry() {
        return entry;
    }
}

