package ip;

public class IPTest {

	public static void main(String[] args) {
		// 指定纯真数据库的文件名，所在文件夹
		IPSeeker ip = new IPSeeker("QQWry.Dat", "c:\\qqwry");
		// 测试IP 58.20.43.13
		System.out.println(ip.getIPLocation("58.20.43.13").getCountry() + ":"
				+ ip.getIPLocation("58.20.43.13").getArea());
	}
}
