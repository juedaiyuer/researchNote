

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class RhinoTest
{
    public static void main(String[] args)
    {
        /* 创建一个Javascript的上下文环境，用来存储Javascript的环境信息 */
        Context cx = Context.enter();
        try {
            /* 初始化Javascript标准对象（例如Object, Function, Array等） */
            Scriptable scope = cx.initStandardObjects();
 
            /* 读取一个.js文件 */
            String script = "";
            File file = null;
            if(args.length > 0)
            {
                file = new File(args[0]);  // 如果有参数，则读入第一个参数中指定的js文件
            }
            else
            {
                file = new File("script.js"); // 如果没有参数，则读入script.js
            }
            BufferedReader in = new BufferedReader(new FileReader(file));
            String s = "";
            while((s = in.readLine()) != null)
            {
                script += s + "\n";
            }
 
            /* 执行代码 */
            cx.evaluateString(scope, script, "[" + file.getName() + "]", 1, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            Context.exit();
        }
    }
}

