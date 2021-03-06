/***********************************************************************
 * Copyright (c) 2007 Anyware Technologies
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Anyware Technologies - initial API and implementation
 *
 * $Id: UpperBoundPropertySection.java,v 1.6 2008/05/26 12:28:57 jlescot Exp $
 **********************************************************************/

package org.eclipse.emf.ecoretools.properties.internal.sections;

import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecoretools.properties.internal.Messages;
import org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractIntegerPropertySection;

/**
 * A section for the upper bound property of an ETypedElement Object.
 * 
 * Creation 5 avr. 2006
 * 
 * @author jlescot
 */
public class UpperBoundPropertySection extends AbstractIntegerPropertySection {

	/**
	 * Predefined string pattern value for numerics and absolute with '-' : -25
	 * '?' (for -2 value) and '*' (for -1 value) characters are also accepted
	 * */
	public static final String UPPER_BOUND_PATTERN = "\\*|\\?|^[-\\d][\\d]*"; //$NON-NLS-1$   

	/** The Pattern used to check an Integer value */
	public static final Pattern UPPER_PATTERN = Pattern.compile(UPPER_BOUND_PATTERN);

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractTextPropertySection#getFeature()
	 */
	@Override
	protected EStructuralFeature getFeature() {
		return EcorePackage.Literals.ETYPED_ELEMENT__UPPER_BOUND;
	}

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractIntegerPropertySection#getFeatureInteger()
	 */
	@Override
	protected Integer getFeatureInteger() {
		return new Integer(((ETypedElement) getEObject()).getUpperBound());
	}

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractTextPropertySection#getLabelText()
	 */
	@Override
	protected String getLabelText() {
		return Messages.UpperBoundPropertySection_UpperBound;
	}

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractTextPropertySection#isTextValid()
	 */
	@Override
	protected boolean isTextValid() {
		return UPPER_PATTERN.matcher(getText().getText()).matches();
	}

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractTextPropertySection#getNewFeatureValue(java.lang.String)
	 */
	@Override
	protected Object getNewFeatureValue(String newText) {
		String text = newText;
		if ("*".equals(newText)) { //$NON-NLS-1$
			text = "-1"; //$NON-NLS-1$
		} else if ("?".equals(newText)) { //$NON-NLS-1$
			text = "-2"; //$NON-NLS-1$
		}
		return new Integer(Integer.parseInt(text));
	}

	/**
	 * @see org.eclipse.emf.ecoretools.tabbedproperties.sections.AbstractTextPropertySection#getFeatureAsString()
	 */
	@Override
	protected String getFeatureAsString() {
		Integer upper = getFeatureInteger();
		if (upper.intValue() == -1) {
			return "*"; //$NON-NLS-1$
		} else if (upper.intValue() == -2) {
			return "?"; //$NON-NLS-1$
		} else {
			return getFeatureInteger().toString();
		}
	}
}
