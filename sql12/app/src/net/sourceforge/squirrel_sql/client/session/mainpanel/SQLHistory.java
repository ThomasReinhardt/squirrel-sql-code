package net.sourceforge.squirrel_sql.client.session.mainpanel;
/*
 * Copyright (C) 2003 Colin Bell
 * colbell@users.sourceforge.net
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * This class represents SQL history.
 * 
 * @author  <A HREF="mailto:colbell@users.sourceforge.net">Colin Bell</A>
 */
public class SQLHistory
{
	private List _history = new ArrayList();

	public SQLHistory()
	{
	}

	public synchronized SQLHistoryItem[] getData()
	{
		SQLHistoryItem[] data = new SQLHistoryItem[_history.size()];
		return (SQLHistoryItem[])_history.toArray(data);
	}

	public synchronized void setData(SQLHistoryItem[] data)
	{
		_history.clear();
		_history.addAll(Arrays.asList(data));
	}


	public synchronized void add(String sql)
	{
		if (sql == null)
		{
			throw new IllegalArgumentException("sql == null");
		}
		add(new SQLHistoryItem(sql));
	}

	public synchronized void add(SQLHistoryItem obj)
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("SQLHistoryItem == null");
		}
		_history.add(obj);
	}
}
