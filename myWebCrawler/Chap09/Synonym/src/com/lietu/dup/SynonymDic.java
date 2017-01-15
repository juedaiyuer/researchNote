package com.lietu.dup;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class SynonymDic {
	/**
	 *  An inner class of Ternary Search Trie that represents a node in the trie.
	 */
	protected final class TSTNode {
		/** The key to the node. */
		protected String data=null;

		/** The relative nodes. */
		protected TSTNode loKID;
		protected TSTNode eqKID;
		protected TSTNode hiKID;

		/** The char used in the split. */
		protected char splitchar;

		/**
		 *  Constructor method.
		 *
		 *@param  splitchar  The char used in the split.
		 *@param  parent     The parent node.
		 */
		protected TSTNode(char splitchar) {
			this.splitchar = splitchar;
		}
	}
	
	protected static class TSTItem {
		/** The key to the node. */
		protected String data=null;

		/** The char used in the split. */
		protected String key;

		/**
		 *  Constructor method.
		 *
		 *@param  splitchar  The char used in the split.
		 *@param  parent     The parent node.
		 */
		protected TSTItem(String key, String data) {
			this.key = key;
			this.data = data;
		}
	}

	/** The base node in the trie. */
	public TSTNode root;
	
	private static SynonymDic dicSynonymous = null;
	
	public static SynonymDic getInstance()
	{
		if (dicSynonymous == null)
			dicSynonymous = new SynonymDic();
		return dicSynonymous;
	}
	
	private SynonymDic()
	{
		this("Synonym.txt");
	}
	
	public static String getDir()
	{
		String dir = System.getProperty("dic.dir");
		if (dir == null)
			dir = "/dic/";
		else if( !dir.endsWith("/"))
			dir += "/";
		return dir;
	}
	
	/**
	 *  Constructs a Ternary Search Trie and loads data from a <code>File</code> into the Trie. 
	 *  The file is a normal text document, where each line is of the form
	 *  word : integer.
	 *
	 *@param  file             The <code>File</code> with the data to load into the Trie.
	 *@exception  IOException  A problem occured while reading the data.
	 */
	public SynonymDic(String dic){
		
		try{
			InputStream file = null;
			if (System.getProperty("dic.dir") == null)
				file = getClass().getResourceAsStream(SynonymDic.getDir()+dic);
			else
				file = new FileInputStream(new File(SynonymDic.getDir()+dic));
			
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(file,"GBK"));
			String line;			
			
			while ((line = in.readLine()) != null) {
//				System.out.println("line:"+line);
				StringTokenizer st = new StringTokenizer(line,"%" );
				
				String value = st.nextToken();
				
				if( "split".equals(value.toLowerCase()) ){
					value = " ";
				}
				if(value.toLowerCase().equals("null")){
					value = "";
				}
				while(st.hasMoreTokens())
				{
					String key = st.nextToken();
//					System.out.println(value+":"+key);
					
					if (root == null) {
						root = new TSTNode(key.charAt(0));
					}
					
					if (key.length() > 0 && root != null) {
						
						TSTNode currentNode =
							getOrCreateNode(key);
						
							currentNode.data = value;
					}
				}
			}
			in.close();
		}catch( IOException e)
		{
			System.out.println(e);
		}
	}

	/**
	 *  Retrieve the object indexed by a key.
	 *
	 *@param      key  A <code>String</code> index.
	 *@return      The object retrieved from the Ternary Search Trie.
	 */
	public Object get(String key) {
		TSTNode node = getNode(key);
		if (node == null) { return null; }
		return node.data;
	}

	/**
	 *  Returns the node indexed by key, or <code>null</code> if that node doesn't exist.
	 *  Search begins at root node.
	 *
	 *@param  key  A <code>String</code> that indexes the node that is returned.
	 *@return   The node object indexed by key. This object is an
	 *      instance of an inner class named <code>TernarySearchTrie.TSTNode</code>.
	 */
	public TSTNode getNode(String key) {
		return getNode(key, root);
	}

    /**
     * get the handle set of a word
     */
	public String getHandle(String key) {
		TSTNode node= getNode(key, root);
		if (node==null)
			return null;
		return node.data;
	}

    /**
     * Weather the word exist in the dictionary
     * @param sWord
     * @return true if exist
     */
    public boolean isExist(String key)
    {
		String q= getHandle(key);
		if (q==null)
			return false;
		
    	return true;
    }
    
	/**
	 *  Returns the node indexed by key, or <code>null</code> if that node doesn't exist.
	 *  The search begins at root node.
	 *
	 *@param  key2        A <code>String</code> that indexes the node that is returned.
	 *@param  startNode  The top node defining the subtrie to be searched.
	 *@return            The node object indexed by key. This object is
	 *      an instance of an inner class named <code>TernarySearchTrie.TSTNode</code>.
	 */
	protected TSTNode getNode(String key, TSTNode startNode) {
		if (key == null || startNode == null || "".equals(key)) {
			return null;
		}
		TSTNode currentNode = startNode;
		int charIndex = 0;
		while (true) {
			if (currentNode == null) {
				return null;
			}
			int charComp = key.charAt(charIndex) - currentNode.splitchar;
			if (charComp == 0) {
				charIndex++;
				if (charIndex == key.length()) {
					return currentNode;
				}
				currentNode = currentNode.eqKID;
			} else if (charComp < 0) {
				currentNode = currentNode.loKID;
			} else {
				currentNode = currentNode.hiKID;
			}
		}
	}

	/**
	 *  Returns the node indexed by key, creating that node if it doesn't exist,
	 *  and creating any required intermediate nodes if they don't exist.
	 *
	 *@param  key                           A <code>String</code> that indexes the node that is returned.
	 *@return                                  The node object indexed by key. This object is an
	 *                                               instance of an inner class named <code>TernarySearchTrie.TSTNode</code>.
	 *@exception  NullPointerException      If the key is <code>null</code>.
	 *@exception  IllegalArgumentException  If the key is an empty <code>String</code>.
	 */
	protected TSTNode getOrCreateNode(String key)
		throws NullPointerException, IllegalArgumentException {
		if (key == null) {
			throw new NullPointerException("attempt to get or create node with null key");
		}
		if ("".equals(key)) {
			throw new IllegalArgumentException("attempt to get or create node with key of zero length");
		}
		if (root == null) {
			root = new TSTNode(key.charAt(0));
		}
		TSTNode currentNode = root;
		int charIndex = 0;
		while (true) {
			int charComp =(
					key.charAt(charIndex) -
					currentNode.splitchar);
			if (charComp == 0) {
				charIndex++;
				if (charIndex == key.length()) {
					return currentNode;
				}
				if (currentNode.eqKID == null) {
					currentNode.eqKID =
						new TSTNode(key.charAt(charIndex));
				}
				currentNode = currentNode.eqKID;
			} else if (charComp < 0) {
				if (currentNode.loKID == null) {
					currentNode.loKID =
						new TSTNode(key.charAt(charIndex));
				}
				currentNode = currentNode.loKID;
			} else {
				if (currentNode.hiKID == null) {
					currentNode.hiKID =
						new TSTNode(key.charAt(charIndex));
				}
				currentNode = currentNode.hiKID;
			}
		}
	}

	public static final class Prefix {
		private byte value;
	    
		public Prefix(byte a)
		{ value = a; }
		
	    /** Match the word exactly */
	    public static final Prefix Match = new Prefix((byte)0);
	    /** MisMatch the word */
	    public static final Prefix MisMatch = new Prefix((byte)1);
	    /** Match the prefix */
	    public static final Prefix MatchPrefix = new Prefix((byte)2);
	    
	    public String toString()
	    {
	    	if( value == Match.value)
	    		return "Match";
	    	else if( value == MisMatch.value)
    			return "MisMatch";
	    	else if( value == MatchPrefix.value)
    			return "MatchPrefix";
	    	return "Invalid";
	    }
	}

	public static class PrefixRet {
		public Prefix value;
		public String data;
		public int next;
		
		public PrefixRet(Prefix v,String d)
		{
			value = v;
			data = d;
		}
		
		public String toString()
		{
			return value+":"+data+":"+next;
		}
	}
	
	public void checkPrefix(String sentence,int offset,PrefixRet ret) {
		if (sentence == null || root == null || "".equals(sentence)) {
			ret.value = Prefix.MisMatch;
			ret.data = null;
			ret.next = offset;
			return ;
		}
		ret.value = Prefix.MisMatch;//初始返回值设为没匹配上
		TSTNode currentNode = root;
		int charIndex = offset;
		while (true) {
			if (currentNode == null) {
				return;
			}
			int charComp = sentence.charAt(charIndex) - currentNode.splitchar;
			
			if (charComp == 0) {
				charIndex++;

				if(currentNode.data != null){
					ret.data = currentNode.data;//候选最长匹配词
					ret.value = Prefix.Match;
					ret.next = charIndex;
				}
				if (charIndex == sentence.length()) {
					return; //已经匹配完
				}
				currentNode = currentNode.eqKID;
			} else if (charComp < 0) {
				currentNode = currentNode.loKID;
			} else {
				currentNode = currentNode.hiKID;
			}
		}
	}
}
