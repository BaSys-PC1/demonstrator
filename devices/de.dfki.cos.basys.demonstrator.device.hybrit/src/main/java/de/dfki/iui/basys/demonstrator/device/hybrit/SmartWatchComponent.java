package de.dfki.iui.basys.demonstrator.device.hybrit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import de.dfki.iui.basys.model.runtime.component.CapabilityRequest;
import de.dfki.iui.basys.model.runtime.component.ComponentConfiguration;
import de.dfki.iui.basys.model.runtime.component.ResponseStatus;
import de.dfki.iui.basys.runtime.component.ComponentException;
import de.dfki.iui.basys.runtime.component.device.packml.UnitConfiguration;
import de.dfki.iui.basys.runtime.component.device.tecs.DeviceStatus;
import de.dfki.iui.basys.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.hybritcommand.CommandState;
import de.dfki.iui.hrc.hybritcommand.CommandStateEvent;
import de.dfki.iui.hrc.hybritcommand.HumanTaskDTO;
import de.dfki.iui.smartwatch.Smartwatch;
import de.dfki.tecs.Event;
import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;

public class SmartWatchComponent extends TecsDeviceComponent {

	private SmartWatchTECS client;

	public SmartWatchComponent(ComponentConfiguration config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		String psUri = componentConfig.getProperty("ps-uri").getValue();

		client = new SmartWatchTECS(protocol, psUri);
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub

		// falls Provisioning --> übersetzte nach HumanTaskDTO
		// HumanTaskDTO task = new HumanTaskDTO("businessKey","operationId","description for worker","clientId");
		
		// falls InteractionCapability --> nehm topic String

		return null;
	}

	@Override
	public void onStarting() {

		Object payload = getUnitConfig().getPayload();

		if (payload instanceof String) {
			try {
				String notification = (String) payload;
				client.showNotification(notification);
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (payload instanceof HumanTaskDTO) {
			try {
				HumanTaskDTO task = (HumanTaskDTO) payload;
				client.requestTaskExecution(task);
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			setErrorCode(99);
			stop();
		}
	}

	@Override
	public void onExecute() {
		busyWait(client);
	}

	@Override
	public void onCompleting() {
		sendComponentResponse(ResponseStatus.OK, 0);
	}

	@Override
	public void onStopping() {
		sendComponentResponse(ResponseStatus.NOT_OK, getErrorCode());
	}

	public class SmartWatchTECS extends Smartwatch.Client implements DeviceStatus {
		private final TProtocol prot;
		private PSClient psClient;
		private Thread psThread;
		private CommandState cmdState;

		public SmartWatchTECS(TProtocol prot, String psUri) {
			super(prot);
			this.prot = prot;

			init(psUri);
		}

		private void init(String psUri) {
			cmdState = CommandState.READY;

			psClient = PSFactory.createPSClient(psUri);
			psClient.subscribe("CommandStateEvent");
			psClient.connect();

			psThread = new Thread(() -> {
				while (true) {
					while (psClient.canRecv()) {
						Event event = psClient.recv();
						LOGGER.debug("Received event of type {} on channel {} from {}.", event.getEtype(),
								event.getChannel(), event.getSource());
						if (event.is("CommandStateEvent")) {
							CommandStateEvent cse = new CommandStateEvent();
							event.parseData(cse);

							if (cse.response.state.equals(CommandState.FINISHED)) {
								cmdState = CommandState.FINISHED;
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});

			psThread.start();
		}

		public synchronized CommandResponse getCommandState() {
			CommandResponse response = new CommandResponse(cmdState, "");
			return response;
		}

		@Override
		public CommandState showNotification(String notification) throws TException {
			CommandState state = super.showNotification(notification);
			cmdState = state;
			return cmdState;
		}

		@Override
		public CommandState requestTaskExecution(HumanTaskDTO task) throws TException {
			CommandState state = super.requestTaskExecution(task);
			cmdState = state;
			return cmdState;
		}

	}

}
