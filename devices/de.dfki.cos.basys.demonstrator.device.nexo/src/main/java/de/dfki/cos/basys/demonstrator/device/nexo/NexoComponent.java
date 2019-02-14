package de.dfki.cos.basys.demonstrator.device.nexo;



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
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.rest.RestDeviceComponent;

public class NexoComponent extends RestDeviceComponent {	
	
//	MirRestClient client = null;
//	MissionInstanceInfo currentMission = null;
	
	public NexoComponent(ComponentConfiguration config) {
		super(config);
		resetOnComplete = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
//		super.connectToExternal();
//		client = new MirRestClientImpl(getConfig().getExternalConnectionString(),getConfig().getProperty("auth").getValue());
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {

		// Recipe No. 1 is mapped to gotoSymbolicPosition
		// Recipe No. 2 is mapped to enqueueMissionInstanceByName
		
		UnitConfiguration config = new UnitConfiguration();

//		CapabilityVariant<?, ?> c = req.getCapabilityVariant();
//		TopologyElement te = null;
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getMoveToLocation())) {
//			te = ((MoveToLocation) c.getCapability()).getTargetLocation();
//			config.setRecipe(1);
//			config.setPayload(te);
//		}
//
//		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getTransport())) {
//			LogisticsCapabilityVariant variant = (LogisticsCapabilityVariant) c;
//			te = variant.getAppliedOn().get(0);
//			if (te.getId().equals("_hx68AOHHEeieRbude1ENJg")) { //Robotable
//				String missionName = getConfig().getProperty("missionName_Robotable").getValue();
//				config.setPayload(missionName);
//				config.setRecipe(2);
//			}
//			if (te.getId().equals("_YR04EOHCEeieRbude1ENJg")) { //Logistiktisch
//				String missionName = getConfig().getProperty("missionName_Logistiktisch").getValue();
//				config.setPayload(missionName);
//				config.setRecipe(2);
//			}
//		}
		
		return config;
	}
	
	@Override
	public void onResetting() {
//		Status status = client.setRobotStatus(MiRState.READY);
//		currentMission = null;
	}
	
	@Override
	public void onStarting() {				
//		endpoint.path("/selectProgram").request().post();
//		if (getUnitConfig().getRecipe() == 1) {
//			TopologyElement targetElement = ((TopologyElement) getUnitConfig().getPayload());
//			LOGGER.info("Moving to position: " + targetElement.getName());
//			try {
//				currentMission = client.gotoSymbolicPosition(targetElement.getName());
//			} catch (Exception e) {
//				e.printStackTrace();
//				stop();
//			}
//		} else if (getUnitConfig().getRecipe() == 2) {
//			String missionName = ((String) getUnitConfig().getPayload());
//			currentMission = client.enqueueMissionInstanceByName(missionName);
//		}
	}
	
	@Override
	public void onExecute() {
//		try {
//			boolean executing = true;
//			while(executing) {
//				currentMission = client.getMissionInstanceInfo(currentMission.id);
//				LOGGER.debug("MissionState is " + currentMission.state);
//				 
//				switch (currentMission.state.toLowerCase()) {
//				case "pending":
//					break;
//				case "executing":
//					break;
//				case "done":
//					executing=false;
//					break;
//				case "failed":
//					executing=false;
//					setErrorCode(1);
//					stop();
//					break;
//				case "aborted":
//					executing=false;
//					setErrorCode(2);
//					stop();
//					break;
//				default:
//					break;
//				}
//	
//				try {
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			setErrorCode(3);
//			stop();
//		}
		
	}


	
	@Override
	public void onStopping() {
		super.onStopping();
		
//		try {
//			Status status = client.setRobotStatus(MiRState.PAUSED);
//			if (currentMission != null) {
//				client.dequeueMissionInstance(currentMission.id);
//				currentMission = null;
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}		
	}
	
	
	
}
