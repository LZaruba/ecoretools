package org.eclipse.emf.ecoretools.design.parts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecoretools.design.service.EReferenceServices;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.LabelDirectEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.AbstractEditPartProvider;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.CreateGraphicEditPartOperation;
import org.eclipse.gmf.runtime.diagram.ui.services.editpart.IEditPartOperation;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DEdgeBeginNameEditPart;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DEdgeEndNameEditPart;
import org.eclipse.sirius.diagram.ui.part.SiriusVisualIDRegistry;
import org.eclipse.sirius.diagram.ui.tools.api.command.GMFCommandWrapper;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;

public class EcoreToolsSpecificEditPartProvider extends
		AbstractEditPartProvider {

	public synchronized IGraphicalEditPart createGraphicEditPart(View view) {
		switch (SiriusVisualIDRegistry.getVisualID(view)) {

		case DEdgeBeginNameEditPart.VISUAL_ID:
			DEdgeBeginNameEditPart dEdgePart = new DEdgeBeginNameEditPart(view) {

				@Override
				protected boolean isDirectEditEnabled() {
					return true;
				}

			};
			dEdgePart.installEditPolicy(
					org.eclipse.gef.RequestConstants.REQ_DIRECT_EDIT,
					new EcoreToolsDirectEditForBeginRole());
			return dEdgePart;

		case DEdgeEndNameEditPart.VISUAL_ID:
			DEdgeEndNameEditPart dEdgeEndPart = new DEdgeEndNameEditPart(view) {
				@Override
				protected boolean isDirectEditEnabled() {
					return true;
				}

			};
			dEdgeEndPart.installEditPolicy(
					org.eclipse.gef.RequestConstants.REQ_DIRECT_EDIT,
					new EcoreToolsDirectEditForEndRole());
			return dEdgeEndPart;
		}
		return null;
	}

	@Override
	public boolean provides(IOperation operation) {
		if (operation instanceof CreateGraphicEditPartOperation) {
			View view = ((IEditPartOperation) operation).getView();
			if (view.getElement() instanceof DSemanticDecorator) {
				EObject semanticTarget = ((DSemanticDecorator) view
						.getElement()).getTarget();
				if (isFromEcoreToolsDesign((DSemanticDecorator) view
						.getElement())
						&& semanticTarget instanceof EReference
						&& ((EReference) semanticTarget).getEOpposite() != null) {
					switch (SiriusVisualIDRegistry.getVisualID(view)) {

					case DEdgeBeginNameEditPart.VISUAL_ID:
						return true;

					case DEdgeEndNameEditPart.VISUAL_ID:
						return true;
					}
				}
			}

		}
		return false;
	}

	private boolean isFromEcoreToolsDesign(DSemanticDecorator element) {
		return true;
		// if (element instanceof DDiagramElement)
		// {
		// new DDiagramElementQuery(element).getMapping();
		// }
		// return false;
	}

	class EcoreToolsDirectEditForBeginRole extends LabelDirectEditPolicy {

		protected org.eclipse.gef.commands.Command getDirectEditCommand(
				org.eclipse.gef.requests.DirectEditRequest edit) {
			final EObject element = ((org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) getHost())
					.resolveSemanticElement();
			final TransactionalEditingDomain domain = TransactionUtil
					.getEditingDomain(element);
			final String labelText = (String) edit.getCellEditor().getValue();
			RecordingCommand cmd = new RecordingCommand(domain) {

				@Override
				protected void doExecute() {
					if (element instanceof DSemanticDecorator) {
						EObject target = ((DSemanticDecorator) element)
								.getTarget();
						if (target instanceof EReference) {
							new EReferenceServices().performEdit(
									(EReference) target, labelText);
						}

					}
				}
			};
			return new ICommandProxy(new GMFCommandWrapper(domain, cmd));

		};

	}

	class EcoreToolsDirectEditForEndRole extends LabelDirectEditPolicy {

		protected org.eclipse.gef.commands.Command getDirectEditCommand(
				org.eclipse.gef.requests.DirectEditRequest edit) {
			final EObject element = ((org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart) getHost())
					.resolveSemanticElement();
			final TransactionalEditingDomain domain = TransactionUtil
					.getEditingDomain(element);
			final String labelText = (String) edit.getCellEditor().getValue();
			RecordingCommand cmd = new RecordingCommand(domain) {

				@Override
				protected void doExecute() {
					if (element instanceof DSemanticDecorator) {
						EObject target = ((DSemanticDecorator) element)
								.getTarget();
						if (target instanceof EReference
								&& ((EReference) target).getEOpposite() != null) {
							new EReferenceServices().performEdit(
									((EReference) target).getEOpposite(),
									labelText);
						}

					}
				}
			};
			return new ICommandProxy(new GMFCommandWrapper(domain, cmd));

		};

	}

}
