package de.dfki.cos.basys.demonstrator.device.hybrit;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import de.dfki.cos.basys.demonstrator.device.hybrit.Ur10Component;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentCategory;
import de.dfki.cos.basys.platform.model.runtime.component.ComponentConfiguration;
import de.dfki.cos.basys.platform.model.runtime.component.impl.ComponentConfigurationImpl;
import de.dfki.cos.basys.platform.runtime.component.ComponentContext;
import de.dfki.cos.basys.platform.runtime.component.ComponentException;

public class Ur10TestGUI {

	public static void main(String[] args) {
		ComponentConfiguration config = new ComponentConfigurationImpl.Builder().componentId("ur10-component")
				.inChannelName("ur10-component#in").outChannelName("ur10-component#out")
				.componentImplementationJavaClass("de.dfki.iui.basys.runtime.component.device.tecs.Ur10Component")
				.componentCategory(ComponentCategory.DEVICE_COMPONENT)
				.externalConnectionString(String.format("tecs.tcp://%s:%s", "10.2.0.71", 9030)).build();
		
		Ur10Component mir100 = new Ur10Component(config);
		ComponentContext emptyContext = new ComponentContext.Builder().build();
		try {
			mir100.activate(emptyContext);
		} catch (ComponentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Frame frame = new Frame("Ur10 UI");
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {System.exit(0);}
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
		
		Panel panel = new Panel();
		panel.setLocation(0, 0);
		panel.setPreferredSize(new Dimension(800, 400));
		panel.setLayout(null);
		frame.add(panel);
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
//		Label robotStateLabel = new Label("Robot State:");
//		robotStateLabel.setLocation(10, 10);
//		robotStateLabel.setSize(260, 36);
//		robotStateLabel.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(robotStateLabel);
//		
//		Label robotState = new Label("NOT IMPLEMENTED");
//		robotState.setLocation(280, 10);
//		robotState.setSize(200, 36);
//		robotState.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(robotState);
//		
//		Label CommandStateLabel = new Label("Command State:");
//		CommandStateLabel.setLocation(10, 56);
//		CommandStateLabel.setSize(260, 36);
//		CommandStateLabel.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(CommandStateLabel);
//		
//		Label CommandState = new Label("NOT IMPLEMENTED");
//		CommandState.setLocation(280, 56);
//		CommandState.setSize(200, 36);
//		CommandState.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(CommandState);
		
		Button reset = new Button("RESET");
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mir100.reset();
			}
		});
		reset.setLocation(10, 120);
		reset.setSize(200, 36);
		reset.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
		panel.add(reset);
		
		Button start = new Button("START");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mir100.start();
			}
		});
		start.setLocation(220, 120);
		start.setSize(200, 36);
		start.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
		panel.add(start);
		
//		Button abort = new Button("ABORT");
//		abort.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				mir100.abort();
//			}
//		});
//		abort.setLocation(10, 166);
//		abort.setSize(200, 36);
//		abort.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(abort);
//		
//		Button stop = new Button("STOP");
//		stop.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				mir100.stop();
//			}
//		});
//		stop.setLocation(430, 120);
//		stop.setSize(200, 36);
//		stop.setFont(new Font("Serif", Font.CENTER_BASELINE, 36));
//		panel.add(stop);
	}
	
}
