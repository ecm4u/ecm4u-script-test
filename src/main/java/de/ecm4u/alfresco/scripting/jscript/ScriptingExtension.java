package de.ecm4u.alfresco.scripting.jscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.mozilla.javascript.Scriptable;
import org.alfresco.repo.jscript.NativeMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;

import de.ecm4u.alfresco.scripting.service.ScriptingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptingExtension extends AbstractScriptService {
	private static final Logger logger = LoggerFactory.getLogger(ScriptingExtension.class);
    private static final String MESSAGE = "message";
    private static final String SUCCESS = "success";
    private static final String DATA = "data";

    @Autowired
    private ScriptingService scriptingService;

    public Object getScriptResult(ScriptNode scriptNode, String scriptLocationObj, final Map<String, Object> arguments) {
        NodeRef ctxNode = scriptNode.getNodeRef();
        Scriptable originalScope = getScope();
        logger.debug("### originalScope: {}", originalScope);
        //Map<String, Object> convertedScriptResult = initScriptingResult();
        Map<Object, Object> convertedScriptResult = new HashMap<>();
        
        try {
            Object scriptResult = scriptingService.getScriptResult(ctxNode, scriptLocationObj, arguments);
            logger.debug("### script response:{} ", scriptResult);
            convertedScriptResult.put(DATA, scriptResult);
            convertedScriptResult.put(SUCCESS, true);
            convertedScriptResult.put(MESSAGE, "OK");
        } catch (Exception ex) {
            convertedScriptResult.put(MESSAGE, ex.getMessage());
            convertedScriptResult.put(SUCCESS, false);
        }
        logger.debug("convertedScriptResult: {}",convertedScriptResult);

        // return convertToScriptable(convertedScriptResult);
        //return convertedScriptResult;
        // return new NativeMap(getScope(), convertedScriptResult);
        return new NativeMap(originalScope, convertedScriptResult);
    }

}
