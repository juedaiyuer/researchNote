

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;

public class TableValid {
    private int trnum;
    private int tdnum;
    private int linknum;
    private int textnum;
    private int scriptnum;

    public int getScriptnum() {
        return scriptnum;
    }
    public void setScriptnum(int scriptnum) {
        this.scriptnum = scriptnum;
    }
    public int getLinknum() {
        return linknum;
    }
    public void setLinknum(int linknum) {
        this.linknum = linknum;
    }
    public int getTdnum() {
        return tdnum;
    }
    public void setTdnum(int tdnum) {
        this.tdnum = tdnum;
    }
    public int getTextnum() {
        return textnum;
    }
    public void setTextnum(int textnum) {
        this.textnum = textnum;
    }
    public int getTrnum() {
        return trnum;
    }
    public void setTrnum(int trnum) {
        this.trnum = trnum;
    }
}




