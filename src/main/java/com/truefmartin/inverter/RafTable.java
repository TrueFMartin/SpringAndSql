package com.truefmartin.inverter;


import com.truefmartin.inverter.structs.Writeable;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * The type Raf table.
 *
 * @param <T> the type parameter
 */
public class RafTable <T extends Writeable> implements AutoCloseable {
    private final int[] colSizes;
    private final int NUM_COLUMNS;
    private final String OUT_FILE_NAME;
    private final String CONFIG_FILE_NAME;
    private RandomAccessFile stream;
    private String configFolder = "/";


    /**
     * close will be used in later homeworks when I turn this class into an actual extension of
     * RandomAccessFile. Methods will throw exceptions that need to be caught instead of handling them inside
     * RafTable. RafTable will be used in a 'try-with-resources' instead of the testing state it is in now.
     */
    @Override
    public void close() throws IOException {
        closeFile();
    }

    /**
     * The enum of the Random Access File status.
     */
    public enum RafStatus{
        /**
         * Read Random Access File status.
         */
        READ,
        /**
         * Write Random Access File status.
         */
        WRITE,
        /**
         * Closed Random Access File status.
         */
        CLOSED;

        /**
         * Random Access File open option string.
         *
         * @return the string, 'r' for READ, 'rw' for WRITE
         */
        String fileOpenOption(){
            switch (this) {
                case READ: return "r";
                case WRITE: return "rw";
                case CLOSED: return "";
            }
            return "";
        }

        /**
         * Get the enum RafStatus associated with the string of Random Access File's options .
         *
         * @param option the option "r" or "rw"
         * @return the raf status READ, WRITE, or CLOSED
         */
        RafStatus statusFromOption(String option) {
            switch (option) {
                case "r": return READ;
                case "rw": return WRITE;
                default: return CLOSED;
            }
        }
    }
    private RafStatus status;



    private int numRecords;
    private final int RECORD_SIZE;

    /**
     * Builder constructor for RafTable. Creates a RAF based on a builder.
     * May not be used in final product. It might make more sense to just use the traditional constructor.
     * I wanted to have both options in case one made more sense in later homeworks.
     */
    private RafTable(Builder builder) {
        this.CONFIG_FILE_NAME = builder.CONFIG_FILE_NAME;
        this.OUT_FILE_NAME = builder.OUT_FILE_NAME;
        this.NUM_COLUMNS = builder.NUM_COLUMNS;
        this.colSizes = builder.colSizes;
        this.status = builder.status;
        this.stream = builder.stream;
        // The number of bytes in a row. Each value length + a space between each + a newline at the end
        this.RECORD_SIZE = builder.rowSize + NUM_COLUMNS + 1;
        this.numRecords = 0;
    }

    /**
     * Instantiates a new Raf table from a config file.
     *
     * @param configFileName the config file name
     * @param outFileName    the out file name
     * @param status         the status to open the stream as
     */
    public RafTable(String configFileName, String outFileName, RafStatus status) {
        this.CONFIG_FILE_NAME = configFileName;
        this.OUT_FILE_NAME = outFileName;
        this.status = status;
        String data;
        try (RandomAccessFile configStream = new RandomAccessFile( configFolder + configFileName, "r")){
            this.stream = new RandomAccessFile( configFolder + OUT_FILE_NAME, status.fileOpenOption());
            data = configStream.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error in Raf Table param constructor" + e);
        }

        String[] configValues = data.split(" ");
        this.numRecords = Integer.parseInt(configValues[0]);
        this.NUM_COLUMNS = configValues.length - 1;
        this.colSizes = new int[NUM_COLUMNS];
        int sumOfSizes = 0;
        for(int i = 1; i < configValues.length; i++) {
            colSizes[i - 1] = Integer.parseInt(configValues[i]);
            sumOfSizes += colSizes[i - 1];
        }
        // The number of bytes in a row. Each value length + a space between each + a newline at the end
        this.RECORD_SIZE = sumOfSizes + NUM_COLUMNS + 1;

    }

    public File getFileFromClassPath(String fileName) {
        try {
            return new ClassPathResource("./config/" +
                    fileName).getFile();
        } catch (IOException e) {
            throw new RuntimeException(" Unable to load file in getFileFromClassPath", e);
        }

    }

    public void printFnames(String sDir, int depth) throws IOException {
        Files.find(Paths.get(sDir), depth, (p, bfa) -> bfa.isRegularFile()).forEach(System.out::println);
    }
    /**
     * Instantiates a new Raf table.
     *
     * @param CONFIG_FILE_NAME the config file name
     * @param OUT_FILE_NAME    the out file name
     * @param status           the status to open the RAF in, READ, CLOSED, WRITE
     * @param colSizes         the column sizes
     */
    public RafTable(String CONFIG_FILE_NAME, String OUT_FILE_NAME, RafStatus status, int... colSizes) {
        this.CONFIG_FILE_NAME = CONFIG_FILE_NAME;
        this.OUT_FILE_NAME = OUT_FILE_NAME;
        this.status = status;
        this.colSizes = colSizes;
        this.NUM_COLUMNS = colSizes.length;
        if (this.status != RafStatus.CLOSED) {
            try {
                this.stream = new RandomAccessFile(OUT_FILE_NAME, status.fileOpenOption());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        // FIXME make sure the stream works as I expect
        this.RECORD_SIZE = Arrays.stream(colSizes).sum() + NUM_COLUMNS + 1;
        this.numRecords = 0;
    }

    /**
     * The RafTable Builder.
     *
     * @param <T> the type parameter
     */
    public static class Builder<T extends Writeable> {
        private int[] colSizes;
        private int NUM_COLUMNS;
        private String OUT_FILE_NAME;
        private final String CONFIG_FILE_NAME;
        private RandomAccessFile stream;
        private RafStatus status;
        private int columnCount = -1;
        private int rowSize;

        /**
         * Instantiates a new Builder.
         *
         * @param outFileName    the out file name
         * @param configFileName the config file name
         * @param status         the status
         * @param numColumns     the num columns
         */
        public Builder(String outFileName, String configFileName, RafStatus status, int numColumns) {
            this.NUM_COLUMNS = numColumns;
            this.colSizes = new int[NUM_COLUMNS];
            this.OUT_FILE_NAME = outFileName;
            this.CONFIG_FILE_NAME = configFileName;
            this.status = status;
            try {
                if (status == RafStatus.READ)
                    this.stream = new RandomAccessFile(OUT_FILE_NAME, "r");
                if (status == RafStatus.WRITE)
                    this.stream = new RandomAccessFile(OUT_FILE_NAME, "rw");
                if (status == RafStatus.CLOSED)
                    this.stream = null;
            } catch (IOException e){
                throw new RuntimeException(e);
            }
        }

        /**
         * Add a column to the builder.
         *
         * @param size the size of the column in bytes
         * @return the builder
         */
        public Builder addColumn(int size) {
            try {
                if (++columnCount == NUM_COLUMNS)
                    throw new ColumnBoundsException("Added more columns than RafTable was set for");
            } catch (ColumnBoundsException e) {
                throw new RuntimeException(e);
            }
            colSizes[columnCount] = size;
            rowSize += size;
            return this;
        }

        /**
         * Build raf table.
         *
         * @return the raf table
         */
        public RafTable build(){
            return new RafTable<T>(this);
        }
    }

    /**
     * Write to a specific row in the RAF.
     *
     * @param data the data to write, of type Writeable
     * @param row  the row to write on
     */
    public void write(T data, int row) {
        try {
            long currentPosition = stream.getFilePointer();
            stream.seek((long) row * RECORD_SIZE);
            write(data);
            stream.seek(currentPosition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Write.
     *
     * @param data the data
     */
    public void write(T data) {
        try {
            for(int size: colSizes) {
                if (!data.hasNext()) {
                    throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                            + " does not match number of columns for RafTable: " + NUM_COLUMNS);
                }
                String next = data.getNext();
                String formatter = "%" + size + "s" + " ";

                stream.write(String.format(formatter, next
                        .substring(0, Math.min(next.length(), size))).getBytes());
            }
            numRecords += 1;
            stream.write('\n');
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
        data.resetNext();
    }

    /**
     * Read one record at position recordNum, loading 'data' with results.
     *
     * @param recordNum the record num
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @return false if record num is outside of bounds
     */
    public boolean read(int recordNum, T data) {
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            return false;
        }

        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            String row = stream.readLine();
            int prevIndex = 0;
            for(int columnSize: colSizes) {
                if (!data.hasNext()) {
                    throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                            + " does not match number of columns for RafTable: " + NUM_COLUMNS);
                }
                data.setNext(row.substring(prevIndex, columnSize+prevIndex).strip());
                // Next value will start from end of current value(columnSize + prevIndex) + 1 space.
                prevIndex = columnSize + prevIndex + 1;
            }
            data.resetNext();
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Read multiple records starting at position recordNum,
     * loading 'data[]' with results.
     *
     * @param recordNum the record num
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @return false if record num is outside of bounds
     */
    public boolean readBatch(int recordNum, T[] data) throws RowBoundsException, ColumnBoundsException{
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            return false;
        }
        if (recordNum + data.length >= numRecords) {
            throw new RowBoundsException("Miscalculated number of records." +
                    "\nTried to read more than is available in file. " +
                    data.getClass().getSimpleName() +
                    "--Requested records: "  + recordNum + "-"
                    + (recordNum + data.length) +
                    ", actual available: " + numRecords);
        }
        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            int rowIndex = 0;
            String row;
            while ((row = stream.readLine()) != null) {
                if (rowIndex >= data.length) {
                    break;
                }
                int columnCharIndex = 0;
                for (int columnSize : colSizes) {
                    if (!data[rowIndex].hasNext()) {
                        throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                                + " does not match number of columns for RafTable: " + NUM_COLUMNS);
                    }
                    data[rowIndex].setNext(row.substring(columnCharIndex, columnSize + columnCharIndex).strip());
                    // Next value will start from end of current value(columnSize + columnCharIndex) + 1 space.
                    columnCharIndex = columnSize + columnCharIndex + 1;
                }
                data[rowIndex].resetNext();
                rowIndex++;
            }
            } catch(IOException e){
                throw new RuntimeException(e);
            }
        return true;
    }

    /**
     * Read multiple records starting at position recordNum,
     * offering 'int[]' int queue.
     *
     * @param recordNum the record num to start at
     * @param numToRead the number of records to read
     * @param queue     the queue to write to
     * @param semaphore used to let queue consumer to start polling
     * @return false if record num is outside of bounds
     */
    public boolean readBatchToQueue(int recordNum, int numToRead, ConcurrentLinkedQueue<int []> queue, Semaphore semaphore) throws RowBoundsException, ColumnBoundsException{
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            return false;
        }
        if (recordNum + numToRead > numRecords) {
            throw new RowBoundsException("Miscalculated number of records." +
                    "\nTried to read more than is available in file.");
        }
        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            int numRead = 0;
            String row;
            int[] rowResult = new int[colSizes.length];
            semaphore.release();
            while ((row = stream.readLine()) != null && numRead < numToRead) {
                int columnCharIndex = 0;
                int colIndex = 0;
                for (int columnSize : colSizes) {
                    rowResult[colIndex++] = Integer.parseInt(row.substring(columnCharIndex, columnSize + columnCharIndex).strip());
                    // Next value will start from end of current value(columnSize + columnCharIndex) + 1 space.
                    columnCharIndex = columnSize + columnCharIndex + 1;
                }
                queue.offer(rowResult.clone());
                numRead++;
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return true;
    }
    /**
     * Read records until the desired result is found, or an empty record.
     *
     * @param recordNumStart the record num to start searching at
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @param desired the string to search for
     * @param columnNumber the column in which the desired string is found in
     * @return false if not found or recordNumStart is out of bounds, true otherwise
     */
    public boolean readAndFind(int recordNumStart, T data, String desired, int columnNumber) {
        if ((recordNumStart < 0) || (recordNumStart >= numRecords)) {
            return false;
        }

        try {
            stream.seek((long) recordNumStart * RECORD_SIZE);
            int currentRecord = recordNumStart - 1;
            String row;
            while((row = stream.readLine()) != null) {
                currentRecord++;
                int prevIndex = 0;
                for (int columnSize : colSizes) {
                    if (!data.hasNext()) {
                        throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                                + " does not match number of columns for RafTable: " + NUM_COLUMNS);
                    }
                    data.setNext(row.substring(prevIndex, columnSize + prevIndex).strip());
                    // Next value will start from end of current value(columnSize + prevIndex) + 1 space.
                    prevIndex = columnSize + prevIndex + 1;
                    }
                data.resetNext();
                // If empty result, desired was not found. Return false
                if (data.isEmpty()) {
                    return false;
                }
                // If the data is equal to desired, return true
                if (data.getValue(columnNumber).equals(
                        // Take substring of desired to match what is in records
                        desired.substring(0, Math.min(colSizes[columnNumber], desired.length())))
                ) {
                    return true;
                }
                // If we reach end of records,
                if(currentRecord == numRecords - 1) {
                    currentRecord = -1;
                    // Reset file to beginning
                    stream.seek(0);
                    continue;
                }
                // If we have traveled full circle and the result before where we started is not it,
                if(currentRecord == recordNumStart - 1) {
                    return false;
                }
            }
            return false;
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close file boolean.
     *
     * @return the boolean
     */
    public boolean closeFile() {
        numRecords = 0;
        if (status != RafStatus.CLOSED) {
            status = RafStatus.CLOSED;
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets read mode.
     */
    public void setReadMode() {
        if (status == RafStatus.READ) {
            return;
        }
        setNewStream(RafStatus.READ);
    }

    /**
     * Sets write mode. Starts num records at 0. Deletes current file
     */
    public void setWriteModeNew() {
        numRecords = 0;
        closeFile();
        try {
            Files.deleteIfExists(Path.of(OUT_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setNewStream(RafStatus.WRITE);
    }

    private void setUpdateMode() {
        // TODO if needed in future HW
        // Set method to public, add RafStatus ENUM 'UPDATE'
    }
    private void setNewStream(RafStatus streamOption) {
        closeFile();
        if (streamOption == RafStatus.CLOSED) {
            return;
        }
        status = streamOption;
        try {
            stream = new RandomAccessFile(OUT_FILE_NAME, streamOption.fileOpenOption());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write configs.
     */
    public void writeConfigs() {
        StringBuilder outString = new StringBuilder();
        outString.append(numRecords).append(' ');
        for(int colSize: colSizes) {
            outString.append(colSize).append(' ');
        }
        try {
            Files.deleteIfExists(Path.of("./" + CONFIG_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try(RandomAccessFile configStream = new RandomAccessFile(CONFIG_FILE_NAME, "rw")){
            configStream.write(outString.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumRecords() {
        return numRecords;
    }
}
