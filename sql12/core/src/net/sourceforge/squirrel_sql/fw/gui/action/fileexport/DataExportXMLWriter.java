/*
 * Copyright (C) 2011 Stefan Willinger
 * wis775@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.squirrel_sql.fw.gui.action.fileexport;

import net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.CellComponentFactory;
import net.sourceforge.squirrel_sql.fw.datasetviewer.cellcomponent.DataTypeRenderingHint;
import net.sourceforge.squirrel_sql.fw.sql.ProgressAbortCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.PrintWriter;


/**
 * Exports {@link IExportData} into a XML File.
 * <p>
 * Uses DOM for output
 * </p>
 * <b>Note:</b> This class is the result of a refactoring task. The code was taken from TableExportCsvCommand.
 *
 * @author Stefan Willinger
 */
public class DataExportXMLWriter extends AbstractDataExportFileWriter
{

   private DocumentBuilderFactory factory;
   private DocumentBuilder builder;
   private Document document;
   private Element columns;
   private Element root;
   private Element rows;
   private Element row;

   /**
    * @param file
    * @param prefs
    * @param includeHeaders
    * @param progressController
    */
   public DataExportXMLWriter(File file, TableExportPreferences prefs, ProgressAbortCallback progressController)
   {
      super(file, prefs, progressController);
   }

   /**
    * @see AbstractDataExportFileWriter#afterWorking()
    */
   @Override
   protected void afterWorking() throws Exception
   {

      // The XML document we created above is still in memory
      // so we have to output it to a real file.
      // In order to do it we first have to create
      // an instance of DOMSource
      DOMSource source = new DOMSource(document);

      // PrintStream will be responsible for writing
      // the text data to the file
      PrintWriter pw = new PrintWriter(getFile(), getCharset().name());
      StreamResult result = new StreamResult(pw);

      // Once again we are using a factory of some sort,
      // this time for getting a Transformer instance,
      // which we use to output the XML
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, getCharset().name());

      // Indenting the XML
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      // The actual output to a file goes here
      transformer.transform(source, result);
   }

   /**
    * @see AbstractDataExportFileWriter#addCell(int, int, IExportDataCell)
    */
   @Override
   protected void addCell(ExportCellData cell)
   {
      String strCellValue = "";
      if (cell.getObject() != null)
      {
         if (getPrefs().isUseGlobalPrefsFormating() && cell.getColumnDisplayDefinition() != null)
         {
            DataTypeRenderingHint renderingHint = DataTypeRenderingHint.NONE;
            if(false == getPrefs().isRenderGroupingSeparator())
            {
               renderingHint = DataTypeRenderingHint.NO_GROUPING_SEPARATOR;
            }

            strCellValue = CellComponentFactory.renderObject(cell.getObject(), cell.getColumnDisplayDefinition(), renderingHint);
         }
         else
         {
            strCellValue = cell.getObject().toString();
         }
      }

      Element value = document.createElement("value");
      value.setAttribute("columnNumber", String.valueOf(cell.getColumnIndex()));
      value.setTextContent(strCellValue);
      row.appendChild(value);
   }

   /**
    * @see AbstractDataExportFileWriter#addHeaderCell(int, int, java.lang.String)
    */
   @Override
   protected void addHeaderCell(int colIdx, String columnName) throws Exception
   {
      Element columnEl = document.createElement("column");
      columnEl.setAttribute("number", String.valueOf(colIdx));
      columns.appendChild(columnEl);

      Element columnNameEl = document.createElement("name");
      columnNameEl.setTextContent(columnName);
      columnEl.appendChild(columnNameEl);

   }

   /**
    * @see AbstractDataExportFileWriter#beforeWorking(java.io.File)
    */
   @Override
   protected void beforeWorking(File file) throws Exception
   {
      // Using a factory to get DocumentBuilder for creating XML's
      factory = DocumentBuilderFactory.newInstance();
      builder = factory.newDocumentBuilder();

      // Here instead of parsing an existing document we want to
      // create a new one.
      document = builder.newDocument();

      // 'table' is the main tag in the XML.
      root = document.createElement("table");
      document.appendChild(root);

      // 'columns' tag will contain informations about columns
      columns = document.createElement("columns");
      root.appendChild(columns);
   }

   /**
    * @see AbstractDataExportFileWriter#beforeRow()
    */
   @Override
   public void beforeRow(int rowIdx) throws Exception
   {
      super.beforeRow(rowIdx);
      row = document.createElement("row");
      row.setAttribute("rowNumber", String.valueOf(rowIdx + 1));
      rows.appendChild(row);
   }

   /**
    * @see AbstractDataExportFileWriter#beforeRows()
    */
   @Override
   public void beforeRows()
   {
      super.beforeRows();
      // 'rows' tag contains the data extracted from the table
      rows = document.createElement("rows");
      root.appendChild(rows);
   }


}
