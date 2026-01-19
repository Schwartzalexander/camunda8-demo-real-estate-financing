package de.aschwartz.camunda8demo.realestatefinancing.camunda.worker;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Helper for reading typed variables from Zeebe job payloads.
 */
public final class VariableMapper {

	private VariableMapper() {
	}

	/**
	 * Reads a {@link BigDecimal} from the variables map.
	 *
	 * @param vars variables map
	 * @param key  variable key
	 * @return BigDecimal value or {@code null}
	 */
	public static BigDecimal getBigDecimal(Map<String, Object> vars, String key) {
		Object v = vars.get(key);
		if (v == null) return null;

		if (v instanceof BigDecimal bd) return bd;

		if (v instanceof Number n) {
			// Zeebe/JSON liefert oft Integer/Long/Double
			return BigDecimal.valueOf(n.doubleValue());
		}

		if (v instanceof String s) {
			s = s.trim();
			if (s.isEmpty()) return null;
			// falls jemand "4.000,50" eingibt
			s = s.replace(".", "").replace(",", ".");
			return new BigDecimal(s);
		}

		throw new IllegalArgumentException("Variable '" + key + "' has unsupported type: " + v.getClass());
	}

	/**
	 * Reads a string variable.
	 *
	 * @param variables variables map
	 * @param key       variable key
	 * @return string value or {@code null}
	 */
	public static String getString(Map<String, Object> variables, String key) {
		Object value = variables.get(key);
		return value != null ? value.toString() : null;
	}
}
