//
//  Copyright 2004, KOBAYASHI Tadashi
//  $Id$
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//

package net.sourceforge.veditor.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;

/**
 * implementation class of VhdlParserCore<p/>
 * for separating definition from JavaCC code
 */
class VhdlParser extends VhdlParserCore implements IParser
{
	private IFile file;
	private ParserManager manager;

	public VhdlParser(Reader reader, IFile file)
	{
		super(reader);
		this.file = file;
		manager = new ParserManager(this);
	}

	public ParserManager getManager()
	{
		return manager;
	}

	public IFile getFile()
	{
		return file;
	}

	// called by VhdlParserCore
	protected void addModule(int begin, String name)
	{
		manager.addModule(begin, name, file);
	}
	protected void endModule(int line)
	{
		manager.endModule(line);
	}
	protected void addPort(int line, String portName)
	{
		manager.addPort(line, portName);
	}
	protected void addVariable(int line, String varName)
	{
		manager.addVariable(line, varName);
	}
	protected void addParameter(int line, String name, String value)
	{
		manager.addParameter(line, name, value);
	}
	protected void addElement(int begin, int end, String type, String name)
	{
		manager.addElement(begin, end, type, name);
	}
	protected void addInstance(int begin, int end, String moduleName, String inst)
	{
		manager.addInstance(begin, end, moduleName,inst);
	}
	protected void beginStatement()
	{
		manager.beginStatement();
	}
	protected void endStatement()
	{
		manager.endStatement();
	}

	/**
	 * parse line comment for content outline
	 */
	public void parseLineComment(Reader reader)
	{
		try
		{
			int line = 1;
			int column = 0;
			int c = reader.read();
			while (c != -1)
			{
				column++;
				switch (c)
				{
					case '\n' :
						line++;
						column = 0;
						c = reader.read();
						break;
					case '-' :
						c = reader.read();
						if (c == '-' && column == 1)
						{
							String comment = getLineComment(reader);
							if (comment != null)
								addComment(line, comment);
							line++;
							column = 0;
						}
						break;
					default :
						c = reader.read();
						break;
				}
			}
		}
		catch (IOException e)
		{
		}
	}

	private String getLineComment(Reader reader) throws IOException
	{
		StringBuffer str = new StringBuffer();
		boolean enable = false;

		//  copy to StringBuffer
		int c = reader.read();
		while (c != '\n' && c != -1)
		{
			if (Character.isLetterOrDigit((char)c) || enable)
			{
				str.append((char)c);
				enable = true;
			}
			c = reader.read();
		}

		// delete tail
		for (int i = str.length() - 1; i >= 0; i--)
		{
			char ch = str.charAt(i);
			if (Character.isLetterOrDigit(ch))
				break;
			else
				str.deleteCharAt(i);
		}

		if (str.length() != 0)
			return str.toString();
		else
			return null;
	}

	private void addComment(int line, String comment)
	{
		// ignore continuous comments
		if (prevCommentLine + 1 == line)
		{
			prevCommentLine = line;
			return;
		}

		prevCommentLine = line;

		Iterator i = manager.getModuleIterator();
		while (i.hasNext())
		{
			Module mod = (Module)i.next();
			if (line >= mod.getLine() + mod.getLength())
				break;
			if (line >= mod.getLine())
			{
				// System.out.println(comment);
				mod.addElement(line, line, "--", comment);
			}
		}
	}
	private int prevCommentLine;
	
}







