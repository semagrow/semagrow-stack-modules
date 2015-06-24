package org.aksw.simba.quetzal.datastructues;

import java.util.HashMap;

/**
 * Trie Node implementation
 * @author Saleem
 *
 */
public class TrieNode {
    char letter;
    HashMap<Character,TrieNode> children;
    TrieNode parent;
    boolean insertFlag =true ;  // if true then child can be inserted
   public static TrieNode root;
    TrieNode(char letter, TrieNode parentNode) {
        this.letter = letter;
        children = new HashMap<Character, TrieNode>();
        this.parent = parentNode;
        this.insertFlag = true;
    }
    /**
     * Constructor
     */
    TrieNode() {
        this.letter = '\0';
        children = new HashMap<Character, TrieNode>();
        this.parent = this;
       root = this;
      this. insertFlag = true;
    }
    
}