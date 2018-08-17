
public class setup {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int[] conf = {784,100,10};
		AvoNet ImgC = new AvoNet(conf, 0.2);
		
		ImgC.Benchmark("full", 1);
		
		double time = System.nanoTime();
		Image img = new Image("F:\\GitLab\\AvoNet\\mnist_train.csv", 28, 28);
		for(int i=0;i<60000;i++) {
			ImageData training = img.getImage();
			ImgC.train(training.getData(), training.getTarget());
		}
		console.line(1);
		console.log("Values of Training with "+60000+" iterations.");
		AvoNet.elapsed(time);
		
		console.line(1);
		console.log("The average Error was: "+ImgC.getWholeError());
		console.log("The minimum Error was: "+ImgC.getMinError());
		console.log("The maximum Error was: "+ImgC.getMaxError());
	}

}
