package de.ecm4u.alfresco.scripting.jscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.NativeMap;
import org.mozilla.javascript.Scriptable;

/**
 * Abstract super class for all Scripting API classes.
 * 
 */

 public abstract class AbstractScriptService extends BaseScopableProcessorExtension {
	protected static final String SUCCESS = "success";

	/**
	 * Initialize a scripting result. By default the operation failed and
	 * {@code SUCCESS} is {@code false}.
	 * 
	 * @return the scripting result with the members {@code SUCCESS} (a boolean),
	 *         {@code MESSAGE} (a String) and {@code DATA} (an Object).
	 */
	protected Map<String, Object> initScriptingResult() {
		Map<String, Object> scriptingResult = new HashMap<>();
		scriptingResult.put(SUCCESS, false);
		return scriptingResult;
	}

	/**
	 * Convert a scripting result to a {@link Scriptable} to be used in JavaScript.
	 * 
	 * @param scriptingResult the scripting result
	 * @return the scripting result a
	 */
	protected Scriptable convertToScriptable(final Map<String, Object> scriptingResult) {
		Map<Object, Object> convertedResult = new HashMap<>();
		for (Map.Entry<String, Object> entry : scriptingResult.entrySet()) {
			if (entry.getValue() instanceof Map) {
				convertedResult.put(entry.getKey(), entry.getValue());
			} else if (entry.getValue() instanceof Object[]) {
				List<Object> l = new ArrayList<>();
				for (Object o : (Object[]) entry.getValue()) {
					l.add(o);
				}
				convertedResult.put(entry.getKey(), l);
			} else {
				convertedResult.put(entry.getKey(), entry.getValue());
			}
		}
		return new NativeMap(getScope(), convertedResult);
	}

}
