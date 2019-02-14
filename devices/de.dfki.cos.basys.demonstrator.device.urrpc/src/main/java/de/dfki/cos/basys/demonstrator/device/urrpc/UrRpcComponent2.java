package de.dfki.cos.basys.demonstrator.device.urrpc;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import de.dfki.cos.basys.common.wmrestclient.dto.RivetPosition;
import de.dfki.cos.basys.common.wmrestclient.dto.RivetPosition.State;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.xmlrpc.XmlRpcDeviceComponent;

public class UrRpcComponent2 extends XmlRpcDeviceComponent {	

	public static final int RECIPE_RIVETING = 4;
	public static final int RECIPE_SEALING = 5;
	
	public UrRpcComponent2(ComponentConfiguration config) {
		super(config);
		//resetOnComplete = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
//		LOGGER.info("CurrentRoutine: " + getCurrentRoutine().toString());
//		LOGGER.info("CurrentStatus : " + getCurrentStatus().toString());
//		setCurrentRoutine(1);
//		LOGGER.info("CurrentRoutine: " + getCurrentRoutine().toString());
//		LOGGER.info("CurrentStatus : " + getCurrentStatus().toString());
//		setCurrentRoutine(2);
//		LOGGER.info("CurrentRoutine: " + getCurrentRoutine().toString());
//		LOGGER.info("CurrentStatus : " + getCurrentStatus().toString());
//		setCurrentRoutine(3);
//		LOGGER.info("CurrentRoutine: " + getCurrentRoutine().toString());
//		LOGGER.info("CurrentStatus : " + getCurrentStatus().toString());
//		setCurrentRoutine(4);
//		LOGGER.info("CurrentRoutine: " + getCurrentRoutine().toString());
//		LOGGER.info("CurrentStatus : " + getCurrentStatus().toString());
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		UnitConfiguration config = new UnitConfiguration();
		
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		
		
		/*
		 * 1: Raceway
		 * 2: Wait
		 * 3: Home Position
		 */
		int recipe = 0;
		
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getGuiding())) {			
			recipe = 1;				
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getInteractionCapability())) {			
			recipe = 2;				
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getMoveToLocation())) {			
			recipe = 3;					
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getBeschichten())) {			
			recipe = RECIPE_SEALING;		
			List<Map<String,Object>> params = new LinkedList<Map<String,Object>>();
			
			//TODO: take params from request
			String sector = "LEFT";
			int count = 5;
		
			List<RivetPosition> rivetPositions = getRivetPositions(sector, count, State.CHECKED_IO);
			for (RivetPosition rivetPosition : rivetPositions) {
				Map<String, Object> p = new HashMap<>();
				p.put("frameIndex", rivetPosition.getFrameIndex());
				p.put("rivetIndex", rivetPosition.getIndex());
				p.put("state", rivetPosition.getState());
				params.add(p);
			}
			
			config.setPayload(params);
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getAnEinpressen())) {			
			recipe = RECIPE_RIVETING;				
			List<Map<String,Object>> params = new LinkedList<Map<String,Object>>();
			
			//TODO: take params from request
			String sector = "LEFT";
			int count = 5;
			
			List<RivetPosition> rivetPositions = getRivetPositions(sector, count, State.EMPTY);
			for (RivetPosition rivetPosition : rivetPositions) {
				Map<String, Object> p = new HashMap<>();
				p.put("frameIndex", rivetPosition.getFrameIndex());
				p.put("rivetIndex", rivetPosition.getIndex());
				p.put("state", rivetPosition.getState());
				params.add(p);
			}
			
			config.setPayload(params);
		}
		
		config.setRecipe(recipe);
		return config;
		
	}
	
	@Override
	public void onResetting() {

	}
	
	@Override
	public void onStarting() {		
		if (getUnitConfig().getRecipe() > 0) {
			
			if (getUnitConfig().getPayload() != null) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> rivetPositions = (List<Map<String,Object>>) getUnitConfig().getPayload();
				setRivetPositions(rivetPositions);
			}
						
			setCurrentRoutine(getUnitConfig().getRecipe());
			// FIXME: do we need to wait a short time? 
			// From the second command onwards, the rpc server has a status of finished, 
			// which can be queried in onExecute if the UR responds slower than the internal state change to execute
		}
	}
	
	@Override
	public void onExecute() {
		boolean executing = true;
		while(executing) {
			String state = getCurrentStatus().toString();
			switch (state) {
			case "busy":
				// wait for completion
				//check for element states
				// get_element_state param=id
				// if change detected: update WM
				break;
			case "finished":
				executing=false;
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
	
	private List<RivetPosition> getRivetPositions(String sector, int count, RivetPosition.State state) {
		List<RivetPosition> rivetPositions = new LinkedList<RivetPosition>();	
		
		// TODO: ask WorldModelService
		
		return rivetPositions;
	}
	

	/*
	 * private XML-RPC functions 
	 */
	
	private Object resetServer() {
		Object params[] = {};
		try {
			return client.execute("reset_server", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private Object getCurrentRoutine() {
		Object params[] = {};
		try {
			return client.execute("get_routine", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}

	private void setCurrentRoutine(int code) {
		Object params[] = { code };
		try {
			client.execute("set_routine", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}

	
	private void setRivetPositions(List<Map<String,Object>> positions) {	
		try {
			client.execute("set_rivet_positions", positions);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
		}
	}
	
	private List<Map<String,Object>> getRivetPositions() {	
		Object params[] = { };
		try {
			return (List<Map<String,Object>>)client.execute("get_rivet_positions", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	

	private Object getCurrentStatus() {
		Object params[] = {};
		try {
			return client.execute("get_status", params);
		} catch (XmlRpcException ex) {
			LOGGER.error("Exception occurred: {}", ex.toString());
			return null;
		}
	}
	
}
