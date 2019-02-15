package de.dfki.cos.basys.demonstrator.device.hybrit;

import java.net.URI;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.MoveToLocation;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.cos.hrc.hmi19.RivotStateQAChangedEvent;
import de.dfki.cos.hrc.hololens.HoloLens;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.hybritcommand.CommandState;
import de.dfki.iui.hrc.hybritcommand.CommandStateEvent;
import de.dfki.iui.hrc.hybritcommand.HumanTaskDTO;
import de.dfki.iui.smartwatch.Smartwatch;
import de.dfki.tecs.Event;
import de.dfki.tecs.ps.PSClient;
import de.dfki.tecs.ps.PSFactory;

public class HololensComponent extends TecsDeviceComponent {

	private HoloLensTECS client;

	public HololensComponent(ComponentConfiguration config) {
		super(config);
		//resetOnComplete = true;
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		String psUri = componentConfig.getProperty("ps-uri").getValue();

		client = new HoloLensTECS(protocol, psUri);
	}

	// ANPASSEN!!!!
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		// TODO Auto-generated method stub
		
		UnitConfiguration config = new UnitConfiguration();
		
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		
		
		//******************************************************
		// TODO
		//******************************************************

//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getQAVisualisationCapability())) {
//			config.setPayload("hallo");
//		}
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getBuffering())) {
//			HumanTaskDTO task = new HumanTaskDTO("businessKey","operationId","Raceways bereitstellen","smartwatch-lg-3691");
//			config.setPayload(task);
//		}
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getScrewing())) {
//			HumanTaskDTO task = new HumanTaskDTO("businessKey","operationId","Raceway montieren","smartwatch-lg-3691");
//			config.setPayload(task);
//		}
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getTake())) {
//			HumanTaskDTO task = new HumanTaskDTO("businessKey","operationId","Akkuschrauber nehmen","smartwatch-lg-3691");
//			config.setPayload(task);
//		}
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getPassingOn())) {
//			HumanTaskDTO task = new HumanTaskDTO("businessKey","operationId","Raceway montieren","smartwatch-lg-3691");
//			config.setPayload(task);
//		}
		
		return config;
	}
	
	@Override
	public void onResetting() {
		super.onResetting();
		client.reset();
	}

	@Override
	public void onStarting() {

		Object payload = getUnitConfig().getPayload();

		if (payload instanceof HumanTaskDTO) {
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

	//TODO: Anpassen!!!
	public class HoloLensTECS extends HoloLens.Client implements DeviceStatus {
		private final TProtocol prot;
		private PSClient psClient;
		private Thread psThread;
		private CommandState cmdState;

		public HoloLensTECS(TProtocol prot, String psUri) {
			super(prot);
			this.prot = prot;

			init(psUri);
		}

		private void init(String psUri) {
			cmdState = CommandState.READY;

			psClient = PSFactory.createPSClient(URI.create(psUri));
			psClient.subscribe("RivotStateQAChangedEvent");
			psClient.connect();

			psThread = new Thread(() -> {
				while (true) {
					while (psClient.canRecv()) {
						Event event = psClient.recv();
						LOGGER.debug("Received event of type {} on channel {} from {}.", event.getEtype(), event.getChannel(), event.getSource());
						if (event.is("RivotStateQAChangedEvent")) {
							RivotStateQAChangedEvent rsce = new RivotStateQAChangedEvent();
							event.parseData(rsce);

							// **********************************************
							// TODO report changed rivet state to world model
							// **********************************************

						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			});

			psThread.start();
		}
		
		public void reset() {
			cmdState = CommandState.READY;
		}
		
		public synchronized CommandResponse getCommandState() {
			CommandResponse response = new CommandResponse(cmdState, "");
			return response;
		}

		@Override
		public CommandState showNotification(String notification, String businessKey) throws TException {
			CommandState state = super.showNotification(notification, businessKey);
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
