/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2013 Andre Oosthuizen (South Africa)
 */
package com.google.code.kettle.openrdf.di;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

/**
 * Shows a dialog allowing the user to configure the step’s behavior to their liking.
 * This dialog class is closely related to the meta class which keeps track of the chosen settings.
 * 
 * @author Andre Oosthuizen
 * 
 */
public class OpenRDFStepDialog extends BaseStepDialog implements StepDialogInterface {
	
	/**
	 * The PKG member is used when looking up internationalized strings. The properties file with localized keys is expected to reside in {the package of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = OpenRDFStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed
	private OpenRDFStepMeta meta;
	
	// text field holding the name of the field to add to the row stream
	private TextVar wRepositoryUrl;
	private StyledTextComp wSparql;
	private Button wTest;
	private Listener lsTest;
	
	/**
	 * The constructor should simply invoke super() and save the incoming meta object to a local variable, so it can conveniently read and write settings from/to it.
	 * 
	 * @param parent
	 *            the SWT shell to open the dialog in
	 * @param in
	 *            the meta object holding the step's settings
	 * @param transMeta
	 *            transformation description
	 * @param sname
	 *            the step name
	 */
	public OpenRDFStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		this.meta = (OpenRDFStepMeta) in;
		transMeta.activateParameters();
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step. It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must be updated to reflect the new step settings. The changed flag of the meta object must reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog, or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);

		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();

		// The ModifyListener used on all controls. It will update the meta object to
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};

		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "OpenRDF.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);

		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// OK and cancel buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wPreview=new Button(shell, SWT.PUSH);
		wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview")); //$NON-NLS-1$
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, null);
				
		// Repository URL
		Label wlRepositoryURL = new Label(shell, SWT.RIGHT);
		wlRepositoryURL.setText(BaseMessages.getString(PKG, "OpenRDF.wlRepositoryURL.Label"));
		props.setLook(wlRepositoryURL);
		FormData fdlRepositoryURL = new FormData();
		fdlRepositoryURL.left = new FormAttachment(0, 0);
		fdlRepositoryURL.right = new FormAttachment(middle, -margin);
		fdlRepositoryURL.top = new FormAttachment(wStepname, margin);
		wlRepositoryURL.setLayoutData(fdlRepositoryURL);

		wRepositoryUrl = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(wRepositoryUrl);
		wRepositoryUrl.addModifyListener(lsMod);
		FormData fdRepositoryURL = new FormData();
		fdRepositoryURL.left = new FormAttachment(middle, 0);
		fdRepositoryURL.right = new FormAttachment(100, 0);
		fdRepositoryURL.top = new FormAttachment(wStepname, margin);
		wRepositoryUrl.setLayoutData(fdRepositoryURL);
		
		// SPARQL
		Label wlSparql = new Label(shell, SWT.LEFT);
		wlSparql.setText(BaseMessages.getString(PKG, "OpenRDF.wlSparql.Label"));
		props.setLook(wlSparql);
		FormData fdlSparql = new FormData();
		fdlSparql.left = new FormAttachment(0, 0);
		fdlSparql.right = new FormAttachment(middle, -margin);
		fdlSparql.top = new FormAttachment(wRepositoryUrl, margin);
		wlSparql.setLayoutData(fdlSparql);
		
		wTest = new Button(shell, SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "OpenRDF.Button.Test")); //$NON-NLS-1$
		wTest.pack();
		FormData fdTest = new FormData();
		fdTest.left = new FormAttachment(100, -(wTest.getBounds().width + margin));
		fdTest.right = new FormAttachment(100,0);
		fdTest.top = new FormAttachment(wRepositoryUrl, margin);
		wTest.setLayoutData(fdTest);
				
		wSparql =  new StyledTextComp(transMeta, shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
	    props.setLook(wSparql, Props.WIDGET_STYLE_FIXED);
		wSparql.addModifyListener(lsMod);
		FormData fdSparql = new FormData();
		fdSparql.left = new FormAttachment(0, 0);
		fdSparql.top = new FormAttachment(wTest, margin);
		fdSparql.right = new FormAttachment(100, -2 * margin);
		fdSparql.bottom= new FormAttachment(wOK, -margin);
		wSparql.setLayoutData(fdSparql);
		wSparql.addLineStyleListener(new SQLValuesHighlight());
		
		
		
		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		
		lsPreview = new Listener() {
			public void handleEvent(Event e) {
				preview();
			}
		};
		
		lsTest = new Listener() {
			public void handleEvent(Event e) {
				test();
			}
		};
		
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};

		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);
		wPreview.addListener(SWT.Selection, lsPreview);
		wTest.addListener(SWT.Selection, lsTest);
		
		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);
		wRepositoryUrl.addSelectionListener(lsDef);
		
		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();

		// restore the changed flag to original value, as the modify listeners fire during dialog population
		meta.setChanged(changed);

		// open dialog and enter event loop
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}

	/**
	 * This helper method puts the step configuration stored in the meta object and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		wRepositoryUrl.setText(meta.getRepositoryURL());
		wSparql.setText(meta.getSparql());
	}
	
	/**
	 * Called when the user cancels the dialog.
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method.
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}

	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method.
		// Setting to step name from the dialog control
		stepname = wStepname.getText();
		// Setting the settings to the meta object
		meta.setRepositoryURL(wRepositoryUrl.getText());
		meta.setSparql(wSparql.getText());
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user clicks on the preview button
	 */
	private void preview() {
		// Create the table input reader step...
		OpenRDFStepMeta meta = new OpenRDFStepMeta();
		String repositoryURL = transMeta.environmentSubstitute(wRepositoryUrl.getText());
		meta.setRepositoryURL(repositoryURL);
		String sparql = transMeta.environmentSubstitute(wSparql.getText());
		meta.setSparql(sparql);
		
		TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, meta, wStepname.getText());
		EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "OpenRDF.EnterPreviewSize"), BaseMessages.getString(PKG, "OpenRDF.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
		int previewSize = numberDialog.open();
		if (previewSize > 0) {
			TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize });
			progressDialog.open();
			Trans trans = progressDialog.getTrans();
			String loggingText = progressDialog.getLoggingText();
			if (!progressDialog.isCancelled()) {
				if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
					EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"), BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true);
					etd.setReadOnly();
					etd.open();
				} else {
					PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRowsMeta(wStepname.getText()), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
					prd.open();
				}
			}
		}
	}
	
	/**
	 * Called when the user tests the connection URL
	 */
	private void test() {
		OpenRDFStepData data = new OpenRDFStepData();
		try {
			String repositoryUrl = transMeta.environmentSubstitute(wRepositoryUrl.getText());
			logBasic("Attempting to connect to "+repositoryUrl);
			data.connect(repositoryUrl);
			String sparql = "SELECT DISTINCT ?type \nWHERE { \n  ?thing a ?type . \n} \nORDER BY ?type";
			data.runQuery(sparql);
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
			mb.setMessage(BaseMessages.getString(PKG, "OpenRDF.Connected.OK") +Const.CR); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "OpenRDF.Connected.Title.OK")); //$NON-NLS-1$
			mb.open();
		} catch (RepositoryException e) {
			logError("openRDF connection test failed", e);
			new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenRDF.Connected.Title.Error"), BaseMessages.getString(PKG, "OpenRDF.Connected.Error"), e);
		} catch (QueryEvaluationException e) {
			logError("openRDF connection test failed", e);
			new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenRDF.Connected.Title.Error"), BaseMessages.getString(PKG, "OpenRDF.Connected.Error"), e);
		} catch (MalformedQueryException e) {
			logError("openRDF connection test failed", e);
			new ErrorDialog(shell, BaseMessages.getString(PKG, "OpenRDF.Connected.Title.Error"), BaseMessages.getString(PKG, "OpenRDF.Connected.Error"), e);
		} finally {
			data.disconnect();
		}
	}

}
