package com.truefmartin.parser;


import com.truefmartin.IRParser;
import com.truefmartin.IRParserBaseListener;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

public class IRParserEvaluator extends IRParserBaseListener {
    private HTMLParser.SynchronizedCounter synchronizedCounter;
    private String outputFileName;
    private int numUniqueTokens;
    private int numTokens;
    private DocumentHashTable hashTable;

    private volatile static boolean failedFromHash;

    // UNUSED! but here for future use
    private static final Pattern PUNCTUATION =
            Pattern.compile("[,-_./=:;<>?@\\[\\]{|}~!\"#$^`%&'()*+]");
    private static final Pattern CONTENT_START =
            Pattern.compile("[a-zA-z]+?\\s*?=\\s*?\"\\{?");

    public IRParserEvaluator(){}
    public IRParserEvaluator(String inputFileName, String outFileDir, int hashSize, HTMLParser.SynchronizedCounter synchronizedCounter) {
        this.synchronizedCounter = synchronizedCounter;
        // Get inputFileName to be in form of "/fileName"
        if (inputFileName.lastIndexOf('/') == -1) {
            inputFileName = '/' + inputFileName;
        } else if (inputFileName.lastIndexOf('/') > 0) {
            inputFileName = inputFileName.substring(inputFileName.lastIndexOf('/'));
        }

        if(outFileDir.endsWith("/"))
            outFileDir =  outFileDir.substring(0, outFileDir.length()-1);
        // End with an output of the form "outfile/someFile.html"
        this.outputFileName = outFileDir + inputFileName;
        hashTable = new DocumentHashTable(hashSize);
    }


    // Called by every listener that has content to output,
    // Adds a new line and sets to lower case
    protected void toHashTable(String s) {
        if(s.length() > 1) {
            hashTable.insert(s.toLowerCase(), 1);
            numTokens++;
        }
    }



    // Remove ' content=" ' or ' alt=" ' from s
    private String contentStartRemove(String s) {
        return CONTENT_START.matcher(s).replaceFirst("");
    }

    // Remove ' " ' from ' someString" '
    private String contentEndRemove(String s) {
        try {
            return s.substring(0, s.lastIndexOf('"'));
        } catch (IndexOutOfBoundsException e) {
            return s;
        }
    }

    // UNUSED! but here for future use
    public static String removePunctuation(String s) {
        return PUNCTUATION.matcher(s).replaceAll("");
    }

    protected void finish() throws FileNotFoundException {
        numUniqueTokens = hashTable.getNumUniqueTerms();
        if (hashTable.hasFailed()) {
            failedFromHash = true;
            return;
        }
        String debugEnv = System.getenv("DEBUG");
        if ((debugEnv != null && debugEnv.equals("true"))) {
            System.out.println("In " + outputFileName + "--\tTotal tokens: " + numTokens + "\tUnique tokens: " + numUniqueTokens );
        }
        byte[] buffer = hashTable.printSorted(numTokens).toString().getBytes();
        try {
            FileChannel rwChannel = new RandomAccessFile(outputFileName, "rw").getChannel();
            ByteBuffer wrBuf = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, buffer.length);
            wrBuf.put(buffer);
            rwChannel.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        this.synchronizedCounter.increaseNumTokens(numTokens);
        this.synchronizedCounter.increaseNumUniqueTokens(numUniqueTokens);
    }

    // At end of document, write everything to output file
    @Override
    public void exitDocument(IRParser.DocumentContext ctx) {
        try {
            this.finish();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Clean text that needs no modification
    @Override
    public void exitOutOfTagClean(IRParser.OutOfTagCleanContext ctx) {
        toHashTable(ctx.getText());
    }

    // The text that is in the content part of: <IMG content="XXX somethingElse", or <IMG alt="XXX somethingElse"
    // Clean up the XXX
    @Override
    public void exitContentText(IRParser.ContentTextContext ctx) {
        if (ctx.IN_TAG_URL() != null) {
            toHashTable(contentEndRemove(contentStartRemove(ctx.IN_TAG_URL().toString())));
            // If it's a URL, it won't have a 'CONTENT_START' and 'CONTENT_END'
        } else {
            toHashTable(contentStartRemove(ctx.CONTENT_START().toString()));
        }
    }

    // Print the rest of content="somethingElse XXX"
    @Override
    public void exitContentOptions(IRParser.ContentOptionsContext ctx) {
        if (ctx.CONTENT_TEXT() != null) {
            toHashTable(ctx.CONTENT_TEXT().toString());
        }
        if (ctx.CONTENT_EMAIL() != null) {
            toHashTable(ctx.CONTENT_EMAIL().toString());
        }
    }

    // Remove commas from integers
    @Override
    public void exitHandleInteger(IRParser.HandleIntegerContext ctx) {
        toHashTable(ctx.getText().replace(",", ""));
    }

    public long getNumTokens() {
        return numTokens;
    }

    public DocumentHashTable getHashTable() {
        return hashTable;
    }

    public long getNumUnique() {
        return numUniqueTokens;
    }

    public static boolean isFailedFromHash() {
        return failedFromHash;
    }
}
