import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvoNet {
	private int outputs, inputs, layers, gen;
	private int[] hidden, nodes;

	private Matrix[] weights, bias;

	private double lr, maxError, minError, ErrorSum, wholeError;

	public AvoNet(int[] layerconfig, double learning_rate) {
		this.initConfig(layerconfig);
		this.initNodes(layerconfig);
		this.initWeights();
		this.initBias();
		this.initInfo(learning_rate);
	}

	private void initConfig(int[] conf) {
		outputs = conf[conf.length - 1];
		inputs = conf[0];
		layers = conf.length;

		if (conf.length > 2) {
			hidden = new int[conf.length - 2];
			for (int i = 1; i < conf.length - 1; i++)
				hidden[i - 1] = conf[i];
		}
	}

	private void initNodes(int[] conf) {
		nodes = conf.clone();
	}

	private void initWeights() {
		weights = new Matrix[layers - 1];
		Arrays.setAll(weights, i -> new Matrix(nodes[i + 1], nodes[i], 0));
		for(Matrix w : weights) {w.randomize(-1, 1);} 
	}

	private void initBias() {
		bias = new Matrix[layers - 1];
		Arrays.setAll(bias, i -> new Matrix(nodes[i + 1], 1, 0));
		for(Matrix b : bias) {b.randomize(-1, 1);} 
	}

	private void initInfo(double rate) {
		lr = rate;
		gen = 0;
		maxError = 0;
		minError = Double.POSITIVE_INFINITY;
		ErrorSum = 0;
		wholeError = 0;
	}
	
	public void export(String title) {
		try {
			writer export = new writer(title+".txt");
			
			export.write("nodes:"+Arrays.toString(nodes)+";");
			export.write("inputs:"+inputs+";");
			export.write("hidden:"+Arrays.toString(hidden)+";");
			export.write("outputs:"+outputs+";");
			export.write("layers:"+layers+";");
			export.write("gen:"+gen+";");
			export.write("lr:"+lr+";");
			export.write("maxError:"+maxError+";");
			export.write("minError:"+minError+";");
			export.write("ErrorSum:"+ErrorSum+";");
			export.write("wholeError:"+wholeError+";");
			
			export.write("weights:{");
			for(Matrix weight : weights) {
				export.write("(");
				export.write(weight.toString_exp(),true);
				export.write(")");
			}
			export.write("};");
			export.write("bias:{");
			for(Matrix bia : bias) {
				export.write("(");
				export.write(bia.toString_exp(),true);
				export.write(")");
			}
			export.write("}");
			export.done();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static AvoNet load(String data_path) {
		BufferedReader data = null;
		try {
			data = new BufferedReader(new FileReader(data_path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = "";
		Pattern pat = Pattern.compile("(\\w+:)([\\[{])?(.*?)([\\]};])");
		AvoNet result = null;
		
		try {
			while((line = data.readLine()) != null) {
				Matcher match = pat.matcher(line);
				match.find();
				switch(match.group(1)) {
					case "nodes:":
						
					break;
				}
			}
		} catch (IOException e) {
			return null;
		}
		return result;
	}

	public double[] guess(double[] input) {
		if (input.length != inputs) {
			console.log("");
			return null;
		}
		Matrix value = Matrix.fromArray(input);
		for (int i = 0; i < weights.length; i++) {
			value = weights[i].mult(value);
			value.add(bias[i]);
			Activation.sigmoid(value);

		}
		return value.toArray_flat();
	}

	public double error(double[] input, double[] real) {
		Matrix output = Matrix.fromArray(guess(input));

		Matrix target = Matrix.fromArray(real);

		Matrix err = Matrix.sub(target, output);

		err.skalar_mult(err);

		double[] conv_err = err.toArray_flat();

		double reduced = Arrays.stream(conv_err).reduce((x, y) -> x + y).getAsDouble(); // what is this OptionalDouble
																						// shit?!

		minError = (reduced < minError) ? reduced : minError;
		maxError = (reduced > maxError) ? reduced : maxError;
		ErrorSum += reduced;
		wholeError = ErrorSum / gen;

		return reduced;
	}

	public void train(double[] input, double[] real) {
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

		for (int i = 0; i < weights.length; i++) {
			layer = weights[i].mult(layer);
			layer.add(bias[i]);
			Activation.sigmoid(layer);
			values[i + 1] = layer.copy();
		}

		Matrix final_output = values[values.length - 1];

		Matrix final_error = Matrix.sub(target, final_output);

		Matrix err = null;

		for (int i = weights.length - 1; i >= 0; i--) {

			if (i == weights.length - 1) {
				err = final_error;
			} else {
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

	public AvoNet copy() {
		AvoNet result = new AvoNet(nodes, lr);
		result.weights = weights.clone();
		result.bias = bias.clone();
		return result;
	}

	public AvoNet full_copy() {
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

	public double Benchmark(String type, int iterations) throws IOException {
		switch (type) {
		case "guess":
			return Benchmark_guess(iterations);
		case "train":
			return Benchmark_train(iterations);
		case "full":
			return Benchmark_full(iterations);
		default:
			console.log("The available benchmark types are [guess,train,full].");
			return 0;
		}
	}

	private double Benchmark_guess(int iterations) throws IOException {
		writer log = new writer("Benchmark_guess_"+iterations+"_iterations.txt");
		
		console.log("Preparing data...");
		log.write("Preparing data...",true);
		
		double prepare = System.nanoTime();

		double[] input = new double[inputs];
		for (int i = 0; i < inputs; i++) {
			input[i] = Math.random();
		}
		console.log("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.");
		log.write("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.",true);
		log.newLine();

		double general_time = System.nanoTime(); // Time starts after input generation!
		double average_guess = 0;

		for (int i = 0; i < iterations; i++) {
			double time = System.nanoTime();
			guess(input);
			average_guess += System.nanoTime() - time;
		}

		average_guess /= iterations;

		console.log("\nAverage value for 1 guess.");
		log.write("\nAverage value for 1 guess.",true);
		AvoNet.logTime(average_guess);
		AvoNet.logTime(average_guess, log);

		console.log("\nValues for " + iterations + " iteration/s.");
		log.write("\nValues for " + iterations + " iteration/s.",true);
		
		AvoNet.elapsed(general_time);
		AvoNet.elapsed(general_time, log);
		log.done();

		return average_guess;
	}

	private double Benchmark_train(int iterations) throws IOException {
		writer log = new writer("Benchmark_train_"+iterations+"_iterations.txt");
		
		console.log("Preparing data...");
		log.write("Preparing data...", true);
		double prepare = System.nanoTime();

		double[] input = new double[inputs];
		for (int i = 0; i < inputs; i++) {
			input[i] = Math.random();
		}

		double[] real = new double[outputs];
		for (int i = 0; i < outputs; i++) {
			real[i] = Math.random();
		}
		console.log("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.");
		log.write("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.", true);

		console.log("Copying Network...");
		log.write("Copying Network...", true);
		double copying = System.nanoTime();
		AvoNet Network_copy = copy();
		console.log("Done! Time elapsed: " + ((System.nanoTime() - copying) / 1E9) + " Seconds.");
		log.write("Done! Time elapsed: " + ((System.nanoTime() - copying) / 1E9) + " Seconds.", true);
		log.newLine();

		double general_time = System.nanoTime(); // Time starts after input generation!
		double average_train = 0;

		for (int i = 0; i < iterations; i++) {
			double time = System.nanoTime();
			Network_copy.train(input, real);
			average_train += System.nanoTime() - time;
		}

		average_train /= iterations;

		console.log("\nAverage value for 1 back-propagation.");
		log.write("\nAverage value for 1 back-propagation.", true);
		AvoNet.logTime(average_train);
		AvoNet.logTime(average_train, log);

		console.log("\nValues for " + iterations + " iteration/s.");
		log.write("\nValues for " + iterations + " iteration/s.");
		AvoNet.elapsed(general_time);
		AvoNet.elapsed(general_time, log);
		
		log.done();
		return average_train;
	}

	private double Benchmark_full(int iterations) throws IOException {
		writer log = new writer("Benchmark_full_"+iterations+"_iterations.txt");
		
		console.log("Preparing data...");
		log.write("Preparing data...", true);
		double prepare = System.nanoTime();

		double[] input = new double[inputs];
		for (int i = 0; i < inputs; i++) {
			input[i] = Math.random();
		}

		double[] real = new double[outputs];
		for (int i = 0; i < outputs; i++) {
			real[i] = Math.random();
		}
		console.log("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.");
		log.write("Done! Time elapsed: " + ((System.nanoTime() - prepare) / 1E9) + " Seconds.", true);

		console.log("Copying Network...");
		log.write("Copying Network...", true);
		double copying = System.nanoTime();
		AvoNet Network_copy = copy();
		console.log("Done! Time elapsed: " + ((System.nanoTime() - copying) / 1E9) + " Seconds.");
		log.write("Done! Time elapsed: " + ((System.nanoTime() - copying) / 1E9) + " Seconds.", true);
		log.newLine();

		double general_time = System.nanoTime(); // Time starts after input generation!
		double average_train = 0;
		double average_guess = 0;
		double average = 0;

		for (int i = 0; i < iterations; i++) {
			double time1 = System.nanoTime();
			Network_copy.guess(input);
			average_guess += System.nanoTime() - time1;

			double time2 = System.nanoTime();
			Network_copy.train(input, real);
			average_train += System.nanoTime() - time2;

			average += System.nanoTime() - time1;
		}

		average_train /= iterations;
		average_guess /= iterations;
		average /= iterations;

		console.log("\nAverage value for 1 guess.");
		log.write("\nAverage value for 1 guess.", true);
		AvoNet.logTime(average_guess);
		AvoNet.logTime(average_guess, log);

		console.log("\nAverage value for 1 back-propagation.");
		log.write("\nAverage value for 1 back-propagation.", true);
		AvoNet.logTime(average_train);
		AvoNet.logTime(average_train, log);

		console.log("\nValues for " + iterations + " iteration/s.");
		log.write("\nValues for " + iterations + " iteration/s.", true);
		AvoNet.elapsed(general_time);
		AvoNet.elapsed(general_time, log);

		log.done();
		return average;
	}
	
	public double getTime() {
		return System.nanoTime();
	}

	public double estimate(String type, int iterations, int batchsize) throws IOException {
		switch (type) {
		case "guess":
			return estimate_guess(iterations, batchsize);
		case "train":
			return estimate_train(iterations, batchsize);
		case "full":
			return estimate_full(iterations, batchsize);
		default:
			console.log("The available estimation types are [guess,train,full].");
			return 0;
		}
	}

	private double estimate_guess(int iterations, int batchsize) throws IOException {
		writer log = new writer("Estimation[guess] for "+iterations+" iterations.txt");
		
		double time = Benchmark("guess", 10) * iterations * batchsize;
		AvoNet.estimation_log(time, iterations);
		AvoNet.estimation_log(time, iterations, log);
		
		log.done();
		return time;
	}

	private double estimate_train(int iterations, int batchsize) throws IOException {
		writer log = new writer("Estimation[train] for "+iterations+" iterations.txt");
		
		double time = Benchmark("train", 10) * iterations * batchsize;
		AvoNet.estimation_log(time, iterations);
		AvoNet.estimation_log(time, iterations, log);
		
		log.done();
		return time;
	}

	private double estimate_full(int iterations, int batchsize) throws IOException {
		writer log = new writer("Estimation[full] for "+iterations+" iterations.txt");
		
		double time = Benchmark("full", 10) * iterations * batchsize;
		AvoNet.estimation_log(time, iterations);
		AvoNet.estimation_log(time, iterations, log);
		
		log.done();
		return time;
	}

	private static void estimation_log(double time, int iterations) {
		console.log("\nEstimated Time " + iterations + " iteration/s will take:");
		console.line(1);
		console.log(time / 1E6 + " Milliseconds.");
		console.log(time / 1E9 + " Seconds.");
		console.log(time / 1E9 / 60 + " Minutes.");
		console.log(time / 1E9 / 3600 + " Hours.");
		console.line(1);
	}
	
	private static void estimation_log(double time, int iterations, writer w) throws IOException {
		w.write("\nEstimated Time " + iterations + " iteration/s will take:", true);
		w.line();
		w.write(time / 1E6 + " Milliseconds.", true);
		w.write(time / 1E9 + " Seconds.", true);
		w.write(time / 1E9 / 60 + " Minutes.", true);
		w.write(time / 1E9 / 3600 + " Hours.", true);
		w.line();
	}

	public static void elapsed(double time) {
		console.log("Time elapsed:");
		console.line(1);
		console.log((System.nanoTime() - time) / 1E6 + " Milliseconds.");
		console.log((System.nanoTime() - time) / 1E9 + " Seconds.");
		console.log((System.nanoTime() - time) / 1E9 / 60 + " Minutes.");
		console.log((System.nanoTime() - time) / 1E9 / 3600 + " Hours.");
		console.line(1);
	}
	
	public static void elapsed(double time, writer w) throws IOException {
		w.write("Time elapsed:", true);
		w.line();
		w.write((System.nanoTime() - time) / 1E6 + " Milliseconds.", true);
		w.write((System.nanoTime() - time) / 1E9 + " Seconds.", true);
		w.write((System.nanoTime() - time) / 1E9 / 60 + " Minutes.", true);
		w.write((System.nanoTime() - time) / 1E9 / 3600 + " Hours.", true);
		w.line();
	}

	public static void logTime(double timeDiff) {
		console.log("Time elapsed:");
		console.line(1);
		console.log(timeDiff / 1E6 + " Milliseconds.");
		console.log(timeDiff / 1E9 + " Seconds.");
		console.log(timeDiff / 1E9 / 60 + " Minutes.");
		console.log(timeDiff / 1E9 / 3600 + " Hours.");
		console.line(1);
	}
	
	public static void logTime(double timeDiff, writer w) throws IOException {
		w.write("Time elapsed:", true);
		w.line();
		w.write(timeDiff / 1E6 + " Milliseconds.", true);
		w.write(timeDiff / 1E9 + " Seconds.", true);
		w.write(timeDiff / 1E9 / 60 + " Minutes.", true);
		w.write(timeDiff / 1E9 / 3600 + " Hours.", true);
		w.line();
	}

	public int getOutputs() {
		return outputs;
	}

	public int getInputs() {
		return inputs;
	}

	public int[] getHidden() {
		return hidden;
	}

	public double getMinError() {
		return minError;
	}

	public double getMaxError() {
		return maxError;
	}

	public double getWholeError() {
		return wholeError;
	}

	public double getErrorSum() {
		return ErrorSum;
	}

	public double getRate() {
		return lr;
	}

	public int getGen() {
		return gen;
	}

	public Matrix getWeight(int index) {
		return weights[index];
	}

	public Matrix getBias(int index) {
		return bias[index];
	}

	public void setRate(double learning_rate) {
		lr = learning_rate;
	}

}
