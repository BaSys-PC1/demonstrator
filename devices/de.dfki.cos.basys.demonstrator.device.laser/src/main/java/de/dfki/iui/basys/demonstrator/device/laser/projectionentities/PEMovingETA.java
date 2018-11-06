package de.dfki.iui.basys.demonstrator.device.laser.projectionentities;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.dfki.iui.basys.demonstrator.device.laser.EProjectionColor;

public class PEMovingETA extends ProjectionEntity {

	@JsonProperty
	private double radius;

	@JsonProperty
	private int angle;

	@JsonProperty
	private long fullTime;

	@JsonProperty
	private long startTime;

	public PEMovingETA(double x, double y, double z, double radius, int angle, long fullTime, long startTime,
			EProjectionColor color) {
		super(x, y, z, color);
		type = "movingETA";
		this.radius = radius;
		this.angle = angle;
		this.fullTime = fullTime;
		this.startTime = startTime;

	}

}
