package de.dfki.cos.basys.platform.runtime.component.device.stako;

import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.DeviceComponent;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;

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
