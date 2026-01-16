package de.aschwartz.camunda7demo.realestatefinancing.camunda.worker;

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
	 * @param variables variables map
	 * @param key variable key
	 * @return BigDecimal value or {@code null}
	 */
	public static BigDecimal getBigDecimal(Map<String, Object> variables, String key) {
		Object value = variables.get(key);
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal bigDecimal) {
			return bigDecimal;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		return new BigDecimal(value.toString());
	}

	/**
	 * Reads a string variable.
	 *
	 * @param variables variables map
	 * @param key variable key
	 * @return string value or {@code null}
	 */
	public static String getString(Map<String, Object> variables, String key) {
		Object value = variables.get(key);
		return value != null ? value.toString() : null;
	}
}
