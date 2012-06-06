package org.apache.stanbol.enhancer.engines.uima;

import java.util.List;

import org.apache.clerezza.uima.utils.UIMAExecutor;
import org.apache.clerezza.uima.utils.UIMAExecutorFactory;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.clerezza.uima.utils.exception.FeatureStructureNotFoundException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.engines.gasoil.TokenAnnotation;
import org.apache.stanbol.enhancer.engines.uima.api.StanbolAnalysisEngine;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

@Component(immediate = true, metatype = true)
@Service()
public class RegexEngine implements StanbolAnalysisEngine {
	
	//TODO : see new implementation and fixes in gasoil engine
	
	@Override
	public UIMAExecutor getExecutor() {
		return UIMAExecutorFactory.getInstance().createUIMAExecutor(this.getClass().getClassLoader(), "configuration/AggregateAE.xml");
	}

	@Override
	public int getUIMAType() {
		return TokenAnnotation.type;
	}

	/*Override
	public JCas getJCas(String document) throws AnalysisEngineProcessException {
		UIMAExecutor uimaExecutor = getExecutor();
        
		// Execute the analysis and get the resulting JCas
		JCas jcas = null;
		try {
			jcas = uimaExecutor.analyzeDocument(document);
		} catch(ResourceInitializationException rie){
			throw new AnalysisEngineProcessException(rie);
		}
		return jcas;
	}

	Override
	public List<FeatureStructure> getAllFeatures(JCas jcas) throws FeatureStructureNotFoundException{
		List<FeatureStructure> fslist = null;
		fslist = UIMAUtils.getAllFSofType(TokenAnnotation.type, jcas);
		//fslist = UIMAUtils.getAllFSofType(getUIMAType(), jcas);
		return fslist;
	}*/
	
	

}