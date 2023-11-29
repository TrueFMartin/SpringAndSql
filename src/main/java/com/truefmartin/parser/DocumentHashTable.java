package com.truefmartin.parser;

/**
 * Filename: DocumentHashTabe.java
 * Author: Susan Gauch, converted to java by Matt Miller, debugged by Patrick Anderson.
 *
 * Modified by: Franklin True Martin for use in inversion algorithm.
 */

import java.util.Arrays;

/**
 * The type Hash table.
 */
public class DocumentHashTable
{
    public interface HashTableSizeInterface {
        public int calcSize(Long fileSize);
    }
    private final int size;
    private long used;
    private long collision;
    private long lookups;
    private Node[] hashtable;
    private StopWords stopWords;
    private boolean hasFailed;

    private final static int TERM_SIZE = HTMLParser.TERM_SIZE;
    private final static int FREQ_SIZE = HTMLParser.FREQ_SIZE;


    /**
     * Initializes a hashtable with size 3 times the size given.
     *
     * @param size One third of the hashtable size;
     */
    public DocumentHashTable(int size)
    {
        this.size= (int) (size*3.01942);
        hasFailed = false;
        this.stopWords = StopWords.getInstance();
        init();
    }

    /**
     * Copies a hashtable
     *
     * @param ht the ht
     */
    public DocumentHashTable(DocumentHashTable ht)
    {
        this.size=ht.getSize();
        used=ht.getUsed();
        collision=ht.getCollisions();
        lookups=ht.getLookups();
        hashtable=new Node[this.size];

        for(int i=0;i<this.size;i++)
            hashtable[i]=new Node(ht.getNode(i).getTerm(),ht.getNode(i).getFreq());
    }

    /**
     * Method to be called by constructors.
     * Also an easy way to reset a already made hashtable.
     * Requires that size already be set.
     */
    public void init()
    {
        used=0;
        collision=0;
        lookups=0;
        hashtable=new Node[this.size];

        //initialize the hashtable
        for(int i=0;i<this.size;i++)
            hashtable[i]=new Node();
    }

    /**
     * Gets num unique terms.
     *
     * @return the num unique terms
     */
    public int getNumUniqueTerms() {
        int numUnique = 0;
        for (Node node: hashtable) {
            if (node.isNotEmpty()) {
                numUnique++;
            }
        }
        return numUnique;
    }

    /**
     * Prints the contents of the hashtable to the file given.
     *
     * @return StringBuilder of hash table with empty fields
     */
    public StringBuilder print()
    {

        StringBuilder out = new StringBuilder(size*20);

        for(int i=0;i<size;i++)
        {

            String termStr = hashtable[i].getTerm();
            int termSpaceSize = 1;
            if (termStr.length() > TERM_SIZE) {
                termStr = termStr.substring(0, TERM_SIZE);
            } else {
                termSpaceSize = TERM_SIZE - termStr.length() + 1;
            }
            String termSpace = " ".repeat(termSpaceSize);
            String freqStr = String.valueOf(hashtable[i].getFreq());
            if(freqStr.length() > FREQ_SIZE) {
                freqStr = "OVER";
            }

            //May use when there are more than two columns
//            int freqSpace = 1;
//            if (freqStr.length() > FREQ_SIZE) {
//                freqStr = freqStr.substring(0, FREQ_SIZE);
//            } else {
//                freqSpace = FREQ_SIZE - freqStr.length() + 1;
//            }

            out.append(termStr).append(termSpace).append(freqStr).append("\n");
        }
        return out;

//        System.out.println("Collisions: "+collision+" Used: "+used+" Lookups: "+lookups);
    }


    /**
     * Sort and remove all empty buckets.
     *
     * @return the string builder
     */
    public StringBuilder printSorted(int numTokens)
    {
        int finalNumTokens = numTokens - removeStopWords();
        StringBuilder out = new StringBuilder(size*20);
        Arrays.stream(this.hashtable).filter(Node::isNotEmpty).sorted().forEachOrdered(node -> out.append(node.rtfPrint(finalNumTokens)));
        return out;
    }

    private int removeStopWords() {
        int sum = 0;
        for (String stopword :
                stopWords.getStopList()) {
            int index = find(stopword);
            if (hashtable[index].isNotEmpty()) {
                sum += hashtable[index].setEmpty();
                used--;
            }
        }
        return sum;
    }

    /**
     * Insert string term, and int freq into hashtable, hashes on term.
     *
     * @param term String to be hashed.
     * @param freq String
     */
    public void insert(String term, int freq)
    {
        int index = find(term);

        //if not already in the table, insert it
        if(hashtable[index].isEmpty())
        {
            hashtable[index].setTerm(term);
            hashtable[index].setFreq(freq);
            used++;
        } else { //increment freq
            hashtable[index].freq++;
        }
    }

    /**
     * Returns the index of the word in the table, or the index of a free space
     * in the table.
     * @param str String to hash.
     * @return index of the word, or of free space in which to place the word.
     */
    private int find(String str)
    {
        int index = Math.abs(str.hashCode())%size;

        boolean onSecondLoop = false;
        /*
         * check to see if the word is in that location
         * if not there, do linear probing until word is found\
         * or empty location found
         */
        while(!hashtable[index].getTerm().equals(str) && hashtable[index].isNotEmpty())
        {
            index++;
            collision++;
            if(index >= size) {
                index = 0;
                if(onSecondLoop) {
                    hasFailed = true;
                    break;
                }
                onSecondLoop = true;
            }
        }

        return index;
    }

    /**
     * Returns the freq at the hashed location of term.
     *
     * @param term String to be hashed.
     * @return freq in the table at the location of term.
     */
    public int getFreq(String term)
    {
        int index=find(term);
        lookups++;
        return hashtable[index].getFreq();
    }

    /**
     * Get the three statistics as a string.  Used, Collisions, and Lookups.
     *
     * @return Used, Collisions, and Lookups as a string.
     */
    public String getUsage()
    {
        return "Used: "+used+" Collisions: "+collision+" Lookups: "+lookups;
    }

    /**
     * Get the amount in the table.
     *
     * @return How full the table is. long
     */
    public long getUsed()
    {
        return used;
    }

    /**
     * Get the number of collisions.
     *
     * @return How much you need to improve your hash function. long
     */
    public long getCollisions()
    {
        return collision;
    }

    /**
     * The number of lookups made.
     *
     * @return long lookups
     */
    public long getLookups()
    {
        return lookups;
    }

    /**
     * Gets the size of the array.
     *
     * @return size, long
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Returns Node at location index.
     * @param index location in hashtable
     * @return Node at location index.
     */
    private Node getNode(int index)
    {
        return hashtable[index];
    }

    /**
     * Has failed boolean.
     *
     * @return the boolean
     */
    public boolean hasFailed() {
        return hasFailed;
    }


    /**
     * Private class node to whole the actual data stored in the hashtable.
     * Provides standard accessor and mutator methods.
     */
    private class Node implements Comparable<Node>
    {
        private String term;
        private int freq;

        public Node() {
            this.term = "";
            this.freq = -1;
        }
        /**
         * Instantiates a new Node.
         *
         * @param term the term
         * @param freq the freq
         */
        public Node(String term, int freq)
        {
            this.term = term;
            this.freq = freq;
        }

        public int setEmpty() {
            int prevFreq = this.freq;
            this.term = "";
            this.freq = -1;
            return prevFreq;
        }

        public boolean isEmpty() {
            return this.freq == -1;
        }
        public boolean isNotEmpty() {
            return !isEmpty();
        }


        /**
         * Gets term.
         *
         * @return the term
         */
        public String getTerm()
        {
            return term;
        }

        /**
         * Gets freq.
         *
         * @return the freq
         */
        public int getFreq()
        {
            return freq;
        }

        /**
         * Sets term.
         *
         * @param term the term
         */
        public void setTerm(String term)
        {
            this.term = term;
        }

        /**
         * Sets freq.
         *
         * @param freq the freq
         */
        public void setFreq(int freq)
        {
            this.freq = freq;
        }

        @Override
        public String toString() {
            return this.term + " " + this.freq + "\n";
        }

        public String rtfPrint(int numTokens) {
            return String.format("%s %.6f\n", this.term, this.freq*1.0/numTokens);
        }

        @Override
        public int compareTo(Node node) {
            if (this.isEmpty() && node.isEmpty())
                return 0;
            return this.isEmpty() ? 1: this.term.compareTo(node.term);
        }
    }
}
