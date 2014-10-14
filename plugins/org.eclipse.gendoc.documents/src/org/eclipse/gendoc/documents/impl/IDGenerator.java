/*****************************************************************************
 * Copyright (c) 2013 Atos.
 * 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Aïcha Boudjelal - Aicha.boudjelal@atos.net
 * 
 *****************************************************************************/
package org.eclipse.gendoc.documents.impl;


/**
 * A very simple id generator in this form :
 * A0 to ZInteger.MAXVALUE
 * @author aboudjel
 *
 */
public class IDGenerator {

	static char[] alphabet = new char[]{'A',
	                                  'B',
	                                  'C',
	                                  'D',
	                                  'E',
	                                  'F',
	                                  'G',
	                                  'H',
	                                  'I',
	                                  'J',
	                                  'K',
	                                  'L',
	                                  'M',
	                                  'N',
	                                  'O',
	                                  'P',
	                                  'Q',
	                                  'R',
	                                  'S',
	                                  'T',
	                                  'U',
	                                  'V',
	                                  'W',
	                                  'X',
	                                  'Y',
	                                  'Z'};
	static Integer start = 1;
	static Integer rank = 0;
	public static String nextID() {
		if (rank == alphabet.length){
			rank = 0 ;
		}
		String result = alphabet[rank] + String.valueOf(start);
		start ++ ;
		if (start == Integer.MAX_VALUE){
			start = 0 ;
			rank ++;
		}
		return result;
	}
	
//	public static void main (String[] args){
//		while (true){
//			System.out.println(IDGenerator.nextID());
//		}
//	}
	
}
