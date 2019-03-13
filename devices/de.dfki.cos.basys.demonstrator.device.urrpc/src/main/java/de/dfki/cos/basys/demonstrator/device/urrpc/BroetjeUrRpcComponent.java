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
import org.apache.xmlrpc.client.util.ClientFactory;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.MoveToLocation;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.communication.Channel;
import de.dfki.cos.basys.platform.model.runtime.communication.Notification;
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
			Object rivetPositions = getRivetPositions(req, "INSERTED");
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
			
			// ##################################################################################
			// FIXME: do we need to wait a short time? 
			// From the second command onwards, the rpc server has a status of finished, 
			// which can be queried in onExecute if the UR responds slower than the internal state change to execute
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// ##################################################################################
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
				
//				if (getUnitConfig().getRecipe() == UrRpcConstants.ROUTINE_PERFORM_RIVETING 
//				    || getUnitConfig().getRecipe() == UrRpcConstants.ROUTINE_PERFORM_SEALING) {
//									
//					List<?> tmp = Arrays.asList((Object[])xmlrpcGetPayload());	
//					List<Map<String,Object>> positions = (List<Map<String,Object>>)tmp; 
//					
//					LOGGER.debug("################### xmlrpcGetPayload result: {}", positions.toString());
//					
//					
//					// if change detected: update WM
//					// TODO
//				}
				break;
			case "finished":
				executing=false;
				// retrieve error code here?
				break;
			default:
				break;
			}

			//check messages here in order to get messages also after finished
			Object[] messages = xmlrpcGetMessages();			
			for (int i = 0; i<messages.length; i++) {
				Map<String, Object> message = (Map<String, Object>)messages[i];
				if (message.get("topic").equals("rivet_state_changed")) {
					//String rivetId = (String) message.get("id");
					int rivetIndex = (int) message.get("rivetIndex");
					int frameIndex = (int) message.get("frameIndex");
					String rivetState = (String) message.get("state");
					
					JsonObject jsonRequest = Json.createObjectBuilder()
							.add("action", "updateRivetPosition")
							//.add("rivetId", rivetId)
							.add("rivetIndex", rivetIndex)
							.add("frameIndex", frameIndex)
							.add("state", rivetState)
							.build();
					
					Notification not = CommFactory.getInstance().createNotification(jsonRequest.toString());
					Channel wmInChannel = CommFactory.getInstance().openChannel(context.getSharedChannelPool(), "world-model#in", false, null);
					wmInChannel.sendNotification(not);
				}
			}
			
//			else {
//				List<Map<String, Object>> messages =  (List<Map<String, Object>>) obj;
//				if (messages != null && messages.size() > 0) {
//					for (Map<String, Object> message : messages) {
//				
//					}
//				}
//			}
			
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
		LOGGER.debug("xmlrpcResetServer");
		Object params[] = {};
		try {
			return client.execute("reset_server", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private Object xmlrpcGetCurrentRoutine() {
		LOGGER.debug("xmlrpcGetCurrentRoutine");
		Object params[] = {};
		try {
			return client.execute("get_routine", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private void xmlrpcSetCurrentRoutine(int code) {
		LOGGER.debug("xmlrpcSetCurrentRoutine");
		Object params[] = { code };
		try {
			String result = 
			client.execute("set_routine", params).toString();
			LOGGER.debug("############################ xmlrpcSetCurrentRoutine result = " + result + "############################");
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}
	
	private void xmlrpcSetPayload(Object payload) {	
		LOGGER.debug("xmlrpcSetPayload");
		Object params[]  = { payload };
		try {
			String result = 
			client.execute("set_rivet_positions", params).toString();
			LOGGER.debug("############################ set_rivet_positions result = " + result + "############################");
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}
	
	private Object xmlrpcGetPayload() {	
		LOGGER.debug("xmlrpcGetPayload");
		Object params[] = { };
		try {
			return client.execute("get_rivet_positions", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	
	private Object[] xmlrpcGetMessages() {	
		LOGGER.debug("xmlrpcGetMessages");
		Object params[] = { true };
		try {
			Object obj = client.execute("get_messages", params);
			//if (obj!=null && obj.getClass().isArray()) {
			return (Object[])obj;
			//}			
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private Object xmlrpcGetCurrentStatus() {
		LOGGER.debug("xmlrpcGetCurrentStatus");
		Object params[] = {};
		try {
			return client.execute("get_status", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	
	private Object xmlrpcGetErrorCode() {
		LOGGER.debug("xmlrpcGetErrorCode");
		Object params[] = {};
		try {
			return client.execute("get_error_code", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	












}
