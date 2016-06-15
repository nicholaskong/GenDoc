package org.eclipse.gendoc.bundle.acceleo.sirius.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.gendoc.bundle.acceleo.commons.files.CommonService;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.sirius.business.api.session.CustomDataConstants;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.viewpoint.description.AnnotationEntry;



public class SiriusServices {
	
	public List<Diagram> getSiriusDiagrams (EObject self){
		// TO IMPROVE ? reuse existing sirius services ?
		CommonService.load(self, "aird");
		ResourceSet set = null;
		if (self.eResource() != null && self.eResource().getResourceSet() != null){
			set = self.eResource().getResourceSet();
		}
		if (set == null){
			return Arrays.asList(); 
		}
		ECrossReferenceAdapter cross = ECrossReferenceAdapter.getCrossReferenceAdapter(set);
		if (cross == null){
			cross = new ECrossReferenceAdapter();
			set.eAdapters().add(cross);
		}
		List<Diagram> result = new LinkedList<Diagram>();
		Collection<Setting> inverses = cross.getInverseReferences(self, true);
		for (Setting s : inverses){
			if (s.getEObject() instanceof DSemanticDiagram) {
				DSemanticDiagram sem = (DSemanticDiagram) s.getEObject();
				for (AnnotationEntry e : sem.getOwnedAnnotationEntries()){
					if (CustomDataConstants.GMF_DIAGRAMS.equals(e.getSource()) && e.getData() instanceof Diagram){
						result.add((Diagram) e.getData());
					}
				}
				
			}
		}
		return result;
	}
}
