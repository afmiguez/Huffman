/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package huffman;

import java.util.Iterator;

/**
 *
 * @author Afmiguez
 */
public class Huffman {

    // alphabet size of extended ASCII
    private static final int R = 256;
    private static Node root;
    private static String compressedCode = "";
    private static String uncompressedCode = "";
    private static ST<Character, String> table = null;
    private static ST<String, Character> LUT = null;

    public static ST<Character, String> getTable() {
        return table;
    }

    public static ST<String, Character> getLUT() {
        return LUT;
    }

    public static String getCompressedCode() {
        return compressedCode;
    }

    public static String getUncompressedCode() {
        return uncompressedCode;
    }

    // Huffman trie node
    private static class Node implements Comparable<Node> {

        private final String ch;
        private final int freq;
        private final Node left, right;

        Node(String ch, int freq, Node left, Node right) {
            this.ch = ch;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        // is the node a leaf node?
        private boolean isLeaf() {
            assert (left == null && right == null) || (left != null && right != null);
            return (left == null && right == null);
        }

        // compare, based on frequency
        public int compareTo(Node that) {
            return this.freq - that.freq;
        }

        @Override
        public String toString() {
            return "Node{" + "ch=" + ch + ", freq=" + freq + '}';
        }

    }

    public static void compress(String s) {
        //reset the static Strings
        compressedCode = "";
        uncompressedCode = "";

        //create new symbol tables
        table = new ST<>();
        LUT = new ST<>();

        // read the input
        char[] input = s.toCharArray();

        // tabulate frequency counts
        int[] freq = new int[R];
        for (int i = 0; i < input.length; i++) {
            freq[input[i]]++;
        }

        // build Huffman trie
        root = buildTrie(freq);

        // build code table
        String[] st = buildCode(root);

        // use Huffman code to encode input
        for (int i = 0; i < input.length; i++) {
            String code = st[input[i]];
            for (int j = 0; j < code.length(); j++) {
                if (code.charAt(j) == '0') {
                    compressedCode += "0";
                } else if (code.charAt(j) == '1') {
                    compressedCode += "1";
                } else {
                    throw new IllegalStateException("Illegal state");
                }
            }
        }
        // build LookUp Table
        buildLUT();
    }

    // build the Huffman trie given frequencies
    private static Node buildTrie(int[] freq) {

        // initialze priority queue with singleton trees
        MinPQ<Node> pq = new MinPQ<Node>();
        for (char i = 0; i < R; i++) {
            if (freq[i] > 0) {
                pq.insert(new Node("" + i, freq[i], null, null));
            }
        }

        // special case in case there is only one character with a nonzero frequency
        if (pq.size() == 1) {
            if (freq['\0'] == 0) {
                pq.insert(new Node("" + '\0', 0, null, null));
            } else {
                pq.insert(new Node("" + '\1', 0, null, null));
            }
        }

        // merge two smallest trees
        while (pq.size() > 1) {
            Node left = pq.delMin();
            Node right = pq.delMin();
            //Node parent = new Node('\0', left.freq + right.freq, left, right);
            Node parent = new Node("(" + left.ch + "," + right.ch + ")", left.freq + right.freq, left, right);
            pq.insert(parent);
        }
        return pq.delMin();
    }

    private static String[] buildCode(Node root) {
        String[] st = new String[R];
        buildCode(st, root, "");
        for (int i = 0; i < R; i++) {
            if (st[i] != null) {
                table.put((char) i, st[i]);
            }
        }
        return st;
    }

    // make a lookup table from symbols and their encodings
    private static void buildCode(String[] st, Node x, String s) {
        if (!x.isLeaf()) {
            buildCode(st, x.left, s + '0');
            buildCode(st, x.right, s + '1');
        } else {
            st[x.ch.charAt(0)] = s;
        }
    }

    // expand Huffman-encoded input static compressedCode and write to static uncompressedCode
    public static void expand() {
        // number of bytes to write
        int length = compressedCode.length();

        // decode using the Huffman trie
        int i = 0;
        while (i < length) {
            Node x = root;
            while (!x.isLeaf()) {
                boolean bit = false;
                if (compressedCode.charAt(i) == '1') {
                    bit = true;
                }
                if (bit) {
                    x = x.right;
                } else {
                    x = x.left;
                }
                i++;
            }
            uncompressedCode += "" + x.ch;
        }
    }

    public static void printTable() {
        Iterable<Character> it = table.keys();
        Iterator iterator = it.iterator();
        while (iterator.hasNext()) {
            Character c = (Character) iterator.next();
            System.out.println(c + "\t" + table.get(c));
        }
    }

    public static String[] tableToString() {
        Iterable<Character> it = table.keys();
        Iterator iterator = it.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        String[] st = new String[count];
        count = 0;
        iterator = it.iterator();
        while (iterator.hasNext()) {
            Character c = (Character) iterator.next();
            st[count++] = "" + c + " -> " + table.get(c);
        }
        return st;
    }

    public static String[] LUTToString() {
        Iterable<String> it = LUT.keys();
        Iterator iterator = it.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        String[] st = new String[count];
        count = 0;
        iterator = it.iterator();
        while (iterator.hasNext()) {
            String c = (String) iterator.next();
            st[count++] = c + " -> " + LUT.get(c);
        }
        return st;
    }

    public static void printTableToString() {
        String[] st = tableToString();
        int length = st.length;
        System.out.println("Si -> Ci");
        for (int i = 0; i < length; i++) {
            System.out.println(st[i]);
        }
    }

    public static int getL() {
        Iterable<Character> it = table.keys();
        Iterator iterator = it.iterator();
        int L = 0;
        while (iterator.hasNext()) {
            Character c = (Character) iterator.next();
            if (L < table.get(c).length()) {
                L = table.get(c).length();
            }
        }
        return L;
    }

    public static void buildLUT() {
        int L = getL();
        int length = (int) Math.pow(2, L);
        for (int i = 0; i < length; i++) {
            LUT.put(getBitCombination(i, L), ',');
        }
        Iterable<Character> it = table.keys();
        Iterator iterator = it.iterator();
        while (iterator.hasNext()) {
            Character c = (Character) iterator.next();
            if (L == table.get(c).length()) {
                LUT.put(table.get(c), c);
            } else {
                String[] comb = getAllBitCombinations(L - table.get(c).length());
                for (int i = 0; i < comb.length; i++) {
                    LUT.put(table.get(c) + comb[i], c);
                }
            }
        }

    }

    public static int getLi(Character c) {
        return table.get(c).length();
    }

    public static String[] getAllBitCombinations(int L) {
        int length = (int) Math.pow(2, L);
        String[] comb = new String[length];
        for (int i = 0; i < length; i++) {
            comb[i] = getBitCombination(i, L);
        }
        return comb;
    }

    public static String getBitCombination(int i, int L) {
        int MSD = L;
        int width = (int) Math.pow(2, MSD - 1);
        String line = "";
        while (MSD > 0) {
            line += i / width % 2;
            MSD--;
            width /= 2;
        }
        return line;
    }

    public static void printLUT() {
        Iterable<String> it = LUT.keys();
        Iterator iterator = it.iterator();
        System.out.println("Ci\tSi");
        while (iterator.hasNext()) {
            String c = (String) iterator.next();
            System.out.println(c + "\t" + LUT.get(c));
        }
    }

    public static void printTrie(Node node) {
        if (node != null) {
            printTrie(node.left);
            System.out.println(node);
            printTrie(node.right);
        }
    }

    public static Iterable<String> levelOrder() {
        Queue<String> keys = new Queue<>();
        Queue<Node> queue = new Queue<Node>();
        queue.enqueue(root);
        while (!queue.isEmpty()) {
            Node x = queue.dequeue();
            if (x == null) {
                continue;
            }
            keys.enqueue(x.ch);
            queue.enqueue(x.left);
            queue.enqueue(x.right);
        }
        return keys;
    }

    public static void main(String[] args) {

        compress("FCP*FCP*FCP*FCP*FCP*FCP*FFFFFFFFFCiiiii");
//        System.out.println(compressedCode);
        expand();
//        System.out.println(uncompressedCode);
//        printTable();
//        printTableToString();
        buildLUT();
//        printLUT();
//        printTrie(root);
        System.out.println(levelOrder());

        compress("ABRACADABRA");
//        System.out.println(compressedCode);
        expand();
//        System.out.println(uncompressedCode);
//        printTable();
//        printTableToString();
        buildLUT();
//        System.out.println(getL());
//        printLUT();
//        printTrie(root);

    }

}
