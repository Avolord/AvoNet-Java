import java.io.IOException;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws IOException {
		int iterations = 60000;
		int batches = 1;
		String src = "F:\\GitLab\\AvoNet\\mnist_train.csv";

		Loading loading = new Loading(iterations);
		GUI testing = new GUI(400, 100, iterations);
		
		testing.setVisible(true);

		AvoNet ImgC = new AvoNet(new int[] { 784, 200, 10 }, 0.15);

		ImgC.estimate("full", iterations, batches);

		double time = System.nanoTime();

		Image img = new Image(src, 28, 28);

		for (int i = 0; i < iterations; i++) {
			loading.load(i, testing);
			ImageData training = img.getImage();
			ImgC.train(training.getData(), training.getTarget());
		}
		console.line(1);

		writer result = new writer("result for " + iterations + " iterations.txt");

		console.log("\nValues of Training with " + iterations + " iterations.");
		result.write("\nValues of Training with " + iterations + " iterations.", true);
		AvoNet.elapsed(time);
		AvoNet.elapsed(time, result);

		console.log("\nThe average Error was: " + ImgC.getWholeError());
		result.write("\nThe average Error was: " + ImgC.getWholeError(), true);
		console.log("The minimum Error was: " + ImgC.getMinError());
		result.write("The minimum Error was: " + ImgC.getMinError(), true);
		console.log("The maximum Error was: " + ImgC.getMaxError());
		result.write("The maximum Error was: " + ImgC.getMaxError(), true);
		result.done();

		double[] av_error = new double[10];
		int[] indeze = new int[10];

		console.line(1);
		console.log("Average Errors For Numbers 1 - 10");

		for (int j = 0; j < 100; j++) {
			ImageData test = img.getImage();
			double[] g = ImgC.guess(test.getData());
			int index = 0;
			for (int i = 0; i < 10; i++) {
				index = (g[i] > g[index]) ? i : index;
			}
			av_error[index] += g[index];
			indeze[index]++;

		}
		console.log("Number 0: " + av_error[0] / indeze[0] * 100 + "%");
		console.log("Number 1: " + av_error[1] / indeze[1] * 100 + "%");
		console.log("Number 2: " + av_error[2] / indeze[2] * 100 + "%");
		console.log("Number 3: " + av_error[3] / indeze[3] * 100 + "%");
		console.log("Number 4: " + av_error[4] / indeze[4] * 100 + "%");
		console.log("Number 5: " + av_error[5] / indeze[5] * 100 + "%");
		console.log("Number 6: " + av_error[6] / indeze[6] * 100 + "%");
		console.log("Number 7: " + av_error[7] / indeze[7] * 100 + "%");
		console.log("Number 8: " + av_error[8] / indeze[8] * 100 + "%");
		console.log("Number 9: " + av_error[9] / indeze[9] * 100 + "%");

		//ImgC.export("Image Classifier");
		
	}

}
