package net.sourceforge.squirrel_sql.plugins.editextras.codereformat;
/*
 * Copyright (C) 2003 Gerd Wagner
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.util.Arrays;
import java.util.Vector;

public class CodeReformator
{
	private static final String INDENT = "   ";
	private static final int TRY_SPLIT_LINE_LEN = 80;

	private String _statementSeparator;
	private CommentSpec[] _commentSpecs;

	public CodeReformator(String statementSeparator, CommentSpec[] commentSpecs)
	{
		_statementSeparator = statementSeparator;
		_commentSpecs = commentSpecs;
	}


	public String reformat(String in)
	{
		in = flatenWhiteSpaces(in, false);

		PieceMarkerSpec[] markerExcludeComma = createPieceMarkerSpecExcludeColon();
		String[] pieces = getRefomatedPieces(in, markerExcludeComma);

      pieces = doInsertSpecial(pieces);

		StringBuffer ret = new StringBuffer();
		int braketCount = 0;
		for(int i=0; i < pieces.length; ++i)
		{
			if(")".equals(pieces[i]))
			{
				--braketCount;
			}
			ret.append( indent(pieces[i], braketCount) ).append('\n');
			if("(".equals(pieces[i]))
			{
				++braketCount;
			}
		}

		validate(in, ret.toString());

		return ret.toString();
	}

	private void validate(String beforeReformat, String afterReformat)
	{
		String normalizedBefore = getNormalized(beforeReformat);
		String normalizedAfter = getNormalized(afterReformat);

		if(! normalizedBefore.equalsIgnoreCase(normalizedAfter) )
		{
			int minLen = Math.min(normalizedAfter.length(), normalizedBefore.length());
			StringBuffer diffPos = new StringBuffer();
			for(int i=0; i < minLen; ++i)
			{
				if( Character.toUpperCase(normalizedBefore.charAt(i)) != Character.toUpperCase(normalizedAfter.charAt(i)))
				{
					break;
				}
				diffPos.append('-');
			}
			diffPos.append('^');

			StringBuffer msg = new StringBuffer();
		   msg.append("Reformat failed, normalized Strings differ\n");
			msg.append(normalizedBefore).append("\n");
			msg.append(normalizedAfter).append("\n");
			msg.append(diffPos).append("\n");

			System.out.println(msg);
			throw new IllegalStateException(msg.toString());
		}
	}

	/**
	 * Returns a normalized version of a SQL string. Normalized strings
	 * before and after reformatting should be the same. So normalized Strings
	 * may be used for validation of the reformating process.
	 */
	private String getNormalized(String s)
	{
		String ret = s.replaceAll("\\(", " ( ");
		ret = ret.replaceAll("\\)", " ) ");
		ret = ret.replaceAll(",", " , ");
		ret = ret.replaceAll(_statementSeparator, " " + _statementSeparator + " ");
		return flatenWhiteSpaces(ret, true).trim();
	}

	private String[] getRefomatedPieces(String in, PieceMarkerSpec[] markers)
	{
		CodeReformatorKernel kernel = new CodeReformatorKernel(_statementSeparator,  markers, _commentSpecs);
		String[] pieces = kernel.toPieces(in);
      Vector piecesBuf = new Vector();

		for(int i=0; i < pieces.length; ++i)
		{
			if(TRY_SPLIT_LINE_LEN < pieces[i].length())
			{
         	String[] splitPieces = trySplit(pieces[i], 0);
				piecesBuf.addAll(Arrays.asList(splitPieces));
			}
			else
			{
				piecesBuf.add(pieces[i]);
			}
		}
		return (String[])piecesBuf.toArray(new String[0]);
	}

	private String[] doInsertSpecial(String[] pieces)
	{
		int insertBegin = -1;
		boolean hasValues = false;

		Vector ret = new Vector();
		Vector insertPieces = new Vector();

		for(int i=0; i < pieces.length; ++i)
		{
      	if("INSERT ".length() <= pieces[i].length() && pieces[i].substring(0, "INSERT ".length()).equalsIgnoreCase("INSERT ") )
			{
            if(-1 != insertBegin)
				{
					// Inserts are not properly separated. We give up.
					return pieces;
				}
				insertBegin = i;
			}

			if(-1 == insertBegin)
			{
				ret.add(pieces[i]);
			}
			else
			{
				insertPieces.add(pieces[i]);
			}

			if(-1 < insertBegin && -1 != pieces[i].toUpperCase().indexOf("VALUES"))
			{
				hasValues = true;
			}

			if( -1 < insertBegin && _statementSeparator.equalsIgnoreCase(pieces[i]) )
			{
				if(hasValues)
				{
					ret.addAll(reformatInsert(insertPieces));
				}
				else
				{
					// No special treatment
					ret.addAll(insertPieces);
				}

				insertBegin = -1;
				hasValues = false;
				insertPieces = new Vector();
			}
		}

		if( -1 < insertBegin)
		{
			if(hasValues)
			{
				ret.addAll(reformatInsert(insertPieces));
			}
			else
			{
				// No special treatment
				ret.addAll(insertPieces);
			}
		}

		return (String[])ret.toArray(new String[0]);
	}

	private Vector reformatInsert(Vector piecesIn)
	{
		String[] pieces = (String[])piecesIn.toArray(new String[0]);

		Vector insertList = new Vector();
		Vector valuesList = new Vector();
		Vector behindInsert = new Vector();

		StringBuffer statementBegin = new StringBuffer();
		int braketCountAbsolute = 0;
		for(int i=0; i < pieces.length; ++i)
		{
			if(3 < braketCountAbsolute)
			{
            behindInsert.add(pieces[i]);
			}
			if("(".equals(pieces[i]) || ")".equals(pieces[i]))
			{
				++braketCountAbsolute;
			}

			if(0 == braketCountAbsolute)
			{
				statementBegin.append(pieces[i]).append(' ');
			}
			if(1 == braketCountAbsolute && !"(".equals(pieces[i]) && !")".equals(pieces[i]))
			{
				String buf = pieces[i].trim();
				if(buf.endsWith(","))
				{
					buf = buf.substring(0, buf.length() - 1);
				}
				insertList.add(buf);
			}
			if(3 == braketCountAbsolute && !"(".equals(pieces[i]) && !")".equals(pieces[i]))
			{
				String buf = pieces[i].trim();
				if(buf.endsWith(","))
				{
					buf = buf.substring(0, buf.length() - 1);
				}
				valuesList.add(buf);
			}
		}

		Vector ret = new Vector();

		if(0 == insertList.size())
		{
			// Not successful
			ret.addAll(piecesIn);
			return ret;
		}

		if(insertList.size() == valuesList.size())
		{
			ret.add(statementBegin.toString());
			StringBuffer insert = new StringBuffer();
			StringBuffer values = new StringBuffer();

			String insBuf = (String)insertList.get(0);
			String valsBuf = (String)valuesList.get(0);

			insert.append('(').append( adoptLength(insBuf, valsBuf) );
			values.append('(').append( adoptLength(valsBuf, insBuf) );

			for(int i=1; i < insertList.size(); ++i)
			{
				insBuf = (String)insertList.get(i);
				valsBuf = (String)valuesList.get(i);

				insert.append(',').append( adoptLength(insBuf, valsBuf) );
				values.append(',').append( adoptLength(valsBuf, insBuf) );
			}
			insert.append(") VALUES");
			values.append(')');
			ret.add(insert.toString());
			ret.add(values.toString());
			ret.addAll(behindInsert);
			return ret;
		}
		else
		{
			// Not successful
			ret.addAll(piecesIn);
			return ret;
		}
	}

	private String adoptLength(String s1, String s2)
	{
		int max = Math.max(s1.length(), s2.length());

		if(s1.length() == max)
		{
			return s1;
		}
		else
		{
			StringBuffer sb = new StringBuffer();
			sb.append(s1);
			while(sb.length() < max)
			{
				sb.append(' ');
			}
			return sb.toString();
		}
	}

	private String[] trySplit(String piece, int braketDepth)
	{
		String trimmedPiece = piece.trim();
		CodeReformatorKernel dum = new CodeReformatorKernel(_statementSeparator, new PieceMarkerSpec[0], _commentSpecs);

		if(hasTopLevelColon(trimmedPiece, dum))
		{
			PieceMarkerSpec[] pms = createPieceMarkerSpecIncludeColon();
			CodeReformatorKernel crk = new CodeReformatorKernel(_statementSeparator, pms, _commentSpecs);
			String[] splitPieces1 = crk.toPieces(trimmedPiece);
			if(1 == splitPieces1.length)
			{
				return splitPieces1;
			}

			Vector ret = new Vector();

			for(int i=0; i < splitPieces1.length; ++i)
			{
				if(TRY_SPLIT_LINE_LEN < splitPieces1[i].length() + braketDepth * INDENT.length())
				{
					String[] splitPieces2 = trySplit(splitPieces1[i], braketDepth);
					for(int j=0; j < splitPieces2.length; ++j)
					{
						ret.add( splitPieces2[j].trim() );
					}
				}
				else
				{
					ret.add( splitPieces1[i].trim() );
				}
			}
			return (String[])ret.toArray(new String[0]);
		}
		else
		{
			int[] tlbi = getTopLevelBraketIndexes(trimmedPiece, dum);
			if( -1 != tlbi[0] && tlbi[0] < tlbi[1])
			{
				//////////////////////////////////////////////////////////////////////////
				// Split the first two matching toplevel brakets here
				PieceMarkerSpec[] pms = createPieceMarkerSpecExcludeColon();
				CodeReformatorKernel crk = new CodeReformatorKernel(_statementSeparator, pms, _commentSpecs);
				String[] splitPieces1 = crk.toPieces(trimmedPiece.substring(tlbi[0] + 1, tlbi[1]));

				Vector buf = new Vector();
				buf.add(trimmedPiece.substring(0, tlbi[0]).trim());
				buf.add("(");
				for(int i=0; i < splitPieces1.length; ++i)
				{
					buf.add(splitPieces1[i]);
				}
				buf.add(")");
				if(tlbi[1] + 1 < trimmedPiece.length())
				{
					buf.add(trimmedPiece.substring(tlbi[1] + 1, trimmedPiece.length()).trim());
				}
				splitPieces1 = (String[])buf.toArray(new String[0]);
				//
				//////////////////////////////////////////////////////////////////////

				/////////////////////////////////////////////////////////////////////
				// Now check length of Strings in splitPieces1 again
				Vector ret = new Vector();
				for(int i=0; i < splitPieces1.length; ++i)
				{
					if(TRY_SPLIT_LINE_LEN < splitPieces1[i].length() + braketDepth * INDENT.length())
					{
						String[] splitPieces2 = trySplit(splitPieces1[i], braketDepth + 1);
						for(int j=0; j < splitPieces2.length; ++j)
						{
							ret.add(splitPieces2[j]);
						}
					}
					else
					{
						ret.add(splitPieces1[i]);
					}
				}
				//
				/////////////////////////////////////////////////////////////////////

				return (String[])ret.toArray(new String[0]);
			}
			else
			{
            return new String[]{piece};
			}
		}
	}

   private boolean hasTopLevelColon(String piece, CodeReformatorKernel crk)
	{
		int ix = piece.indexOf(",");
		StateOfPosition[] stateOfPositions = crk.getStatesOfPosition(piece);

		while(-1 != ix)
		{
			if(stateOfPositions[ix].isTopLevel)
			{
				return true;
			}
			if(ix < piece.length() - 1)
			{
				ix = piece.indexOf(",", ix + 1);
			}
			else
			{
				break;
			}
		}

		return false;

	}

	private int[] getTopLevelBraketIndexes(String piece, CodeReformatorKernel crk)
	{
		int[] ret = new int[2];
		ret[0] = -1;
		ret[1] = -1;

      StateOfPosition[] stateOfPositions = crk.getStatesOfPosition(piece);

		int bra = piece.indexOf("(");
		while(-1 != bra)
		{
			crk.getStatesOfPosition(piece);

			if(0 == bra || stateOfPositions[bra-1].isTopLevel)
			{
				ret[0] = bra;
				break; // break when first braket found
			}
			if(bra < piece.length() - 1)
			{
				bra = piece.indexOf("(", bra + 1);
			}
			else
			{
				break;
			}
		}

		if(-1 == ret[0])
		{
			return ret;
		}

		int ket = piece.indexOf(")", bra);
		while(-1 != ket)
		{
			if(ket == piece.length() - 1 || stateOfPositions[ket].isTopLevel)
			{
				// the next top level ket is the counterpart to bra
				ret[1] = ket;
				break;
			}
			if(ket < piece.length() - 1)
			{
				ket = piece.indexOf(")", ket + 1);
			}
			else
			{
				break;
			}
		}
      return ret;
	}

	private String indent(String piece, int callDepth)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < callDepth; ++i)
		{
			sb.append(INDENT);
		}
		sb.append(piece);

		return sb.toString();
	}

	private String flatenWhiteSpaces(String in, boolean force)
	{
		if(hasCommentEndingWithLineFeed(in) && ! force)
		{
			// No flaten. We would turn statement parts to comment
			return in;
		}

		int lenOld = 0;
		int lenNew = -1;
		while(lenOld > lenNew)
		{
			lenOld = in.length();
			in = in.replaceAll("\\s\\s", " ");
			lenNew = in.length();
		}
		return in.replaceAll("\\s", " ");
	}

	boolean hasCommentEndingWithLineFeed(String in)
	{
		CodeReformatorKernel dum = new CodeReformatorKernel(_statementSeparator, new PieceMarkerSpec[0], _commentSpecs);
		StateOfPosition[] sops = dum.getStatesOfPosition(in);

		boolean inComment = false;
		for(int i=0; i < sops.length; ++i)
		{
			if(!inComment && -1 < sops[i].commentIndex)
			{
				if(-1 < _commentSpecs[sops[i].commentIndex].commentEnd.indexOf('\n'))
				{
					return true;
				}
				inComment = true;
			}
			if(-1 == sops[i].commentIndex)
			{
				inComment = false;
			}
		}
		return false;
	}

	private PieceMarkerSpec[] createPieceMarkerSpecIncludeColon()
	{
		PieceMarkerSpec[] buf = createPieceMarkerSpecExcludeColon();
		Vector ret = new Vector();
		ret.addAll(Arrays.asList(buf));
		ret.add(new PieceMarkerSpec(",", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_END));

		return (PieceMarkerSpec[])ret.toArray(new PieceMarkerSpec[0]);
	}

	private PieceMarkerSpec[] createPieceMarkerSpecExcludeColon()
	{
		return new PieceMarkerSpec[]
		{
			new PieceMarkerSpec("SELECT", PieceMarkerSpec.TYPE_PIECE_MARKER_IN_OWN_PIECE),
			new PieceMarkerSpec("UNION", PieceMarkerSpec.TYPE_PIECE_MARKER_IN_OWN_PIECE),
			new PieceMarkerSpec("FROM", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("INNER", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("LEFT", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("RIGHT", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("WHERE", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("AND", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("GROUP", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("ORDER", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("INSERT", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("VALUES", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("UPDATE", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec("DELETE", PieceMarkerSpec.TYPE_PIECE_MARKER_AT_BEGIN),
			new PieceMarkerSpec(_statementSeparator, PieceMarkerSpec.TYPE_PIECE_MARKER_IN_OWN_PIECE)
		};
	}
}
