import java.awt.Toolkit;

public class GameUtil {
	public static int getScreenMiddleLocationX(int width) {
		int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
		if (width > screenWidth || width < 0)
			return 0;
		return (screenWidth - width) / 2;
	}

	public static int getScreenMiddleLocationY(int height){
		int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
		if(height > screenHeight || height < 0) return 0;
		return (screenHeight - height)/2;
	}
}
