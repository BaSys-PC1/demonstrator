package de.dfki.iui.basys.demonstrator.device.urrpc;


import de.dfki.iui.basys.model.domain.capability.CapabilityPackage;
import de.dfki.iui.basys.model.domain.capability.MoveToLocation;
import de.dfki.iui.basys.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.iui.basys.model.domain.resourceinstance.LogisticsCapabilityVariant;
import de.dfki.iui.basys.model.domain.topology.TopologyElement;
import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.model.runtime.component.ResponseStatus;
import de.dfki.iui.basys.runtime.component.ComponentContext;
import de.dfki.iui.basys.runtime.component.ComponentException;
import de.dfki.iui.basys.runtime.component.device.DeviceComponent;
import de.dfki.iui.basys.runtime.component.device.packml.UnitConfiguration;

public class UrRpcComponent extends DeviceComponent {	
	
	UrRpcClient client = null;
	
	public UrRpcComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new UrRpcClient(getConfig().getExternalConnectionString());
		client.setCurrentRoutine(1);
		LOGGER.info(client.getCurrentStatus().toString());
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
			client.setCurrentRoutine(getUnitConfig().getRecipe());
	}
	
	@Override
	public void onExecute() {
		boolean executing = true;
		while(executing) {
			String state = client.getCurrentStatus().toString();
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
	
	
	
}
