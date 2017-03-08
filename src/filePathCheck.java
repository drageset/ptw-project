import java.io.File;

public class filePathCheck {
	public static void main(String[] args) {
		File file = new File(".");
		String curDir = System.getProperty("user.dir");
		
		System.out.println(file.getAbsolutePath());
		System.out.println(curDir);
	}
}
