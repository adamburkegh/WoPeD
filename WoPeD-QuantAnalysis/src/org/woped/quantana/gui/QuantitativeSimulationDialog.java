package org.woped.quantana.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

import org.woped.core.analysis.StructuralAnalysis;
import org.woped.core.config.ConfigurationManager;
import org.woped.core.config.DefaultStaticConfiguration;
import org.woped.core.model.ModelElementContainer;
import org.woped.core.model.PetriNetModelProcessor;
import org.woped.core.model.petrinet.ResourceClassModel;
import org.woped.core.model.petrinet.TransitionModel;
import org.woped.core.utilities.LoggerManager;
import org.woped.editor.controller.vc.EditorVC;
import org.woped.language.Messages;
import org.woped.quantana.Constants;
import org.woped.quantana.graph.Node;
import org.woped.quantana.graph.WorkflowNetGraph;
import org.woped.quantana.gui.CapacityAnalysisDialog.MyTableHeaderRenderer;
import org.woped.quantana.model.ReportServerStats;
import org.woped.quantana.model.ResUtilTableModel;
import org.woped.quantana.model.ResourceStats;
import org.woped.quantana.model.RunStats;
import org.woped.quantana.model.ServerTableModel;
import org.woped.quantana.model.TasksResourcesAllocation;
import org.woped.quantana.model.TimeModel;
import org.woped.quantana.resourcealloc.Resource;
import org.woped.quantana.resourcealloc.ResourceAllocation;
import org.woped.quantana.resourcealloc.ResourceUtilization;
import org.woped.quantana.simulation.ProbabilityDistribution;
import org.woped.quantana.simulation.Server;
import org.woped.quantana.simulation.SimParameters;
import org.woped.quantana.simulation.Simulator;
import org.woped.quantana.utilities.ExportStatistics;

public class QuantitativeSimulationDialog extends JDialog implements
		MouseMotionListener {

	private static final long serialVersionUID = 1L;

	private EditorVC editor = null;

	private JPanel iatPanel = null;

	private JPanel stPanel = null;

	private JPanel queuePanel = null;

	private JPanel termPanel = null;

	private JPanel generalPanel = null;

	private JPanel distPanel = null;

	private JPanel statsPanel = null;

	private JPanel utilPanel = null;

	private JPanel buttonPanel = null;

	private int groupRoleNum = 0;

	private int resObjNum = 0;

	private double period = 60.0;

	private double lambda = 50.0;

	private StructuralAnalysis sa;

	private ModelElementContainer mec;

	private WorkflowNetGraph graph;

	private ResourceAllocation resAlloc;

	private JTextField txtRuns;

	private JTextField txtLambda;

	private JTextField txtPeriod;

	private JTextField txtIATInterval;

	private JTextField txtSTInterval;

	private JTextField txtIATStdDev;

	private JTextField txtSTStdDev;

	private JComboBox cboTimeUnits;
	
	private int timeUnit = 1;

	private int periodIndex = 2;

	private ButtonGroup groupIAT;

	private ButtonGroup groupST;

	private ButtonGroup groupQD;

	private JCheckBox stop1;

	private JCheckBox stop2;

	private JTable tableServers;

	private JScrollPane serverTableScrollPane;

	private Object[][] serverTableMatrix;

	private ServerTableModel serverTableModel;

	private JTable tableResUtil;

	private JScrollPane resUtilTableScrollPane;

	private Object[][] resUtilTableMatrix;

	private ResUtilTableModel resUtilTableModel;

	private TimeModel tm = null;

	private Simulator sim;

	private String[] colServers = {
			Messages.getString("QuantAna.Simulation.Column.Names"),
			Messages.getString("QuantAna.Simulation.Column.L"),
			Messages.getString("QuantAna.Simulation.Column.Lq"),
			Messages.getString("QuantAna.Simulation.Column.Ls"),
			Messages.getString("QuantAna.Simulation.Column.W"),
			Messages.getString("QuantAna.Simulation.Column.Wq"),
			Messages.getString("QuantAna.Simulation.Column.Details") };

	private String[] ttipsServers = {
			Messages.getString("QuantAna.Simulation.ToolTip.Names"),
			Messages.getString("QuantAna.Simulation.ToolTip.L"),
			Messages.getString("QuantAna.Simulation.ToolTip.Lq"),
			Messages.getString("QuantAna.Simulation.ToolTip.Ls"),
			Messages.getString("QuantAna.Simulation.ToolTip.W"),
			Messages.getString("QuantAna.Simulation.ToolTip.Wq"),
			Messages.getString("QuantAna.Simulation.TooTip.Details") };

	private String[] colResUtil = {
			Messages.getString("QuantAna.Simulation.Column.Object"),
			Messages.getString("QuantAna.Simulation.Column.Util") };

	private String[] ttipsResUtil = {
			Messages.getString("QuantAna.Simulation.ToolTip.Object"),
			Messages.getString("QuantAna.Simulation.ToolTip.Util") };

	private int numServers;

	private Dialog thisDialog = this;

	private JButton btnProtocol;
	
	private JButton btnExport;
	
	private JButton btnDiagram;
	
	private JButton btnColumn[];

	private String[] servNames;
	
	private TasksResourcesAllocation tasksAndResources;
	
	private ArrayList<RunStats> simStatistics;
	

	
	private ExportStatistics export;
	
	private JFileChooser fileChooser;
	
	private File dir;
	
	private final ExtensionFileFilter eff = new ExtensionFileFilter();
	
	private boolean errorDetected = false;

	/**
	 * This is the default constructor
	 */
	public QuantitativeSimulationDialog(JFrame owner, EditorVC editor) {
		super(owner, Messages.getTitle("QuantAna.Simulation"), true);
		this.editor = editor;
		sa = new StructuralAnalysis(editor);
		mec = editor.getModelProcessor().getElementContainer();
		graph = new WorkflowNetGraph(sa, mec);
		tm = new TimeModel(1, 1.0);
		servNames = graph.getTransitions();
		numServers = graph.getNumTransitions();
		initResourceAlloc();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(new GridBagLayout());
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		constraints.weighty = 0;

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		constraints.insets = new Insets(10, 10, 5, 10);
		getContentPane().add(getGeneralPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		constraints.insets = new Insets(5, 10, 5, 10);
		getContentPane().add(getQueuePanel(), constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		getContentPane().add(getTermPanel(), constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 2;
		constraints.insets = new Insets(10, 0, 0, 20);
		getContentPane().add(getButtonPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.insets = new Insets(0, 10, 5, 10);
		getContentPane().add(getDistPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.weighty = 1;
		getContentPane().add(getStatsPanel(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 4;
		constraints.gridwidth = 3;
		constraints.gridheight = 1;
		constraints.weighty = 1;
		constraints.insets = new Insets(5, 10, 5, 10);
		getContentPane().add(getUtilPanel(), constraints);
		
		makeTasksAndResources();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width > 770 ? 770 : screenSize.width;
		int x = screenSize.width > width ? (screenSize.width - width) / 2 : 0;
		int height = screenSize.height > 740 ? 740 : screenSize.height;
		int y = screenSize.height > height ? (screenSize.height - height) / 2 : 0;
		this.setBounds(x, y, width, height);
		this.setVisible(true);
	}

	private JPanel getGeneralPanel() {
		if (generalPanel == null) {
			generalPanel = new JPanel();
			generalPanel
					.setBorder(BorderFactory
							.createCompoundBorder(
									BorderFactory
											.createTitledBorder(Messages
													.getString("QuantAna.Simulation.GeneralProperties")),
									BorderFactory.createEmptyBorder(5, 5, 0, 5)));
			GridBagConstraints constraints = new GridBagConstraints();
			generalPanel.setLayout(new GridBagLayout());
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;

			JLabel lblLambda = new JLabel(Messages
					.getString("QuantAna.Simulation.Mean"));
			lblLambda.setMinimumSize(new Dimension(100, 20));
			lblLambda.setMaximumSize(new Dimension(100, 20));
			lblLambda.setPreferredSize(new Dimension(100, 20));
			lblLambda.setHorizontalAlignment(SwingConstants.LEFT);
			constraints.insets = new Insets(5, 10, 5, 5);
			constraints.gridx = 0;
			constraints.gridy = 0;
			generalPanel.add(lblLambda, constraints);

			txtLambda = new JTextField("50");
			txtLambda.setMinimumSize(new Dimension(100, 20));
			txtLambda.setMaximumSize(new Dimension(100, 20));
			txtLambda.setPreferredSize(new Dimension(100, 20));
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 5, 5, 5);
			generalPanel.add(txtLambda, constraints);

			JLabel lblPeriod = new JLabel(Messages
					.getString("QuantAna.Simulation.Period"));
			lblPeriod.setMinimumSize(new Dimension(100, 20));
			lblPeriod.setMaximumSize(new Dimension(100, 20));
			lblPeriod.setPreferredSize(new Dimension(100, 20));
			lblPeriod.setHorizontalAlignment(SwingConstants.RIGHT);
			constraints.gridx = 2;
			constraints.gridy = 0;
			generalPanel.add(lblPeriod, constraints);

			txtPeriod = new JTextField("8.0");
			txtPeriod.setMinimumSize(new Dimension(100, 20));
			txtPeriod.setMaximumSize(new Dimension(100, 20));
			txtPeriod.setPreferredSize(new Dimension(100, 20));
			constraints.gridx = 3;
			constraints.gridy = 0;
			generalPanel.add(txtPeriod, constraints);

			cboTimeUnits = new JComboBox(Constants.TIMEUNITS);
			cboTimeUnits.setMinimumSize(new Dimension(100, 20));
			cboTimeUnits.setMaximumSize(new Dimension(100, 20));
			cboTimeUnits.setPreferredSize(new Dimension(100, 20));
			cboTimeUnits.setSelectedIndex(periodIndex);
			constraints.gridx = 4;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 5, 5, 20);
			generalPanel.add(cboTimeUnits, constraints);
		}

		return generalPanel;
	}

	private JPanel getDistPanel() {
		if (distPanel == null) {
			distPanel = new JPanel();
			distPanel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;

			constraints.insets = new Insets(5, 0, 5, 20);
			constraints.gridx = 0;
			constraints.gridy = 0;
			distPanel.add(getIATPanel(), constraints);

			constraints.insets = new Insets(5, 0, 5, 0);
			constraints.gridx = 1;
			constraints.gridy = 0;
			distPanel.add(getSTPanel(), constraints);
		}

		return distPanel;
	}

	private JPanel getStatsPanel() {
		if (statsPanel == null) {
			statsPanel = new JPanel();
			statsPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(Messages
							.getString("QuantAna.Simulation.ServerStats")),
					BorderFactory.createEmptyBorder(5, 5, 0, 5)));

			statsPanel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(0, 5, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.weightx = 0;
			constraints.weighty = 1;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			statsPanel.add(getServerTableScrollPane(), constraints);
			statsPanel.setMinimumSize(new Dimension(720, 140));
		}

		return statsPanel;
	}

	private JScrollPane getServerTableScrollPane() {
		if (serverTableScrollPane == null) {
			serverTableScrollPane = new JScrollPane(getServerTable());
			serverTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
			serverTableScrollPane.setWheelScrollingEnabled(true);
			serverTableScrollPane.setMinimumSize(new Dimension(720, 120));
		}
		return serverTableScrollPane;
	}

	private JTable getServerTable() {
		if (tableServers == null) {
			serverTableMatrix = new Object[numServers + 1][colServers.length];

			serverTableMatrix[0][0] = Messages.getString("QuantAna.Simulation.Process");

			for (int i = 1; i <= numServers; i++) {
				serverTableMatrix[i][0] = servNames[i-1];
			}

			serverTableModel = new ServerTableModel(colServers,
					serverTableMatrix);
			serverTableModel.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
				}
			});

			tableServers = new JTable(serverTableModel) {
				private static final long serialVersionUID = 11L;

				// Implement table header tool tips.
				protected JTableHeader createDefaultTableHeader() {
					JTableHeader jt = new JTableHeader(columnModel) {
						private static final long serialVersionUID = 12L;

						public String getToolTipText(MouseEvent e) {
							Point p = e.getPoint();
							int index = columnModel.getColumnIndexAtX(p.x);
							int realIndex = columnModel.getColumn(index)
									.getModelIndex();
							return ttipsServers[realIndex];
						}
					};
					jt.setDefaultRenderer(new MyTableHeaderRenderer());
					return jt;
				}
			};

			btnColumn = new JButton[numServers + 1];
			for (int i = 0; i < btnColumn.length; i++){
				btnColumn[i] = new JButton("...");
				btnColumn[i].setSize(20, 10);
				btnColumn[i].setEnabled(false);
				btnColumn[i].setActionCommand(new Integer(i).toString());
				btnColumn[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int row = Integer.parseInt(e.getActionCommand());
						String name = (String)serverTableModel.getValueAt(row, 0);
						new DetailsDialog(thisDialog, name);
					}
				});
			}
			
			MyTableCellRenderer mt = new MyTableCellRenderer();
			tableServers.setDefaultRenderer(Object.class, mt);
			tableServers.setDefaultEditor(Object.class, mt);
		}

		return tableServers;
	}

	private JPanel getUtilPanel() {
		if (utilPanel == null) {
			utilPanel = new JPanel();
			utilPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(Messages
							.getString("QuantAna.Simulation.ResUtil")),
					BorderFactory.createEmptyBorder(5, 5, 0, 5)));

			utilPanel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(0, 5, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.weightx = 0;
			constraints.weighty = 1;
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			utilPanel.add(getResUtilTableScrollPane(), constraints);
			utilPanel.setMinimumSize(new Dimension(720, 120));
		}
		return utilPanel;
	}

	private JScrollPane getResUtilTableScrollPane() {
		if (resUtilTableScrollPane == null) {
			resUtilTableScrollPane = new JScrollPane(getResUtilTable());
			resUtilTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
			resUtilTableScrollPane.setWheelScrollingEnabled(true);
			resUtilTableScrollPane.setMinimumSize(new Dimension(720, 120));
		}
		return resUtilTableScrollPane;
	}

	private JTable getResUtilTable() {
		if (tableResUtil == null) {
			resUtilTableMatrix = new Object[resObjNum][colResUtil.length];
			Object[] resObjNames = resAlloc.getResources().values().toArray();

			for (int i = 0; i < resObjNum; i++) {
				resUtilTableMatrix[i][0] = ((Resource) resObjNames[i])
						.getName();
			}

			resUtilTableModel = new ResUtilTableModel(colResUtil,
					resUtilTableMatrix);
			resUtilTableModel.addTableModelListener(new TableModelListener() {
				public void tableChanged(TableModelEvent e) {
				}
			});

			tableResUtil = new JTable(resUtilTableModel) {
				private static final long serialVersionUID = 11L;

				// Implement table header tool tips.
				protected JTableHeader createDefaultTableHeader() {
					JTableHeader jt = new JTableHeader(columnModel) {
						private static final long serialVersionUID = 12L;

						public String getToolTipText(MouseEvent e) {
							Point p = e.getPoint();
							int index = columnModel.getColumnIndexAtX(p.x);
							int realIndex = columnModel.getColumn(index)
									.getModelIndex();
							return ttipsResUtil[realIndex];
						}
					};
					jt.setDefaultRenderer(new MyTableHeaderRenderer());
					return jt;
				}
			};

			tableResUtil.setDefaultRenderer(Object.class,
					new MyTableCellRenderer());

			tableResUtil.setEnabled(false);
		}

		return tableResUtil;
	}

	class MyTableCellRenderer extends DefaultTableCellRenderer implements
		TableCellEditor {

		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);

			setFont(DefaultStaticConfiguration.DEFAULT_TABLE_FONT);
			setBackground(DefaultStaticConfiguration.DEFAULT_CELL_BACKGROUND_COLOR);

			if (column == 0) {
				setHorizontalAlignment(LEFT);
			} else if (column == 6) {
				setHorizontalAlignment(CENTER);
				
				return btnColumn[row];
			} else {
				setHorizontalAlignment(RIGHT);
			}
			return this;
		}
		
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			return btnColumn[row];
		}

		public String getCellEditorValue() {
			return "Edit...";
		}	
		
		public void addCellEditorListener(CellEditorListener l) {
		}

		public void cancelCellEditing() {
		}

		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		public void removeCellEditorListener(CellEditorListener l) {
		}

		public boolean shouldSelectCell(EventObject anEvent) {
			return false;
		}

		public boolean stopCellEditing() {
			return true;
		}
	}

	private JPanel getIATPanel() {
		if (iatPanel == null) {
			iatPanel = new JPanel();
			iatPanel
					.setBorder(BorderFactory
							.createCompoundBorder(
									BorderFactory
											.createTitledBorder(Messages
													.getString("QuantAna.Simulation.ArrivalRateDistribution")),
									BorderFactory.createEmptyBorder(5, 5, 0, 5)));
			GridBagConstraints constraints = new GridBagConstraints();
			iatPanel.setLayout(new GridBagLayout());
			constraints.insets = new Insets(5, 0, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			groupIAT = new ButtonGroup();
			JRadioButton optIATConstant = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Constant"), false);
			JRadioButton optIATPoisson = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Poisson"), true);
			JRadioButton optIATGaussian = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Gaussian"), false);
			optIATConstant.setHorizontalAlignment(SwingConstants.LEFT);
			optIATConstant.setActionCommand("IAT_UNIFORM");
			optIATConstant.setPreferredSize(new Dimension(100, 20));
			optIATConstant.setMinimumSize(new Dimension(100, 20));
			optIATConstant.setMaximumSize(new Dimension(100, 20));
			optIATPoisson.setHorizontalAlignment(SwingConstants.LEFT);
			optIATPoisson.setActionCommand("IAT_EXP");
			optIATPoisson.setPreferredSize(new Dimension(100, 20));
			optIATPoisson.setMinimumSize(new Dimension(100, 20));
			optIATPoisson.setMaximumSize(new Dimension(100, 20));
			optIATGaussian.setHorizontalAlignment(SwingConstants.LEFT);
			optIATGaussian.setActionCommand("IAT_GAUSS");
			optIATGaussian.setPreferredSize(new Dimension(100, 20));
			optIATGaussian.setMinimumSize(new Dimension(100, 20));
			optIATGaussian.setMaximumSize(new Dimension(100, 20));

			groupIAT.add(optIATConstant);
			groupIAT.add(optIATPoisson);
			groupIAT.add(optIATGaussian);

			txtIATInterval = new JTextField();
			txtIATInterval.setEnabled(false);
			txtIATInterval.setMinimumSize(new Dimension(40, 20));
			txtIATInterval.setMaximumSize(new Dimension(40, 20));
			txtIATInterval.setPreferredSize(new Dimension(40, 20));
			txtIATStdDev = new JTextField();
			txtIATStdDev.setEnabled(false);
			txtIATStdDev.setMinimumSize(new Dimension(40, 20));
			txtIATStdDev.setMaximumSize(new Dimension(40, 20));
			txtIATStdDev.setPreferredSize(new Dimension(40, 20));
			JLabel lblInterval = new JLabel(Messages
					.getString("QuantAna.Simulation.Interval"));
			lblInterval.setMinimumSize(new Dimension(120, 20));
			lblInterval.setMaximumSize(new Dimension(120, 20));
			lblInterval.setPreferredSize(new Dimension(120, 20));
			lblInterval.setHorizontalAlignment(SwingConstants.RIGHT);
			JLabel lblDeviation = new JLabel(Messages
					.getString("QuantAna.Simulation.Deviation"));
			lblDeviation.setMinimumSize(new Dimension(120, 20));
			lblDeviation.setMaximumSize(new Dimension(120, 20));
			lblDeviation.setPreferredSize(new Dimension(120, 20));
			lblDeviation.setHorizontalAlignment(SwingConstants.RIGHT);

			constraints.gridx = 0;
			constraints.gridy = 0;
			iatPanel.add(optIATConstant, constraints);
			constraints.gridx = 1;
			constraints.gridy = 0;
			iatPanel.add(lblInterval, constraints);
			constraints.gridx = 2;
			constraints.gridy = 0;
			iatPanel.add(txtIATInterval, constraints);
			constraints.gridx = 3;
			constraints.gridy = 0;
			iatPanel.add(new JLabel("%"), constraints);
			constraints.gridx = 0;
			constraints.gridy = 1;
			iatPanel.add(optIATPoisson, constraints);
			constraints.gridx = 0;
			constraints.gridy = 2;
			iatPanel.add(optIATGaussian, constraints);
			constraints.gridx = 1;
			constraints.gridy = 2;
			iatPanel.add(lblDeviation, constraints);
			constraints.gridx = 2;
			constraints.gridy = 2;
			iatPanel.add(txtIATStdDev, constraints);

			optIATConstant.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtIATInterval.setEnabled(true);
					txtIATStdDev.setEnabled(false);
				}
			});

			optIATPoisson.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtIATInterval.setEnabled(false);
					txtIATStdDev.setEnabled(false);
				}
			});

			optIATGaussian.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtIATInterval.setEnabled(false);
					txtIATStdDev.setEnabled(true);
				}
			});
		}

		return iatPanel;
	}

	private JPanel getSTPanel() {
		if (stPanel == null) {
			stPanel = new JPanel();
			stPanel
					.setBorder(BorderFactory
							.createCompoundBorder(
									BorderFactory
											.createTitledBorder(Messages
													.getString("QuantAna.Simulation.ServiceTimeDistribution")),
									BorderFactory.createEmptyBorder(5, 5, 0, 5)));

			GridBagConstraints constraints = new GridBagConstraints();
			stPanel.setLayout(new GridBagLayout());
			constraints.insets = new Insets(5, 0, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 1;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;

			groupST = new ButtonGroup();
			JRadioButton optSTConstant = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Constant"), false);
			JRadioButton optSTPoisson = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Poisson"), true);
			JRadioButton optSTGaussian = new JRadioButton(Messages
					.getString("QuantAna.Simulation.Gaussian"), false);
			optSTConstant.setHorizontalAlignment(SwingConstants.LEFT);
			optSTConstant.setActionCommand("ST_UNIFORM");
			optSTConstant.setPreferredSize(new Dimension(100, 20));
			optSTConstant.setMinimumSize(new Dimension(100, 20));
			optSTConstant.setMaximumSize(new Dimension(100, 20));
			optSTPoisson.setHorizontalAlignment(SwingConstants.LEFT);
			optSTPoisson.setActionCommand("ST_EXP");
			optSTPoisson.setPreferredSize(new Dimension(100, 20));
			optSTPoisson.setMinimumSize(new Dimension(100, 20));
			optSTPoisson.setMaximumSize(new Dimension(100, 20));
			optSTGaussian.setHorizontalAlignment(SwingConstants.LEFT);
			optSTGaussian.setActionCommand("ST_GAUSS");
			optSTGaussian.setPreferredSize(new Dimension(100, 20));
			optSTGaussian.setMinimumSize(new Dimension(100, 20));
			optSTGaussian.setMaximumSize(new Dimension(100, 20));

			groupST.add(optSTConstant);
			groupST.add(optSTPoisson);
			groupST.add(optSTGaussian);

			txtSTInterval = new JTextField();
			txtSTInterval.setEnabled(false);
			txtSTInterval.setMinimumSize(new Dimension(40, 20));
			txtSTInterval.setMaximumSize(new Dimension(40, 20));
			txtSTInterval.setPreferredSize(new Dimension(40, 20));
			txtSTStdDev = new JTextField();
			txtSTStdDev.setEnabled(false);
			txtSTStdDev.setMinimumSize(new Dimension(40, 20));
			txtSTStdDev.setMaximumSize(new Dimension(40, 20));
			txtSTStdDev.setPreferredSize(new Dimension(40, 20));
			JLabel lblInterval = new JLabel(Messages
					.getString("QuantAna.Simulation.Interval"));
			lblInterval.setMinimumSize(new Dimension(120, 20));
			lblInterval.setMaximumSize(new Dimension(120, 20));
			lblInterval.setPreferredSize(new Dimension(120, 20));
			lblInterval.setHorizontalAlignment(SwingConstants.RIGHT);
			JLabel lblDeviation = new JLabel(Messages
					.getString("QuantAna.Simulation.Deviation"));
			lblDeviation.setMinimumSize(new Dimension(120, 20));
			lblDeviation.setMaximumSize(new Dimension(120, 20));
			lblDeviation.setPreferredSize(new Dimension(120, 20));
			lblDeviation.setHorizontalAlignment(SwingConstants.RIGHT);

			constraints.gridx = 0;
			constraints.gridy = 0;
			stPanel.add(optSTConstant, constraints);
			constraints.gridx = 1;
			constraints.gridy = 0;
			stPanel.add(lblInterval, constraints);
			constraints.gridx = 2;
			constraints.gridy = 0;
			stPanel.add(txtSTInterval, constraints);
			constraints.gridx = 3;
			constraints.gridy = 0;
			stPanel.add(new JLabel("%"), constraints);
			constraints.gridx = 0;
			constraints.gridy = 1;
			stPanel.add(optSTPoisson, constraints);
			constraints.gridx = 0;
			constraints.gridy = 2;
			stPanel.add(optSTGaussian, constraints);
			constraints.gridx = 1;
			constraints.gridy = 2;
			stPanel.add(lblDeviation, constraints);
			constraints.gridx = 2;
			constraints.gridy = 2;
			stPanel.add(txtSTStdDev, constraints);

			optSTConstant.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtSTInterval.setEnabled(true);
					txtSTStdDev.setEnabled(false);
				}
			});

			optSTPoisson.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtSTInterval.setEnabled(false);
					txtSTStdDev.setEnabled(false);
				}
			});

			optSTGaussian.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					txtSTInterval.setEnabled(false);
					txtSTStdDev.setEnabled(true);
				}
			});
		}

		return stPanel;
	}

	private JPanel getQueuePanel() {
		if (queuePanel == null) {
			queuePanel = new JPanel();
			queuePanel
					.setBorder(BorderFactory
							.createCompoundBorder(
									BorderFactory
											.createTitledBorder(Messages
													.getString("QuantAna.Simulation.QueueingDiscipline")),
									BorderFactory.createEmptyBorder(5, 5, 0, 5)));

			GridBagConstraints constraints = new GridBagConstraints();
			queuePanel.setLayout(new GridBagLayout());
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.FIRST_LINE_START;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			groupQD = new ButtonGroup();

			JRadioButton opt_q_1 = new JRadioButton(Messages
					.getString("QuantAna.Simulation.QueueingFIFO"), true);
			opt_q_1.setPreferredSize(new Dimension(180, 20));
			opt_q_1.setMinimumSize(new Dimension(180, 20));
			opt_q_1.setMaximumSize(new Dimension(180, 20));
			opt_q_1.setActionCommand("QUEUE_FIFO");
			groupQD.add(opt_q_1);
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 10, 5, 50);
			queuePanel.add(opt_q_1, constraints);

			JRadioButton opt_q_2 = new JRadioButton(Messages
					.getString("QuantAna.Simulation.QueueingLIFO"), false);
			opt_q_2.setPreferredSize(new Dimension(180, 20));
			opt_q_2.setMinimumSize(new Dimension(180, 20));
			opt_q_2.setMaximumSize(new Dimension(180, 20));
			opt_q_2.setActionCommand("QUEUE_LIFO");
			groupQD.add(opt_q_2);
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.insets = new Insets(5, 10, 40, 50);
			queuePanel.add(opt_q_2, constraints);
		}
		return queuePanel;
	}

	private JPanel getTermPanel() {
		if (termPanel == null) {
			termPanel = new JPanel();
			termPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(Messages
							.getString("QuantAna.Simulation.TerminationRule")),
					BorderFactory.createEmptyBorder(5, 5, 0, 5)));

			GridBagConstraints constraints = new GridBagConstraints();
			termPanel.setLayout(new GridBagLayout());
			constraints.fill = GridBagConstraints.BOTH;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;

			JLabel lblRuns = new JLabel(Messages
					.getString("QuantAna.Simulation.NumRuns"));
			lblRuns.setPreferredSize(new Dimension(80, 20));
			lblRuns.setMinimumSize(new Dimension(80, 20));
			lblRuns.setMaximumSize(new Dimension(80, 20));
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 15, 5, 5);
			termPanel.add(lblRuns, constraints);

			txtRuns = new JTextField("1");
			txtRuns.setPreferredSize(new Dimension(80, 20));
			txtRuns.setMinimumSize(new Dimension(80, 20));
			txtRuns.setMaximumSize(new Dimension(80, 20));
			txtRuns.setHorizontalAlignment(SwingConstants.RIGHT);
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.insets = new Insets(5, 10, 5, 20);
			termPanel.add(txtRuns, constraints);

			stop1 = new JCheckBox(Messages
					.getString("QuantAna.Simulation.CasesCompleted"));
			stop1.setSelected(true);
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.insets = new Insets(5, 10, 5, 5);
			termPanel.add(stop1, constraints);

			stop2 = new JCheckBox(Messages
					.getString("QuantAna.Simulation.TimeElapsed"));
			stop2.setSelected(true);
			constraints.gridx = 0;
			constraints.gridy = 2;
			termPanel.add(stop2, constraints);
		}
		return termPanel;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(5, 25, 5, 10);
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;

			JButton btnStart = new JButton();
			btnStart.setText(Messages.getTitle("QuantAna.Button.Start"));
			btnStart.setIcon(Messages.getImageIcon("QuantAna.Button.Start"));
			btnStart.setMinimumSize(new Dimension(120, 25));
			btnStart.setMaximumSize(new Dimension(120, 25));
			btnStart.setPreferredSize(new Dimension(120, 25));
			btnStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					checkParams();
					if(!errorDetected) startSimulation();
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 0;
			buttonPanel.add(btnStart, constraints);

			JButton btnConf = new JButton();
			btnConf.setText(Messages.getTitle("QuantAna.Button.TimeModel"));
			btnConf.setIcon(Messages.getImageIcon("QuantAna.Button.TimeModel"));
			btnConf.setMinimumSize(new Dimension(120, 25));
			btnConf.setMaximumSize(new Dimension(120, 25));
			btnConf.setPreferredSize(new Dimension(120, 25));
			btnConf.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					getTimeModelDialog();
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 1;
			buttonPanel.add(btnConf, constraints);

			btnProtocol = new JButton();
			btnProtocol.setText(Messages.getTitle("QuantAna.Button.Protocol"));
			btnProtocol.setIcon(Messages.getImageIcon("QuantAna.Button.Protocol"));
			btnProtocol.setEnabled(false);
			btnProtocol.setMinimumSize(new Dimension(120, 25));
			btnProtocol.setMaximumSize(new Dimension(120, 25));
			btnProtocol.setPreferredSize(new Dimension(120, 25));
			btnProtocol.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					new ProtocolDialog(thisDialog, sim.getProtocolContent());
				}
			});

			constraints.gridx = 0;
			constraints.gridy = 2;
			buttonPanel.add(btnProtocol, constraints);

			btnExport = new JButton();
			btnExport.setText(Messages.getTitle("QuantAna.Button.Export"));
			btnExport.setIcon(Messages.getImageIcon("QuantAna.Button.Export"));
			btnExport.setEnabled(false);
			btnExport.setMinimumSize(new Dimension(120, 25));
			btnExport.setMaximumSize(new Dimension(120, 25));
			btnExport.setPreferredSize(new Dimension(120, 25));
			btnExport.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					export = new ExportStatistics((QuantitativeSimulationDialog)thisDialog);
					
					dir = new File(ConfigurationManager.getConfiguration().getLogdir());
					
					fileChooser = new JFileChooser();
					getFileFilter();
					save(Integer.parseInt(txtRuns.getText()));
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 3;
			buttonPanel.add(btnExport, constraints);

			btnDiagram = new JButton();
			btnDiagram.setText(Messages.getTitle("QuantAna.Button.Diagram"));
			btnDiagram.setIcon(Messages.getImageIcon("QuantAna.Button.Diagram"));
			btnDiagram.setEnabled(false);
			btnDiagram.setMinimumSize(new Dimension(120, 25));
			btnDiagram.setMaximumSize(new Dimension(120, 25));
			btnDiagram.setPreferredSize(new Dimension(120, 25));
			btnDiagram.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					new ServerStatisticsDialog(thisDialog);
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 4;
			buttonPanel.add(btnDiagram, constraints);

			JButton btnClose = new JButton();
			btnClose.setText(Messages.getTitle("QuantAna.Button.Close"));
			btnClose.setIcon(Messages.getImageIcon("QuantAna.Button.Close"));
			btnClose.setMinimumSize(new Dimension(120, 25));
			btnClose.setMaximumSize(new Dimension(120, 25));
			btnClose.setPreferredSize(new Dimension(120, 25));
			btnClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});
			constraints.gridx = 0;
			constraints.gridy = 5;
			buttonPanel.add(btnClose, constraints);
		}

		return buttonPanel;
	}

	private void getTimeModelDialog() {
		new TimeModelDialog(this, tm);
	}
	
	public void updTimeModel(){
		lambda = Double.parseDouble(txtLambda.getText());
		periodIndex = cboTimeUnits.getSelectedIndex();
		period = tm.cv(periodIndex, Double.parseDouble(txtPeriod.getText()));
		
		if (serverTableModel.getValueAt(0, 4) != null){
			ServerTableModel stm = serverTableModel;
			
			for (int i = 0; i < stm.getRowCount(); i++){
				double val;
				Object o = stm.getValueAt(i, 4);
				if (o instanceof String)
					val = Double.parseDouble(((String)o));
				else
					val = ((Double)o).doubleValue();
				val = tm.cv(timeUnit, val);
				stm.setValueAt(val, i, 4);
				
				o = stm.getValueAt(i, 5);
				if (o instanceof String)
					val = Double.parseDouble((String)o);
				else
					val = ((Double)o).doubleValue();
						
				val = tm.cv(timeUnit, val);
				stm.setValueAt(val, i, 5);
			}
		}
		
		timeUnit = tm.getStdUnit();
	}

	public void updContents() {
		ServerTableModel stm = serverTableModel;
		ResUtilTableModel rtm = resUtilTableModel;
		HashMap<String, Server> serv = sim.getServerList();
		HashMap<String, Resource> res = resAlloc.getResources();
		
		simStatistics = sim.getRunStats();
		RunStats rs = simStatistics.get(simStatistics.size() - 1);
		double l_ = lambda / period;
		
		stm.setValueAt(String.format("%,.2f", rs.getProcCompTime()*l_), 0, 1);
		stm.setValueAt(String.format("%,.2f", rs.getProcWaitTime()*l_), 0, 2);
		stm.setValueAt(String.format("%,.2f", rs.getProcServTime()*l_), 0, 3);
		stm.setValueAt(String.format("%,.2f", rs.getProcCompTime()), 0, 4);
		stm.setValueAt(String.format("%,.2f", rs.getProcCompTime()-rs.getProcServTime()), 0, 5);
		
		for (int i = 1; i <= numServers; i++) {
			String id = produceID((String) stm.getValueAt(i, 0));
			Server s = serv.get(id);
			ReportServerStats sst = (ReportServerStats)rs.getServStats().get(s);
			
			stm.setValueAt(String.format("%,.2f", sst.getAvgQLength()+sst.getAvgResNumber()), i, 1);
			stm.setValueAt(String.format("%,.2f", sst.getAvgQLength()), i, 2);
			stm.setValueAt(String.format("%,.2f", sst.getAvgResNumber()), i, 3);
			stm.setValueAt(String.format("%,.2f", sst.getAvgServTime()+sst.getAvgWaitTime()), i, 4);
			stm.setValueAt(String.format("%,.2f", sst.getAvgWaitTime()), i, 5);
		}
		
		for (int i = 0; i < resObjNum; i++) {
			String name = (String) rtm.getValueAt(i, 0);
			Resource r = res.get(name);
			ResourceStats rst = rs.getResStats().get(r);
			
			String util = String.format("%,.2f", rst.getUtilizationRatio()*100);
			rtm.setValueAt(util, i, 1);
		}
	}

	private String produceID(String key) {
		if (key.equals("Protocol") || key.equals("Process"))
			return key;
		else
			return key.substring(key.indexOf("(") + 1, key.indexOf(")"));
	}

	public WorkflowNetGraph getGraph() {
		return graph;
	}
 
	private void startSimulation() {
		WaitDialog wd = new WaitDialog(this, Messages.getString("QuantAna.Simulation.Wait")); 
		wd.start();
		LoggerManager.info(Constants.QUANTANA_LOGGER, Messages
				.getString("QuantAna.Started"));

		updTimeModel();
		SimParameters sp = new SimParameters(lambda, period);
		sp.setRuns(Integer.parseInt(txtRuns.getText()));

		String op1 = groupIAT.getSelection().getActionCommand();
		if (op1.equals("IAT_UNIFORM")) {
			sp.setDistCases(ProbabilityDistribution.DIST_TYPE_UNIFORM);
			double cp = Double.parseDouble(txtIATInterval.getText()) / 100;
			sp.setCParam(cp);
		} else if (op1.equals("IAT_GAUSS")) {
			sp.setDistCases(ProbabilityDistribution.DIST_TYPE_GAUSS);
			double cp = Double.parseDouble(txtIATStdDev.getText());
			sp.setCParam(cp);
		} else {
			sp.setDistCases(ProbabilityDistribution.DIST_TYPE_EXP);
		}

		String op2 = groupST.getSelection().getActionCommand();
		if (op2.equals("ST_UNIFORM")) {
			sp.setDistServ(ProbabilityDistribution.DIST_TYPE_UNIFORM);
			double spa = Double.parseDouble(txtSTInterval.getText()) / 100;
			sp.setSParam(spa);
		} else if (op2.equals("ST_GAUSS")) {
			sp.setDistServ(ProbabilityDistribution.DIST_TYPE_GAUSS);
			double spa = Double.parseDouble(txtSTStdDev.getText());
			sp.setSParam(spa);
		} else {
			sp.setDistServ(ProbabilityDistribution.DIST_TYPE_EXP);
		}

		String op3 = groupQD.getSelection().getActionCommand();
		if (op3.equals("QUEUE_LIFO")) {
			sp.setQueue(Simulator.QD_LIFO);
		} else {
			sp.setQueue(Simulator.QD_FIFO);
		}

		if (stop1.isSelected()) {
			if (stop2.isSelected())
				sp.setStop(Simulator.STOP_BOTH);
			else
				sp.setStop(Simulator.STOP_CASE_DRIVEN);
		} else if (stop2.isSelected()) {
			sp.setStop(Simulator.STOP_TIME_DRIVEN);
		} else {
			sp.setStop(Simulator.STOP_NONE);
		}

		if (groupRoleNum > 2 && resObjNum > 1) {
			sp.setResUse(Simulator.RES_USED);
		} else {
			sp.setResUse(Simulator.RES_NOT_USED);
		}

		sim = new Simulator(graph, new ResourceUtilization(resAlloc), sp, wd);
		sim.start();

		activateDetails();
		updContents();
		wd.stop();
		sim.setDuration(wd.getDuration());
	}

	private void initResourceAlloc() {
		PetriNetModelProcessor pmp = (PetriNetModelProcessor) editor
				.getModelProcessor();

		ArrayList<String> roles = new ArrayList<String>();
		ArrayList<String> groups = new ArrayList<String>();
		Vector rVec = (Vector) pmp.getRoles();
		Vector gVec = (Vector) pmp.getOrganizationUnits();

		groupRoleNum = rVec.size() + gVec.size();

		for (int i = 0; i < rVec.size(); i++)
			roles.add(((ResourceClassModel) rVec.get(i)).getName());

		for (int i = 0; i < gVec.size(); i++)
			groups.add(((ResourceClassModel) gVec.get(i)).getName());

		Iterator iter = getTransModels().iterator();

		resAlloc = new ResourceAllocation(roles, groups, iter, pmp);

		resObjNum = resAlloc.getResources().size();
	}
	
	private LinkedList<TransitionModel> getTransModels() {
		LinkedList<TransitionModel> lst = new LinkedList<TransitionModel>();
		ArrayList<String> ids = new ArrayList<String>();
		Node[] nodes = graph.getNodeArray();

		for (int i = 0; i < nodes.length; i++)
			if (graph.isTransition(nodes[i].getId()))
				ids.add(nodes[i].getId());

		for (int i = 0; i < ids.size(); i++) {
			lst.add((TransitionModel) mec.getElementById(ids.get(i)));
		}

		return lst;
	}

	public Simulator getSimulator() {
		return sim;
	}

	private void activateDetails() {
		for (JButton b : btnColumn){
			b.setEnabled(true);
//			b.repaint();
		}
		btnProtocol.setEnabled(true);
		btnExport.setEnabled(true);
		btnDiagram.setEnabled(true);
	}
	
	public void mouseMoved(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public void mouseDragged(MouseEvent e) {
	}

	public ResUtilTableModel getResUtilTableModel() {
		return resUtilTableModel;
	}

	public ServerTableModel getServerTableModel() {
		return serverTableModel;
	}
	
	private void makeTasksAndResources(){
		String[] tasks = graph.getTransitionsGT0();
		tasksAndResources = new TasksResourcesAllocation();
		
		for (String s : tasks){
			tasksAndResources.addTaskResourcesPair(s, resAlloc.getResourcesPerTask(s));
		}
	}

	public TasksResourcesAllocation getTasksAndResources() {
		return tasksAndResources;
	}
	
	private void save(int runs){
		fileChooser.setCurrentDirectory(dir);
		fileChooser.setMultiSelectionEnabled(false);

		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION){
			String fname = fileChooser.getSelectedFile().getAbsolutePath();
			dir = fileChooser.getCurrentDirectory();
			String ext = "";
			int idx = fname.lastIndexOf(".");
			if (idx > -1){
				ext = fname.substring(idx + 1);
				fname = fname.substring(0, idx);
			} else {
				ext = "csv";
			}
			
			String text = "";
			
			if (ext.equals("")) ext = "csv";
			
			for (int i = 0; i <= runs; i++){
				if (i < runs)
					text = export.getStatsTable(i, false);
				else
					text = export.getStatsTable(i, true);
				
				try {
					if (ext.equals("csv")) {
						FileWriter fw;
						if (i == runs)
							fw = new FileWriter(fname + "." + ext);
						else 
							fw = new FileWriter(fname + "_" + (i+1) + "." + ext);
						fw.write(text);
						fw.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void getFileFilter(){
		eff.addExtension("csv");
		eff.setDescription(Messages.getString("QuantAna.Simulation.Export.FileFilter"));
		fileChooser.setFileFilter(eff);
	}
	
	class ExtensionFileFilter extends FileFilter {

		private ArrayList<String> extensions = new ArrayList<String>();
		private String description = " ";

		public void addExtension(String ext){
			if (!ext.startsWith(".")) ext = "." + ext;
			extensions.add(ext.toLowerCase());
		}

		public boolean accept(File f){
			if (f.isDirectory()) return true;

			String name = f.getName().toLowerCase();
			for (String ext : extensions)
				if (name.endsWith(ext))
					return true;

			return false;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	private void checkParams() throws InvalidRunsException, InvalidIntervalException {
		double tmp;
		int i;
		boolean isInteger = false;
		JTextField tf = null;

		try {
			tf = txtLambda;
			isInteger = false;
			tmp = Double.parseDouble(txtLambda.getText());

			tf = txtPeriod;
			isInteger = false;
			tmp = Double.parseDouble(txtPeriod.getText());

			tf = txtIATStdDev;
			if (tf.isEnabled()){
				isInteger = false;
				tmp = Double.parseDouble(txtIATStdDev.getText());
			}

			tf = txtSTStdDev;
			if (tf.isEnabled()){
				isInteger = false;
				tmp = Double.parseDouble(txtSTStdDev.getText());
			}

			tf = txtRuns;
			isInteger = true;
			i = Integer.parseInt(txtRuns.getText());
			if (i < 1) throw new InvalidRunsException();

			tf = txtIATInterval;
			if (tf.isEnabled()){
				isInteger = true;
				i = Integer.parseInt(txtIATInterval.getText());
				if ((i < 0) || (i > 100)) throw new InvalidIntervalException();
			}

			tf = txtSTInterval;
			if (tf.isEnabled()){
				isInteger = true;
				i = Integer.parseInt(txtSTInterval.getText());
				if ((i < 0) || (i > 100)) throw new InvalidIntervalException();
			}

			errorDetected = false;
		} catch(InvalidRunsException ire){
			JOptionPane.showMessageDialog(null, Messages.getString("QuantAna.Message.InvalidRuns"));
			tf.requestFocus();
			tf.selectAll();
			errorDetected = true;
		} catch(InvalidIntervalException iie){
			JOptionPane.showMessageDialog(null, Messages.getString("QuantAna.Message.InvalidInterval"));
			tf.requestFocus();
			tf.selectAll();
			errorDetected = true;
		} catch(NumberFormatException nfe) {
			if (isInteger)
				JOptionPane.showMessageDialog(null, Messages.getString("QuantAna.Message.NumberFormatErrorInt"));
			else
				JOptionPane.showMessageDialog(null, Messages.getString("QuantAna.Message.NumberFormatErrorDouble"));
			tf.requestFocus();
			tf.selectAll();
			errorDetected = true;
		}
	}
	
	class InvalidRunsException extends NumberFormatException {
		private static final long serialVersionUID = 1L;
		
		public InvalidRunsException() {}
		
		public InvalidRunsException(String msg){
			super(msg);
		}
	}

	
	class InvalidIntervalException extends NumberFormatException {
		private static final long serialVersionUID = 1L;
		
		public InvalidIntervalException() {}
		
		public InvalidIntervalException(String msg){
			super(msg);
		}
	}
} // @jve:decl-index=0:visual-constraint="4,4"
