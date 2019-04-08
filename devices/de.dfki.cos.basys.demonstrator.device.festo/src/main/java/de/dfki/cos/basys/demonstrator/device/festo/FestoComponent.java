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
import de.dfki.cos.basys.platform.runtime.component.packml.UnitConfiguration;

public class FestoComponent extends OpcUaDeviceComponent {

	final NodeId NODE_VARIABLE_JOB_STATUS = new NodeId(1, 320);
	final NodeId NODE_VARIABLE_JOB_ERRORCODE = new NodeId(1, 318);

	final NodeId NODE_SERVICES = new NodeId(1, 52);
	final NodeId NODE_EXECUTE_PP_JOB = new NodeId(1, 242);
	final NodeId NODE_CANCEL_PP_JOB = new NodeId(1, 119);
	final NodeId NODE_ACK = new NodeId(1, 314);

	// private CountDownLatch completeLatch;
	// private CountDownLatch cancelLatch;

	private short oldJobStatus = 0;
	private short jobStatus = 0;
	private short jobErrorCode = 0;

//	private Lock lock;
//	private Condition executeCondition;
//  private Condition stoppingCondition;
	
	public FestoComponent(ComponentConfiguration config) {
		super(config);

//		lock = new ReentrantLock();
//		executeCondition = lock.newCondition();
//      stoppingCondition = lock.newCondition();
		//resetOnComplete = true;
		// resetOnStopped = true;
	}

	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);

		if (!simulated) {
			try {
				subscribeToValue(NODE_VARIABLE_JOB_STATUS);
				subscribeToValue(NODE_VARIABLE_JOB_ERRORCODE);
	
			} catch (Exception e) {
				throw new ComponentException(new OpcUaException(e));
			}
		}
		else {
			simulated = false;
			setMode(ControlMode.MAINTENANCE);
			simulated = true;
		}
	}

	@Override
	public void onResetting() {
		oldJobStatus = 0;
		jobStatus = 0;
		ackn();
	}

	@Override
	public void onStarting() {
		int lidNumber = 1;
		if (getUnitConfig() != null)
			lidNumber = getUnitConfig().getRecipe();
		
		executeJob((short)lidNumber);
	}

	@Override
	public void onExecute() {
		awaitExecuteComplete();
	}

	@Override
	public void onStopping() {
		try {
			short jobErrorCode = readValue(NODE_VARIABLE_JOB_ERRORCODE);
			if (jobErrorCode != 0) {
				// internal error report
				setErrorCode(jobErrorCode);
			} else {
				// external stop
				int lidNumber = getUnitConfig().getRecipe();
				cancelJob((short) lidNumber);
			}
			
		} catch (OpcUaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		super.onStopping();
	}

	@Override
	protected void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
		super.onSubscriptionValue(item, value);

		if (item.getReadValueId().getNodeId() == NODE_VARIABLE_JOB_ERRORCODE) {
			jobErrorCode = (short) (value.getValue().getValue());
			System.out.println("NEW ERROR CODE: " + jobErrorCode);
			if (jobErrorCode != 0) {
				//stop();
			}
		}

		if (item.getReadValueId().getNodeId() == NODE_VARIABLE_JOB_STATUS) {
			short newJobStatus = (short) (value.getValue().getValue());

			System.out.println("NEW JOB STATUS " + newJobStatus);
			
			oldJobStatus = jobStatus;
			jobStatus = newJobStatus;
			switch (jobStatus) {
			case 0: // IDLE
				if (getState() == State.STOPPED) {
					//externes Reset
					reset();
				} else if (getState() == State.EXECUTE) {
					// Fügen erfolgeich, EXECUTE->COMPLETING
					signalExecuteComplete();
				} else if (getState() == State.STOPPING) {
					// signalComplete();
				} else if (getState() == State.RESETTING) {
					// signalComplete();
				}
				break;
			case 1: // RUNNING
				// ignore
				break;
			case 2: // RESETTING
				// ignore
				break;
			case 3: // ERROR
				// wird über Subscription auf jobErrorCode getriggert
				stop();
				break;
			default:
				break;
			}
		}

		

	}


	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub
		return null;
	}

	public void ackn() {
		System.out.println("ackn");
		try {
			invokeMethodAsync(NODE_SERVICES, NODE_ACK);
			sleep(1);
		} catch (OpcUaException e) {
			e.printStackTrace();
		}
	}

	public void executeJob(short lidNumber) {
		System.out.println("executeJob");
		try {
			invokeMethodAsync(NODE_SERVICES, NODE_EXECUTE_PP_JOB, lidNumber);
			sleep(1);
		} catch (OpcUaException e) {
			e.printStackTrace();
		}
	}

	public void cancelJob(short lidNumber) {
		System.out.println("cancelJob");
		try {
			invokeMethodAsync(NODE_SERVICES, NODE_CANCEL_PP_JOB, lidNumber);
			sleep(1);
		} catch (OpcUaException e) {
			e.printStackTrace();
		}
	}



}
