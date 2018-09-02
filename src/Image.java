import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Image {
	private String path;
	private BufferedReader data = null;
	private int width, height, index = 0;

	public Image(String Path, int image_width, int image_height) {
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
				
				double deg = Math.floor(Math.random()*30)-15;
				img_data = rotateImg(img_data, deg);

				number = Integer.parseInt(img_string[0]);

				for (int i = 0; i < 10; i++) {
					target_data[i] = (i == number) ? 0.99 : 0.01;
				} 
			} else {
				initBufferedReader(path);
				return getImage();
			}
		} catch (IOException e) {
			return null;
		}
		return new ImageData(img_data, number, target_data);
	}
	
	private double[] rotateImg(double[] data, double deg) {
	    double rad = deg / 180 * Math.PI;
	    double[] result = new double[width*height];
	    Arrays.fill(result, 0.01);
	    
	    for (int x = 0; x < width; x++) {
	      for (int y = 0; y < height; y++) {
	        if (data[x + y * width] == 0.01) {
	          continue;
	        }
	        int x_ = x - width/2,
	          y_ = y - height/2;
	        double dist = Math.sqrt(x_*x_ + y_*y_);
	        int x_value = (int) Math.floor(Math.cos(rad + Math.acos(x_ / dist)) * dist) + width/2;
	        int y_value = (int) Math.floor(Math.sin(rad + Math.asin(y_ / dist)) * dist) + height/2;
	        
	        if(x_value + y_value * width < width*height && x_value + y_value * width >= 0) {
	        	try {
	        		result[x_value + y_value * width] = data[x + y * width];
	        	} catch(ArrayIndexOutOfBoundsException e) {
	        		e.printStackTrace();
	        	}
	        }
	        
	      }
	    }
	    return result;
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
	private double[] data, target;
	private int num;

	ImageData(double[] img_data, int number, double[] img_target) {
		data = img_data.clone();
		num = number;
		target = img_target.clone();
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
