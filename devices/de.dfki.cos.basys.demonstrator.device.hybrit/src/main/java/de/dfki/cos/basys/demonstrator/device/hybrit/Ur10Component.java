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

public class Ur10Component extends TecsDeviceComponent {

	private Ur10TECS client;

	public Ur10Component(ComponentConfiguration config) {
		super(config);
	}

	@Override
	public void connectToExternal() throws ComponentException {
		super.connectToExternal();
		client = new Ur10TECS(protocol);
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		UnitConfiguration config = new UnitConfiguration();

		if (req.getCapabilityVariant().eClass()
				.equals(ResourceinstancePackage.eINSTANCE.getLogisticsCapabilityVariant())) {
			LogisticsCapabilityVariant variant = (LogisticsCapabilityVariant) req.getCapabilityVariant();
			if (variant.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getPickAndPlace())) {
				PickAndPlace capability = (PickAndPlace) variant.getCapability();
				if (capability.getLoadCarrierUnit() == LoadCarrierUnitEnum.RKLT_3215) {
					if (variant.getAppliedOn().size() == 2) {
						TopologyElement from = variant.getAppliedOn().get(0);
						TopologyElement to = variant.getAppliedOn().get(1);
						if (from.getId().equals("_xHhjwV2TEeit97PGgoQOAQ" /* QA AGV STATION */)
								&& to.getId().equals("_F6qPdzB5Eei1bbwBPPZWOA" /* QA WORKCELL */)) {
							// Unload MiR (KLT)
							config.setPayload(urConstants.KNOWN_POSE_4);
						}
						if (from.getId().equals("_F6qPdzB5Eei1bbwBPPZWOA") 
								&& to.getId().equals("_xHhjwV2TEeit97PGgoQOAQ")) {
							// Load MiR (KLT)
							config.setPayload(urConstants.KNOWN_POSE_2);
						}
						if (from.getId().equals("_F6qPdzB5Eei1bbwBPPZWOA")
								&& to.getId().equals("_7yRYkmmvEei3B5AQTC2gAw")) {
							// KLT from QA to table
							config.setPayload(urConstants.KNOWN_POSE_3);
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
		public CommandResponse EnterTeachMode(String action) throws ActionException, TException {
			return super.EnterTeachMode(action);
		}

		@Override
		public CommandResponse ExitTeachMode(String action) throws ActionException, TException {
			return super.ExitTeachMode(action);
		}
	}

}
