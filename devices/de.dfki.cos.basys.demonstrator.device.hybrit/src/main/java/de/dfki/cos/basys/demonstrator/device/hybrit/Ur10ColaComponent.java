package de.dfki.cos.basys.demonstrator.device.hybrit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.LoadCarrierUnitEnum;
import de.dfki.cos.basys.platform.model.domain.capability.PickAndPlace;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.LogisticsCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstancePackage;
import de.dfki.cos.basys.platform.model.domain.topology.TopologyElement;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.iui.hrc.franka.ActionException;
import de.dfki.iui.hrc.franka.FrankaConstants;
import de.dfki.iui.hrc.franka.LoadException;
import de.dfki.iui.hrc.franka.MoveException;
import de.dfki.iui.hrc.general3d.TransformationMatrix;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.ur.UR;
import de.dfki.iui.hrc.ur.URState;
import de.dfki.iui.hrc.ur.URStatus;
import de.dfki.tecs.Event;

public class Ur10ColaComponent extends TecsDeviceComponent{
	
	private Ur10TECS client;

//	protected enum ACTION{
//		// fill enums here
//		;
//		
//		private final String action;
//		
//		private ACTION(String a) {
//			action = a;
//		}
//		
//		@Override
//		public String toString() {
//			return action;
//		}
//	}
//	
//	protected enum OBJECT_ID{
//		// fill enums here
//		;
//		
//		private final String objectId;
//		
//		private OBJECT_ID(String id) {
//			objectId = id;
//		}
//		
//		@Override
//		public String toString() {
//			return objectId;
//		}
//	}
//	
//	protected enum POSITION{
//		//fill here
//		;
//		
//		private final String position;
//		
//		private POSITION(String p) {
//			position = p;
//		}
//		
//		@Override
//		public String toString() {
//			return position;
//		}
//	}

	private String variant1 = "{\r\n" + 
			"    \"eClass\" : \"http://www.dfki.de/iui/basys/model/component#//CapabilityRequest\",\r\n" + 
			"    \"capabilityVariant\" : {\r\n" + 
			"      \"eClass\" : \"http://www.dfki.de/iui/basys/model/resourceinstance#//LogisticsCapabilityVariant\",\r\n" + 
			"      \"id\" : \"_gTVaBV-lEeixtLE-b5nbbQ\",\r\n" + 
			"      \"name\" : \"Unload MiR (bottle)\",\r\n" + 
			"      \"capability\" : {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/capability#//PickAndPlace\",\r\n" + 
			"        \"id\" : \"_sLcgPl-UEeioxNEjr9hsyQ\",\r\n" + 
			"        \"loadCarrierUnit\" : \"BOTTLE\"\r\n" + 
			"      },\r\n" + 
			"      \"appliedOn\" : [ {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/topology#//AGVStation\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_rBfZoV2TEeit97PGgoQOAQ\"\r\n" + 
			"      }, {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/topology#//StorageZone\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_NQFk4zB5Eei1bbwBPPZWOA\"\r\n" + 
			"      } ]\r\n" + 
			"    }\r\n" + 
			"  }";

	private String variant2 = "{\r\n" + 
			"    \"eClass\" : \"http://www.dfki.de/iui/basys/model/component#//CapabilityRequest\",\r\n" + 
			"    \"capabilityVariant\" : {\r\n" + 
			"      \"eClass\" : \"http://www.dfki.de/iui/basys/model/resourceinstance#//LogisticsCapabilityVariant\",\r\n" + 
			"      \"id\" : \"_gTWBEV-lEeixtLE-b5nbbQ\",\r\n" + 
			"      \"name\" : \"Load MiR (bottle)\",\r\n" + 
			"      \"capability\" : {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/capability#//PickAndPlace\",\r\n" + 
			"        \"id\" : \"_s_aTzl-UEeioxNEjr9hsyQ\",\r\n" + 
			"        \"loadCarrierUnit\" : \"BOTTLE\"\r\n" + 
			"      },\r\n" + 
			"      \"appliedOn\" : [ {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/topology#//StorageZone\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_NQFk4zB5Eei1bbwBPPZWOA\"\r\n" + 
			"      }, {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/topology#//AGVStation\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_rBfZoV2TEeit97PGgoQOAQ\"\r\n" + 
			"      } ]\r\n" + 
			"    }\r\n" + 
			"  }";
	
	
	public Ur10ColaComponent(ComponentConfiguration config) {
		super(config);	
		//resetOnComplete = true;
	}
	
	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new Ur10TECS(protocol);
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		UnitConfiguration config = new UnitConfiguration();
		if (req.getCapabilityVariant().eClass().equals(ResourceinstancePackage.eINSTANCE.getLogisticsCapabilityVariant())) {
			LogisticsCapabilityVariant variant = (LogisticsCapabilityVariant) req.getCapabilityVariant();
			if (variant.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getPickAndPlace())) {
				PickAndPlace capability = (PickAndPlace) variant.getCapability();
				if (capability.getLoadCarrierUnit() == LoadCarrierUnitEnum.BOTTLE) {
					if (variant.getAppliedOn().size() == 2) {
						TopologyElement from = variant.getAppliedOn().get(0);						
						TopologyElement to   = variant.getAppliedOn().get(1);
						if (from.getId().equals("_rBfZoV2TEeit97PGgoQOAQ") && to.getId().equals("_NQFk4zB5Eei1bbwBPPZWOA")) {
							// Unload MiR (bottle)
							config.setPayload(FrankaConstants.KNOWN_POSE_3);
						}
						if (to.getId().equals("_rBfZoV2TEeit97PGgoQOAQ") && from.getId().equals("_NQFk4zB5Eei1bbwBPPZWOA")) {
							// Load MiR (bottle)
							config.setPayload(FrankaConstants.KNOWN_POSE_2);
						} 
					}
				}
			}			
		}
		
		return config;
	}
	
	@Override
	public void onResetting() {
		reconnect();
	}

	@Override
	public void onStarting() {
		String pose = (String)getUnitConfig().getPayload();
		LOGGER.info("Start executing pose: " + pose);
		try {			
			client.Load(pose);
		} catch (TException e) {
			e.printStackTrace();
			if (e instanceof TTransportException ) {
				LOGGER.warn("Trying to reconnect");
				reconnect();
				try {
					client.Load(pose);
				} catch (TException e1) {
					e1.printStackTrace();
					setErrorCode(1);
					stop();
				}
			}
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

	private class Ur10TECS extends UR.Client implements DeviceStatus {

		private TProtocol protocol;
		
		public Ur10TECS(TProtocol prot) {
			super(prot);
			protocol = prot;
		}
		
		@Override
		public TransformationMatrix requestM() throws TException {
			return super.requestM();
		}
		
		@Override
		public URStatus getStatus() throws TException {
			return super.getStatus();
		}
		
		@Override
		public URState getState() throws TException {
			return super.getState();
		}
		
		@Override
		public CommandResponse getCommandState() throws TException {
			return super.getCommandState();
		}
		
		@Override
		public CommandResponse Load(String targetPosition) throws LoadException, TException {
			return super.Load(targetPosition);
		}
		
		@Override
		public CommandResponse MoveToKnownPosition(String knownPosition) throws MoveException, TException {
			return super.MoveToKnownPosition(knownPosition);
		}
		
		@Override
		public CommandResponse Recover(String action) throws ActionException, TException {
			return super.Recover(action);
		}
		
		@Override
		public CommandResponse EnterTeachMode(String action) throws TException {
			return super.EnterTeachMode(action);
		}
		
		@Override
		public CommandResponse ExitTeachMode(String action) throws TException {
			return super.ExitTeachMode(action);
		}		
	}

	@Override
	protected void handleTecsEvent(Event event) {}

}
