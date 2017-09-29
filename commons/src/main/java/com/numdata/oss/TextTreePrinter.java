/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numdata.oss;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class provides functionality to print text-based trees.
 *
 * @author Peter S. Heijnen
 */
public abstract class TextTreePrinter<T>
{
	/*
	 * The following box figure is split up into character below:
	 *
	 *  .----.----.
	 *  |    |    |
	 *  |----|----|
	 *  |    |    |
	 *  |____|____|
	 *
	 */

	/** -   . */
	private char _boxHorizontal = '-';

	/** |   . */
	private char _boxVertical = '|';

	/** .-  . */
	private char _boxDownRight = '.';

	/** -.- . */
	private char _boxDownHorizontal = 'v';

	/** -.  . */
	private char _boxDownLeft = '.';

	/** |-  . */
	private char _boxVerticalRight = '>';

	/** -|- . */
	private char _boxVerticalHorizontal = '+';

	/** -|  . */
	private char _boxVerticalLeft = '<';

	/** |_  . */
	private char _boxUpRight = '`';

	/** _|_ . */
	private char _boxUpHorizontal = '^';

	/** _|  . */
	private char _boxUpLeft = '\'';

	/** space. */
	private char _boxEmpty = ' ';

	/**
	 * If set, insert empty lines between nodes in the text tree to make it more
	 * readable.
	 */
	public boolean _insertEmptyLines = false;

	/**
	 * Appendable to write to.
	 */
	private Appendable _out = null;

	/**
	 * Create tree printer.
	 */
	protected TextTreePrinter()
	{
	}

	/**
	 * Create tree printer.
	 *
	 * @param out Target to print to.
	 */
	protected TextTreePrinter( final Appendable out )
	{
		_out = out;
	}

	public boolean isInsertEmptyLines()
	{
		return _insertEmptyLines;
	}

	public void setInsertEmptyLines( final boolean insertEmptyLines )
	{
		_insertEmptyLines = insertEmptyLines;
	}

	public Appendable getOut()
	{
		return _out;
	}

	public void setOut( final Appendable out )
	{
		_out = out;
	}

	public void useAsciiCharacters()
	{
		_boxHorizontal = '-';
		_boxVertical = '|';
		_boxDownRight = '+';
		_boxDownHorizontal = '+';
		_boxDownLeft = '+';
		_boxVerticalRight = '+';
		_boxVerticalHorizontal = '+';
		_boxVerticalLeft = '+';
		_boxUpRight = '+';
		_boxUpHorizontal = '+';
		_boxUpLeft = '+';
		_boxEmpty = ' ';
	}

	public void useAsciiArtCharacters()
	{
		_boxHorizontal = '-';
		_boxVertical = '|';
		_boxDownRight = '.';
		_boxDownHorizontal = 'v';
		_boxDownLeft = '.';
		_boxVerticalRight = '>';
		_boxVerticalHorizontal = '+';
		_boxVerticalLeft = '<';
		_boxUpRight = '`';
		_boxUpHorizontal = '^';
		_boxUpLeft = '\'';
		_boxEmpty = ' ';
	}

	public void useUnicodeBoxCharacters()
	{
		_boxHorizontal = '\u2500';
		_boxVertical = '\u2502';
		_boxDownRight = '\u250C';
		_boxDownHorizontal = '\u252C';
		_boxDownLeft = '\u2510';
		_boxVerticalRight = '\u251C';
		_boxVerticalHorizontal = '\u253C';
		_boxVerticalLeft = '\u2524';
		_boxUpRight = '\u2514';
		_boxUpHorizontal = '\u2534';
		_boxUpLeft = '\u2518';
		_boxEmpty = '\u2003';
	}

	/**
	 * Print tree.
	 *
	 * @param node Tree node to display.
	 */
	public void printTree( final T node )
	{
		printTree( "", node );
	}

	/**
	 * Print tree.
	 *
	 * @param prefix Prefix for each printed line.
	 * @param node   Tree node to display.
	 */
	public void printTree( final String prefix, final T node )
	{
		printTree( prefix, null, node );
	}

	/**
	 * Recursively print tree.
	 *
	 * @param prefix         Prefix for each printed line.
	 * @param parentChildren Child nodes of parent node.
	 * @param node           Tree node to display.
	 */
	protected void printTree( final String prefix, final List<T> parentChildren, final T node )
	{
		final List<T> children = getChildren( node );
		final boolean isRoot = ( parentChildren == null );
		final boolean isLast = ( isRoot || ( parentChildren.indexOf( node ) == ( parentChildren.size() - 1 ) ) );
		final boolean isLeaf = children.isEmpty();

		final String childPrefix;

		if ( isRoot )
		{
			print( prefix );
			childPrefix = prefix + _boxEmpty;
		}
		else
		{
			if ( _insertEmptyLines )
			{
				print( prefix );
				print( _boxVertical );
				println();
			}

			print( prefix );
			print( isLast ? _boxUpRight : _boxVerticalRight );
			print( _boxHorizontal );
			print( ' ' );

			if ( isLast )
			{
				childPrefix = prefix + _boxEmpty + _boxEmpty + _boxEmpty + _boxEmpty;
			}
			else
			{
				childPrefix = prefix + _boxVertical + _boxEmpty + _boxEmpty + _boxEmpty;
			}
		}

		printNode( childPrefix, node );
		println();

		if ( !isLeaf )
		{
			for ( int i = 0; i < children.size(); i++ )
			{
				printTree( childPrefix, children, children.get( i ) );
			}
		}
	}

	/**
	 * Print string.
	 *
	 * @param string String to print.
	 */
	protected void print( final String string )
	{
		final Appendable out = _out;
		if ( out instanceof PrintWriter )
		{
			( (PrintWriter)out ).print( string );
		}
		else if ( out instanceof PrintStream )
		{
			( (PrintStream)out ).print( string );
		}
		else
		{
			try
			{
				out.append( string );
			}
			catch ( final IOException e )
			{
				/* ignore, just dump stack */
				e.printStackTrace();
			}
		}
	}

	/**
	 * Print character.
	 *
	 * @param ch Character to print.
	 */
	protected void print( final char ch )
	{
		final Appendable out = _out;
		if ( out instanceof PrintWriter )
		{
			( (PrintWriter)out ).print( ch );
		}
		else if ( out instanceof PrintStream )
		{
			( (PrintStream)out ).print( ch );
		}
		else
		{
			try
			{
				out.append( ch );
			}
			catch ( final IOException e )
			{
				/* ignore, just dump stack */
				e.printStackTrace();
			}
		}
	}

	/**
	 * Print new-line.
	 */
	protected void println()
	{
		final Appendable out = _out;
		if ( out instanceof PrintWriter )
		{
			( (PrintWriter)out ).println();
		}
		else if ( out instanceof PrintStream )
		{
			( (PrintStream)out ).println();
		}
		else
		{
			try
			{
				out.append( '\n' );
			}
			catch ( final IOException e )
			{
				/* ignore, just dump stack */
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get children of the given node.
	 *
	 * @param node Node to get children of.
	 *
	 * @return Children of the given node.
	 */
	@NotNull
	protected abstract List<T> getChildren( final T node );

	/**
	 * Print node.
	 *
	 * @param prefix Prefix for each printed line.
	 * @param node   Tree node to display.
	 */
	protected abstract void printNode( final String prefix, final T node );
}
