package de.dfki.iui.basys.demonstrator.device.urrpc;

import org.apache.xmlrpc.XmlRpcException;

import de.dfki.iui.basys.model.domain.capability.CapabilityPackage;
import de.dfki.iui.basys.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.model.runtime.component.ResponseStatus;
import de.dfki.iui.basys.runtime.component.ComponentException;
import de.dfki.iui.basys.runtime.component.device.packml.UnitConfiguration;
import de.dfki.iui.basys.runtime.component.device.xmlrpc.XmlRpcDeviceComponent;

public class UrRpcComponent extends XmlRpcDeviceComponent {	
		
	public UrRpcComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
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
		
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getGuiding())) {			
			int recipe = Integer.parseInt(getConfig().getProperty("recipe").getValue());					
			config.setRecipe(recipe);
		}
		return config;
		
	}
	
	@Override
	public void onResetting() {

	}
	
	@Override
	public void onStarting() {		
		if (getUnitConfig().getRecipe() > 0)
			setCurrentRoutine(getUnitConfig().getRecipe());
	}
	
	@Override
	public void onExecute() {
		boolean executing = true;
		while(executing) {
			String state = getCurrentStatus().toString();
			switch (state) {
			case "busy":
				// wait for completion
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

	@Override
	public void onCompleting() {		
		sendComponentResponse(ResponseStatus.OK, 0);
	}
	
	@Override
	public void onStopping() {
		sendComponentResponse(ResponseStatus.NOT_OK, getErrorCode());
	}
	
	
	/*
	 * private XML-RPC functions 
	 */
	

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
