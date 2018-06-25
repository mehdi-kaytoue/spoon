/**
 * Copyright (C) 2006-2018 INRIA and contributors
 * Spoon - http://spoon.gforge.inria.fr/
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package spoon.support;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import spoon.Launcher;
import spoon.reflect.ModelStreamer;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Filter;

/**
 * This class provides a regular Java serialization-based implementation of the
 * model streamer.
 */
public class SerializationModelStreamer implements ModelStreamer {

	/**
	 * Default constructor.
	 */
	public SerializationModelStreamer() {
	}

	public void save(Factory f, OutputStream out) throws IOException {
		if (f.getEnvironment().getSerializationType() == SerializationType.STANDARD_GZIP) {
			out = new GZIPOutputStream(out);
		}
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		oos.writeObject(f);
		oos.flush();
		oos.close();
	}

	public Factory load(InputStream in) throws IOException {
		try {
			try {
				in = new GZIPInputStream(in);
			} catch (ZipException e) {
				Launcher.LOGGER.error("Given stream is not a GZIP or is corrupted. " + e.getMessage(), e);
			}
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
			final Factory f = (Factory) ois.readObject();
			//create query using factory directly
			//because any try to call CtElement#map or CtElement#filterChildren will fail on uninitialized factory
			f.createQuery(f.Module().getAllModules().toArray()).filterChildren(new Filter<CtElement>() {
				@Override
				public boolean matches(CtElement e) {
					e.setFactory(f);
					return false;
				}
			}).list();
			ois.close();
			return f;
		} catch (ClassNotFoundException e) {
			Launcher.LOGGER.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

}
