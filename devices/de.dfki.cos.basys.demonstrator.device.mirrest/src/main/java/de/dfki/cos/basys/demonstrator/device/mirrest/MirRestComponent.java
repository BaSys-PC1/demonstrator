package de.dfki.cos.basys.demonstrator.device.mirrest;


import de.dfki.cos.basys.common.mirrestclient.MiRState;
import de.dfki.cos.basys.common.mirrestclient.MirRestClient;
import de.dfki.cos.basys.common.mirrestclient.MirRestClientImpl;
import de.dfki.cos.basys.common.mirrestclient.dto.MissionInstanceInfo;
import de.dfki.cos.basys.common.mirrestclient.dto.Status;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.MoveToLocation;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.LogisticsCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.topology.TopologyElement;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.DeviceComponent;
import de.dfki.cos.basys.platform.runtime.component.device.rest.RestDeviceComponent;
import de.dfki.cos.basys.platform.runtime.component.packml.UnitConfiguration;

public class MirRestComponent extends RestDeviceComponent {	
	
	MirRestClient mirClient = null;
	MissionInstanceInfo currentMission = null;
	
	public MirRestComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		if (isConnectedToExternal()) {
			mirClient = new MirRestClientImpl(endpoint,getConfig().getProperty("auth").getValue());
		} else {
			throw new ComponentException("MiR endpoint not available!");
		}
//		try {
//			Status status = client.getRobotStatus();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		// Recipe No. 1 is mapped to gotoSymbolicPosition
		// Recipe No. 2 is mapped to enqueueMissionInstanceByName
		
		UnitConfiguration config = new UnitConfiguration();

		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
		TopologyElement te = null;
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getMoveToLocation())) {
			te = ((MoveToLocation) c.getCapability()).getTargetLocation();
			
			if (te.getId().equals("_HM3kBDBrEem8P9knmM6g-Q")) { //Left
				String missionName = getConfig().getProperty("missionName_GotoLeft").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			} 
			else if (te.getId().equals("_HzUgBTBrEem8P9knmM6g-Q")) { //Center
				String missionName = getConfig().getProperty("missionName_GotoCenter").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
			else if (te.getId().equals("_IO3MhTBrEem8P9knmM6g-Q")) { //Right
				String missionName = getConfig().getProperty("missionName_GotoRight").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
			else if (te.getId().equals("_IqVnlTBrEem8P9knmM6g-Q")) { //Wait-1
				String missionName = getConfig().getProperty("missionName_GotoWait_1").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
			else if (te.getId().equals("_JJfoxTBrEem8P9knmM6g-Q")) { //Wait-2
				String missionName = getConfig().getProperty("missionName_GotoWait_2").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
			else {			
				config.setRecipe(1);
				config.setPayload(te);
			}
		}

		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getTransport())) {
			LogisticsCapabilityVariant variant = (LogisticsCapabilityVariant) c;
			te = variant.getAppliedOn().get(0);
			if (te.getId().equals("_hx68AOHHEeieRbude1ENJg")) { //Robotable
				String missionName = getConfig().getProperty("missionName_Robotable").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
			if (te.getId().equals("_YR04EOHCEeieRbude1ENJg")) { //Logistiktisch
				String missionName = getConfig().getProperty("missionName_Logistiktisch").getValue();
				config.setPayload(missionName);
				config.setRecipe(2);
			}
		}
		
		return config;
	}
	
	@Override
	public void onResetting() {
		Status status = mirClient.setRobotStatus(MiRState.READY);
		currentMission = null;
	}
	
	@Override
	public void onStarting() {		
		if (getUnitConfig().getRecipe() == 1) {
			TopologyElement targetElement = ((TopologyElement) getUnitConfig().getPayload());
			LOGGER.info("Moving to position: " + targetElement.getName());
			try {
				currentMission = mirClient.gotoSymbolicPosition(targetElement.getName());
			} catch (Exception e) {
				e.printStackTrace();
				stop();
			}
		} else if (getUnitConfig().getRecipe() == 2) {
			String missionName = ((String) getUnitConfig().getPayload());
			try {
				currentMission = mirClient.enqueueMissionInstanceByName(missionName);	
			} catch (Exception e) {
				e.printStackTrace();
				stop();
			}
		}
	}
	
	@Override
	public void onExecute() {
		try {
			boolean executing = true;
			while(executing) {
				currentMission = mirClient.getMissionInstanceInfo(currentMission.id);
				LOGGER.debug("MissionState is " + currentMission.state);
				 
				switch (currentMission.state.toLowerCase()) {
				case "pending":
					break;
				case "executing":
					break;
				case "done":
					executing=false;
					break;
				case "failed":
					executing=false;
					setErrorCode(1);
					stop();
					break;
				case "aborted":
					executing=false;
					setErrorCode(2);
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
	public void onStopping() {
		super.onStopping();
		
		try {
			Status status = mirClient.setRobotStatus(MiRState.PAUSED);
			if (currentMission != null) {
				mirClient.dequeueMissionInstance(currentMission.id);
				currentMission = null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
	
}
