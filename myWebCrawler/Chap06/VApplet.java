

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Component;
import java.util.Vector; // JMF相关的类
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.format.VideoFormat;
import javax.swing.JPanel;
import javax.swing.JApplet;

public class VApplet extends JApplet {
	private JPanel jContentPane = null;
	private Choice choice = null;

	public VApplet() {
		super();
	}

	public void init() {
		this.setSize(320, 240);
		this.setContentPane(getJContentPane());
		this.setName("VApplet");
	}

	// 取系统所有可采集的硬件设备列表
	private CaptureDeviceInfo[] getDevices() {
		Vector devices = CaptureDeviceManager.getDeviceList(null);
		CaptureDeviceInfo[] info = new CaptureDeviceInfo[devices.size()];
		for (int i = 0; i < devices.size(); i++) {
			info[i] = (CaptureDeviceInfo) devices.get(i);
		}
		return info;
	}

	// 从已知设备中取所有视频设备的列表
	private CaptureDeviceInfo[] getVideoDevices() {
		CaptureDeviceInfo[] info = getDevices();
		CaptureDeviceInfo[] videoDevInfo;
		Vector vc = new Vector();
		for (int i = 0; i < info.length; i++) {
			// 取设备支持的格式，如果有一个是视频格式，则认为此设备为视频设备
			Format[] fmt = info[i].getFormats();
			for (int j = 0; j < fmt.length; j++) {
				if (fmt[j] instanceof VideoFormat) {
					vc.add(info[i]);
				}
				break;
			}
		}
		videoDevInfo = new CaptureDeviceInfo[vc.size()];
		for (int i = 0; i < vc.size(); i++) {
			videoDevInfo[i] = (CaptureDeviceInfo) vc.get(i);
		}
		return videoDevInfo;
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			BorderLayout borderLayout = new BorderLayout();
			jContentPane = new JPanel();
			jContentPane.setLayout(borderLayout);

			MediaLocator ml = null;
			Player player = null;
			try {
				// 这里我只有一个视频设备，直接取第一个
				// 取得当前设备的MediaLocator
				ml = getVideoDevices()[0].getLocator();
				// 用已经取得的MediaLocator得到一个Player
				player = Manager.createRealizedPlayer(ml);
				player.start();
				// 取得Player的AWT Component
				Component comp = player.getVisualComponent();
				// 如果是音频设备这个方法将返回null,这里要再判断一次
				if (comp != null) {
					// 将Component加到窗体
					jContentPane.add(comp, BorderLayout.EAST);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jContentPane;
	}
}
