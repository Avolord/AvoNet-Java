import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Image {
	private String path;
	private BufferedReader data = null;
	private int width;
	private int height;
	private int index = 0;

	Image(String Path, int image_width, int image_height) {
		path = Path;
		width = image_width;
		height = image_height;
		initBufferedReader(path);
	}

	private void initBufferedReader(String Path) {
		try {
			data = new BufferedReader(new FileReader(Path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ImageData getImage() {
		String line = "";
		String[] img_string = {};
		double[] target_data = new double[10];
		double[] img_data = new double[width * height];
		int number = 0;

		try {
			if ((line = data.readLine()) != null) {
				img_string = line.split(",");

				for (int i = 1; i < img_string.length; i++) {
					img_data[i - 1] = Integer.parseInt(img_string[i]) / 255.0 * 0.99 + 0.01;
				}

				number = Integer.parseInt(img_string[0]);

				for (int i = 0; i < 10; i++) {
					target_data[i] = (i == number) ? 0.99 : 0.01;
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ImageData(img_data, number, target_data);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String Path) {
		path = Path;
		initBufferedReader(path);
	}

	public BufferedReader getData() {
		return data;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int img_width) {
		width = img_width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int img_height) {
		height = img_height;
	}

	public int getIndex() {
		return index;
	}

}

class ImageData {
	private double[] data;
	private int num;
	private double[] target;

	ImageData(double[] img_data, int number, double[] img_target) {
		data = img_data.clone();
		num = number;
		target = img_target;
	}

	public double[] getData() {
		return data;
	}

	public double[] getTarget() {
		return target;
	}

	public int getNum() {
		return num;
	}
}
