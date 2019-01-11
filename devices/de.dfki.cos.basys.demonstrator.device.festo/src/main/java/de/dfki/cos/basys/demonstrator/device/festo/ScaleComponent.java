package de.dfki.cos.basys.demonstrator.device.festo;

import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ControlMode;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.model.runtime.component.State;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.opcua.OpcUaDeviceComponent;
import de.dfki.cos.basys.platform.runtime.component.device.opcua.OpcUaException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;

public class ScaleComponent extends OpcUaDeviceComponent {


	final NodeId NODE_SCALE_CONTROLLER = new NodeId(2, "ScaleController");
	final NodeId NODE_START_MEASUREMENT = new NodeId(2, "ScaleController/StartMeasurement");
	final NodeId NODE_STOP_MEASUREMENT = new NodeId(2, "ScaleController/StopMeasurement");

	public ScaleComponent(ComponentConfiguration config) {
		super(config);
	}

	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);
		if (!simulated) {
			try {
				//subscribeToValue(NODE_VARIABLE_JOB_STATUS);
				//subscribeToValue(NODE_VARIABLE_JOB_ERRORCODE);
	
			} catch (Exception e) {
				throw new ComponentException(new OpcUaException(e));
			}
		}		
	}

	@Override
	public void onResetting() {
		
	}

	@Override
	public void onStarting() {
		startMeasurement();
	}

	@Override
	public void onExecute() {
		awaitExecuteComplete();
	}
	
	@Override
	public void onCompleting() {
		super.onCompleting();
		stopMeasurement();
	}

	@Override
	public void onStopping() {
		// erstmal analog zu onCompleting
		super.onCompleting(); // super.onStopping();
		stopMeasurement();		
	}

	@Override
	protected void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		super.onSubscriptionValue(item, value);		

	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	public void startMeasurement() {
		try {
			invokeMethodAsync(NODE_SCALE_CONTROLLER, NODE_START_MEASUREMENT);
			sleep(1);
		} catch (OpcUaException e) {
			e.printStackTrace();
		}
	}
	
	public void stopMeasurement() {
		try {
			invokeMethodAsync(NODE_SCALE_CONTROLLER, NODE_STOP_MEASUREMENT);
			sleep(1);
		} catch (OpcUaException e) {
			e.printStackTrace();
		}
	}




}
