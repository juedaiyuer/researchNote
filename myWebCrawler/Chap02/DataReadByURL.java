
import java.io.InputStream;
import java.net.URL;
import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
import org.apache.hadoop.io.IOUtils;

public class DataReadByURL {
	static {
		URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
	}

	public static void main(String[] args) throws Exception {
		InputStream in = null;
		try {
			in = new URL("hdfs://127.0.0.1:9000/data/mydata").openStream();
			IOUtils.copyBytes(in, System.out, 2048, false);
		} finally {
			IOUtils.closeStream(in);
		}
	}
}
