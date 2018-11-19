package de.dfki.iui.basys.demonstrator.device.hybrit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.runtime.component.ComponentException;
import de.dfki.iui.basys.runtime.component.device.packml.UnitConfiguration;
import de.dfki.iui.basys.runtime.component.device.tecs.DeviceStatus;
import de.dfki.iui.basys.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.smartwatch.Smartwatch;

public class SmartWatchComponent extends TecsDeviceComponent {

	private SmartWatchTECS client;
	
	public SmartWatchComponent(ComponentConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new SmartWatchTECS(protocol);
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	private class SmartWatchTECS extends Smartwatch.Client implements DeviceStatus {

		public SmartWatchTECS(TProtocol prot) {
			super(prot);
			// TODO Auto-generated constructor stub
		}

		@Override
		public CommandResponse getCommandState() throws TException {
			//return super.getCommandState();
			return null;
		}
		
	}
	
}
