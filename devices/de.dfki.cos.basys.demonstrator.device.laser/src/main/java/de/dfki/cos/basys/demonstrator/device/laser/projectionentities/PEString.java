package de.dfki.cos.basys.demonstrator.device.laser.projectionentities;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.dfki.cos.basys.demonstrator.device.laser.EProjectionColor;

public class PEString extends ProjectionEntity {

	@JsonProperty
	private String text;

	@JsonProperty
	private double height;

	public PEString(double x, double y, double z, String text, double height, EProjectionColor color) {
		super(x, y, z, color);
		type = "string";
		this.text = text;
		this.height = height;
	}

}
