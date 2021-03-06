package de.dfki.cos.basys.demonstrator.device.laser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.dfki.cos.basys.common.emf.json.JsonUtils;
import de.dfki.cos.basys.demonstrator.device.laser.LaserDeviceComponent;
import de.dfki.cos.basys.platform.model.data.CartesianCoordinate;
import de.dfki.cos.basys.platform.model.data.DataFactory;
import de.dfki.cos.basys.platform.model.data.Path;
import de.dfki.cos.basys.platform.model.data.impl.CartesianCoordinateImpl;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityFactory;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectETA;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectPath;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.GeneralCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstanceFactory;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentCategory;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentFactory;
import de.dfki.cos.basys.platform.model.runtime.component.State;
import de.dfki.cos.basys.platform.model.runtime.component.impl.ComponentConfigurationImpl;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;

public class LaserDeviceComponentTest {

	ComponentConfiguration componentConfig;
	ComponentContext emptyContext = new ComponentContext.Builder().build();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		componentConfig = new ComponentConfigurationImpl.Builder()
				.componentId("laser-component")
				.componentName("laser-component")
				.inChannelName("laser-component#in")
				.outChannelName("laser-component#out")
				.componentCategory(ComponentCategory.DEVICE_COMPONENT)
				.externalConnectionString("http://10.2.0.70:9000/laserControl").build();
	}

	@After
	public void tearDown() throws Exception {
	}

	private void sleep(long seconds) {
		try {
			TimeUnit.MILLISECONDS.sleep(seconds*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	@Ignore
	public void testConnection() throws ComponentException {
		
		LaserDeviceComponent component = new LaserDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
	@Test
	@Ignore
	public void testPathProjection() throws ComponentException {
		LaserDeviceComponent component = new LaserDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		sleep(1); // für reset()
		
		CartesianCoordinate c1 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.0).z(0.0).build();
		CartesianCoordinate c2 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.1).z(0.0).build();
		CartesianCoordinate c3 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.2).z(0.0).build();
		CartesianCoordinate c4 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.3).z(0.0).build();
		CartesianCoordinate c5 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.4).z(0.0).build();
		CartesianCoordinate c6 = new CartesianCoordinateImpl.Builder().x(1.0).y(1.5).z(0.0).build();
		
		Path path = DataFactory.eINSTANCE.createPath();
		path.getCoordinates().add(c1);
		path.getCoordinates().add(c2);
		path.getCoordinates().add(c3);
		path.getCoordinates().add(c4);
		path.getCoordinates().add(c5);
		path.getCoordinates().add(c6);
		
		ProjectPath capability = CapabilityFactory.eINSTANCE.createProjectPath();
		capability.setPath(path);
		capability.setDelay(1000);
		capability.setArrowCount(3);
		capability.setEta(60*1000);
		capability.setColor(0);
		
		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();		
		variant.setCapability(capability);
		
		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(componentConfig.getComponentId());
		
		try {
			String payload = JsonUtils.toString(req);
			Request message = CommFactory.getInstance().createRequest(payload);
			component.handleRequest(null, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		sleep(10);
				
		assertEquals(State.EXECUTE, component.getState());
		
		component.stop();
		
		sleep(3);
		
		assertEquals(State.IDLE, component.getState());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
	@Test
	@Ignore
	public void testEtaProjection() throws ComponentException {
		LaserDeviceComponent component = new LaserDeviceComponent(componentConfig);
		assertTrue(!component.isConnectedToExternal());

		component.activate(emptyContext);
		assertTrue(component.isConnectedToExternal());
		
		sleep(1); // für reset()
		
		CartesianCoordinate position = new CartesianCoordinateImpl.Builder().x(1.0).y(1.0).z(0.0).build();		
		
		ProjectETA capability = CapabilityFactory.eINSTANCE.createProjectETA();
		capability.setEta(30*1000); // Millisekunden
		capability.setPosition(position);
		capability.setRadius(0.25); // Meter
		capability.setColor(0);
		
		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();		
		variant.setCapability(capability);
		
		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(componentConfig.getComponentId());
		
		try {
			String payload = JsonUtils.toString(req);
			Request message = CommFactory.getInstance().createRequest(payload);
			component.handleRequest(null, message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		sleep(3);
				
		assertEquals(State.EXECUTE, component.getState());
		
		component.stop();
		
		sleep(3);
		
		assertEquals(State.IDLE, component.getState());
		
		component.deactivate();		
		assertTrue(!component.isConnectedToExternal());
	}
	
}
