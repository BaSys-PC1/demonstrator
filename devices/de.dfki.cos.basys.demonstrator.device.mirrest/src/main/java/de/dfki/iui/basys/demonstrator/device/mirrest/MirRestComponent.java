package de.dfki.iui.basys.demonstrator.device.mirrest;

import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.model.runtime.component.ResponseStatus;
import de.dfki.iui.basys.runtime.component.ComponentContext;
import de.dfki.iui.basys.runtime.component.ComponentException;
import de.dfki.iui.basys.runtime.component.device.DeviceComponent;
import de.dfki.iui.basys.runtime.component.device.packml.UnitConfiguration;

public class MirRestComponent extends DeviceComponent {	
	
	public MirRestComponent(ComponentConfiguration config) {
		super(config);
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		//CapabilityVariant<?> variant = (CapabilityVariant<?>)req.getCapabilityVariant();
		// c = variant.getCapability();

		//if (c.eClass().equals(CapabilityPackage.eINSTANCE.getSwitchConfirmationCapability()))
		{
			//return new UnitConfiguration();
		}
		return new UnitConfiguration();
		//throw new ComponentException("unknown capability");
		//return null;
		

	}
	
	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);
		
	
	}


	
	@Override
	public void onStarting() {
		

	}
	
	@Override
	public void onExecute() {
		
		
	}

	@Override
	public void onCompleting() {
		
		//TODO: notify Basys
		//outChannel.sendMessage("");
		sendComponentResponse(ResponseStatus.OK, 0);
	}
	
	@Override
	public void onStopping() {
	}
	
	@Override
	public void onResetting() {
	}
	
	
}
