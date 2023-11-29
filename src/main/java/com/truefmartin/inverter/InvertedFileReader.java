package com.truefmartin.inverter;


import com.truefmartin.inverter.structs.DictData;
import com.truefmartin.inverter.structs.MapData;
import com.truefmartin.inverter.structs.PostData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class InvertedFileReader implements AutoCloseable{
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

    private RafTable<MapData> rafMap;
    private RafTable<DictData> rafDict;
    private RafTable<PostData> rafPost;
    private final Map<InvertedFileWriter.FileType, Boolean> fileTypeMap = new HashMap<>(3);


    /**
     * Instantiates a new Inverted file writer from the config files. Used when the outfiles are already present, and
     * you want to read them. Opens the map, post, and dict files.
     */
    public InvertedFileReader() {
        rafMap = new RafTable<MapData>(FILENAME_CONFIG_MAP, FILENAME_MAP, RafTable.RafStatus.READ);
        rafPost = new RafTable<PostData>(FILENAME_CONFIG_POST, FILENAME_POST, RafTable.RafStatus.READ);
        rafDict = new RafTable<DictData>(FILENAME_CONFIG_DICT, FILENAME_DICT, RafTable.RafStatus.READ);

        setFileTypeMap(InvertedFileWriter.FileType.MAP, InvertedFileWriter.FileType.DICT, InvertedFileWriter.FileType.POST);

    }

    /**
     * Instantiates a new Inverted file writer from the config files. Used when the outfiles are already present, and
     * you want to read them. Opens only the passed in FileTypes.
     */
    public InvertedFileReader(InvertedFileWriter.FileType... fileTypes) {
        for(InvertedFileWriter.FileType fileType: fileTypes) {
            switch (fileType) {
                case MAP:
                    rafMap = new RafTable<MapData>(FILENAME_CONFIG_MAP, FILENAME_MAP, RafTable.RafStatus.READ);
                    break;
                case DICT:
                    rafDict = new RafTable<DictData>(FILENAME_CONFIG_DICT, FILENAME_DICT, RafTable.RafStatus.READ);
                    break;
                case POST:
                    rafPost = new RafTable<PostData>(FILENAME_CONFIG_POST, FILENAME_POST, RafTable.RafStatus.READ);
                    break;
            }
        }
        setFileTypeMap(fileTypes);
    }

    /**
     * Close after reading.
     */
    public void closeAfterReading() {
        if(fileTypeMap.get(InvertedFileWriter.FileType.MAP)) {
            rafMap.closeFile();
        }
        if(fileTypeMap.get(InvertedFileWriter.FileType.POST)) {
            rafPost.closeFile();
        }
        if(fileTypeMap.get(InvertedFileWriter.FileType.DICT)) {
            rafDict.closeFile();
        }
    }

    /**
     * Read map record.
     *
     * @param recordNum the record num to find a map entry
     * @return the map entry
     */
    public MapData readMapRecord(int recordNum) {
        if(fileTypeMap.get(InvertedFileWriter.FileType.MAP)) {
            MapData temp = new MapData();
            if (rafMap.read(recordNum, temp) && !temp.isEmpty()) {
                return temp;
            } else {
                return null;
            }
        } else throw new UnsupportedOperationException("Map not initialized to read.");
    }

    /**
     * Read post record.
     *
     * @param recordNum the record num
     * @return
     */
    public PostData readPostRecord(int recordNum) {
        if(fileTypeMap.get(InvertedFileWriter.FileType.POST)) {
            PostData temp = new PostData();
            if (rafPost.read(recordNum, temp) && !temp.isEmpty()) {
                return temp;
            } else {
                return null;
            }
        } else throw new UnsupportedOperationException("Post not initialized");
    }

    public PostData[] readBulkPostRecord(int recordStart, int numRecords) {
        if(fileTypeMap.get(InvertedFileWriter.FileType.POST)) {

            var posts = new PostData[numRecords];
            try {
                rafPost.readBatch(recordStart, posts);
            } catch (RowBoundsException e) {
            } catch (ColumnBoundsException e) {
                throw new RuntimeException(e);
            }
            return posts;
        } else throw new UnsupportedOperationException("Post not initialized");

    }

    public void readPostToQueue(int recordStart, int numRecords, ConcurrentLinkedQueue<int []> queue, Semaphore semaphore) {
        if(fileTypeMap.get(InvertedFileWriter.FileType.POST)) {
            try {
                rafPost.readBatchToQueue(recordStart, numRecords, queue, semaphore);
            } catch (RowBoundsException e) {
            } catch (ColumnBoundsException e) {
                throw new RuntimeException(e);
            }
        } else throw new UnsupportedOperationException("Post not initialized");

    }

    /**
     * Read dict record.
     *
     * @param recordNum the record num
     * @return DictData at recordNum or null if not found.
     */
    public DictData readDictRecord(int recordNum) {
        if(fileTypeMap.get(InvertedFileWriter.FileType.DICT)) {
            DictData temp = new DictData();
            if (rafDict.read(recordNum, temp) && !temp.isEmpty()) {
                return temp;
            } else {
                return null;
            }
        } else throw new UnsupportedOperationException("Dict not initialized");

    }

    /**
     * Read dict record.
     *
     * @param recordNums int array of records to read
     * @return an array of DictData of all records that were not empty.
     */
    public DictData[] readDictRecords(int ... recordNums) {
        if (fileTypeMap.get(InvertedFileWriter.FileType.DICT)) {
            DictData[] nonEmptyDictRecords = new DictData[recordNums.length];
            boolean atLeastOne = false;
            int i = 0;
            for(int recordNum: recordNums) {
                var result = readDictRecord(recordNum);
                if (result != null) {
                    nonEmptyDictRecords[i++] = result;
                    atLeastOne = true;
                }
            }
            return atLeastOne? Arrays.copyOfRange(nonEmptyDictRecords, 0, i): null;
        } else throw new UnsupportedOperationException("Dict not initialized");
    }

    /**
     * Read dict record.
     *
     * @param desired stringToFind
     * @param hashFunc HashFunc interface implementation to perform on desired
     *                 to search Dict with the result of 'hash(desired)' % dict size
     * @return DictData of result, null if not found.
     */
    public DictData findInDict(String desired, HashFunc hashFunc) {
        if (fileTypeMap.get(InvertedFileWriter.FileType.DICT)) {
            int index = hashFunc.hash(desired) % rafDict.getNumRecords();
            var temp = new DictData();
            if(rafDict.readAndFind(index, temp, desired, 0) && !temp.isEmpty())
                return temp;
            else
                return null;
        } else throw new UnsupportedOperationException("Dict not initialized");
    }

    @Override
    public void close() throws Exception {
        closeAfterReading();
    }

    public interface HashFunc {
        int hash(String s);
    }
    private void setFileTypeMap(InvertedFileWriter.FileType... initializedFileTypes) {
        for (InvertedFileWriter.FileType fileType: initializedFileTypes) {
            this.fileTypeMap.put(fileType, true);
        }
        for (InvertedFileWriter.FileType fileType: InvertedFileWriter.FileType.values()) {
            this.fileTypeMap.putIfAbsent(fileType, false);
        }
    }

}
