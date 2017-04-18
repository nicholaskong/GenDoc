/*****************************************************************************
 * (c) Copyright 2016 Telefonaktiebolaget LM Ericsson
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Antonio Campesino (Ericsson) antonio.campesino.robles@ericsson.com - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.gendoc.document.parser.documents.helper;

public class UnitsHelper {	
	public static final double DXA_TO_POINTS =  1.0d / 20.0d; 
	public static final double POINTS_TO_DXA =  20.0d; 

	public static final double INCHES_TO_POINTS =  72.0d; 
	public static final double POINTS_TO_INCHES =  1.0d / INCHES_TO_POINTS; 
	
	public static final double CM_TO_POINTS =  2.54d * INCHES_TO_POINTS; 
	public static final double POINTS_TO_CM =  1.0d / CM_TO_POINTS; 

	public static final double POINTS_TO_EMU =  12700.0 ; 
	public static final double EMU_TO_POINTS =  1.0 / POINTS_TO_EMU; 


	public enum Unit {
		DXA(DXA_TO_POINTS),
		INCH(INCHES_TO_POINTS),
		CM(CM_TO_POINTS),
		POINT(1.0d),
		EMU(EMU_TO_POINTS);
		
		private Unit(double cte) {
			this.cte = cte;
		}

		private double cte;
	}
	
	
	public static final double convert(Unit from, double val, Unit to) {
		double points = val * from.cte;
		return points / to.cte;		
	}
	
	public static final double convertToPixels(Unit from, double val, int dpi) {
		double points = (val * from.cte); 
		return points * dpi  / INCHES_TO_POINTS;	
	}	

	public static final double convertFromPixels(double val, int dpi, Unit to) {
		double points = val * INCHES_TO_POINTS / dpi;
		return points / to.cte;	
	}	
}
