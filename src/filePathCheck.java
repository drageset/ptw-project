import java.io.File;

public class filePathCheck {
	public static void main(String[] args) {
		File file = new File("data.csv");
		
		System.out.println(file.getAbsolutePath());
	}
}
