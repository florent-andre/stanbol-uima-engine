package org.apache.stanbol.enhancer.engines.uima;

import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.RDF_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.uima.utils.UIMAExecutor;
import org.apache.clerezza.uima.utils.UIMAExecutorFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.engines.uima.api.StanbolAnalysisEngine;

@Component(immediate = true, metatype = true)
@Service()
public class GasoilEngine implements StanbolAnalysisEngine {

	@Override
	public UIMAExecutor getExecutor() {
		return UIMAExecutorFactory.getInstance().createUIMAExecutor(this.getClass().getClassLoader(), "configuration/AggregateAE.xml");
	}

	@Override
	public List<String> getUIMATypes() {

		List<String> r = new ArrayList<String>();
		r.add("org.apache.stanbol.enhancer.engines.gasoil.TokenAnnotation");
		r.add("org.apache.uima.EmailAddress");
		return r;
	}
	
	public Map<String,Map<UriRef,Resource>> getEntityMap(){
		
		Map<UriRef,Resource> entityprops = new HashMap<UriRef, Resource>();
		Map<String,Map<UriRef,Resource>> entityMap = new HashMap<String, Map<UriRef,Resource>>();
		
		//for the emailAddress mapping
		entityprops.put(RDF_TYPE, new UriRef("http://schema.org/ContactPoint"));
		entityprops.put(new UriRef("http://schema.org/ContactPoint#email"), null);
		
		entityMap.put("org.apache.uima.EmailAddress", entityprops);
				
		return entityMap;
	}

}