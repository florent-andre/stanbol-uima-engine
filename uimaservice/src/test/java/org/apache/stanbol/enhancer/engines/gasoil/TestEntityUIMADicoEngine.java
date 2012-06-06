/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.enhancer.engines.gasoil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.utils.GraphNode;

import org.apache.stanbol.enhancer.engines.uima.UIMAEngine;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

import static org.junit.Assert.assertFalse;

import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;

public class TestEntityUIMADicoEngine {

	static UIMAEngine uimaDicoEngine = new UIMAEngine();

	@BeforeClass
	public static void setUpServices() throws IOException, ConfigurationException {
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		
		MockComponentContext context = new MockComponentContext(properties);
		//uimaDicoEngine.activate(context);
	}

	@AfterClass
	public static void shutdownServices() {
		//uimaDicoEngine.deactivate(null);
	}
	
	public String getDocument(){
		return "New York Paris London are cities, and there is some VALEUR PROFESSIONNELLE and ELEMENT DISSIPATIF in it. Also some centrale c.";
	}
		
	// This create a mock contentItem
	ContentItem ci = new ContentItem() {
		MGraph meta = null;
		
		@Override
		public InputStream getStream() {
			String document = getDocument();
			InputStream is = new ByteArrayInputStream(document.getBytes());
			// TODO Auto-generated method stub
			return is;
		}

		@Override
		public String getMimeType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return "Content-Item-for-UIMA-engine-Test-class";
		}

		@Override
		public MGraph getMetadata() {
			if (meta == null){
				meta = new SimpleMGraph();
			}
			// TODO Auto-generated method stub
			return meta;
		}
	};


	/*Test
	public void testgetUIMAGraphNode() {
		try {
			GraphNode UIMAGraphNode = uimaDicoEngine.getUIMAGraphNode(getDocument());

			System.out.println("======== UIMA graphNode");
			System.out.println(UIMAGraphNode.toString());
			System.out.println("======== END UIMA graphNode");

			Iterator<UriRef> iter = UIMAGraphNode.getProperties();
			while (iter.hasNext()) {
				System.out.println("======== New property");
				UriRef uriRef = iter.next();
				System.out.println(uriRef.toString());

				System.out.println("======== Objects with this property");
				Iterator<Resource> objList = UIMAGraphNode.getObjects(uriRef);
				while (objList.hasNext()) {
					Resource obj = objList.next();
					System.out.println(obj.toString());
				}

				System.out.println("======= Object count"
						+ UIMAGraphNode.countObjects(uriRef));
				System.out.println("======= subject count"
						+ UIMAGraphNode.countSubjects(uriRef));

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("=====There is an error in getUIMAGraphNode");
			System.out.println(ex.getMessage());
			assertFalse(ex.getMessage(), true);
		}

	}

	@Test
	public void testFeaturesValues() {
		try {
			GraphNode UIMAGraphNode = uimaDicoEngine.getUIMAGraphNode(getDocument());
			System.out.println("===== graphNODE size");
			System.out.println(UIMAGraphNode.getGraph().size());
			
			TripleCollection g = UIMAGraphNode.getGraph();
			
			System.out.println("===== graph representation");
			org.apache.clerezza.rdf.core.serializedform.Serializer ser = Serializer.getInstance();
			ser.serialize(System.out, g, "text/turtle");
			
			System.out.println("===== graph filter");
			Iterator<Triple> allFeatures = g
					.filter(null,
							new UriRef(
									"http://clerezza.apache.org/2010/22/uima-entities#coveredText"),
							null);
			
			System.out.println(allFeatures.toString());
			
			//Iterator<Triple> ftest = g.filter(null, new UriRef("http://clerezza.apache.org/2010/22/uima-entities#hasFeature"), null);
			//System.out.println(ftest.toString());
			
			
			System.out.println("===== begin iteration");

			// String sbjTest = "falseInit";

			while (allFeatures.hasNext()) {
				System.out.println("_____________ NEW feature ____");
				Triple trp = allFeatures.next();

				System.out.println("================= GET SUBJECT =====");
				NonLiteral sbj = trp.getSubject();
				System.out.println(sbj.toString());

				System.out.println("================= GET OBJECT =====");
				System.out.println(trp.getObject().toString());
				System.out.println(((Literal) trp.getObject()).getLexicalForm());

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("=====There is an error in getUIMAGraphNode");
			System.out.println(ex.getMessage());
			assertFalse(ex.getMessage(), true);
		}

	}
	
	@Test
	public void testUIMAtoStanbolGraphTransformation(){
		GraphNode UIMAGraphNode;
		try {
			UIMAGraphNode = uimaDicoEngine.getUIMAGraphNode(getDocument());
			System.out.println("============ Before transformation =========");
			uimaDicoEngine.UIMAtoStanbolGraph(UIMAGraphNode,ci);
			System.out.println("============ RDF représentation of the transformed graph =========");
			org.apache.clerezza.rdf.core.serializedform.Serializer ser = Serializer.getInstance();
			ser.serialize(System.out, ci.getMetadata(), "text/turtle");
		} catch (EngineException e) {
			assertFalse(e.getMessage(), true);
		}
		
		
	}*/
	
}
