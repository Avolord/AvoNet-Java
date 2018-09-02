
public class Loading {
	private int percentage = 0;
	private double max;
	private double perc;
	
	public Loading(double maximum) {
		percentage = 0;
		max = maximum;
		perc = maximum / 100;
	}
	
	public void load(int iteration, GUI l) {
		if(percentage == 0) {
			console.log("\n-----------------Loading----------------");
		}
		while(iteration >= percentage*perc) {
			percentage++;
			l.load(percentage);
//			if(percentage == 100 || iteration == max)
//				System.out.print(percentage+"!");
//			else if(percentage < 10)
//				System.out.print("0"+percentage+"..");
//			else
//				System.out.print(percentage+"..");
//			if(percentage % 10 == 0)
//				console.log("");
		}
	}
}
