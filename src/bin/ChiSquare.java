package bin;

import java.util.List;

import org.rosuda.JRI.Rengine;

public class ChiSquare {
	private static int[] convertListToArray(List<Integer> list) {
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static double computeChiSquareValue(List<Integer> list) {
		Rengine engine = new Rengine(new String[] { "--no-save" }, false, null);
		engine.assign("data", convertListToArray(list));
		engine.eval(" value = (chisq.test(data))$p.value");
		double mean = engine.eval("value").asDouble();
		return mean;
	}
}