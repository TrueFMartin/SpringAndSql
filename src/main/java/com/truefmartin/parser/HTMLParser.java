package com.truefmartin.parser;

import com.truefmartin.IRLexer;
import com.truefmartin.IRParser;
import com.truefmartin.IRParserBaseListener;
import com.truefmartin.builder.InvertedFileBuilder;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HTMLParser {
    private String outFileDir;
    private final String inFileDir;
    private SynchronizedCounter synchronizedCounter;
    private final boolean debug;
    //---------- NOT USED YET ---------------
    public static final int TERM_SIZE = 14;
    public static final int FREQ_SIZE = 6;
    //--------------------------------------


    public HTMLParser(String inFileDir, String outFileDir) {
        this.inFileDir = inFileDir;
        this.outFileDir = outFileDir;
        this.synchronizedCounter = new SynchronizedCounter();
        debug = InvertedFileBuilder.DEBUG_MODE;
    }

    public Set<String> begin(DocumentHashTable.HashTableSizeInterface dhtSizeCalc) {
        // Get max number of processors as possible
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Get a map of FileName to FileLength in bytes
        Map<String, Long> htmlFiles = getHTMLFilesFromDirectory(this.inFileDir);

        // Create a list of Callable tasks for parsing
        List<Callable<Void>> tasks = new LinkedList<>();

        for (Map.Entry<String, Long> fileEntry: htmlFiles.entrySet()) {
            tasks.add(() -> {
                parseHTMLFile(fileEntry.getKey(), fileEntry.getValue(), dhtSizeCalc);
                return null;
            });
        }

        try {
            // Invoke tasks in parrallel, err parallell, pearal? parallel!
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            if (IRParserEvaluator.isFailedFromHash())
                throw new RuntimeException("ERROR: Hash table to small. Rerun with a larger third arg input.");
            if (this.debug) {
                System.out.println("Number of total tokens in corpus: " + synchronizedCounter.getNumTokens());
                System.out.println("Number of unique terms per document summed: " + synchronizedCounter.getNumTokensUnique());
            }

            return htmlFiles.keySet();
        }
    }

    private void parseHTMLFile(String filePath, Long fileSize, DocumentHashTable.HashTableSizeInterface dhtSizeCalc) {
        IRLexer lexer;
        try {
            lexer = new IRLexer(CharStreams.fromFileName(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IRParser parser = new IRParser(tokens);
        parser.removeErrorListeners();

        if (debug) {
            System.out.println("H-table size: "  + dhtSizeCalc.calcSize(fileSize) + "\tfor: " + filePath);
        }
        // Create an instance of listener that handles exiting of rules
        IRParserBaseListener customListener = new IRParserEvaluator(
                filePath, outFileDir, dhtSizeCalc.calcSize(fileSize), synchronizedCounter);

        parser.addParseListener(customListener);

        // Begin parsing
        IRParser.DocumentContext documentContext = parser.document();
    }

    public static Map<String, Long> getHTMLFilesFromDirectory(String directoryPath) {
        try (Stream<Path> stream = Files.list(Paths.get(directoryPath))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .collect(Collectors.toMap(Function.identity(), file -> new File(file).length()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class SynchronizedCounter {

        private AtomicInteger globalNumTokensUnique;
        private AtomicInteger globalNumTokens;

        SynchronizedCounter() {
            this.globalNumTokens = new AtomicInteger(0);
            this.globalNumTokensUnique = new AtomicInteger(0);
        }
        public void increaseNumTokens(int num) {
            this.globalNumTokens.getAndAdd(num);
        }
        public void increaseNumUniqueTokens(int num) {
            this.globalNumTokensUnique.getAndAdd(num);
        }

        public int getNumTokensUnique() {
            return this.globalNumTokensUnique.get();
        }

        public int getNumTokens() {
            return this.globalNumTokens.get();
        }
    }
}
