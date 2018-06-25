package spoon.test.serializable;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import spoon.Launcher;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.Filter;
import spoon.support.SerializationModelStreamer;

public class ModelStreamerTest {

	private final String filename = "./src/test/resources/serialization/factory.ser";
	
	@Test
	public void compareModelBeforeAndAfterSerizalition() throws IOException {

		File file = new File(filename);
		file.deleteOnExit();
		
		Launcher launcher = new Launcher();
		launcher.addInputResource("./src/main/java/spoon/reflect");
		launcher.buildModel();

		Factory factory = launcher.getFactory();
		new SerializationModelStreamer().save(factory, new FileOutputStream(file));

		Factory factoryFromFile = new SerializationModelStreamer().load(new FileInputStream(file));

		Filter<CtElement> filter = new Filter<CtElement>() {
			public boolean matches(CtElement element) {
				return true;
			};
		};
		
		List<CtElement> elementsFactory = factory.getModel().getRootPackage().filterChildren(filter).list();
		List<CtElement> elementsFactoryFromFile = factoryFromFile.getModel().getRootPackage().filterChildren(filter).list();

		assertTrue("Model before & after serialization must have the same number of elements", 
				elementsFactory.size() == elementsFactoryFromFile.size());
		
		List<String> st1 = elementsFactory.stream().map(CtElement::toString).collect(Collectors.toList());
		List<String> st2 = elementsFactoryFromFile.stream().map(CtElement::toString).collect(Collectors.toList());
		Collections.sort(st1);
		Collections.sort(st2);
		
		for (int i = 0; i < st1.size(); i++) {
			assertTrue("All CtElement of the model should be striclty identical before & after serialization",
					st1.get(i).equals(st2.get(i)));
		}
	}
}
