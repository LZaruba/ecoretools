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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecoretools.diagram.edit.parts.EReferenceUtils;
import org.eclipse.emf.ecoretools.diagram.part.EcoreLinkDescriptor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;

/**
 * 
 * Restores the missing nodes of outgoing or incoming links for the selected
 * parts <br>
 * creation : 15 avr. 2008
 * 
 * @author <a href="mailto:gilles.cannenterre@anyware-tech.com">Gilles
 *         Cannenterre</a>
 */
public class RestoreRelatedMissingNodesCommand extends RestoreRelatedLinksCommand {

	public RestoreRelatedMissingNodesCommand(DiagramEditPart diagramEditPart, List selection) {
		super(diagramEditPart, selection);
	}

	/**
	 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		for (Iterator iter = adapters.iterator(); iter.hasNext();) {
			Object object = iter.next();
			if (object instanceof IAdaptable) {
				IAdaptable ad = (IAdaptable) object;
				View view = (View) ad.getAdapter(View.class);
				if (view != null) {
					refreshMissingNodes(view);
				}
			} else if (object instanceof View) {
				refreshMissingNodes((View) object);
			}

		}

		return CommandResult.newOKCommandResult();
	}

	/**
	 * Refresh related missing nodes for graphicalEditPart
	 * 
	 * @param graphicalEditPart
	 */
	protected void refreshMissingNodes(View notationView) {
		Map domain2NotationMap = new HashMap();

		// Create related missing nodes for all semantic link
		Collection linkDescriptors = getLinkDescriptorToProcess(notationView, domain2NotationMap);
		createRelatedMissingNodes(linkDescriptors, domain2NotationMap);
	}

	/**
	 * Create related missing nodes corresponding to linkDescriptions
	 * 
	 * @param part
	 * @param linkDescriptors
	 * @param domain2NotationMap
	 */
	protected void createRelatedMissingNodes(Collection linkDescriptors, Map domain2NotationMap) {
		// map diagram
		mapModel(diagram, domain2NotationMap);

		for (Iterator linkDescriptorsIterator = linkDescriptors.iterator(); linkDescriptorsIterator.hasNext();) {
			final EcoreLinkDescriptor nextLinkDescriptor = (EcoreLinkDescriptor) linkDescriptorsIterator.next();
			// EditPart sourceEditPart =
			// getEditPart(nextLinkDescriptor.getSource(), domain2NotationMap);
			// EditPart targetEditPart =
			// getEditPart(nextLinkDescriptor.getDestination(),
			// domain2NotationMap);
			View sourceView = (View) domain2NotationMap.get(nextLinkDescriptor.getSource());
			View targetView = (View) domain2NotationMap.get(nextLinkDescriptor.getDestination());

			// Create missing parts
			List<CreateViewRequest.ViewDescriptor> normalViewDescriptors = new ArrayList<CreateViewRequest.ViewDescriptor>();

			if (sourceView == null) {
				normalViewDescriptors.add(new CreateViewRequest.ViewDescriptor(new EObjectAdapter((EObject) nextLinkDescriptor.getSource()), Node.class, null, host.getDiagramPreferencesHint()));
			} else {
				setViewVisible(Collections.singletonList(sourceView));
			}

			if (targetView == null) {
				normalViewDescriptors.add(new CreateViewRequest.ViewDescriptor(new EObjectAdapter((EObject) nextLinkDescriptor.getDestination()), Node.class, null, host.getDiagramPreferencesHint()));
			} else {
				setViewVisible(Collections.singletonList(targetView));
			}

			if (false == normalViewDescriptors.isEmpty()) {

				CreateViewRequest cvr = new CreateViewRequest(normalViewDescriptors);
				cvr.setLocation(new Point(-1, -1));
				Command cmd = host.getCommand(cvr);

				if (cmd != null && cmd.canExecute()) {
					EReferenceUtils.executeCommand(cmd, host);
				}
			}
		}
	}

}
