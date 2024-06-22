package net.sourceforge.squirrel_sql.fw.gui.table.columndisplaychoice;

import net.sourceforge.squirrel_sql.client.Main;
import net.sourceforge.squirrel_sql.client.action.SquirrelAction;
import net.sourceforge.squirrel_sql.client.session.ISQLPanelAPI;
import net.sourceforge.squirrel_sql.client.session.action.ISQLPanelAction;
import net.sourceforge.squirrel_sql.client.session.mainpanel.ResultTab;
import net.sourceforge.squirrel_sql.client.session.mainpanel.TabButton;
import net.sourceforge.squirrel_sql.client.session.mainpanel.resulttabactions.ResultTabProvider;
import net.sourceforge.squirrel_sql.fw.datasetviewer.ColumnDisplayDefinition;
import net.sourceforge.squirrel_sql.fw.datasetviewer.DataSetViewerTable;
import net.sourceforge.squirrel_sql.fw.datasetviewer.DataSetViewerTablePanel;
import net.sourceforge.squirrel_sql.fw.datasetviewer.ExtTableColumn;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;

public class ColumnDisplayChoiceAction extends SquirrelAction implements ISQLPanelAction
{
   private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(ColumnDisplayChoiceAction.class);
   private static ILogger s_log = LoggerController.createLogger(ColumnDisplayChoiceAction.class);

   private final ResultTabProvider _resultTabProvider;

   public ColumnDisplayChoiceAction(ResultTab resultTab)
   {
      super(Main.getApplication());
      _resultTabProvider = new ResultTabProvider(resultTab);
   }

   public ColumnDisplayChoiceAction()
   {
      this(null);
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      if(false == _resultTabProvider.hasResultTab())
      {
         return;
      }

      if(false == _resultTabProvider.getResultTab().getSQLResultDataSetViewer() instanceof DataSetViewerTablePanel)
      {
         Main.getApplication().getMessageHandler().showWarningMessage(s_stringMgr.getString("ColumnDisplayChoiceAction.display.choice.for.table.only"));
         s_log.warn(s_stringMgr.getString("ColumnDisplayChoiceAction.display.choice.for.table.only"));
         return;
      }

      if(false == e.getSource() instanceof TabButton)
      {
         s_log.error("ColumnDisplayChoiceAction.actionPerformed() called with unknown source.");
      }

      TabButton tabButton = (TabButton) e.getSource();


      DataSetViewerTable table = ((DataSetViewerTablePanel) _resultTabProvider.getResultTab().getSQLResultDataSetViewer()).getTable();

      int selCol = table.getSelectedColumn();
      int selRow = table.getSelectedRow();

      JPopupMenu popupMenu = new JPopupMenu();

      if(-1 == selCol || -1 == selRow)
      {
         JMenuItem mnuNoQuickCoice = new JMenuItem(s_stringMgr.getString("ColumnDisplayChoiceAction.click.cell.for.quick.choice"));
         mnuNoQuickCoice.setEnabled(false);
         popupMenu.add(mnuNoQuickCoice);
      }
      else
      {
         TableColumn col = table.getColumnModel().getColumn(selCol);
         if(col instanceof ExtTableColumn)
         {
            ColumnDisplayDefinition colDisp = ((ExtTableColumn) col).getColumnDisplayDefinition();

            JMenuItem mnuDisplayAsImage = new JMenuItem(s_stringMgr.getString("ColumnDisplayChoiceAction.display.image", colDisp.getFullTableColumnName(), colDisp.getSqlTypeName()));
            popupMenu.add(mnuDisplayAsImage);
         }
         else
         {
            JMenuItem mnuNoChoiceFOrCol = new JMenuItem(s_stringMgr.getString("ColumnDisplayChoiceAction.cannot.offer.display.choice.for.selected.cell"));
            mnuNoChoiceFOrCol.setEnabled(false);
            popupMenu.add(mnuNoChoiceFOrCol);
         }
      }

      JMenuItem mnuMore = new JMenuItem(s_stringMgr.getString("ColumnDisplayChoiceAction.more"));
      popupMenu.add(mnuMore);


      popupMenu.show(tabButton, 0, tabButton.getHeight());
   }

   @Override
   public void setSQLPanel(ISQLPanelAPI sqlPanelApi)
   {
      _resultTabProvider.setSQLPanelAPI(sqlPanelApi);
      setEnabled(null != sqlPanelApi);
   }
}
