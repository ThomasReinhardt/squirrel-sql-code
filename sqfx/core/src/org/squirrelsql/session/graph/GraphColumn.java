package org.squirrelsql.session.graph;

import org.squirrelsql.services.SQLUtil;
import org.squirrelsql.session.ColumnInfo;
import org.squirrelsql.session.TableInfo;

import java.util.*;

public class GraphColumn
{
   private ColumnInfo _columnInfo;
   private final PrimaryKeyInfo _primaryKeyInfo;
   private final ImportedKeysInfo _impKeysInfo;
   private String _postFix;
   private HashMap<String, NonDbImportedKey> _nonDbImportedKeyByNonDbFkId = new HashMap<>();
   private HashSet<String> _nonDbFkIdsPointingAtMe = new HashSet<>();

   public GraphColumn(ColumnInfo columnInfo, PrimaryKeyInfo primaryKeyInfo, ImportedKeysInfo impKeysInfo, NonDbColumnImportPersistence pers, GraphChannel graphChannel)
   {
      _columnInfo = columnInfo;
      _primaryKeyInfo = primaryKeyInfo;
      _impKeysInfo = impKeysInfo;

      _postFix = " ";

      if(_primaryKeyInfo.belongsToPk(_columnInfo))
      {
         _postFix += "(PK)";
      }

      if(_impKeysInfo.isFk(_columnInfo))
      {
         _postFix += "(FK)";
      }


      if(null != pers)
      {
         graphChannel.addAllTablesAddedListener(() -> onAllTablesAdded(pers, graphChannel));
      }

   }

   private void onAllTablesAdded(NonDbColumnImportPersistence pers, GraphChannel graphChannel)
   {
      _nonDbFkIdsPointingAtMe = pers.getNonDbFkIdsPointingAtMe();
      _nonDbImportedKeyByNonDbFkId = NonDbColumnImportPersistence.toNonDbImportedKeyByNonDbFkId(pers, graphChannel.getGraphFinder());
   }

   public String getDescription()
   {
      return _columnInfo.getDescription() + _postFix;
   }

   public boolean belongsToPk()
   {
      return _primaryKeyInfo.belongsToPk(_columnInfo);
   }

   public boolean belongsToFk(String fkNameOrId)
   {
      return _impKeysInfo.belongsToFk(fkNameOrId, _columnInfo);
   }

   public String getFkNameTo(TableInfo toPkTable)
   {
      return _impKeysInfo.getFkNameTo(toPkTable, _columnInfo);
   }


   public void addNonDbImportedKey(NonDbImportedKey nonDbImportedKey)
   {
      _nonDbImportedKeyByNonDbFkId.put(nonDbImportedKey.getNonDbFkId(), nonDbImportedKey);
   }

   public boolean doesNonDbFkIdPointAtMe(String nonDbFkId)
   {
      return _nonDbFkIdsPointingAtMe.contains(nonDbFkId);
   }

   public void addNonDbFkIdPointingAtMe(String nonDbFkId)
   {
      _nonDbFkIdsPointingAtMe.add(nonDbFkId);
   }

   public ArrayList<NonDbImportedKey> getNonDbImportedKeys()
   {
      return new ArrayList<>(_nonDbImportedKeyByNonDbFkId.values());
   }

   public NonDbImportedKey getNonDbImportedKeyForId(String nonDbFkId)
   {
      return _nonDbImportedKeyByNonDbFkId.get(nonDbFkId);
   }


   public boolean importsNonDbFkId(String nonDbFkId)
   {
      return _nonDbImportedKeyByNonDbFkId.containsKey(nonDbFkId);
   }

   public boolean isMyNonDbPkCol(GraphColumn pkCol, String nonDbFkId)
   {
      NonDbImportedKey nonDbImportedKey = _nonDbImportedKeyByNonDbFkId.get(nonDbFkId);

      if(null == nonDbImportedKey)
      {
         return false;
      }

      return nonDbImportedKey.getColumnThisImportedKeyPointsTo().matches(pkCol);
   }

   private boolean matches(GraphColumn other)
   {
      return _columnInfo.matches(other._columnInfo);
   }

   public ColumnInfo getColumnInfo()
   {
      return _columnInfo;
   }

   @Override
   public String toString()
   {
      return _columnInfo.getDescription();
   }

   public void removeNonDbFkId(String nonDbFkId)
   {
      _nonDbImportedKeyByNonDbFkId.remove(nonDbFkId);
      _nonDbFkIdsPointingAtMe.remove(nonDbFkId);
   }

   public NonDbColumnImportPersistence getNonDbImportPersistence()
   {
      NonDbColumnImportPersistence ret = new NonDbColumnImportPersistence(_columnInfo.getCatalogName(), _columnInfo.getSchemaName(), _columnInfo.getTableName(), _columnInfo.getColName());

      ret.setNonDbFkIdsPointingAtMe(_nonDbFkIdsPointingAtMe);

      HashMap<String, NonDbImportedKeyPersistence> buf = new HashMap<>();
      for (Map.Entry<String, NonDbImportedKey> entry : _nonDbImportedKeyByNonDbFkId.entrySet())
      {
         buf.put(entry.getKey(), NonDbImportedKeyPersistence.toNonDbImportedKeyPersistence(entry.getValue()));
      }
      ret.setNonDbImportedKeyPersistenceByNonDbFkId(buf);

      return ret;
   }

   public List<String> removeNonDBConstraintDataTo(GraphColumn otherCol)
   {
      ArrayList<String> keysToRemove = new ArrayList<>();

      for (String nonDbFkId : otherCol._nonDbImportedKeyByNonDbFkId.keySet())
      {
         removeNonDbFkId(nonDbFkId);
         keysToRemove.add(nonDbFkId);
      }

      for (String nonDbFkId : otherCol._nonDbFkIdsPointingAtMe)
      {
         removeNonDbFkId(nonDbFkId);
         keysToRemove.add(nonDbFkId);
      }

      return keysToRemove;
   }

   public String getQualifiedTableName()
   {
      return _columnInfo.getQualifiedTableName();
   }
}
