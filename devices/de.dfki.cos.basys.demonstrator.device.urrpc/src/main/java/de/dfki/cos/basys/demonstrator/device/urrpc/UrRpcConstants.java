package de.dfki.cos.basys.demonstrator.device.urrpc;

public class UrRpcConstants {

	/*
	 * Standard routines common to all URs via XML-RPC
	 * If corresponding routine not implemented (default switch case) report back with an additional error code of "ERRORCODE_UNKNOWN_ROUTINE (-1)"
	 * If requested pose not taught report back with an additional error code of "ERRORCODE_UNKNOWN_POSE (-2)"
	 */
	
	public static final int ROUTINE_HOME_POSE = 0;
	public static final int ROUTINE_SAFE_HOME_POSE = 1;
	public static final int ROUTINE_SAFE_POSE = 2;
	public static final int ROUTINE_WAIT_FOR_WORKER = 3; 				// Airbus, but can be used in other scenarios as well

	
	 /* 
	 * Other low-level capabilities, we should think about, e.g.,
	 * Gripper stuff
	 * 10	: Open Gripper
	 * 11	: Close Gripper
	 */
	
	
	/* 
	 * Custom routines depending on the scenario starting from 100
	 * 100	: Perform raceway positioning (Airbus)
	 * 101	: Perform riveting (Broetje)
	 * 101	: Perform sealing (Broetje)
	 */
	
	public static final int ROUTINE_PERFORM_RACEWAY_POSITIONING = 101; 	// Airbus
	public static final int ROUTINE_PERFORM_RIVETING = 101;				// Broetje
	public static final int ROUTINE_PERFORM_SEALING = 102;				// Broetje
	
	
	/*
	 * Error codes, require additional XML-RPC methods get_error_code/set_error_code 
	 */
		
	public static final int ERRORCODE_OK = 0;
	public static final int ERRORCODE_UNKNOWN_ROUTINE = -1;
	public static final int ERRORCODE_UNKNOWN_POSE = -2;
	public static final int ERRORCODE_UNKNOWN_ERROR = -100;
	
}
