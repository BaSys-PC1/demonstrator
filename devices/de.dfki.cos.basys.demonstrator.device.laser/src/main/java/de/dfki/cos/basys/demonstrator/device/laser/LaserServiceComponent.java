package de.dfki.cos.basys.demonstrator.device.laser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;

import de.dfki.cos.basys.common.emf.json.JsonUtils;
import de.dfki.cos.basys.platform.model.data.CartesianCoordinate;
import de.dfki.cos.basys.platform.model.data.DataPackage;
import de.dfki.cos.basys.platform.model.data.Path;
import de.dfki.cos.basys.platform.model.data.impl.CartesianCoordinateImpl;
import de.dfki.cos.basys.platform.model.data.impl.DataFactoryImpl;
import de.dfki.cos.basys.platform.model.domain.capability.CapabilityFactory;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectETA;
import de.dfki.cos.basys.platform.model.domain.capability.ProjectPath;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.GeneralCapabilityVariant;
import de.dfki.cos.basys.platform.model.domain.resourceinstance.ResourceinstanceFactory;
import de.dfki.cos.basys.platform.model.domain.topology.AGVStation;
import de.dfki.cos.basys.platform.model.domain.topology.TopologyElement;
import de.dfki.cos.basys.platform.model.domain.topology.TopologyPackage;
import de.dfki.cos.basys.platform.model.runtime.communication.Channel;
import de.dfki.cos.basys.platform.model.runtime.communication.ChannelListener;
import de.dfki.cos.basys.platform.model.runtime.communication.Notification;
import de.dfki.cos.basys.platform.model.runtime.communication.Request;
import de.dfki.cos.basys.platform.model.runtime.communication.Response;
import de.dfki.cos.basys.platform.model.runtime.component.CapabilityRequest;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentFactory;
import de.dfki.cos.basys.platform.model.runtime.component.Property;
import de.dfki.cos.basys.platform.runtime.communication.CommFactory;
import de.dfki.cos.basys.platform.runtime.component.Component;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;
import de.dfki.cos.basys.platform.runtime.component.service.DeviceControllerServiceComponent;

public class LaserServiceComponent extends DeviceControllerServiceComponent {

	Channel mirOut;
	ExecutorService executor;
	List<CartesianCoordinate> mPath = new ArrayList<>();
	private long mVisualizeETACircleUntil;
	private TopologyElement mCurrentTargetPosition;
	protected boolean mWaitingForNewPath;
	protected long mVisualizeBeginningUntil;
	protected boolean mETANotYetVisualized;
	protected boolean mFinalPathNotYetVisualized;
	// private String mTargetString;

	private int mETA = 61;
	protected boolean mBeginningVisualizationRunning;
	protected boolean mETAVisualizationRunning;
	protected boolean mFinalPathVisualizationRunning;
	protected long mVisualizeFinalPathUntil;

	public LaserServiceComponent(ComponentConfiguration config) {
		super(config);
	}

	@Override
	public void connectToExternal() throws ComponentException {
		// TODO Auto-generated method stub
		super.connectToExternal();
		device.reset();
	}
	
	private synchronized void stopDevice() {
		LOGGER.info("Device State is " + device.getState());
		device.stop();
		try {
			TimeUnit.MILLISECONDS.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		if (device.getState() != State.IDLE) {
//			device.stop();
//
//			while (device.getState() != State.IDLE) {
//				try {
//					Thread.sleep(200);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

	private static double getDistance(CartesianCoordinate cc1, CartesianCoordinate cc2) {
		return Math.sqrt(Math.pow((cc2.getX() - cc1.getX()), 2) + Math.pow((cc2.getY() - cc1.getY()), 2)
				+ Math.pow((cc2.getZ() - cc1.getZ()), 2));
	}

	private static double getLength(CartesianCoordinate cc) {
		return Math.sqrt(Math.pow(cc.getX(), 2) + Math.pow(cc.getY(), 2) + Math.pow(cc.getZ(), 2));
	}

	private static CartesianCoordinate getDirectionVector(CartesianCoordinate cc1, CartesianCoordinate cc2) {

		return new CartesianCoordinateImpl.Builder().x(cc2.getX() - cc1.getX()).y(cc2.getY() - cc1.getY())
				.z(cc2.getZ() - cc1.getZ()).build();
	}

	private static CartesianCoordinate norm(CartesianCoordinate cc) {
		double length = getLength(cc);
		return new CartesianCoordinateImpl.Builder().x(cc.getX() / length).y(cc.getY() / length).z(cc.getZ() / length)
				.build();
	}

	private static CartesianCoordinate scalarProduct(CartesianCoordinate cc, double scalar) {
		return new CartesianCoordinateImpl.Builder().x(cc.getX() * scalar).y(cc.getY() * scalar).z(cc.getZ() * scalar)
				.build();
	}

	private static CartesianCoordinate add(CartesianCoordinate cc1, CartesianCoordinate cc2) {
		return new CartesianCoordinateImpl.Builder().x(cc1.getX() + cc2.getX()).y(cc1.getY() + cc2.getY())
				.z(cc1.getZ() + cc2.getZ()).build();
	}

	private static List<CartesianCoordinate> beautifyPath(List<CartesianCoordinate> coords, double pathLengthToCover,
			double interArrowDistance) {
		if (coords == null) {
			return new ArrayList<>();
		}
		if (coords.size() < 2) {
			return coords;
		}
		List<CartesianCoordinate> result = new ArrayList<CartesianCoordinate>();
		result.add(coords.get(0));

		double currentDesiredDistance = interArrowDistance;

		for (int x = 1; x < coords.size(); x++) {

			CartesianCoordinate cc1 = coords.get(x - 1);
			CartesianCoordinate cc2 = coords.get(x);

			double remainingSegmentLength = getDistance(cc1, cc2);
			CartesianCoordinate normedSegmentDirectionVector = norm(getDirectionVector(cc1, cc2));

			CartesianCoordinate baseCoordinate = cc1;

			while (remainingSegmentLength > currentDesiredDistance && pathLengthToCover > 0) {

				baseCoordinate = add(baseCoordinate,
						scalarProduct(normedSegmentDirectionVector, currentDesiredDistance));
				result.add(baseCoordinate);
				remainingSegmentLength -= currentDesiredDistance;
				pathLengthToCover -= interArrowDistance;
				currentDesiredDistance = interArrowDistance;
			}
			currentDesiredDistance -= remainingSegmentLength;
		}
		return result;
	}

	private boolean coordEquals(CartesianCoordinate cc1, CartesianCoordinate cc2) {

		double epsilon = 0.00001;
		return doubleEquals(cc1.getX(), cc2.getX(), epsilon) && doubleEquals(cc1.getY(), cc2.getY(), epsilon)
				&& doubleEquals(cc1.getZ(), cc2.getZ(), epsilon);
	}

	private boolean doubleEquals(double d1, double d2, double epsilon) {
		return Math.abs(d1 - d2) < epsilon;
	}

	private List<CartesianCoordinate> getPathEnd(List<CartesianCoordinate> coords, double lengthToCover) {

		List<CartesianCoordinate> result = new ArrayList<CartesianCoordinate>();
		if (coords == null) {
			return result;
		}

		CartesianCoordinate baseCoordinate = coords.get(coords.size() - 1);
		result.add(baseCoordinate);

		for (int i = coords.size() - 2; i >= 0; i--) {
			CartesianCoordinate cc2 = coords.get(i);
			double dist = getDistance(baseCoordinate, cc2);
			result.add(0, cc2);
			baseCoordinate = cc2;
			lengthToCover -= dist;
			if (lengthToCover <= 0) {
				break;
			}
		}
		return result;

	}

	private void visualizeETA() {
		CartesianCoordinate targetPos = new DataFactoryImpl().createCartesianCoordinate();
		int orientation = 0;
		switch (mCurrentTargetPosition.getName()) {

		case "Station-QA":
			targetPos.setX(-1.75);
			targetPos.setY(-.5);
			targetPos.setZ(0);
			orientation = 90;
			break;
		case "Station-Festo":
			targetPos.setX(2);
			targetPos.setY(-3.25);
			targetPos.setZ(0);
			orientation = 270;
			break;
		case "Station-BaSys":
			targetPos.setX(2);
			targetPos.setY(0);
			targetPos.setZ(0);
			orientation = 90;
			break;
		case "Station-Cola":
			targetPos.setX(0.15);
			targetPos.setY(-2.75);
			targetPos.setZ(0);
			orientation = 270;
			break;
		case "Station-Wait":
			targetPos.setX(1.35);
			targetPos.setY(0.25);
			targetPos.setZ(0);
			orientation = 90;
			break;
		case "Station-TeachIn":
			targetPos.setX(0.15);
			targetPos.setY(-2.75);
			targetPos.setZ(0);
			orientation = 270;
			break;
		}

		ProjectETA capability = CapabilityFactory.eINSTANCE.createProjectETA();

		
		capability.setEta(22500);
		capability.setPosition(targetPos);
		capability.setRadius(.1);
		capability.setOrientation(orientation);
		capability.setColor(0);
		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();
		variant.setCapability(capability);

		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(device.getComponentInfo().getComponentId());

		device.sendComponentRequest(req);

	}

	@Override
	public void activate(ComponentContext context) throws ComponentException {
		super.activate(context);

		// Thread t2 = new Thread(new Runnable() {
		// @Override
		// public void run() {
		//
		// JFrame meinFrame = new JFrame("Beispiel JFrame");
		// meinFrame.setSize(200, 200);
		// JTextField edt = new JTextField();
		// JButton btn = new JButton("Beispiel");
		// btn.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		//
		// mETA = Integer.parseInt(edt.getText());
		// }
		// });
		//
		// JButton btn2 = new JButton("Center");
		// btn2.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		//
		// mTargetString = "Center";
		// mWaitingForNewPath = true;
		// mVisualizeBeginningUntil = System.currentTimeMillis() + 10000;
		// mETANotYetVisualized = true;
		// mFinalPathNotYetVisualized = true;
		// }
		// });
		// JButton btn3 = new JButton("Station");
		// btn3.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		//
		// mTargetString = "Station";
		// mWaitingForNewPath = true;
		// mVisualizeBeginningUntil = System.currentTimeMillis() + 10000;
		// mETANotYetVisualized = true;
		// mFinalPathNotYetVisualized = true;
		// }
		// });
		//
		// meinFrame.setLayout(new BorderLayout());
		// meinFrame.add(edt, BorderLayout.NORTH);
		// meinFrame.add(btn, BorderLayout.SOUTH);
		// meinFrame.add(btn2, BorderLayout.WEST);
		// meinFrame.add(btn3, BorderLayout.EAST);
		//
		// meinFrame.setVisible(true);
		// }
		// });
		// t2.start();

		executor = Executors.newCachedThreadPool();

		mirOut = CommFactory.getInstance().openChannel(context.getSharedChannelPool(), "_rUJzsDJhEei1p5hKOf5Slw#out",
				false, new ChannelListener() {

					@Override
					public Response handleRequest(Channel channel, Request req) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void handleNotification(Channel channel, Notification not) {
						try {
							EObject payload = JsonUtils.fromString(not.getPayload(), EObject.class);

							if (payload.eClass().equals(TopologyPackage.eINSTANCE.getAGVStation())) {
								AGVStation station = (AGVStation)payload;
								
								LOGGER.info("Neue Position: " + station.getName());
								
								// MIR should go somewhere
								mWaitingForNewPath = true;
								mVisualizeBeginningUntil = System.currentTimeMillis() + 4500;
								mCurrentTargetPosition = (TopologyElement) payload;
								mETANotYetVisualized = true;
								mFinalPathNotYetVisualized = true;
							}
							if (payload.eClass().equals(DataPackage.eINSTANCE.getPath())) {
								

								LOGGER.info("Path received");
								
								CompletableFuture<Boolean> cf = new CompletableFuture<Boolean>();

								executor.submit(() -> {
									
									// if
									// (payload.eClass().equals(DataPackage.eINSTANCE.getRobotPositionInformation()))
									// {
									//
									// RobotPositionInformation MIRPosition = (RobotPositionInformation) payload;
									// CartesianCoordinate MIRPos = MIRPosition.getPosition();
									// MIRPos.setZ(0);
									//
									// }

									if (mWaitingForNewPath) {
										mWaitingForNewPath = false;

										stopDevice();
										Path epath = (Path) payload;
										mPath.clear();
										for (CartesianCoordinate cc : epath.getCoordinates()) {
											cc.setZ(0);
											mPath.add(cc);
										}
										visualizePath(mPath, true);
										mBeginningVisualizationRunning = true;

									} else {
										Path epath = (Path) payload;
										mPath.clear();
										for (CartesianCoordinate cc : epath.getCoordinates()) {
											cc.setZ(0);
											mPath.add(cc);
										}
									}

									cf.complete(true);
									return null;
								});
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					@Override
					public void handleMessage(Channel channel, String msg) {
						// TODO Auto-generated method stub
					}

				});

		Thread runner = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {
					try {
					Component mir =  context.getComponentManager()
							.getLocalComponentById("_rUJzsDJhEei1p5hKOf5Slw");
					Property etaProperty =mir.getConfig().getProperty("estimatedETA");

					if (etaProperty != null) {
						mETA = Integer.parseInt(etaProperty.getValue());
					}

					if (mETA < 30000 && mETA > 15000 && mETANotYetVisualized) {
						mETANotYetVisualized = false;
						visualizeETA();
						mETAVisualizationRunning = true;
						mVisualizeETACircleUntil = System.currentTimeMillis() + 15000;

					}

					if (mETA < 15000 && mFinalPathNotYetVisualized) {
						mFinalPathNotYetVisualized = false;
						visualizePath(getPathEnd(mPath, .4));
						mFinalPathVisualizationRunning = true;
						mVisualizeFinalPathUntil = System.currentTimeMillis() + 5000;
					}

					if (mBeginningVisualizationRunning && System.currentTimeMillis() >= mVisualizeBeginningUntil) {
						mBeginningVisualizationRunning = false;
						stopDevice();
					}
					if (mETAVisualizationRunning && System.currentTimeMillis() >= mVisualizeETACircleUntil) {
						mETAVisualizationRunning = false;
						stopDevice();

					}
					if (mFinalPathVisualizationRunning && System.currentTimeMillis() >= mVisualizeFinalPathUntil) {
						mFinalPathVisualizationRunning = false;
						stopDevice();

					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		});
		runner.start();

	}

	private void visualizePath(List<CartesianCoordinate> input) {
		visualizePath(input, false);

	}

	private void visualizePath(List<CartesianCoordinate> input, boolean cutStart) {
		Path path = new DataFactoryImpl().createPath();
		List<CartesianCoordinate> beautifiedPath = beautifyPath(input, 0.8, 0.05);
		if (cutStart) {
			path.getCoordinates().addAll(beautifiedPath.subList(8, beautifiedPath.size()));
		} else {
			path.getCoordinates().addAll(beautifiedPath);
		}
		ProjectPath capability = CapabilityFactory.eINSTANCE.createProjectPath();
		capability.setPath(path);
		capability.setDelay(200);
		capability.setArrowCount(3);
		capability.setColor(0);

		GeneralCapabilityVariant variant = ResourceinstanceFactory.eINSTANCE.createGeneralCapabilityVariant();
		variant.setCapability(capability);

		CapabilityRequest req = ComponentFactory.eINSTANCE.createCapabilityRequest();
		req.setCapabilityVariant(variant);
		req.setComponentId(device.getComponentInfo().getComponentId());

		device.sendComponentRequest(req);
	}

	@Override
	public void deactivate() throws ComponentException {
		// TODO Auto-generated method stub
		super.deactivate();

		try {
			System.out.println("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("tasks interrupted");
		} finally {
			if (!executor.isTerminated()) {
				System.err.println("cancel non-finished tasks");
			}
			executor.shutdownNow();
			System.out.println("shutdown finished");
		}
	}

	@Override
	public void handleNotification(Channel channel, Notification not) {

	}

	@Override
	public Response handleRequest(Channel channel, Request req) {
		return super.handleRequest(channel, req);
	}

}
