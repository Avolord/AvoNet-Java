import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Image {
	String path;
	BufferedReader data = null;
	int width;
	int height;
	int index = 0;
	
	Image(String Path, int image_width, int image_height) {
		path = Path;
		try {
			data = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		width = image_width;
		height = image_height;	
	}
	
	public ImageData getImage() {
		String line = "";
		String[] img_string = {};
		double[] target_data = new double[10];
		double[] img_data = new double[width*height];
		int number = 0;
		
		try {
			if((line = data.readLine()) != null) {
				img_string = line.split(",");
				
				for(int i=1; i<img_string.length; i++) {
					img_data[i - 1] = Integer.parseInt(img_string[i]) / 255.0 * 0.99 + 0.01;
				}
				
				number = Integer.parseInt(img_string[0]);
				
				for(int i=0;i<10;i++) {
					target_data[i] = (i == number) ? 0.99 : 0.01;
				}
				
			}		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ImageData(img_data,number,target_data);
	}
	
}

class ImageData {
	double[] data;
	int num;
	double[] target;
	
	ImageData(double[] img_data, int number, double[] img_target) {
		data = img_data.clone();
		num = number;
		target = img_target;
	}
}
