package de.dfki.cos.basys.demonstrator.device.hybrit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.LoadCarrierUnitEnum;
import de.dfki.cos.basys.platform.model.domain.capability.PickAndPlace;
import de.dfki.cos.basys.platform.model.domain.productdefinition.BOMEntry;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ManufacturingCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstancePackage;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ResponseStatus;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.DeviceStatus;
import de.dfki.cos.basys.platform.runtime.component.device.tecs.TecsDeviceComponent;
import de.dfki.cos.basys.platform.runtime.component.packml.UnitConfiguration;
import de.dfki.iui.hrc.general3d.TransformationMatrix;
import de.dfki.iui.hrc.generalrobots.RobotState;
import de.dfki.iui.hrc.hybritcommand.CommandResponse;
import de.dfki.iui.hrc.ur.ActionException;
import de.dfki.iui.hrc.ur.LoadException;
import de.dfki.iui.hrc.ur.MoveException;
import de.dfki.iui.hrc.ur.UR;
import de.dfki.iui.hrc.ur.URState;
import de.dfki.iui.hrc.ur.URStatus;
import de.dfki.iui.hrc.ur.urConstants;

public class Ur3Component extends TecsDeviceComponent{

	private Ur3TECS client;
	
	private String variant = "{\r\n" + 
			"    \"eClass\" : \"http://www.dfki.de/iui/basys/model/component#//CapabilityRequest\",\r\n" + 
			"    \"capabilityVariant\" : {\r\n" + 
			"      \"eClass\" : \"http://www.dfki.de/iui/basys/model/resourceinstance#//ManufacturingCapabilityVariant\",\r\n" + 
			"      \"id\" : \"_gTSWsV-lEeixtLE-b5nbbQ\",\r\n" + 
			"      \"name\" : \"Apply dark blue cap\",\r\n" + 
			"      \"capability\" : {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/capability#//PickAndPlace\",\r\n" + 
			"        \"id\" : \"_xio67l8yEeiUo-65_7rTBQ\",\r\n" + 
			"        \"loadCarrierUnit\" : \"MATERIAL\"\r\n" + 
			"      },\r\n" + 
			"      \"appliedOn\" : [ {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/productdefinition#//MaterialEntry\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_IpqbzV29EeixDOGCyjgf_g\"\r\n" + 
			"      }, {\r\n" + 
			"        \"eClass\" : \"http://www.dfki.de/iui/basys/model/productdefinition#//AssemblyGroupEntry\",\r\n" + 
			"        \"$ref\" : \"http://localhost:8080/services/entity/_IpqbzF29EeixDOGCyjgf_g\"\r\n" + 
			"      } ]\r\n" + 
			"    }\r\n" + 
			"  }";
	
	public Ur3Component(ComponentConfiguration config) {
		super(config);
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new Ur3TECS(protocol);
	}
	
	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		UnitConfiguration config = new UnitConfiguration();
		
		if (req.getCapabilityVariant().eClass().equals(ResourceinstancePackage.eINSTANCE.getManufacturingCapabilityVariant())) {
			ManufacturingCapabilityVariant variant = (ManufacturingCapabilityVariant) req.getCapabilityVariant();
			if (variant.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getPickAndPlace())) {
				PickAndPlace capability = (PickAndPlace) variant.getCapability();
				if (capability.getLoadCarrierUnit() == LoadCarrierUnitEnum.MATERIAL) {
					if (variant.getAppliedOn().size() == 2) {
						BOMEntry bom1 = variant.getAppliedOn().get(0);
						BOMEntry bom2 = variant.getAppliedOn().get(1);
						if (bom1.getId().equals("_IpqbzV29EeixDOGCyjgf_g") && bom2.getId().equals("_IpqbzF29EeixDOGCyjgf_g")) {
							// place cap
							config.setPayload(urConstants.KNOWN_POSE_8);
						}
					}
				}
			}			
		}
		
		return config;
	}

	@Override
	public void onStarting() {
		String pose = (String) getUnitConfig().getPayload();
		LOGGER.info("Start executing pose: " + pose);
		
		try {
			gotoHomeFromSafePosition();
		
			client.Load(pose);
		
		} catch (MoveException e2) {
			e2.printStackTrace();
			stop();
		} catch (TException e2) {
			e2.printStackTrace();
			stop();
		}
	}

	@Override
	public void onExecute() {
		busyWait(client);
	}	
	
	@Override
	public void onCompleting() {
		gotoSafePosition();
		//gotoHomePosition();		
		super.onCompleting();
	}

	@Override
	public void onStopping() {
		gotoSafePosition();
		//gotoHomePosition();
		super.onStopping();
	}
	
	private void gotoSafePosition() {
		gotoPosition(urConstants.KNOWN_POSE_6);			
	}
	
	private void gotoHomePosition() {
		gotoPosition(urConstants.KNOWN_POSE_1);		
	}
	
	private void gotoHomeFromSafePosition() {
		gotoPosition(urConstants.KNOWN_POSE_7);	
	}
	
	private void gotoPosition(String position) {
		try {
			client.MoveToKnownPosition(position);
			busyWait(client);
		} catch (MoveException e2) {
			e2.printStackTrace();
			stop();
		} catch (TException e2) {
			e2.printStackTrace();
			stop();
		}		
	}


	private class Ur3TECS extends UR.Client implements DeviceStatus {

		private TProtocol protocol;
		
		public Ur3TECS(TProtocol prot) {
			super(prot);
			protocol = prot;
		}
		
		@Override
		public TransformationMatrix requestM() throws TException{
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
		public RobotState getRobotState() throws TException {
			return super.getRobotState();
		}
		
		@Override
		public RobotState setRobotState(RobotState state) throws TException {
			return super.setRobotState(state);
		}
		
		@Override
		public CommandResponse Load(String targetPosition) throws LoadException, TException {
			return super.Load(targetPosition);
		}
		
		@Override
		public CommandResponse Calibrate() throws TException {
			return super.Calibrate();
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
		public CommandResponse EnterTeachMode(String action) throws ActionException, TException{
			return super.EnterTeachMode(action);
		}
		
		@Override
		public CommandResponse ExitTeachMode(String action) throws ActionException, TException {
			return super.ExitTeachMode(action);
		}

		
	}
	
}
