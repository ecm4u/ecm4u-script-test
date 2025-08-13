package de.ecm4u.alfresco.scripting.service;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ScriptingService {

    public Object getScriptResult(NodeRef ctxNode, String scriptLocationObj, Map<String, Object> arguments);
    
}
