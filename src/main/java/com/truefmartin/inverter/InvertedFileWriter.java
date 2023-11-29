package com.truefmartin.inverter;

import com.truefmartin.inverter.structs.DictData;
import com.truefmartin.inverter.structs.MapData;
import com.truefmartin.inverter.structs.PostData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Inverted file writer.
 */
public class InvertedFileWriter implements AutoCloseable{
    /**
     * The Default token size.
     */
    static final int DEFAULT_TOKEN_SIZE = 25;
    /**
     * The Default num docs size.
     */
    static final int DEFAULT_NUM_DOCS_SIZE = 4;
    /**
     * The Default file name size.
     */
    static final int DEFAULT_FILE_NAME_SIZE = 10;
    /**
     * The Default start size.
     */
    static final int DEFAULT_START_SIZE = 6;
    /**
     * The Default doc id size.
     */
    static final int DEFAULT_DOC_ID_SIZE = 4;
    /**
     * The Default freq size.
     */
    static final int DEFAULT_FREQ_SIZE = 9;

    /**
     * The Filename map.
     */
    static String FILENAME_MAP = "map.txt";
    /**
     * The Filename dict.
     */
    static String FILENAME_DICT = "dict.txt";
    /**
     * The Filename post.
     */
    static String FILENAME_POST = "post.txt";
    /**
     * The Filename config map.
     */
    static String FILENAME_CONFIG_MAP = "config_map.txt";
    /**
     * The Filename config dict.
     */
    static String FILENAME_CONFIG_DICT = "config_dict.txt";
    /**
     * The Filename config post.
     */
    static String FILENAME_CONFIG_POST = "config_post.txt";

    private RafTable rafMap;
    private RafTable rafDict;
    private RafTable rafPost;

    @Override
    public void close() throws Exception {
        closeAfterWriting();
    }

    public enum FileType {
        MAP, DICT, POST
    }

    private final Map<FileType, Boolean> fileTypeMap = new HashMap<>(3);

    /**
     * Instantiates a new Inverted file writer with the default settings.
     */
    public InvertedFileWriter() {
        rafMap = new RafTable.Builder<MapData>(FILENAME_MAP,FILENAME_CONFIG_MAP, RafTable.RafStatus.WRITE, 2)
                .addColumn(DEFAULT_DOC_ID_SIZE)
                .addColumn(DEFAULT_FILE_NAME_SIZE)
                .build();

        rafPost = new RafTable.Builder<PostData>(FILENAME_POST, FILENAME_CONFIG_POST, RafTable.RafStatus.WRITE, 2)
                .addColumn(DEFAULT_DOC_ID_SIZE)
                .addColumn(DEFAULT_FREQ_SIZE)
                .build();

        rafDict = new RafTable.Builder<DictData>(FILENAME_DICT, FILENAME_CONFIG_DICT, RafTable.RafStatus.WRITE, 3)
                .addColumn(DEFAULT_TOKEN_SIZE)
                .addColumn(DEFAULT_NUM_DOCS_SIZE)
                .addColumn(DEFAULT_START_SIZE)
                .build();
        setFileTypeMap(FileType.MAP, FileType.DICT, FileType.POST);
    }

    /**
     * Instantiates a new Inverted file writer with the default settings.
     */
    public InvertedFileWriter(FileType... fileTypes) {
        for(FileType fileType: fileTypes) {
            switch (fileType) {
                case MAP:
                    rafMap = new RafTable.Builder<MapData>(FILENAME_MAP,FILENAME_CONFIG_MAP, RafTable.RafStatus.WRITE, 2)
                            .addColumn(DEFAULT_DOC_ID_SIZE)
                            .addColumn(DEFAULT_FILE_NAME_SIZE)
                            .build();
                    try {
                        Files.deleteIfExists(Path.of(FILENAME_MAP));
                        Files.deleteIfExists(Path.of(FILENAME_CONFIG_MAP));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    rafMap.setWriteModeNew();

                    break;
                case DICT:
                    rafDict = new RafTable.Builder<DictData>(FILENAME_DICT, FILENAME_CONFIG_DICT, RafTable.RafStatus.WRITE, 3)
                            .addColumn(DEFAULT_TOKEN_SIZE)
                            .addColumn(DEFAULT_NUM_DOCS_SIZE)
                            .addColumn(DEFAULT_START_SIZE)
                            .build();
                    try {
                        Files.deleteIfExists(Path.of(FILENAME_DICT));
                        Files.deleteIfExists(Path.of(FILENAME_CONFIG_DICT));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    rafDict.setWriteModeNew();
                    break;
                case POST:
                    rafPost = new RafTable.Builder<PostData>(FILENAME_POST, FILENAME_CONFIG_POST, RafTable.RafStatus.WRITE, 2)
                            .addColumn(DEFAULT_DOC_ID_SIZE)
                            .addColumn(DEFAULT_FREQ_SIZE)
                            .build();
                    try {
                        Files.deleteIfExists(Path.of(FILENAME_POST));
                        Files.deleteIfExists(Path.of(FILENAME_CONFIG_POST));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    rafPost.setWriteModeNew();
                    break;
            }
        }
        setFileTypeMap(fileTypes);
    }

    /**
     * Instantiates a new Inverted file writer from the config files. Used when the outfiles are already present, and
     * you want to update them.
     *
     * @param openFromConfig are the files present and a config file exists?
     */
    public InvertedFileWriter(boolean openFromConfig) {
        if (openFromConfig) {
            rafMap = new RafTable(FILENAME_CONFIG_MAP, FILENAME_MAP, RafTable.RafStatus.WRITE);
            rafPost = new RafTable(FILENAME_CONFIG_POST, FILENAME_POST, RafTable.RafStatus.WRITE);
            rafDict = new RafTable(FILENAME_CONFIG_DICT, FILENAME_DICT, RafTable.RafStatus.WRITE);
        } else {
            rafMap = new RafTable.Builder<MapData>(FILENAME_MAP,FILENAME_CONFIG_MAP, RafTable.RafStatus.WRITE, 2)
                    .addColumn(DEFAULT_DOC_ID_SIZE)
                    .addColumn(DEFAULT_FILE_NAME_SIZE)
                    .build();

            rafPost = new RafTable.Builder<PostData>(FILENAME_POST, FILENAME_CONFIG_POST, RafTable.RafStatus.WRITE, 2)
                    .addColumn(DEFAULT_DOC_ID_SIZE)
                    .addColumn(DEFAULT_FREQ_SIZE)
                    .build();

            rafDict = new RafTable.Builder<DictData>(FILENAME_DICT, FILENAME_CONFIG_DICT, RafTable.RafStatus.WRITE, 3)
                    .addColumn(DEFAULT_TOKEN_SIZE)
                    .addColumn(DEFAULT_NUM_DOCS_SIZE)
                    .addColumn(DEFAULT_START_SIZE)
                    .build();
        }
        setFileTypeMap(FileType.MAP, FileType.DICT, FileType.POST);

    }
    /**
     * Open for write, overwriting the current files. Used with default constructor.
     */
    public void openForWriteNew() {
        try {
            Files.deleteIfExists(Path.of(FILENAME_MAP));
            Files.deleteIfExists(Path.of(FILENAME_POST));
            Files.deleteIfExists(Path.of(FILENAME_DICT));
            Files.deleteIfExists(Path.of(FILENAME_CONFIG_MAP));
            Files.deleteIfExists(Path.of(FILENAME_CONFIG_POST));
            Files.deleteIfExists(Path.of(FILENAME_CONFIG_DICT));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        rafMap.setWriteModeNew();
        rafPost.setWriteModeNew();
        rafDict.setWriteModeNew();
    }

    /**
     * Close after writing, writes config files when finished.
     */
    public void closeAfterWriting() {
        if(fileTypeMap.get(FileType.MAP)) {
            rafMap.writeConfigs();
            rafMap.closeFile();
        }
        if(fileTypeMap.get(FileType.POST)) {
            rafPost.writeConfigs();
            rafPost.closeFile();
        }
        if(fileTypeMap.get(FileType.DICT)) {
            rafDict.writeConfigs();
            rafDict.closeFile();
        }
    }

    /**
     * Write map record.
     *
     * @param docID    the doc id
     * @param fileName the file name
     */
    public void writeMapRecord(int docID, String fileName) {
        if(fileTypeMap.get(FileType.MAP)) {
            rafMap.write(new MapData(Integer.toString(docID), fileName));
        } else throw new UnsupportedOperationException("Map not initialized");
    }

    /**
     * Write post record.
     *
     * @param docID  the doc id
     * @param weight the weight
     */
    public void writePostRecord(int docID, int weight) {
        if(fileTypeMap.get(FileType.POST)) {
            rafPost.write(new PostData(Integer.toString(docID), Integer.toString(weight)));
        } else throw new UnsupportedOperationException("Post not initialized");
    }

    /**
     * Write dict record to a particular row.
     *
     * @param row     the row
     * @param term    the term
     * @param numDocs the num docs
     * @param start   the start
     */
    public void writeDictRecordHashed(int row, String term, int numDocs, int start) {
        if(fileTypeMap.get(FileType.DICT)) {
            rafDict.write(new DictData(term, Integer.toString(numDocs), Integer.toString(start)), row);
        } else throw new UnsupportedOperationException("Dict not initialized");
    }

    /**
     * Write dict record.
     *
     * @param term    the term
     * @param numDocs the num docs
     * @param start   the start
     */
    public void writeDictRecord(String term, int numDocs, int start) {
        if(fileTypeMap.get(FileType.DICT)) {
            rafDict.write(new DictData(term, Integer.toString(numDocs), Integer.toString(start)));
        } else throw new UnsupportedOperationException("Dict not initialized");

    }

    private void setFileTypeMap(FileType... initializedFileTypes) {
        for (FileType fileType: initializedFileTypes) {
            this.fileTypeMap.put(fileType, true);
        }
        for (FileType fileType: FileType.values()) {
            this.fileTypeMap.putIfAbsent(fileType, false);
        }
    }
}
