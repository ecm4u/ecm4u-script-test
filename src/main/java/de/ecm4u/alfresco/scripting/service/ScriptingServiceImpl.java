package de.ecm4u.alfresco.scripting.service;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;
import org.alfresco.repo.security.authentication.AuthenticationUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.search.ResultSet;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ScriptingServiceImpl implements ScriptingService, InitializingBean {
    
	private static final Logger logger = LoggerFactory.getLogger(ScriptingServiceImpl.class);

	@Autowired
	private ServiceRegistry serviceRegistry;

	@Autowired
	private ScriptService scriptService;

	private NodeService nodeService;

    @Override
	public void afterPropertiesSet() throws Exception {
		nodeService = serviceRegistry.getNodeService();
	}

    public Object getScriptResult(NodeRef ctxNode, String scriptLocationObj, final Map<String, Object> arguments){
        if (StringUtils.isNotBlank(scriptLocationObj)) {
            HashMap<String, Object> model = new HashMap<>();
            model.put("document", new ScriptNode(ctxNode, serviceRegistry));
			model.putAll(arguments);

			if (scriptLocationObj.startsWith("workspace:")) {
				NodeRef scriptNodeRef = new NodeRef(scriptLocationObj);
				return executeScript(scriptNodeRef,  null,model);
			} else if (scriptLocationObj.startsWith("classpath:")) {
				String path = scriptLocationObj;
				path = path.substring(path.indexOf(":") + 1);
				return executeScript(null,  path,model);
			} else {
				NodeRef nodeRef = getNodeRef(scriptLocationObj);
				if (!Objects.isNull(nodeRef)) {
					return executeScript(nodeRef,  null,model);
				}
			}
        }
		return null;
    }

    @SuppressWarnings("unchecked")
	private Object executeScript(NodeRef nodeRef, String classPath, HashMap<String, Object> model) {
	        String runAsUser = AuthenticationUtil.getRunAsUser();
	        logger.debug("Executing script as user: {}, script: {}, nodeRef: {}", runAsUser, classPath, nodeRef);
	        Object result = runAs(() -> {
	            Map<String, Object> defaultModel = new HashMap<>();
	            defaultModel.put("model", "");
	            defaultModel.putAll(model);
	            return nodeRef != null 
	                ? scriptService.executeScript(nodeRef, ContentModel.PROP_CONTENT, defaultModel)
	                : scriptService.executeScript(classPath, defaultModel);
	        }, runAsUser);
	        if(result==null) {
	        	throw new AlfrescoRuntimeException("No script result found");
	        }
			if (result instanceof String || result instanceof Number || result instanceof Boolean
					|| result instanceof NodeRef || result instanceof Date) {
				return (Serializable) result;
			} else if(result instanceof Map) {
	        	ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.convertValue(result, Map.class);
	        } else {
				throw new IllegalArgumentException("value mapping not supported: value=" + result
						+ ", class=" + result.getClass());
			}
			// return Collections.emptyMap();

	    }

    private NodeRef getNodeRef(String scriptLocationObj) {
	    // TODO: use SearchService.selectNodes() to avoid solr dependency    
		String query = "PATH:\"/" + scriptLocationObj + "\"";
		logger.debug("Query to find script: {}", query);
		ResultSet rs = serviceRegistry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
		return rs != null && rs.length() > 0 ? rs.getNodeRefs().stream().findFirst().orElse(null) : null;
	}

}