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
package org.apache.stanbol.enhancer.engines.uima;

import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_RELATION;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_END;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTED_TEXT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_START;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.ontologies.ENTITY;
import org.apache.clerezza.uima.utils.UIMAExecutor;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.clerezza.uima.utils.exception.FeatureStructureNotFoundException;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.stanbol.enhancer.engines.uima.api.StanbolAnalysisEngine;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @scr.component immediate="true"
 * @scr.service
 * @scr.reference name="StanbolAnalysisEngine"
 *                interface="org.apache.stanbol.enhancer.engines.uima.api.StanbolAnalysisEngine"
 *                cardinality="0..n" policy="dynamic"
 *
 */
public class UIMAEngine implements EnhancementEngine, ServiceProperties {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final Integer defaultOrder = ServiceProperties.ORDERING_EXTRACTION_ENHANCEMENT;

	/**
	 * This contains the only supported MIME type of this enhancement engine.
	 */
	private static final String TEXT_PLAIN_MIMETYPE = "text/plain";
	
	public LiteralFactory literalFactory;
	
	@Reference
	Serializer ser;
	
	//Reference // 0..1, dynamic
	//private StanbolAnalysisEngine sae;
	private List<StanbolAnalysisEngine> saeList;
	
	@Activate
	protected void activate(ComponentContext context) throws ConfigurationException {
		//init the LiteralFactory
        literalFactory = LiteralFactory.getInstance();
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		
	}
	
	@Override
	public Map<String, Object> getServiceProperties() {
		return Collections.unmodifiableMap(Collections.singletonMap(
				ServiceProperties.ENHANCEMENT_ENGINE_ORDERING,
				(Object) defaultOrder));
	}

	@Override
	// enhance if plain text
	public int canEnhance(ContentItem ci) throws EngineException {
		String mimeType = ci.getMimeType().split(";", 2)[0];
		System.out.println("Dans le test can enhance");
		if(saeList == null || saeList.size() == 0){
			System.out.println("sae est NULL");
			return CANNOT_ENHANCE;
		}
		else if (TEXT_PLAIN_MIMETYPE.equalsIgnoreCase(mimeType)) {
			System.out.println("FAIT ENHANCE");
			return ENHANCE_SYNCHRONOUS;
		}
		return CANNOT_ENHANCE;
	}
	
	public void bindStanbolAnalysisEngine(StanbolAnalysisEngine e) {
		if (saeList == null){
			saeList = new ArrayList<StanbolAnalysisEngine>();
		}
		System.out.println("Bind service : " + e.getClass().getName());
		saeList.add(e);
    }

    public void unbindStanbolAnalysisEngine(StanbolAnalysisEngine e) {
    	System.out.println("Unbind service : " + e.getClass().getName());
        saeList.remove(e);
    }
	

	@Override
	public void computeEnhancements(ContentItem ci) throws EngineException {
		System.out.println("début enhancement");
		String document = null;
		try {
			document = IOUtils.toString(ci.getStream());
		} catch (IOException e) {
			throw new EngineException(e);
		}
		
		Iterator<StanbolAnalysisEngine> saeIter = saeList.iterator();
		while(saeIter.hasNext()){
			StanbolAnalysisEngine sae = saeIter.next();
			
			GraphNode UIMAGraphNode = getUIMAGraphNode(sae, document);
		    //for output the "pure" uimagraphnode
			//ci.getMetadata().addAll(UIMAGraphNode.getGraph()); 
		    UIMAtoStanbolGraph(UIMAGraphNode, ci);
		    
		    //create entities from annotations
		    getUIMAEntities(ci,sae);
		}
		
	}
	
	private void getUIMAEntities(ContentItem ci, StanbolAnalysisEngine sae) {
		MGraph ciGraph = ci.getMetadata();
		Map<String, Map<UriRef, Resource>> em = sae.getEntityMap();
		Iterator<Entry<String, Map<UriRef, Resource>>> iter = em.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<String, Map<UriRef, Resource>> mappingEntry = iter.next();
			String selectedType = mappingEntry.getKey();
			Map<UriRef, Resource> mapping = mappingEntry.getValue();

			Map<String, Related> relatedCache = getRelatedCache(ciGraph, selectedType);

			// /create entities
			for (Related rel : relatedCache.values()) {
				createUIMAEntities(rel, mapping,ci);
			}
		}
	}

	private void createUIMAEntities(Related rel, Map<UriRef, Resource> mapping, ContentItem ci) {
		MGraph ciGraph = ci.getMetadata();
		
		// get all related of this element
		Collection<UriRef> related = rel.getRelatedList();
		
		// Now create the entityAnnotation
		UriRef entityAnnotation = EnhancementEngineHelper.createEntityEnhancement(ciGraph, this, new UriRef(ci.getId()));

		// creation de l'enhancement de l'entity
		Set<Entry<UriRef, Resource>> mIter = mapping.entrySet();
		for (Entry<UriRef, Resource> e : mIter) {
			UriRef p = e.getKey();
			Resource o = e.getValue();
			if (o == null) {
				o = new PlainLiteralImpl(rel.getSearchString());
			}
			ciGraph.add(new TripleImpl(entityAnnotation, p, o));
		}

		// first relate this entity annotation to the text
		// annotation(s)
		for (NonLiteral enhancement : related) {
			ciGraph.add(new TripleImpl(entityAnnotation, DC_RELATION, enhancement));
		}
	}

	private Map<String, Related> getRelatedCache(MGraph ciGraph, String selectedType) {
		Map<String, Related> relatedCache = new TreeMap<String, Related>();

    	Iterator<Triple> uimaTriples = ciGraph.filter(null, ENTITY.uimaType, literalFactory.createTypedLiteral(selectedType));
	
    	while (uimaTriples.hasNext()) {
    		Triple t = uimaTriples.next();
    		
    		Iterator<Triple> uimaTextAnnotations = ciGraph.filter(t.getSubject(), ENHANCER_SELECTED_TEXT, null);
    		while (uimaTextAnnotations.hasNext()){
    			Triple textAnnot = uimaTextAnnotations.next();
	    		String text = ((Literal)textAnnot.getObject()).getLexicalForm();
	    		if (relatedCache.containsKey(text)){
	    			relatedCache.get(text).addLinked((UriRef)textAnnot.getSubject());
	    		}
	    		else {
	    			relatedCache.put(text, new Related(text, (UriRef)textAnnot.getSubject()));
	    		}
    		}
    		
    	}
		return relatedCache;
	}

	public GraphNode getUIMAGraphNode(StanbolAnalysisEngine sae, String document) throws EngineException {
		
        UIMAExecutor uimaExecutor = sae.getExecutor();
        
		// Execute the analysis and get the resulting JCas
		JCas jcas = null;
		try {
			jcas = uimaExecutor.analyzeDocument(document);
		} catch (AnalysisEngineProcessException aepe) {
			throw new EngineException(aepe);
		} catch(ResourceInitializationException rie){
			throw new EngineException(rie);
		}
		
		
		List<FeatureStructure> fslist = new ArrayList<FeatureStructure>();
		try {
			List<FeatureStructure> fslistAll = null;
			//get all fs for all types
			fslistAll = UIMAUtils.getAllFSofType(TOP.type, jcas);
			//fslistAll = UIMAUtils.getAllFSofType(sae.getUIMAType(), jcas);
			
			List<String> keepedTypes = sae.getUIMATypes();
			if (keepedTypes != null){
				for (FeatureStructure uimaObj : fslistAll){
					if (keepedTypes.contains(uimaObj.getType().getName())){
						fslist.add(uimaObj);
					}
				}	
			}
			else{
				fslist = fslistAll;
			}
			
		} catch (FeatureStructureNotFoundException fsnfe) {
			throw new EngineException(fsnfe);
		}
		
		
		MGraph mGraph = new SimpleMGraph();
		GraphNode graphNode = new GraphNode(new UriRef("MGraph2GraphNode"),
				mGraph);

		UIMAUtils.enhanceNode(graphNode, fslist);

		return graphNode;
	}
	
	private Map<String, UriRef> featureTransposeMap = new HashMap<String, UriRef>(){
		private static final long serialVersionUID = 5871293199246400958L;

		{
			put("uima.tcas.Annotation:begin", ENHANCER_START); 
			put("uima.tcas.Annotation:end", ENHANCER_END);
		}
	};
		
	public void UIMAtoStanbolGraph(GraphNode UIMAGraphNode, ContentItem stanbolCI) {

		Iterator<GraphNode> annotations = UIMAGraphNode.getObjectNodes(ENTITY.contains);
		while (annotations.hasNext()) {
			UriRef stanbolAnnot = EnhancementEngineHelper.createTextEnhancement(stanbolCI, this);
			
			GraphNode graphNode = annotations.next();
			
			Literal coveredText = graphNode.getLiterals(ENTITY.coveredText).next();
			stanbolCI.getMetadata().add(new TripleImpl(stanbolAnnot, ENHANCER_SELECTED_TEXT, coveredText));
			
			//transfert the uima type as it's help for create entities after
			Literal mainFeatureName = graphNode.getLiterals(ENTITY.uimaType).next();
			stanbolCI.getMetadata().add(new TripleImpl(stanbolAnnot, ENTITY.uimaType, mainFeatureName));
			
			Iterator<GraphNode> features = graphNode.getObjectNodes(ENTITY.hasFeature);
			while (features.hasNext()) {
				GraphNode feature = features.next();
				String featureName = feature.getLiterals(ENTITY.featureName).next().getLexicalForm();
				
				if (featureTransposeMap.containsKey(featureName)) {
					Literal featureValue = feature.getLiterals(ENTITY.featureValue).next(); 
					stanbolCI.getMetadata().add(new TripleImpl(stanbolAnnot, featureTransposeMap.get(featureName), featureValue));
				}
			}
		}
	}
	
}
