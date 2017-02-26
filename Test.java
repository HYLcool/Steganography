
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.*;

public class Test {
	public static void main(String[] args) {
		try {
			File inf = new File("picture.jpg");
			BufferedImage bi = ImageIO.read(inf);

			File outf = new File("picture.png");
			ImageIO.write(bi, "png", outf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}