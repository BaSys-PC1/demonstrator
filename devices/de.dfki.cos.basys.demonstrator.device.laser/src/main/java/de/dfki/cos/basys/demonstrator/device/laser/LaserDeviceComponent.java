package de.dfki.cos.basys.demonstrator.device.laser;

import java.util.concurrent.CountDownLatch;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.dfki.cos.basys.demonstrator.device.laser.action.Projection;
import de.dfki.cos.basys.demonstrator.device.laser.action.StopProjection;
import de.dfki.cos.basys.demonstrator.device.laser.projectionentities.PEMovingArrows;
import de.dfki.cos.basys.demonstrator.device.laser.projectionentities.PEMovingETA;
import de.dfki.cos.basys.platform.model.data.CartesianCoordinate;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityPackage;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectETA;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectPath;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.CapabilityVariant;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.packml.UnitConfiguration;
import de.dfki.cos.basys.platform.runtime.component.device.rest.RestDeviceComponent;

public class LaserDeviceComponent extends RestDeviceComponent {

	CountDownLatch counter;

	public LaserDeviceComponent(ComponentConfiguration config) {
		super(config);
		resetOnStopped = true;
	}

	@Override
	protected UnitConfiguration translateCapabilityRequest(CapabilityRequest req) {
		
		// in einem CapabilityRequest steckt nun eine CapabilityVariant. Diese enthält
		// wiederum eine Capability (dp, 22.05.2018)
		CapabilityVariant<?, ?> c = req.getCapabilityVariant();

		Projection p;
		if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getProjectPath())) {
			p = createPathProjection((ProjectPath) c.getCapability());
		} else if (c.getCapability().eClass().equals(CapabilityPackage.eINSTANCE.getProjectETA())) {
			p = createEtaProjection((ProjectETA) c.getCapability());
		} else {
			p = null;
		}

		UnitConfiguration uc = new UnitConfiguration();
		uc.setPayload(p);
		return uc;

	}

	private Projection createPathProjection(ProjectPath cap) {
		Projection p = new Projection();

		p.addEntity(new PEMovingArrows(cap.getPath().getCoordinates(), cap.getArrowCount(), cap.getDelay(),
				EProjectionColor.getColorByIndex(cap.getColor())));

		return p;
	}

	private Projection createEtaProjection(ProjectETA cap) {
		Projection p = new Projection();

		CartesianCoordinate center = cap.getPosition();
		p.addEntity(new PEMovingETA(center.getX(), center.getY(), center.getZ(), cap.getRadius(), cap.getOrientation(),
				30000, cap.getEta(), EProjectionColor.getColorByIndex(cap.getColor())));

		return p;
	}

	@Override
	public void onResetting() {

		counter = new CountDownLatch(1);
	}

	@Override
	public void onStarting() {

		Projection p = (Projection) (getUnitConfig().getPayload());

		LOGGER.info("call rest client");
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		
		try {
			String json = ow.writeValueAsString(p);
			endpoint.request(MediaType.APPLICATION_JSON).put(Entity.entity(json, MediaType.APPLICATION_JSON));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onExecute() {
		try {
			counter.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStopping() {
		counter.countDown();

		LOGGER.info("call rest client");
		endpoint.request(MediaType.APPLICATION_JSON).put(Entity.entity(new StopProjection(), MediaType.APPLICATION_JSON));
	}
}
