package net.sourceforge.squirrel_sql.client.mainframe.action;

import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.action.SquirrelAction;
import net.sourceforge.squirrel_sql.client.gui.db.IAliasesList;
import net.sourceforge.squirrel_sql.client.gui.db.ICompletionCallback;
import net.sourceforge.squirrel_sql.client.gui.db.SQLAlias;
import net.sourceforge.squirrel_sql.client.gui.recentfiles.RecentFilesController;
import net.sourceforge.squirrel_sql.client.gui.session.SessionInternalFrame;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.fw.sql.ISQLConnection;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class AliasFileOpenAction extends SquirrelAction
{
   private static final StringManager s_stringMgr = StringManagerFactory.getStringManager(AliasFileOpenAction.class);

   private IAliasesList _aliasList;

   public AliasFileOpenAction(IApplication app, IAliasesList al)
   {
      super(app);
      _aliasList = al;
   }

   public void actionPerformed(ActionEvent e)
   {
      SQLAlias selectedAlias = _aliasList.getSelectedAlias(null);

      if(null == selectedAlias)
      {
         getApplication().getMessageHandler().showWarningMessage(s_stringMgr.getString("AliasFileOpenAction.noAliasSelected"));
         return;
      }

      final File fileToOpen = new RecentFilesController(getApplication(), selectedAlias).getFileToOpen();

      if(null == fileToOpen)
      {
         return;
      }


      ICompletionCallback callback = new ICompletionCallback()
      {
         @Override
         public void connected(ISQLConnection conn)
         {
         }

         @Override
         public void sessionCreated(ISession session)
         {
         }

         @Override
         public void sessionInternalFrameCreated(SessionInternalFrame sessionInternalFrame)
         {
            onSessionInternalFrameCreated(sessionInternalFrame, fileToOpen);
         }

         @Override
         public void errorOccured(Throwable th, boolean stopConnection)
         {
         }
      };

      new ConnectToAliasCommand(getApplication(), selectedAlias, true, callback).execute();



   }

   private void onSessionInternalFrameCreated(final SessionInternalFrame sessionInternalFrame, final File fileToOpen)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            sessionInternalFrame.getMainSQLPanelAPI().fileOpen(fileToOpen);
         }
      });
   }

}
