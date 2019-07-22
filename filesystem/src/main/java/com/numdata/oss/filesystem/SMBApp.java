/*
 * Copyright (c) 2019-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.filesystem;

import java.io.*;

import com.numdata.oss.io.*;
import jcifs.smb.*;
import org.jetbrains.annotations.*;

/**
 * Command-line tool for SMB.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "ThrowablePrintedToSystemOut", "Duplicates" } )
public class SMBApp
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		if ( args.length < 2 )
		{
			System.err.println( "syntax: <command> <url> [<command dependent options>]" );
			System.out.println();
			System.out.println( "URL format: smb://[[[domain;]username[:password]@]server[:port]/[[share/[dir/]file]]][?[param=value[param2=value2[...]]]" );
			System.out.println();
			System.out.println( "Available commands:" );
			//noinspection SpellCheckingInspection
			System.out.println( "    att[ributes] <url> [<name>]    Show file attributes" );
			System.out.println( "    del[ete] <url>                 Delete file" );
			System.out.println( "    get <url>                      Get file content" );
			System.out.println( "    lis[t]  <url>                  List contents (of directory)" );
			System.out.println( "    ren[ame] <from> <to>           Rename file" );
		}

		final String command = args[ 0 ];
		final SmbFile smbFile = new SmbFile( args[ 1 ] );

		if ( match( command, "attributes" ) )
		{
			printAttributes( smbFile, args );
		}
		else if ( match( command, "delete" ) )
		{
			smbFile.delete();
		}
		else if ( match( command, "get" ) )
		{
			final InputStream in = smbFile.getInputStream();
			try
			{
				final FileOutputStream out = new FileOutputStream( args.length > 2 ? args[ 2 ] : smbFile.getName() );
				try
				{
					DataStreamTools.pipe( out, in );
				}
				finally
				{
					out.close();
				}
			}
			finally
			{
				in.close();
			}
		}
		else if ( match( command, "list" ) )
		{
			for ( final String path : smbFile.list() )
			{
				System.out.println( path );
			}
		}
		else if ( match( command, "rename" ) )
		{
			final String to = args[ 2 ];
			smbFile.renameTo( to.contains( "://" ) ? new SmbFile( to ) : new SmbFile( smbFile, to ) );
		}
		else
		{
			System.out.println( "Unrecognized command '" + command + "'" );
		}
	}

	private static boolean match( @NotNull final String string, @NotNull final String command )
	{
		final int length = string.length();
		return ( length >= 3 ) && ( length <= command.length() ) && string.regionMatches( true, 0, command, 0, length );
	}

	private static boolean match( @NotNull final String name, @NotNull final String[] args )
	{
		boolean result = ( args.length <= 2 );
		if ( !result )
		{
			for ( int i = 2; i < args.length; i++ )
			{
				if ( match( args[ i ], name ) )
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private static void printAttributes( @NotNull final SmbFile smbFile, @NotNull final String[] args )
	{
		if ( match( "url", args ) )
		{
			System.out.print( "URL ....................... " );
			try
			{
				System.out.println( smbFile.getURL() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "path", args ) )
		{
			System.out.print( "Path ...................... " );
			try
			{
				System.out.println( smbFile.getPath() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "canonicalPath", args ) ||
		     match( "canonical path", args ) ||
		     match( "canonical-path", args ) )
		{
			System.out.print( "Canonical path ............ " );
			try
			{
				System.out.println( smbFile.getCanonicalPath() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "exists", args ) )
		{
			System.out.print( "Exists? ................... " );
			try
			{
				System.out.println( smbFile.exists() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "directory", args ) )
		{
			System.out.print( "directory? ................ " );
			try
			{
				System.out.println( smbFile.isDirectory() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "parent", args ) )
		{
			System.out.print( "Parent .................... " );
			try
			{
				System.out.println( smbFile.getParent() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "exists", args ) )
		{
			System.out.print( "Exists? ................... " );
			try
			{
				System.out.println( smbFile.exists() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "directory", args ) )
		{
			System.out.print( "directory? ................ " );
			try
			{
				System.out.println( smbFile.isDirectory() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "modified", args ) ||
		     match( "lastModified", args ) ||
		     match( "last modified", args ) ||
		     match( "last-modified", args ) )
		{
			System.out.print( "Last modified ............. " );
			try
			{
				System.out.println( smbFile.getLastModified() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "principal", args ) )
		{
			System.out.print( "Principal ................. " );
			try
			{
				System.out.println( smbFile.getPrincipal() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "permissions", args ) )
		{
			System.out.print( "Permission ................ " );
			try
			{
				System.out.println( smbFile.getPermission() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "type", args ) ||
		     match( "contentType", args ) ||
		     match( "content type", args ) ||
		     match( "content-type", args ) )
		{
			System.out.print( "Content type .............. " );
			try
			{
				System.out.println( smbFile.getContentType() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "encoding", args ) ||
		     match( "contentEncoding", args ) ||
		     match( "content encoding", args ) ||
		     match( "content-encoding", args ) )
		{
			System.out.print( "Content encoding .......... " );
			try
			{
				System.out.println( smbFile.getContentEncoding() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}

		if ( match( "length", args ) ||
		     match( "contentLength", args ) ||
		     match( "content length", args ) ||
		     match( "content-length", args ) )
		{
			System.out.print( "Content length ............ " );
			try
			{
				System.out.println( smbFile.getContentLengthLong() );
			}
			catch ( Exception e )
			{
				System.out.println( e );
			}
		}
	}
}
