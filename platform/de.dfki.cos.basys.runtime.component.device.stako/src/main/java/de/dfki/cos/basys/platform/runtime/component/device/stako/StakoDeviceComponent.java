package de.dfki.cos.basys.platform.runtime.component.device.stako;

import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.DeviceComponent;

public abstract class StakoDeviceComponent extends DeviceComponent {

	public StakoDeviceComponent(ComponentConfiguration config) {
		super(config);
	}

	@Override
	public void connectToExternal() throws ComponentException {

	}
	
	@Override
	public void disconnectFromExternal() {

	}
	
}
