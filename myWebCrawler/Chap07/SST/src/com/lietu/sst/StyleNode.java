package com.lietu.sst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class StyleNode {
    private int count;
    private ArrayList<ElementNode> nodeList = new ArrayList<ElementNode>();
   
    public StyleNode() {
            this.count = 1;
    }
   
    public boolean equals(StyleNode obj) {
            if (this.nodeList.size() != obj.nodeList.size()) {
                    return false;
            }
           
            for (int i=0; i<this.nodeList.size(); i++) {
                    if (!this.nodeList.get(i).getNodeName().equals(obj.nodeList.get(i).getNodeName())) {
                            return false;
                    }
            }
            return true;
    }
   
    public void addStyleNode(ElementNode node) {
            nodeList.add(node);
    }
   
    public void print() {
           
            if (nodeList.size() == 1 &&
                            nodeList.get(0).isText()) {
                    return;
            }
            System.out.print("# " + count + "[");
            for (int i=0; i<nodeList.size(); i++) {
                    System.out.print(nodeList.get(i).toString() + " ");
            }
            System.out.println("]");
           
            for (int i=0; i<nodeList.size(); i++) {
                    nodeList.get(i).printChildren();
            }
    }
   
    public void printNodeSetImportance() {
            for (int i=0; i<nodeList.size(); i++) {
                    nodeList.get(i).printNodeImportance();
            }
    }
   
    public void printCompositeImportance() {
            for (int i=0; i<nodeList.size(); i++) {
                    nodeList.get(i).printCompositeImportance();
            }
    }

    public int getCount() {
            return count;
    }

    public void increaseNumber() {
            this.count++;
    }

    public ElementNode getStyleNode(int i) {
            // TODO Auto-generated method stub
            if (i > nodeList.size()) {
                    return null;
            }
            return nodeList.get(i);
    }

    public double getImportance(int numOfStyles) {
            if (numOfStyles == 1) {
                    return 1.0;
            }
            double ratio = (double)count / numOfStyles;
           
            return -ratio * Math.log(ratio) / Math.log(numOfStyles);
    }

    public double getCompositeImportance() {
            double sum = 0.0;
            for (ElementNode styleNode : nodeList) {
                    sum += styleNode.getCompositeImportance();
            }
            return sum / nodeList.size();
    }

    public double getLeafCompositeImportance() {
            boolean isText = true;
            for (ElementNode styleNode : nodeList) {
                    if(!styleNode.isLeaf()) {
                    	isText = false;
                    	break;
                	}
            }
            
            if(!isText){
            	return 0;
            }
            int m = count;
            if(m==0){
            	return 0;
            }
            HashMap<String,Integer> atts = new HashMap<String,Integer>();
            for (ElementNode styleNode : nodeList) {
            	ArrayList<String> contents = styleNode.getContents();
            	for(String a:contents)
            	{
            		Integer count = atts.get(a);
            		if(count == null)
            		{
            			atts.put(a, 1);
            		}
            		else
            		{
            			++count;
            			atts.put(a, count);
            		}
            	}
            }
            
            double sumHE =0;
            
            for (Entry<String, Integer> attEntry : atts.entrySet()) {
            	String att = attEntry.getKey();
            	for (ElementNode styleNode : nodeList) {
            		ArrayList<String> contents = styleNode.getContents();
            		double heAtt = 0;
            		if(contents.contains(att)) {
            			double p = 1/(double)(attEntry.getValue());
            			heAtt = p*( Math.log(p) / Math.log(m) );
            		}
            		sumHE += heAtt;
            	}
            }
            int n = atts.size();
            if(n==0)
            	return 0;
            return (1+sumHE/n);
    }

    public void addContents(StyleNode styleNodeSet) {
            int i = 0;
           
            for (ElementNode styleNode : this.nodeList) {
                    styleNode.addContent(styleNodeSet.getStyleNode(i++));
            }
    }

    public void printInformation() {
            System.out.println(getInformation());
            for (int i=0; i<nodeList.size(); i++) {
                    nodeList.get(i).printInformation();
            }
    }
   
    public String getInformation() {
            StringBuffer childrenInfo = new StringBuffer();
            for (int i=0; i<nodeList.size(); i++) {
                    childrenInfo.append(nodeList.get(i).getNodeName() + " ");
            }
            return "[NODESET] " + childrenInfo + " " + this.nodeList.size() + " " + count;
    }

    public void printContentImportance() {
            for (ElementNode node : nodeList) {
                    node.printContentImportance();
            }
    }
   
    public void printTree(String sep) {
            System.out.println(sep + "[NS] " + nodeList.size());
            for (ElementNode styleNode : nodeList) {
                    styleNode.printTree(sep + " ");
            }
    }
}