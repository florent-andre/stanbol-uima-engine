package org.apache.stanbol.enhancer.engines.uima;

import java.util.ArrayList;
import java.util.List;

import org.apache.clerezza.uima.utils.UIMAExecutor;
import org.apache.clerezza.uima.utils.UIMAExecutorFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.enhancer.engines.uima.api.StanbolAnalysisEngine;

@Component(immediate = true, metatype = true)
@Service()
public class UIMADictionaryEngine implements StanbolAnalysisEngine {

	//TODO : see new implementation and fixes in gasoil engine
	
	@Override
	public UIMAExecutor getExecutor() {
		
		return UIMAExecutorFactory.getInstance().createUIMAExecutor(this.getClass().getClassLoader(), "configuration/AggregateAE.xml");
		//return UIMAExecutorFactory.getInstance().createUIMAExecutor(Activator.class.getClassLoader(), "configuration/AggregateAE.xml");
		//return UIMAExecutorFactory.getInstance().createUIMAExecutor("configuration/AggregateAE.xml");
	}

	@Override
	public List<String> getUIMATypes() {
		List<String> r = new ArrayList<String>();
		r.add("org.apache.stanbol.enhancer.engines.gasoil.TokenAnnotation");
		return r;
	}
	
	
	/*@Override
	public int getUIMAType() {
		return TokenAnnotation.type;
	}*/
	

}
