package ca.sqlpower.matchmaker.swingui;

import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchType;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.action.NewMatchGroupAction;
import ca.sqlpower.matchmaker.util.HibernateUtil;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MatchEditor {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);

	private SQLObjectChooser sourceChooser;
	private SQLObjectChooser resultChooser;

	private JPanel panel;

	private JLabel message = new JLabel();
    private JTextField matchId = new JTextField();
    private JComboBox folderComboBox = new JComboBox();
    private JTextArea desc = new JTextArea();
    private JComboBox type = new JComboBox();

    private JTextField resultTableName = new JTextField();

    private JButton viewBuilder;
    private JButton createResultTable;

    private JButton saveMatch;
    private JButton showAuditInfo;
    private JButton runMatch;
    private JButton validationStatus;
    private JButton validateMatch;
    private FilterComponentsPanel filterPanel;

    private final MatchMakerSwingSession swingSession;

    /**
     * The match that this editor is editing.  If you want to edit a different match,
     * create a new MatchEditor.
     */
	private final Match match;

    private PlFolder plFolder;


    public MatchEditor(MatchMakerSwingSession swingSession, Match match) throws HeadlessException, ArchitectException {
        this(swingSession, match, null);
    }

    public MatchEditor(MatchMakerSwingSession swingSession, Match match, PlFolder folder) throws ArchitectException {
        super();
        this.swingSession = swingSession;
        if (match == null) throw new NullPointerException("You can't edit a null plmatch");
        this.match = match;
        if (folder == null) {
        	if ( match != null ) {
        		this.plFolder = match.getFolder();
        	}
        } else {
        	this.plFolder = folder;
        }
        buildUI();
    }

    // TODO: remove
    private boolean checkStringNullOrEmpty (String value, String name) {
        String trimedValue = null;
        if ( value != null ) {
            trimedValue = value.trim();
        }
        if ( value == null || trimedValue == null || trimedValue.length() == 0 ) {
            JOptionPane.showMessageDialog(
                    panel,
                    name + " is required",
                    name + " is required",
                    JOptionPane.ERROR_MESSAGE );
            return false;
        }
        return true;
    }

    // TODO: remove
    private boolean checkObjectNullOrEmpty (Object value, String name) {
        if ( value == null ) {
            JOptionPane.showMessageDialog(
                    panel,
                    name + " is required",
                    name + " is required",
                    JOptionPane.ERROR_MESSAGE );
            return false;
        }
        return true;
    }

    /**
     * Saves the current match (which is referenced in the plMatch member variable of this editor instance).
     * If there is no current plMatch, a new one will be created and its properties will be set just like
     * they would if one had existed.  In either case, this action will then use Hibernate to save the
     * match object back to the database (but it should use the MatchHome interface instead).
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(ActionEvent e) {
            try {
                saveMatch();
                JOptionPane.showMessageDialog(panel,
                        "Match Interface Save Successfully",
                        "Saved",JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ASUtils.showExceptionDialog(panel, "Match Interface Not Saved", ex);
            }
		}
	};

	private Window getParentWindow() {
	    return SwingUtilities.getWindowAncestor(panel);
	}

    /**
     * Returns the parent (owning) frame of this match editor.  If the owner
     * isn't a frame (it might be a dialog or AWT Window) then null is returned.
     * You should always use {@link #getParentWindow()} in preference to
     * this method unless you really really need a JFrame.
     *
     * @return the parent JFrame of this match editor's panel, or null if
     * the owner is not a JFrame.
     */
    private JFrame getParentFrame() {
        Window owner = getParentWindow();
        if (owner instanceof JFrame) return (JFrame) owner;
        else return null;
    }

	private Action showAuditInfoAction = new AbstractAction("Show Audit Info") {
		public void actionPerformed(ActionEvent e) {

			MatchInfoPanel p = new MatchInfoPanel(match);
			JDialog d = ArchitectPanelBuilder.createSingleButtonArchitectPanelDialog(
					p, getParentWindow(),
					"Audit Information", "OK");
			d.pack();
			d.setVisible(true);
		}};

	private Action runMatchAction = new AbstractAction("Run Match") {
		public void actionPerformed(ActionEvent e) {
			RunMatchDialog p = new RunMatchDialog(swingSession, match, getParentFrame());
			p.pack();
			p.setVisible(true);
		}};

	private Action validationStatusAction = new AbstractAction("View Validation Status") {
		public void actionPerformed(ActionEvent e) {
			MatchValidationStatus p = new MatchValidationStatus(swingSession, match,
                    ArchitectPanelBuilder.makeOwnedDialog(getPanel(),"View Match Validation Status"));
			p.pack();
			p.setVisible(true);
		}};
	private Action validateMatchAction = new AbstractAction("Validate Match") {
		public void actionPerformed(ActionEvent e) {
			try {
				MatchValidation v = new MatchValidation(swingSession, match);
				v.pack();
				v.setVisible(true);
			} catch (HeadlessException e1) {
				ASUtils.showExceptionDialog(panel, "Unknown Error",e1);
			} catch (SQLException e1) {
				ASUtils.showExceptionDialog(panel, "Unknown SQL Error",e1);
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialog(panel, "Unknown Error",e1);
			}
		}};
	private Action viewBuilderAction = new AbstractAction("View Builder") {
		public void actionPerformed(ActionEvent e) {
            SQLTable t = (SQLTable)sourceChooser.getTableComboBox().getSelectedItem();
            JDialog d;
			if (t !=null){
                try {
                    d = new ViewBuilderDialog(swingSession, getParentFrame(), t);
                    d.pack();
                    d.setSize(800, d.getPreferredSize().height);
                    d.setVisible(true);
                } catch (ArchitectException ex) {
                    ASUtils.showExceptionDialog(panel, "Couldn't create view builder", ex);
                }
            }
		}};

	private Action createResultTableAction = new AbstractAction("Create Table") {
		public void actionPerformed(ActionEvent e) {
            // TODO
            JOptionPane.showMessageDialog(panel, "We can't create tables yet, sorry.");
		}
	};

    private void buildUI() throws ArchitectException {

		sourceChooser = new SQLObjectChooser(panel,
        		swingSession.getContext().getDataSources());
        resultChooser = new SQLObjectChooser(panel,
        		swingSession.getContext().getDataSources());

        filterPanel = new FilterComponentsPanel();

        List<String> types = new ArrayList<String>();
        for ( MatchType mt : MatchType.values() ) {
        	types.add(mt.getName());
        }
        type.setModel(new DefaultComboBoxModel(types.toArray()));

        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		filterPanel.getFilterTextArea().setText("");
        	}});

    	viewBuilder = new JButton(viewBuilderAction);
    	createResultTable = new JButton(createResultTableAction);
    	saveMatch = new JButton(saveAction);
    	showAuditInfo = new JButton(showAuditInfoAction);
    	runMatch= new JButton(runMatchAction);
    	validationStatus = new JButton(validationStatusAction);
    	validateMatch = new JButton(validateMatchAction);

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,10dlu, pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,   16dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,   4dlu,32dlu,  16dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows
    	//		 1     2     3    4     5    6     7    8        9 10    11   12    13   14  15   16       17    18     19  20    21   22    23   24    25

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(new JLabel("Match ID:"), cc.xy(2,2,"r,c"));
		pb.add(new JLabel("Folder:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,t"));
		pb.add(new JLabel("Type:"), cc.xy(2,8,"r,c"));

		pb.add(matchId, cc.xy(4,2));
		pb.add(folderComboBox, cc.xy(4,4));
		pb.add(new JScrollPane(desc), cc.xy(4,6,"f,f"));
		pb.add(type, cc.xy(4,8));

		pb.add(sourceChooser.getCatalogTerm(), cc.xy(2,10,"r,c"));
		pb.add(sourceChooser.getSchemaTerm(), cc.xy(2,12,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,14,"r,c"));
		pb.add(new JLabel("Unique Index:"), cc.xy(2,16,"r,t"));
		pb.add(new JLabel("Filter:"), cc.xy(2,18,"r,t"));

		pb.add(sourceChooser.getCatalogComboBox(), cc.xy(4,10));
		pb.add(sourceChooser.getSchemaComboBox(), cc.xy(4,12));
		pb.add(sourceChooser.getTableComboBox(), cc.xy(4,14));
		pb.add(sourceChooser.getUniqueKeyComboBox(), cc.xy(4,16,"f,f"));
		pb.add(filterPanel, cc.xy(4,18,"f,f"));

		pb.add(resultChooser.getCatalogTerm(), cc.xy(2,20,"r,c"));
		pb.add(resultChooser.getSchemaTerm(), cc.xy(2,22,"r,c"));
		pb.add(new JLabel("Table Name:"), cc.xy(2,24,"r,c"));

		pb.add(resultChooser.getCatalogComboBox(), cc.xy(4,20));
		pb.add(resultChooser.getSchemaComboBox(), cc.xy(4,22));
		pb.add(resultTableName, cc.xy(4,24));



		pb.add(viewBuilder, cc.xy(6,10,"f,f"));
		pb.add(createResultTable, cc.xywh(6,20,1,3));

		ButtonStackBuilder bb = new ButtonStackBuilder();
		bb.addGridded(saveMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(new JButton(new NewMatchGroupAction(swingSession, match)));
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(showAuditInfo);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(runMatch);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(validationStatus);
		bb.addRelatedGap();
		bb.addRelatedGap();
		bb.addGridded(validateMatch);



		pb.add(bb.getPanel(), cc.xywh(8,2,1,14,"f,f"));
		panel = pb.getPanel();

		setDefaultSelections();
    }


    private void setDefaultSelections() {

    	final List<PlFolder> folders = swingSession.getFolders();
    	final SQLDatabase loginDB = swingSession.getDatabase();
        sourceChooser.getDataSourceComboBox().setSelectedItem(loginDB.getDataSource());
        resultChooser.getDataSourceComboBox().setSelectedItem(loginDB.getDataSource());
        folderComboBox.setModel(new DefaultComboBoxModel(folders.toArray()));
        folderComboBox.setRenderer(new FolderComboBoxCellRenderer());

        if ( plFolder != null ) {
        	folderComboBox.setSelectedItem(plFolder);
        } else if ( match.getFolder() != null) {
        	PlFolder f = (PlFolder) match.getFolder();
        	if ( f != null ) {
        		folderComboBox.setSelectedItem(f);
        	}
        }

        matchId.setText(match.getName());
        desc.setText(match.getDescription());
        type.setSelectedItem(match.getType());
        filterPanel.getFilterTextArea().setText(match.getFilter());

        if ( match.getSourceTable() != null ) {

        	SQLTable tableByName = match.getSourceTable().getTable();
        	if (tableByName == null) {
        	} else {
        		filterPanel.setTable(tableByName);
        		SQLCatalog cat = tableByName.getCatalog();
	    		SQLSchema sch = tableByName.getSchema();
	    		if ( cat != null ) {
	    			sourceChooser.getCatalogComboBox().setSelectedItem(cat);
	    		}
	    		if ( sch != null ) {
	    			sourceChooser.getSchemaComboBox().setSelectedItem(sch);
	    		}
	    		sourceChooser.getTableComboBox().setSelectedItem(tableByName);

    			SQLIndex pk = null;
				pk = match.getSourceTable().getUniqueIndex();
    			if ( pk != null ) {
    				sourceChooser.getUniqueKeyComboBox().setSelectedItem(pk);
    			}
        	}
    	}

    	SQLTable resultTable = match.getResultTable();
    	if ( resultTable != null ) {
    		SQLCatalog cat = resultTable.getCatalog();
    		SQLSchema sch = resultTable.getSchema();
    		if ( cat != null ) {
    			resultChooser.getCatalogComboBox().setSelectedItem(cat);
    		}
    		if ( sch != null ) {
    			resultChooser.getSchemaComboBox().setSelectedItem(sch);
    		}
    		resultTableName.setText(match.getResultTable().getName());
    	}


    }

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		if (this.panel != panel) {
			this.panel = panel;
			//TODO fire event
		}
	}

    /**
     * Copies all the values from the GUI components into the PlMatch
     * object this component is editing, then persists it to the database.
     */
    private void saveMatch() {
        if ( !checkObjectNullOrEmpty(
        		sourceChooser.getTableComboBox().getSelectedItem(),
        		"Source Table") )
        	return;
        if ( !checkStringNullOrEmpty(
        		((SQLTable) sourceChooser.getTableComboBox().getSelectedItem()).getName(),
        		"Source Table Name") )
        	return;

        if ( sourceChooser.getCatalogComboBox().isEnabled() ) {
        	if ( !checkObjectNullOrEmpty(
        			sourceChooser.getCatalogComboBox().getSelectedItem(),
        			"Source Catalog" ) )
        		return;
        	if ( !checkStringNullOrEmpty(
        			((SQLCatalog)sourceChooser.getCatalogComboBox().getSelectedItem()).getName(),
        			"Source Catalog Name" ) )
        		return;
        }

        if ( sourceChooser.getSchemaComboBox().isEnabled() ) {
        	if ( !checkObjectNullOrEmpty(
        			sourceChooser.getSchemaComboBox().getSelectedItem(),
        			"Source Schema"))
        		return;
        	if ( !checkStringNullOrEmpty(
        			((SQLSchema)sourceChooser.getSchemaComboBox().getSelectedItem()).getName(),
        			"Source Schema Name"))
        		return;
        }

        match.setType((Match.MatchType)type.getSelectedItem());

        match.setDescription(desc.getText());
        match.getSourceTable().setTable(
        		((SQLTable) sourceChooser.getTableComboBox().
        				getSelectedItem()));

        String id = matchId.getText().trim();
        if ( id == null || id.length() == 0 ) {
        	StringBuffer s = new StringBuffer();
        	s.append("MATCH_");
        	SQLTable table = match.getSourceTable().getTable();
			if ( table.getCatalogName() != null &&
        			table.getCatalogName().length() > 0 ) {
        		s.append(table.getCatalogName()).append("_");
        	}
        	if ( table.getSchemaName() != null &&
        			table.getSchemaName().length() > 0 ) {
        		s.append(table.getSchemaName()).append("_");
        	}
        	s.append(table.getName());
        	id = s.toString();
        	if ( swingSession.getMatchByName(id) == null ) {
        		matchId.setText(id);
            }
        }

        if ( !checkStringNullOrEmpty(matchId.getText(),"Match ID") )
            return;

        match.setName(matchId.getText());
        logger.debug("Saving Match:" + match.getName());


        if ( sourceChooser.getUniqueKeyComboBox().getSelectedItem() != null ) {
            match.getSourceTable().setUniqueIndex(
            		((SQLIndex)sourceChooser.getUniqueKeyComboBox().getSelectedItem()));
        }
        match.setFilter(filterPanel.getFilterTextArea().getText());

        if ( resultChooser.getCatalogComboBox().isEnabled() &&
        		resultChooser.getCatalogComboBox().getSelectedItem() == null ) {
        	SQLDatabase db = resultChooser.getDb();
        	try {
        		SQLCatalog cat = db.getCatalogByName(
        				((SQLCatalog)sourceChooser.getCatalogComboBox().
        						getSelectedItem()).getName());
        		resultChooser.getCatalogComboBox().setSelectedItem(cat);
        	} catch (ArchitectException e1) {
        		ASUtils.showExceptionDialogNoReport(panel,
        				"Unknown Database error", e1);
        	}
        }



        String trimedValue = null;
        String resultTable = resultTableName.getText();
        if ( resultTable != null ) {
            trimedValue = resultTable.trim();
        }
        if ( trimedValue == null || trimedValue.length() == 0 ) {
            resultTableName.setText("MM_"+match.getName());
        }


        match.setResultTable(new SQLTable());

        PlFolder f = (PlFolder)folderComboBox.getSelectedItem();
        match.setFolder( f);

        // XXX don't use hibernate directly; use the DAOs
        Transaction tx = HibernateUtil.primarySession().beginTransaction();
        HibernateUtil.primarySession().saveOrUpdate(match);
        HibernateUtil.primarySession().flush();
        tx.commit();
        HibernateUtil.primarySession().refresh(match);
        HibernateUtil.primarySession().flush();
    }
}