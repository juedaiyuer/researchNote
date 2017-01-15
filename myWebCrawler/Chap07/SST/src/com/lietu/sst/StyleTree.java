package com.lietu.sst;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StyleTree {
	private ElementNode styleRoot;

	private static String defaultCharEncoding = "utf-8";// "windows-1252";

	public StyleTree() {
		this.styleRoot = ElementNode.getInstanceOf();
	}

	protected static Node parseBytes(byte[] bytes) {
		DocumentFragment root = null;
		Node currentNode;
		InputSource input = null;
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		input = new InputSource(stream);

		try {
			root = StyleTree.parse(input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		currentNode = root;
		return currentNode;
	}

	private static byte[] readBytesFromFile(File file) {
		DataInputStream in = null;
		byte[] bytes = new byte[(int) file.length()];

		try {
			in = new DataInputStream(new FileInputStream(file));
			in.readFully(bytes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return bytes;
	}

	private static byte[] readBytesFromFile(String filename) {
		File file = new File(filename);

		return readBytesFromFile(file);
	}

	private static DocumentFragment parse(InputSource input) throws Exception {
		DOMFragmentParser parser = new DOMFragmentParser();
		HTMLDocumentImpl doc = new HTMLDocumentImpl();

		try {
			parser.setFeature(
					"http://cyberneko.org/html/features/augmentations", false);
			parser.setProperty(
					"http://cyberneko.org/html/properties/default-encoding",
					defaultCharEncoding);
			parser
					.setFeature(
							"http://cyberneko.org/html/features/scanner/ignore-specified-charset",
							true);
			parser
					.setFeature(
							"http://cyberneko.org/html/features/balance-tags/ignore-outside-content",
							false);
			parser
					.setFeature(
							"http://cyberneko.org/html/features/balance-tags/document-fragment",
							true);
			parser.setFeature(
					"http://cyberneko.org/html/features/report-errors", false);
		} catch (SAXException e) {
		}

		doc.setErrorChecking(false);
		DocumentFragment res = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		parser.parse(input, frag);
		res.appendChild(frag);

		try {
			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				System.out.println(" - new frag, "
						+ frag.getChildNodes().getLength() + " nodes.");
				res.appendChild(frag);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public static void getContent(ElementNode styleRoot, Node root,
			StringWriter out) {
		NodeList nodeList = root.getChildNodes();
		int i = 0;
		StyleNode styleNodeSet = new StyleNode();
		Node node = null;

		if (styleRoot.isLeaf() && styleRoot.isText() && styleRoot.isImportant()) {
			if (out == null) {
				System.err.println("out is null");
			}
			out.append(ElementNode.getNodeValue(root));
		}
		for (i = 0; i < nodeList.getLength(); i++) {
			node = nodeList.item(i);
			ElementNode styleNode = ElementNode.getInstanceOf(node);
			styleNodeSet.addStyleNode(styleNode);
		}
		if (!root.hasChildNodes()) {
			return;
		}
		StyleNode child = styleRoot.get(styleNodeSet);
		if (child == null) {
			System.err.println(styleRoot.getInformation()
					+ "child is null -- it should not be occured!!");
		}
		for (i = 0; i < nodeList.getLength(); i++) {
			if (child.getStyleNode(i) == null) {
				System.err.println("child is null");
			}
			if (nodeList.item(i) == null) {
				System.err.println("nodeList is null");
			}
			getContent(child.getStyleNode(i), nodeList.item(i), out);
		}
	}

	public void trainFile(String filename) {
		trainFile(new File(filename));
	}

	public void trainFile(File file) {
		Node currentNode = null;
		byte[] bytes = null;

		bytes = readBytesFromFile(file);
		currentNode = parseBytes(bytes);
		this.styleRoot.trainNode(currentNode);
	}

	public void trainURL(String urlString) {
		//Node currentNode = null;
		
		//bytes = readBytesFromFile(file);
		//currentNode = parseBytes(bytes);
		//this.styleRoot.trainNode(currentNode);
	}

	public String getText(String filename) {
		StringWriter out = new StringWriter();
		byte[] bytes = readBytesFromFile(filename);
		Node currentNode = parseBytes(bytes);

		this.styleRoot.getContent(currentNode, out);

		return out.toString();
	}

	public String getText(File file) {
		StringWriter out = new StringWriter();
		byte[] bytes = readBytesFromFile(file);
		Node currentNode = parseBytes(bytes);

		this.styleRoot.getContent(currentNode, out);

		return out.toString();
	}

	private static void printNode(Node node, String str) {
		System.out
				.println(str + node.getNodeName() + " " + node.getNodeValue());
		if (node.hasChildNodes() != true) {
			return;
		}
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			printNode(nodeList.item(i), str + " ");
		}
	}

	public static void printNode(Node node) {
		printNode(node, "");
	}

	public void trainFilesInDirectory(String dirName) {
		File dir = new File(dirName);

		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				if (children[i].isFile()) {
					trainFile(children[i]);
				}
			}
		}
	}

	public void getTextInDirectory(String dirName) {
		File dir = new File(dirName);

		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++) {
				if (children[i].isFile()) {
					System.out.println(children[i].getName() + " : "
							+ getText(children[i]));
				}
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage : arg0");
			return;
		}

		StyleTree sTree = new StyleTree();
		sTree.trainFilesInDirectory(args[0]);
		sTree.getTextInDirectory(args[0]);
	}

	public void printTree() {
		this.styleRoot.printTree("");
	}
}