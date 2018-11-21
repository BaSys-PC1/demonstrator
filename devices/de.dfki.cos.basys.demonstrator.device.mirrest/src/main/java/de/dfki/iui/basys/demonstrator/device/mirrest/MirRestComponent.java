package de.dfki.iui.basys.demonstrator.device.mirrest;


import de.dfki.cos.basys.common.mirrestclient.MiRState;
import de.dfki.cos.basys.common.mirrestclient.MirRestClient;
import de.dfki.cos.basys.common.mirrestclient.MirRestClientImpl;
import de.dfki.cos.basys.common.mirrestclient.dto.MissionInstanceInfo;
import de.dfki.cos.basys.common.mirrestclient.dto.Status;
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

public class MirRestComponent extends DeviceComponent {	
	
	MirRestClient client = null;
	MissionInstanceInfo currentMission = null;
	
	public MirRestComponent(ComponentConfiguration config) {
		super(config);
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new MirRestClientImpl(getConfig().getExternalConnectionString(),getConfig().getProperty("auth").getValue());
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		UnitConfiguration config = new UnitConfiguration();

		CapabilityVariant<?> c = req.getCapabilityVariant();
		TopologyElement te = null;
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getMoveToLocation())) {
			te = ((MoveToLocation) c.getCapability()).getTargetLocation();
		}

		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getTransport())) {
			LogisticsCapabilityVariant variant = (LogisticsCapabilityVariant) c;
			te = variant.getAppliedOn().get(0);
		}

		config.setPayload(te);
		return config;
	}
	
	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);
		
	
	}


	@Override
	public void onResetting() {
		Status status = client.setRobotStatus(MiRState.READY);
		currentMission = null;
	}
	
	@Override
	public void onStarting() {		
		TopologyElement targetElement = ((TopologyElement) getUnitConfig().getPayload());
		LOGGER.info("Moving to position: " + targetElement.getName());
		try {
			currentMission = client.gotoSymbolicPosition(targetElement.getName());
		} catch (Exception e) {
			e.printStackTrace();
			stop();
		}
	}
	
	@Override
	public void onExecute() {
		try {
			boolean executing = true;
			while(executing) {
				currentMission = client.getMissionInstanceInfo(currentMission.id);
				LOGGER.debug("MissionState is " + currentMission.state);
				 
				switch (currentMission.state.toLowerCase()) {
				case "pending":
					break;
				case "executing":
					break;
				case "finished":
					executing=false;
					break;
				case "failed":
					executing= false;
					setErrorCode(1);
					stop();
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
		} catch (Exception e) {
			e.printStackTrace();
			setErrorCode(3);
			stop();
		}
		
	}

	@Override
	public void onCompleting() {		
		sendComponentResponse(ResponseStatus.OK, 0);
	}
	
	@Override
	public void onStopping() {
		sendComponentResponse(ResponseStatus.NOT_OK, getErrorCode());
		try {
			Status status = client.setRobotStatus(MiRState.PAUSED);
			if (currentMission != null) {
				client.dequeueMissionInstance(currentMission.id);
				currentMission = null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
