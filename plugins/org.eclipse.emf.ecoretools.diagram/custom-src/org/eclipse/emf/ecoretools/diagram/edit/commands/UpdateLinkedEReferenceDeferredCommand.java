/**
 * Copyright (c) 2008 Anyware Technologies
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anyware Technologies - initial API and implementation
 */
package org.eclipse.emf.ecoretools.diagram.edit.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EReferenceEditPart;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.util.EditPartUtil;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Anchor;
import org.eclipse.gmf.runtime.notation.ConnectorStyle;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.IdentityAnchor;
import org.eclipse.gmf.runtime.notation.NotationFactory;
import org.eclipse.gmf.runtime.notation.NotationPackage;
import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;

/**
 * 
 * TODO Describe the class here <br>
 * creation : 17 janv. 08
 * 
 * @author <a href="mailto:gilles.cannenterre@anyware-tech.com">Gilles
 *         Cannenterre</a>
 */
public class UpdateLinkedEReferenceDeferredCommand extends AbstractTransactionalCommand {

	private EReferenceEditPart part1;

	private EReferenceEditPart part2;

	public UpdateLinkedEReferenceDeferredCommand(TransactionalEditingDomain domain, EReferenceEditPart part1, EReferenceEditPart part2) {
		super(domain, "LinkEReferenceDeferredCommand", null);
		this.part1 = part1;
		this.part2 = part2;
	}

	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		if (part1 == null || part2 == null) {
			return CommandResult.newWarningCommandResult("Unable to proceed with null parts", null);
		}
		RunnableWithResult refreshRunnable = new RunnableWithResult() {

			private IStatus status;

			private Object result;

			public Object getResult() {
				return result;
			}

			public void setStatus(IStatus status) {
				this.status = status;
			}

			public IStatus getStatus() {
				return status;
			}

			public void run() {
				if (false == part1.getNotationView() instanceof Edge || false == part2.getNotationView() instanceof Edge) {
					return;
				}

				Edge edge1 = (Edge) part1.getNotationView();
				Edge edge2 = (Edge) part2.getNotationView();
				// Verify source and target
				if (false == edge1.getTarget().equals(edge2.getSource()) || false == edge1.getSource().equals(edge2.getTarget())) {
					return;
				}

				// Refresh anchors
				if (edge1.getSourceAnchor() == null) {
					IdentityAnchor sourceAnchor = NotationFactory.eINSTANCE.createIdentityAnchor();
					sourceAnchor.setId("(0.5,0.5)");
					edge1.setSourceAnchor(sourceAnchor);
				}
				if (edge1.getTargetAnchor() == null) {
					IdentityAnchor targetAnchor = NotationFactory.eINSTANCE.createIdentityAnchor();
					targetAnchor.setId("(0.5,0.5)");
					edge1.setTargetAnchor(targetAnchor);
				}

				if (false == edge1.getTargetAnchor().equals(edge2.getSourceAnchor())) {
					edge2.setSourceAnchor((Anchor) EcoreUtil.copy(edge1.getTargetAnchor()));
				}

				if (false == edge1.getSourceAnchor().equals(edge2.getTargetAnchor())) {
					edge2.setTargetAnchor((Anchor) EcoreUtil.copy(edge1.getSourceAnchor()));
				}

				// Refresh bendpoints
				if ((false == edge1.getBendpoints() instanceof RelativeBendpoints) || (false == edge2.getBendpoints() instanceof RelativeBendpoints)) {
					return;
				}

				RelativeBendpoints edge1Benpoints = (RelativeBendpoints) edge1.getBendpoints();
				if (edge1Benpoints.getPoints().isEmpty()) {
					return;
				}

				List relativePoints = new ArrayList();
				for (int index = edge1Benpoints.getPoints().size() - 1; index >= 0; index--) {
					RelativeBendpoint relativePoint = (RelativeBendpoint) edge1Benpoints.getPoints().get(index);
					relativePoints.add(new RelativeBendpoint(relativePoint.getTargetX(), relativePoint.getTargetY(), relativePoint.getSourceX(), relativePoint.getSourceY()));
				}

				RelativeBendpoints edge2Benpoints = (RelativeBendpoints) edge2.getBendpoints();
				if (false == isBendpointEqual(edge2Benpoints.getPoints(), relativePoints)) {
					edge2Benpoints.setPoints(relativePoints);
				}

				// Refresh routing
				ConnectorStyle edge1Style = (ConnectorStyle) edge1.getStyle(NotationPackage.eINSTANCE.getConnectorStyle());
				ConnectorStyle edge2Style = (ConnectorStyle) edge2.getStyle(NotationPackage.eINSTANCE.getConnectorStyle());
				if (edge1Style == null || edge2Style == null) {
					return;
				}

				if (false == edge1Style.getRouting().equals(edge2Style.getRouting())) {
					edge2Style.setRouting(edge1Style.getRouting());
				}
			}
		};

		EditPartUtil.synchronizeRunnableToMainThread(part2, refreshRunnable);
		return CommandResult.newOKCommandResult();
	}

	protected boolean isBendpointEqual(List<RelativeBendpoint> relativePoints1, List<RelativeBendpoint> relativePoints2) {
		if (relativePoints1.size() != relativePoints2.size()) {
			return false;
		}

		for (int index = 0; index < relativePoints1.size(); index++) {
			if (false == isRelativePointEqual(relativePoints1.get(index), relativePoints2.get(index))) {
				return false;
			}
		}

		return true;
	}

	private boolean isRelativePointEqual(RelativeBendpoint relativeBendpoint1, RelativeBendpoint relativeBendpoint2) {
		if (relativeBendpoint1.getSourceX() != relativeBendpoint2.getSourceX() || relativeBendpoint1.getTargetX() != relativeBendpoint2.getTargetX()
				|| relativeBendpoint1.getSourceY() != relativeBendpoint2.getSourceY() || relativeBendpoint1.getTargetY() != relativeBendpoint2.getTargetY()) {
			return false;
		}
		return true;
	}
}