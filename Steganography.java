
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import java.util.*;

public class Steganography {

	private final String targetImage = "resultImage.png";
	private final String targetFile = "hiddenContent.txt";

	// the operation mode
	private static final int HIDE = 256;
	private static final int EXTRACT = 257;
	private int mode;

	// image information
	private BufferedImage image = null;
	private int width;
	private int height;
	private int minX;
	private int minY;

	// text file information
	int [] content;
	int len = 0;

	public Steganography(int md) {
		this.mode = md;
		switch (this.mode) {
			case HIDE:
				System.out.println("In HIDE mode");
				break;
			case EXTRACT:
				System.out.println("In EXTRACT mode");
				break;
			default:
				System.out.println("Wrong mode number");
				System.exit(1);
		}
	}

	// load the specific image and get some necessary information about this image
	public void setImage(String imagename) {
		File file = new File(imagename);
		try {
			this.image = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.width = this.image.getWidth();
		this.height = this.image.getHeight();
		this.minX = this.image.getMinX();
		this.minY = this.image.getMinY();
		System.out.println("width = " + this.width + ", height = " + this.height);
		// System.out.println("Max file size: " + (this.width * this.height * 3 / 8.0) + " bytes");
	}

	// load the specific information text
	public void setTxt(String txtname) {
		File file = new File(txtname);
		Reader reader = null;
		this.len = 0;
		this.content = new int[(int)file.length()];
		try {
			reader = new InputStreamReader(new FileInputStream(file));
			int temp;
			while((temp = reader.read()) != -1) {
				this.content[len++] = temp;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Actual file size: " + this.content.length);
	}

	// hide the text into the image
	public void hide() {
		int [] rgbData = new int[this.width * this.height];
		int [] outData = new int[this.width * this.height];

		// get the rgb data from image
		getRGB(0, 0, rgbData);
		int wordnum = 0;
		int bitnum = 0;
		int currWord = this.content[wordnum];
		for (int y = minY; y < this.height; y++) {
			for (int x = minX; x < this. width; x++) {
				// divide the r, g, b
				int [] rgb = {
					clamp((rgbData[x + this.width * y] >> 16) & 0xff),
					clamp((rgbData[x + this.width * y] >> 8) & 0xff),
					clamp(rgbData[x + this.width * y] & 0xff)
				};
				
				// use the LSB of each r/g/b to store the binary information
				for (int i = 0; i < 3; i++) {
					rgb[i] = (rgb[i] & -2) | ((currWord >> bitnum) & 0x1);
					bitnum++;
					if (bitnum > 15) {
						bitnum = 0;
						wordnum++;
						if (wordnum >= this.len) {
							currWord = 0;
						} else {
							currWord = this.content[wordnum];
						}
					}
				}
				
				outData[x + this.width * y] = ((0xff << 24) | (rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
			}
		}

		// update the RGB data
		this.setRGB(0, 0, outData);

		this.storeImage();
	}

	// extract hidden information from an image
	public void extract() {
		int [] rgbData = new int[this.width * this.height];
		getRGB(0, 0, rgbData);
		this.content = new int[this.width * this.height / 3 + 5];
		int wordnum = 0;
		int bitnum = 0;
		int currWord = 0;
		for (int y = minY; y < this.height; y++) {
			for (int x = minX; x < this.width; x++) {
				int [] rgb = {
					clamp((rgbData[x + this.width * y] >> 16) & 0xff),
					clamp((rgbData[x + this.width * y] >> 8) & 0xff),
					clamp(rgbData[x + this.width * y] & 0xff)
				};
				
				// extract the LSB from the r/g/b information and combine them into single words
				for (int i = 0; i < 3; i++) {
					currWord = currWord | ((rgb[i] & 0x1) << bitnum);
					bitnum++;
					if (bitnum > 15) {
						bitnum = 0;
						this.content[wordnum++] = currWord;
						// System.out.println(currWord);
						if (currWord == 0) {
							y = this.height;
							x = this.width;
							break;
						}
						currWord = 0;
					}
				}
			}
		}

		this.len = this.getLength();
		System.out.println("Hidden Message Length: " + this.len);
		this.storeFile();
	}

	// store image into the specific file
	private void storeImage() {
		try {

			File out = new File(this.targetImage);
			ImageIO.write(this.image, "png", out);

			System.out.println("Store the result image into file " + this.targetImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// store text into the specific file
	private void storeFile() {
		try {
			File out = new File(this.targetFile);
			Writer writer = new FileWriter(out);
			for (int i = 0; i < this.len; i++) {
				writer.write(this.content[i]);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// get the actual length of the extracted text
	// remove the useless 0s from the tail
	private int getLength() {
		for (int l = this.content.length - 1; l >= 0; l--) {
			if (this.content[l] != 0)
				return l + 1;
		}
		return 0;
	}

	// get RGB array
	private int [] getRGB(int x, int y, int[] pixels) {
		int type= this.image.getType();  
		if ( type ==BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )  
		    return (int [])this.image.getRaster().getDataElements(x, y, this.width, this.height, pixels);  
		else  
		    return this.image.getRGB( x, y, this.width, this.height, pixels, 0, this.width );
	}

	// set the RGB array
	private void setRGB(int x, int y, int [] rgb) {
		int type= this.image.getType();  
		if ( type ==BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )  
		   this.image.getRaster().setDataElements(x, y, width, height, rgb );  
		else  
		   this.image.setRGB(x, y, this.width, this.height, rgb, 0, this.width ); 
	}

	// keep the pixel value in a valid range
	private int clamp(int p) {
		return p > 255 ? 255 : (p < 0 ? 0 : p);
	}

	public static void main(String[] args) throws Exception {
		// including a image file name and a text file name
		// put the content of text file into the image
		if (args.length == 2) {
			Steganography st = new Steganography(Steganography.HIDE);
			st.setImage(args[0]);
			st.setTxt(args[1]);

			st.hide();
		} else if (args.length == 1) { // including a image file name, extract the hidden content from the image
			Steganography st = new Steganography(Steganography.EXTRACT);
			st.setImage(args[0]);

			st.extract();
		} else {
			System.out.println("Wrong arguments!");
			return;
		}
		System.out.println("DONE");
	}
}