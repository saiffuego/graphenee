/*
 * Copyright 2014 mattitahvonenitmill.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.viritin.fields;

import java.util.EventObject;
import java.util.Map;

import org.vaadin.viritin.util.HtmlElementPropertySetter;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.ConverterUtil;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.CompositeErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.Resource;
import com.vaadin.ui.TextArea;

/**
 *
 * @author mattitahvonenitmill
 */
public class MTextArea extends TextArea implements EagerValidateable {

	private static final long serialVersionUID = -9001913175922725871L;

	private boolean eagerValidation = false;
	private boolean eagerValidationStatus;
	private String lastKnownTextChangeValue;
	private Validator.InvalidValueException eagerValidationError;
	private AutoComplete autocomplete;
	private AutoCapitalize autocapitalize;
	private AutoCorrect autocorrect;
	private Boolean spellcheck;
	boolean userValueChange;

	public MTextArea() {
		configureMaddonStuff();
	}

	private void configureMaddonStuff() {
		setNullRepresentation("");
		setTextChangeEventMode(TextChangeEventMode.LAZY);
	}

	public MTextArea(String caption) {
		super(caption);
		configureMaddonStuff();
	}

	public MTextArea(Property dataSource) {
		super(dataSource);
		configureMaddonStuff();
	}

	public MTextArea(String caption, Property dataSource) {
		super(caption, dataSource);
		configureMaddonStuff();
	}

	public MTextArea(String caption, String value) {
		super(caption, value);
	}

	public MTextArea withCaption(String caption) {
		setCaption(caption);
		return this;
	}

	@Override
	protected void setValue(String newFieldValue, boolean repaintIsNotNeeded) throws ReadOnlyException, Converter.ConversionException, Validator.InvalidValueException {
		lastKnownTextChangeValue = null;
		eagerValidationError = null;
		super.setValue(newFieldValue, repaintIsNotNeeded);
	}

	@Override
	public boolean isEagerValidation() {
		return eagerValidation;
	}

	@Override
	public void setEagerValidation(boolean eagerValidation) {
		this.eagerValidation = eagerValidation;
	}

	@Override
	protected void fireEvent(EventObject event) {
		if (isEagerValidation() && event instanceof TextChangeEvent) {
			lastKnownTextChangeValue = ((TextChangeEvent) event).getText();
			doEagerValidation();
		}
		super.fireEvent(event);
	}

	/**
	 *
	 * @return the value of the field or if a text change event have sent a
	 * value to the server since last value changes, then that.
	 */
	public String getLastKnownTextContent() {
		return lastKnownTextChangeValue;
	}

	public MTextArea withConversionError(String message) {
		setConversionError(message);
		return this;
	}

	public MTextArea withConverter(Converter<String, ?> converter) {
		setConverter(converter);
		return this;
	}

	public MTextArea withFullWidth() {
		setWidth("100%");
		return this;
	}

	public MTextArea withValue(String value) {
		setValue(value);
		return this;
	}

	public MTextArea withInputPrompt(String inputPrompt) {
		setInputPrompt(inputPrompt);
		return this;
	}

	public MTextArea withReadOnly(boolean readOnly) {
		setReadOnly(readOnly);
		return this;
	}

	public MTextArea withValidator(Validator validator) {
		setImmediate(true);
		addValidator(validator);
		return this;
	}

	public MTextArea withWidth(float width, Unit unit) {
		setWidth(width, unit);
		return this;
	}

	public MTextArea withWidth(String width) {
		setWidth(width);
		return this;
	}

	public MTextArea withNullRepresentation(String nullRepresentation) {
		setNullRepresentation(nullRepresentation);
		return this;
	}

	public MTextArea withStyleName(String... styleNames) {
		for (String styleName : styleNames) {
			addStyleName(styleName);
		}
		return this;
	}

	public MTextArea withIcon(Resource icon) {
		setIcon(icon);
		return this;
	}

	public MTextArea withRequired(boolean required) {
		setRequired(required);
		return this;
	}

	public MTextArea withRequiredError(String requiredError) {
		setRequiredError(requiredError);
		return this;
	}

	public MTextArea withVisible(boolean visible) {
		setVisible(visible);
		return this;
	}

	public MTextArea withTextChangeListener(FieldEvents.TextChangeListener listener) {
		addTextChangeListener(listener);
		return this;
	}

	public MTextArea withValueChangeListener(Property.ValueChangeListener listener) {
		addValueChangeListener(listener);
		return this;
	}

	public MTextArea withBlurListener(FieldEvents.BlurListener listener) {
		addBlurListener(listener);
		return this;
	}

	public void setSpellcheck(Boolean spellcheck) {
		this.spellcheck = spellcheck;
	}

	public Boolean getSpellcheck() {
		return spellcheck;
	}

	public MTextArea withSpellCheckOff() {
		setSpellcheck(false);
		return this;
	}

	public MTextArea withId(String id) {
		setId(id);
		return this;
	}

	public enum AutoComplete {
		on,
		off
	}

	public enum AutoCorrect {
		on,
		off
	}

	public enum AutoCapitalize {
		on,
		off
	}

	public MTextArea withAutocompleteOff() {
		return setAutocomplete(AutoComplete.off);
	}

	public MTextArea setAutocomplete(AutoComplete autocomplete) {
		this.autocomplete = autocomplete;
		return this;
	}

	public AutoComplete getAutocomplete() {
		return autocomplete;
	}

	public MTextArea withAutoCapitalizeOff() {
		return setAutoCapitalize(AutoCapitalize.off);
	}

	public MTextArea setAutoCapitalize(AutoCapitalize autoCapitalize) {
		this.autocapitalize = autoCapitalize;
		return this;
	}

	public AutoCapitalize getAutoCapitalize() {
		return autocapitalize;
	}

	public MTextArea withAutoCorrectOff() {
		return setAutoCorrect(AutoCorrect.off);
	}

	public MTextArea setAutoCorrect(AutoCorrect autoCorrect) {
		this.autocorrect = autoCorrect;
		return this;
	}

	public AutoCorrect getAutoCorrect() {
		return autocorrect;
	}

	private HtmlElementPropertySetter heps;

	protected HtmlElementPropertySetter getHtmlElementPropertySetter() {
		if (heps == null) {
			heps = new HtmlElementPropertySetter(this);
		}
		return heps;
	}

	@Override
	public void beforeClientResponse(boolean initial) {
		super.beforeClientResponse(initial);
		if (initial) {
			if (spellcheck != null) {
				getHtmlElementPropertySetter().setProperty("spellcheck", spellcheck);
			}
			if (autocomplete != null) {
				// sending here to keep value if toggling visibility
				getHtmlElementPropertySetter().setProperty("autocomplete", autocomplete.toString());
			}
			if (autocorrect != null) {
				// sending here to keep value if toggling visibility
				getHtmlElementPropertySetter().setProperty("autocorrect", autocorrect.toString());
			}
			if (autocapitalize != null) {
				// sending here to keep value if toggling visibility
				getHtmlElementPropertySetter().setProperty("autocapitalize", autocapitalize.toString());
			}
		}
	}

	@Override
	public ErrorMessage getErrorMessage() {

		Validator.InvalidValueException validationError = getValidationError();

		final ErrorMessage superError = getComponentError();

		if (superError == null && validationError == null && getCurrentBufferedSourceException() == null) {
			return null;
		}
		// Throw combination of the error types
		return new CompositeErrorMessage(new ErrorMessage[] { superError, AbstractErrorMessage.getErrorMessageForException(validationError),
				AbstractErrorMessage.getErrorMessageForException(getCurrentBufferedSourceException()) });
	}

	protected Validator.InvalidValueException getValidationError() {
		if (isEagerValidation() && lastKnownTextChangeValue != null) {
			return eagerValidationError;
		}
		/*
		 * Check validation errors only if automatic validation is enabled.
		 * Empty, required fields will generate a validation error containing
		 * the requiredError string. For these fields the exclamation mark will
		 * be hidden but the error must still be sent to the client.
		 */
		Validator.InvalidValueException validationError = null;
		if (isValidationVisible()) {
			try {
				validate();
			} catch (Validator.InvalidValueException e) {
				if (!e.isInvisible()) {
					validationError = e;
				}
			}
		}
		return validationError;
	}

	protected void doEagerValidation() {
		final boolean wasvalid = eagerValidationStatus;
		eagerValidationStatus = true;
		eagerValidationError = null;
		try {
			if (isRequired() && getLastKnownTextContent().isEmpty()) {
				String errorMessage = getRequiredError();
				if (errorMessage == null || errorMessage.length() == 0)
					errorMessage = getCaption() + " is required";
				throw new Validator.EmptyValueException(errorMessage);
			}
			validate(getLastKnownTextContent());
			if (!wasvalid) {
				markAsDirty();
			}
			// Also eagerly pass content to backing bean to make top level
			// validation eager, but do not listen the value back in value change
			// event
			if (getPropertyDataSource() != null) {
				skipValueChangeEvent = true;
				Object convertedValue = ConverterUtil.convertToModel(getLastKnownTextContent(), getPropertyDataSource().getType(), getConverter(), getLocale());
				getPropertyDataSource().setValue(convertedValue);
				skipValueChangeEvent = false;
			}
		} catch (Validator.InvalidValueException e) {
			eagerValidationError = e;
			eagerValidationStatus = false;
			markAsDirty();
		}
	}

	private boolean skipValueChangeEvent = false;

	@Override
	public void valueChange(Property.ValueChangeEvent event) {
		if (!skipValueChangeEvent) {
			super.valueChange(event);
		} else {
			skipValueChangeEvent = false;
		}
	}

	@Override
	public boolean isValid() {
		if (isEagerValidation() && lastKnownTextChangeValue != null) {
			return eagerValidationStatus;
		} else {
			return super.isValid();
		}
	}

	@Override
	public void validate() throws Validator.InvalidValueException {
		if (isEagerValidation() && lastKnownTextChangeValue != null) {
			// This is most likely not executed, unless someone, for some weird
			// reason calls this explicitly
			if (isRequired() && getLastKnownTextContent().isEmpty()) {
				throw new Validator.EmptyValueException(getRequiredError());
			}
			validate(getLastKnownTextContent());
		} else {
			super.validate();
		}
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		userValueChange = true;
		super.changeVariables(source, variables); //To change body of generated methods, choose Tools | Templates.
		userValueChange = false;
	}

	/**
	 * @return true if the current value change event is created by a user
	 * action.
	 */
	public boolean isUserValueChange() {
		return userValueChange;
	}

	public MTextArea withRows(int rows) {
		setRows(rows);
		return this;
	}

}
