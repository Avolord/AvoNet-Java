
public class Activation {
	public static Matrix sigmoid(Matrix M) {
		for (int i = 0; i < M.rows; i++) {
			for (int j = 0; j < M.cols; j++) {
				M.data[i][j] = Activation.sigmoid(M.data[i][j]);
			}
		}
		return M;
	}

	public static double sigmoid(double x) {
		return 1 / (1 + Math.pow(Math.E, -x));
	}

	public static double dsigmoid(double x) {
		return Activation.sigmoid(x) * (1 - Activation.sigmoid(x));
	}
}
