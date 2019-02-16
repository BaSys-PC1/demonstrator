package de.dfki.cos.basys.demonstrator.device.urrpc;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.xmlrpc.XmlRpcException;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.MoveToLocation;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.communication.Channel;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.communication.Response;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.Variable;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.xmlrpc.XmlRpcDeviceComponent;

public class BroetjeUrRpcComponent extends XmlRpcDeviceComponent {	
	
	public BroetjeUrRpcComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
	}
		
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		UnitConfiguration config = new UnitConfiguration();
		config.setRecipe(-1);
		
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getMoveToLocation())) {
			MoveToLocation move = (MoveToLocation) c.getCapability();
			switch (move.getName()) {
			case "HOME_POSE":
				config.setRecipe(UrRpcConstants.ROUTINE_HOME_POSE);		
				break;
			case "SAFE_HOME_POSE":
				config.setRecipe(UrRpcConstants.ROUTINE_SAFE_HOME_POSE);	
				break;
			case "SAFE_POSE":
				config.setRecipe(UrRpcConstants.ROUTINE_SAFE_POSE);	
				break;
			}
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getInteractionCapability())) {			
			config.setRecipe(UrRpcConstants.ROUTINE_WAIT_FOR_WORKER);				
		}
				
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getGuiding())) {			
			config.setRecipe(UrRpcConstants.ROUTINE_PERFORM_RACEWAY_POSITIONING);				
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getFügen())) {			
			config.setRecipe(UrRpcConstants.ROUTINE_PERFORM_RIVETING);
			Object rivetPositions = getRivetPositions(req, "EMPTY");
			config.setPayload(rivetPositions);
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getBeschichten())) {			
			config.setRecipe(UrRpcConstants.ROUTINE_PERFORM_SEALING);
			Object rivetPositions = getRivetPositions(req, "CHECKED_IO");
			config.setPayload(rivetPositions);
		}
		
		return config;
		
	}
	
	@Override
	public void onResetting() {
		xmlrpcResetServer();
	}
	
	@Override
	public void onStarting() {		
		if (getUnitConfig().getRecipe() >= 0) {
			
			if (getUnitConfig().getPayload() != null) {
				xmlrpcSetPayload(getUnitConfig().getPayload());
			}
						
			xmlrpcSetCurrentRoutine(getUnitConfig().getRecipe());
			// FIXME: do we need to wait a short time? 
			// From the second command onwards, the rpc server has a status of finished, 
			// which can be queried in onExecute if the UR responds slower than the internal state change to execute
		}
	}
	
	@Override
	public void onExecute() {
		boolean executing = true;
		while(executing) {
			String state = xmlrpcGetCurrentStatus().toString();
			switch (state) {
			case "busy":
				// wait for completion
				
				if (getUnitConfig().getRecipe() == UrRpcConstants.ROUTINE_PERFORM_RIVETING 
				    || getUnitConfig().getRecipe() == UrRpcConstants.ROUTINE_PERFORM_SEALING) {
				
					List<Map<String,Object>> positions = (List<Map<String,Object>>)xmlrpcGetPayload();
					// if change detected: update WM
				}
				break;
			case "finished":
				executing=false;
				// retrieve error code here?
				break;
			default:
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private List<Map<String,Object>> getRivetPositions(CapabilityRequest req, String state) {

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		
		int count = 0;
		String sector = null;
		
		Variable[] vars = req.getInputParameters().toArray(new Variable[0]);
		
		for (Variable v : vars) {
			if ("count".equalsIgnoreCase(v.getName())) {
				count = Integer.parseInt(v.getValue());
			}
			else if ("sector".equalsIgnoreCase(v.getName())) {
				sector = v.getValue();
			}
		}
		
		JsonObject jsonRequest = Json.createObjectBuilder()
				.add("action", "getRivetPositions")
				.add("count", count)
				.add("sector", sector)
				.add("state", state)
				.build();
	
		Request wmReq = CommFactory.getInstance().createRequest(jsonRequest.toString());						
		Channel wmInChannel = CommFactory.getInstance().openChannel(context.getSharedChannelPool(), "world-model#in", false, null);
		Response wmResp = wmInChannel.sendRequest(wmReq);
		
		JsonReader jsonReader = Json.createReader(new StringReader(wmResp.getPayload()));
		JsonArray jsonArray = jsonReader.readArray();
		List<JsonObject> rivetPositions = jsonArray.getValuesAs(JsonObject.class);
					
		for (JsonObject rivetPosition : rivetPositions) {
			Map<String, Object> p = new HashMap<>();
			//p.put("id", rivetPosition.getString("id"));
			p.put("frameIndex", rivetPosition.getInt("frameIndex"));
			p.put("rivetIndex", rivetPosition.getInt("index"));
			p.put("state", rivetPosition.getString("state"));
			result.add(p);
		}		
				
		return result;
	}
	

	/*
	 * private XML-RPC functions 
	 */
	
	private Object xmlrpcResetServer() {
		Object params[] = {};
		try {
			return client.execute("reset_server", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private Object xmlrpcGetCurrentRoutine() {
		Object params[] = {};
		try {
			return client.execute("get_routine", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private void xmlrpcSetCurrentRoutine(int code) {
		Object params[] = { code };
		try {
			client.execute("set_routine", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}

	
	private void xmlrpcSetPayload(Object payload) {	
		Object params[]  = { payload };
		try {
			client.execute("set_rivet_positions", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}
	
	private Object xmlrpcGetPayload() {	
		Object params[] = { };
		try {
			return client.execute("get_rivet_positions", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	

	private Object xmlrpcGetCurrentStatus() {
		Object params[] = {};
		try {
			return client.execute("get_status", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	
	private Object xmlrpcGetErrorCode() {
		Object params[] = {};
		try {
			return client.execute("get_error_code", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	
}
