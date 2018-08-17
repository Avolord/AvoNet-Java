import java.util.Arrays;

public class AvoNet {
	int 	outputs;
	int 	inputs;
	int[] 	hidden;
	int 	layers;
	int[]	nodes;
	
	Matrix[] weights;
	Matrix[] bias;
	
	double 	lr;
	int 	gen;
	double 	maxError;
	double	minError;
	double	ErrorSum;
	double	wholeError;
	
	AvoNet(int[] layerconfig, double learning_rate) {
		this.initConfig(layerconfig);
		this.initNodes(layerconfig);
		this.initWeights();
		this.initBias();
		this.initInfo(learning_rate);
	}
	
	void initConfig(int[] conf) {
		outputs = conf[conf.length - 1];
		inputs = conf[0];
		layers = conf.length;
		
		if(conf.length > 2) {
			hidden = new int[conf.length - 2];
			for(int i=1; i<conf.length - 1; i++)
				hidden[i - 1] = conf[i];
		}
	}
	
	void initNodes(int[] conf) {
		nodes = conf.clone();
	}
	
	void initWeights() {
		weights = new Matrix[layers - 1];
		for(int i=0; i<weights.length; i++) {
			weights[i] = new Matrix(nodes[i + 1], nodes[i], 0);
			weights[i].randomize(-1, 1);
		}
	}
	
	void initBias() {
		bias = new Matrix[layers - 1];
		for(int i=0; i<bias.length; i++) {
			bias[i] = new Matrix(nodes[i + 1], 1, 0);
			bias[i].randomize(-1, 1);
		}
	}
	
	void initInfo(double rate) {
		lr = rate;
		gen = 0;
		maxError = 0;
		minError = Double.POSITIVE_INFINITY;
		ErrorSum = 0;
		wholeError = 0;
	}
	
	double[] guess(double[] input) {
		if(input.length != inputs) {
			console.log("Wrong Dimension of input array!");
			return null;
		}
		Matrix value = Matrix.fromArray(input);
		for(int i=0; i<weights.length; i++) {
			value = weights[i].mult(value);
			value.add(bias[i]);
			Activation.sigmoid(value);
			
		}
		return value.toArray_flat();
	}
	
	double error(double[] input, double[] real) {
		Matrix output = Matrix.fromArray(guess(input));
		
		Matrix target = Matrix.fromArray(real);
		
		Matrix err = Matrix.sub(target, output);
		
		err.skalar_mult(err);
		
		double[] conv_err = err.toArray_flat();
		
		double reduced = Arrays.stream(conv_err).reduce((x,y) -> x+y).getAsDouble(); //what is this OptionalDouble shit?!	
		
		minError = (reduced < minError) ? reduced : minError;
		maxError = (reduced > maxError) ? reduced : maxError;
		ErrorSum += reduced;
		wholeError = ErrorSum / gen;
				
		return reduced;
	}
	
	void train(double[] input, double[] real) {
		gen++;
		
		double reduced = error(input, real);
		
		minError = (reduced < minError) ? reduced : minError;
		maxError = (reduced > maxError) ? reduced : maxError;
		ErrorSum += reduced;
		wholeError = ErrorSum / gen;
		
		Matrix target = Matrix.fromArray(real);
		
		Matrix layer = Matrix.fromArray(input);
		
		Matrix[] values = new Matrix[weights.length + 1];
		
		values[0] = layer.copy();
		
		for(int i=0; i<weights.length; i++) {
			layer = weights[i].mult(layer);
			layer.add(bias[i]);
			Activation.sigmoid(layer);
			values[i + 1] = layer.copy();
		}
		
		Matrix final_output = values[values.length - 1];
		
		Matrix final_error = Matrix.sub(target, final_output);
		
		Matrix err = null;
		
		for(int i=weights.length - 1; i >= 0; i--) {
			
			if(i == weights.length - 1) {
				err = final_error;
			} 
			else {
				err = weights[i + 1].transpose().mult(err);
			}
			
			Matrix val_a = values[i + 1].copy();
			Matrix val_b = values[i].transpose();
			
			Matrix gradient;
			
			gradient = Matrix.add(Matrix.invert(val_a), 1);
			
			gradient = Matrix.skalar_mult(val_a, gradient);
			
			gradient = Matrix.skalar_mult(err, gradient);
			
			Matrix delta_W;
			
			delta_W = gradient.mult(val_b);
			
			delta_W.skalar_mult(lr);
			
			weights[i].add(delta_W);
			
			Matrix delta_B = Matrix.skalar_mult(gradient, lr);
			
			bias[i].add(delta_B);			
		}
		
	}
	
	AvoNet copy() {
		AvoNet result = new AvoNet(nodes, lr);
		result.weights = weights.clone();
		result.bias = bias.clone();
		return result;
	}
	
	AvoNet full_copy() {
		AvoNet result = new AvoNet(nodes, lr);
		result.weights = weights.clone();
		result.bias = bias.clone();
		result.gen = gen;
		result.maxError = maxError;
		result.minError = minError;
		result.ErrorSum = ErrorSum;
		result.wholeError = wholeError;
		
		return result;
	}
	
	void Benchmark(String type, int iterations) {
		switch(type) {
		case "guess":
			Benchmark_guess(iterations);
		break;
		case "train":
			Benchmark_train(iterations);
		break;
		case "full":
			Benchmark_full(iterations);
		break;
		default:
			console.log("The available benchmark types are [guess,train,full].");
		}
	}
	
	void Benchmark_guess(int iterations) {
		console.log("Preparing data...");
		double prepare = System.nanoTime();
		
		double[] input = new double[inputs];
		for(int i=0; i<inputs; i++) {
			input[i] = Math.random();
		}
		console.log("Done! Time elapsed: "+((System.nanoTime() - prepare) / 1E9)+" Seconds.");
		
		double general_time = System.nanoTime(); //Time starts after input generation!
		double average_guess = 0;
		
		for(int i=0; i<iterations; i++) {
			double time = System.nanoTime();
			guess(input);
			average_guess += System.nanoTime() - time;
		}
		average_guess /= iterations;
		console.log("\nAverage value for 1 guess.");
		AvoNet.logTime(average_guess);
		
		console.log("\nValues for "+iterations+" iterations.");
		AvoNet.elapsed(general_time);
	}
	
	void Benchmark_train(int iterations) {
		console.log("Preparing data...");
		double prepare = System.nanoTime();
		
		double[] input = new double[inputs];
		for(int i=0; i<inputs; i++) {
			input[i] = Math.random();
		}
		
		double[] real = new double[outputs];
		for(int i=0; i<outputs; i++) {
			real[i] = Math.random();
		}
		console.log("Done! Time elapsed: "+((System.nanoTime() - prepare) / 1E9)+" Seconds.");
		
		console.log("Copying Network...");
		double copying = System.nanoTime();
		AvoNet Network_copy = copy();
		console.log("Done! Time elapsed: "+((System.nanoTime() - copying) / 1E9)+" Seconds.");
		
		double general_time = System.nanoTime(); //Time starts after input generation!
		double average_train = 0;
		
		for(int i=0; i<iterations; i++) {
			double time = System.nanoTime();
			Network_copy.train(input, real);
			average_train += System.nanoTime() - time;
		}
		average_train /= iterations;
		console.log("\nAverage value for 1 back-propagation.");
		AvoNet.logTime(average_train);
		
		console.log("\nValues for "+iterations+" iterations.");
		AvoNet.elapsed(general_time);
	}
	
	void Benchmark_full(int iterations) {
		console.log("Preparing data...");
		double prepare = System.nanoTime();
		
		double[] input = new double[inputs];
		for(int i=0; i<inputs; i++) {
			input[i] = Math.random();
		}
		
		double[] real = new double[outputs];
		for(int i=0; i<outputs; i++) {
			real[i] = Math.random();
		}
		console.log("Done! Time elapsed: "+((System.nanoTime() - prepare) / 1E9)+" Seconds.");
		
		console.log("Copying Network...");
		double copying = System.nanoTime();
		AvoNet Network_copy = copy();
		console.log("Done! Time elapsed: "+((System.nanoTime() - copying) / 1E9)+" Seconds.");
		
		double general_time = System.nanoTime(); //Time starts after input generation!
		double average_train = 0;
		double average_guess = 0;
		
		for(int i=0; i<iterations; i++) {
			double time = System.nanoTime();
			Network_copy.guess(input);
			average_guess += System.nanoTime() - time;
			Network_copy.train(input, real);
			average_train += System.nanoTime() - time;
		}
		
		average_train /= iterations;
		average_guess /= iterations;
		
		console.log("\nAverage value for 1 guess.");
		AvoNet.logTime(average_guess);
		
		console.log("\nAverage value for 1 back-propagation.");
		AvoNet.logTime(average_train);
		
		console.log("\nValues for "+iterations+" iterations.");
		AvoNet.elapsed(general_time);
	}
	
	public static void elapsed(double time) {
		console.log("Time elapsed:");
		console.line(1);
		console.log((System.nanoTime() - time) / 1E6+" Milliseconds.");
		console.log((System.nanoTime() - time) / 1E9+" Seconds.");
		console.log((System.nanoTime() - time) / 1E9 / 60+" Minutes.");
		console.log((System.nanoTime() - time) / 1E9 / 3600+" Hours.");
		console.line(1);
	}
	
	public static void logTime(double timeDiff) {
		console.log("Time elapsed:");
		console.line(1);
		console.log(timeDiff / 1E6+" Milliseconds.");
		console.log(timeDiff / 1E9+" Seconds.");
		console.log(timeDiff / 1E9 / 60+" Minutes.");
		console.log(timeDiff / 1E9 / 3600+" Hours.");
		console.line(1);
	}
	
}
