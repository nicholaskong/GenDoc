/*****************************************************************************
 * Copyright (c) 2012 Atos.
 *
 *    
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Aicha Boudjelal (Atos).
 *
 *****************************************************************************/
package org.eclipse.gendoc.bundle.acceleo.commons.files;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.gendoc.documents.FileRunnable;

/**
 * Copy an input stream to a FileOutputStream, the input stream is closed by this class
 * @author aboudjel
 */
public class InputStream2FileRunnable implements FileRunnable{

	private final String fileExtension;
	private final InputStream input;

	/**
	 * Copy an input stream to a FileOutputStream, the input stream is closed by this class
	 * @param fileExtension the extension for the generated file
	 * @param input the input stream
	 */
	public InputStream2FileRunnable(String fileExtension, InputStream input) {
		this.fileExtension = fileExtension;
		this.input = input;
	}

	public void run(String resourceId, String outputResourceFolder) {
		BufferedOutputStream output = null;
		try {
			File file = new File(outputResourceFolder
					+ File.separator + resourceId + "."
					+ getFileExtension());
			file.getParentFile().mkdirs();
			output = new BufferedOutputStream(new FileOutputStream(
					file));
			int byte_;
			while ((byte_ = input.read ()) != -1){
				output.write (byte_);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public String getFileExtension() {
		return fileExtension;
	}

}
