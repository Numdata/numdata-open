/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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
package com.numdata.printer;

import java.awt.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;
import javax.swing.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;
import jcifs.*;
import jcifs.smb.*;
import org.jetbrains.annotations.*;

/**
 * This class provides functionality for printing.
 *
 * @author Peter S. Heijnen
 */
public class PrintTools
{
	/**
	 * Length of an inch expressed in millimeters.
	 */
	public static final double INCH_IN_MILLIMETERS = 25.4;

	/**
	 * Size of a PostScript point (1/72 inch) expressed in inches.
	 */
	public static final double POINT_IN_INCHES = 1.0 / 72.0;

	/**
	 * Size of a PostScript point (1/72 inch) expressed in millimeters.
	 */
	public static final double POINT_IN_MILLIMETERS = POINT_IN_INCHES * INCH_IN_MILLIMETERS;

	/**
	 * The default LPD port number.
	 */
	public static final int LPD_PORT = 515;

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private PrintTools()
	{
	}

	/**
	 * Print a document. A print dialog is displayed before the actual print job is
	 * executed.
	 *
	 * @param doc Document to print.
	 */
	public static void printDoc( final Doc doc )
	{
		final PrintService printService = printDialogForDoc( doc );
		if ( printService != null )
		{
			try
			{
				printToJPS( printService, doc, false );
			}
			catch ( PrinterException e )
			{
				WindowTools.showErrorDialog( null, "Printing failed", "Could not print the document.\n\nERROR: " + e.getMessage() );
			}
		}
		else
		{
			WindowTools.showErrorDialog( null, "Printing failed", "Print service unavailable." );
		}
	}

	/**
	 * Print a {@link JTable}. The table is scaled to fit the page. A print dialog
	 * is displayed before the actual print job is executed.
	 *
	 * @param table  Table to print.
	 * @param header Header text ({@code null} => none).
	 * @param footer Footer text ({@code null} => none).
	 */
	public static void printTable( final JTable table, final String header, final String footer )
	{
		final MessageFormat headerFormat = ( header == null ) ? null : new MessageFormat( header );
		final MessageFormat footerFormat = ( footer == null ) ? null : new MessageFormat( footer );

		try
		{
			table.print( JTable.PrintMode.FIT_WIDTH, headerFormat, footerFormat );
		}
		catch ( PrinterException e )
		{
			WindowTools.showErrorDialog( null, "Printing failed", "Could not print the document.\n\nERROR: " + e.getMessage() );
		}
	}

	/**
	 * Print a plain text document. A print dialog is displayed before the actual
	 * print job is executed.
	 *
	 * @param text Content of text document to print.
	 */
	public static void printText( final String text )
	{
		printDoc( new SimpleDoc( text, DocFlavor.STRING.TEXT_PLAIN, null ) );
	}

	/**
	 * Show print dialog for the given document.
	 *
	 * @param doc Document to print.
	 *
	 * @return {@link PrintService} that was selected; {@code null} if dialog was
	 *         cancelled.
	 */
	public static PrintService printDialogForDoc( final Doc doc )
	{
		final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

		final PrintService[] printServices = PrintServiceLookup.lookupPrintServices( doc.getDocFlavor(), attributes );
		final PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
		return ServiceUI.printDialog( null, 200, 200, printServices, defaultService, doc.getDocFlavor(), attributes );
	}

	/**
	 * Print a document by using the specified {@link PrinterSettings}.
	 *
	 * @param settings     Printer settings to use.
	 * @param documentName Name of document to display in queue.
	 * @param documentData Document data to print.
	 * @param raw          Document data is raw vs. cooked (from printer's
	 *                     perspective).
	 *
	 * @throws PrinterException if the document could not be printed.
	 */
	public static void printRaw( final PrinterSettings settings, final String documentName, final byte[] documentData, final boolean raw )
	throws PrinterException
	{
		switch ( settings.getMethod() )
		{
			case LPD:
			{
				final LPDClient client = new LPDClient( settings.getHostname(), settings.getNetworkPort(), settings.getQueueName() );
				client.print( documentName, documentData, raw );
				break;
			}

			case SMB:
			{
				printToSMB( settings.getHostname(), settings.getQueueName(), documentData );
				break;
			}

			case JPS:
			{
				final Doc doc = new SimpleDoc( documentData, DocFlavor.BYTE_ARRAY.AUTOSENSE, null );
				final PrintService printService = getPrintService( settings.getQueueName(), doc.getDocFlavor() );
				printToJPS( printService, doc, true );
				break;
			}

			case VOID:
				break;

			default:
				throw new FatalPrinterException( "Unsupported print method: " + settings.getMethod() );
		}
	}

	/**
	 * Renders the given printable document to an image matching the given page
	 * format and resolution. Only the imageable area of the given page format is
	 * included in the image.
	 *
	 * @param pageFormat Page format to be used.
	 * @param resolution Resolution of the image, in dots per inch.
	 * @param document   Document to be rendered to an image.
	 *
	 * @return Image on which the document is rendered.
	 *
	 * @throws PrinterException if an error occurs while rendering the image.
	 * @throws IllegalArgumentException if the document doesn't contain any pages
	 * to be rendered, or if the given resolution and/or page size are too small to
	 * render an image of the page.
	 */
	public static BufferedImage printToImage( final PageFormat pageFormat, final double resolution, final Printable document )
	throws PrinterException
	{
		final int width = (int)( Math.ceil( pageFormat.getImageableWidth() * POINT_IN_INCHES * resolution ) );
		final int height = (int)( Math.ceil( pageFormat.getImageableHeight() * POINT_IN_INCHES * resolution ) );

		final BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );

		final Graphics2D g = result.createGraphics();

		final double scale = resolution / 72.0;
		g.scale( scale, scale );
		g.translate( -pageFormat.getImageableX(), -pageFormat.getImageableY() );

		if ( document.print( g, pageFormat, 0 ) == Printable.NO_SUCH_PAGE )
		{
			throw new FatalPrinterException( "document: contains no pages" );
		}
		g.dispose();

		return result;
	}

	/**
	 * Prints the given document to a local printer using the Java Print API.
	 *
	 * @param queueName    Name of the printer; {@code null} to use the default
	 *                     printer.
	 * @param documentName Name of the document.
	 * @param document     Document to be printed.
	 * @param pageFormat   Optional page format for document.
	 *
	 * @throws PrinterException if an error occurs while printing.
	 */
	public static void printToJPS( @Nullable final String queueName, @Nullable final String documentName, @NotNull final Printable document, @Nullable final PageFormat pageFormat )
	throws PrinterException
	{
		final PrinterJob job = PrinterJob.getPrinterJob();

		if ( ( queueName != null ) && !TextTools.isEmpty( queueName ) )
		{
			job.setPrintService( getPrintService( queueName, DocFlavor.SERVICE_FORMATTED.PRINTABLE ) );
		}

		final PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		attributes.add( new JobName( documentName, null ) );

		if ( pageFormat != null )
		{
			job.setPrintable( document, pageFormat );
		}
		else
		{
			job.setPrintable( document );
		}

		job.print( attributes );
	}

	/**
	 * Print a document to the given print service.
	 *
	 * @param printService  Print service to use.
	 * @param doc           Document to print.
	 * @param waitUntilDone Wait until the print job is done.
	 *
	 * @throws PrinterException if the document could not be printed.
	 */
	public static void printToJPS( final PrintService printService, final Doc doc, final boolean waitUntilDone )
	throws PrinterException
	{
		try
		{
			final DocPrintJob job = printService.createPrintJob();
			if ( waitUntilDone )
			{
				final PrintJobMonitor jobMonitor = new PrintJobMonitor( job );
				job.print( doc, null );
				jobMonitor.waitUntilDone();
			}
			else
			{
				job.print( doc, null );
			}
		}
		catch ( PrintException e )
		{
			// replace 'PrintException' with 'PrinterException'
			final PrinterException exception = new PrinterException( e.getMessage() );
			exception.initCause( e.getCause() );
			exception.setStackTrace( e.getStackTrace() );
			throw exception;
		}
	}

	/**
	 * Send raw file to print service.
	 *
	 * @param printerName Name of printer ({@code null} to use default/any).
	 * @param file        Raw file.
	 *
	 * @throws PrinterException if the document could not be printed.
	 */
	public static void printRawFileToJPS( @Nullable final String printerName, @NotNull final File file )
	throws PrinterException
	{
		printRawFileToJPS( getPrintService( printerName, DocFlavor.INPUT_STREAM.AUTOSENSE ), file );
	}

	/**
	 * Send raw file to print service.
	 *
	 * @param printService Print service to use.
	 * @param file         Raw file.
	 *
	 * @throws PrinterException if the document could not be printed.
	 */
	public static void printRawFileToJPS( @NotNull final PrintService printService, @NotNull final File file )
	throws PrinterException
	{
		try
		{
			final FileInputStream in = new FileInputStream( file );
			try
			{
				final Doc doc = new SimpleDoc( in, DocFlavor.INPUT_STREAM.AUTOSENSE, null );
				printToJPS( printService, doc, true );
			}
			finally
			{
				in.close();
			}
		}
		catch ( IOException e )
		{
			throw new PrinterIOException( e );
		}
	}

	/**
	 * Get {@link PrintService} for printer that has the given name and supports
	 * the given document flavor, if either is specified.
	 *
	 * @param printerName Printer name (if {@code null}, use default).
	 * @param docFlavor   Required document flavor ({@code null} = any).
	 *
	 * @return {@link PrintService}.
	 *
	 * @throws PrinterException if the printer service is not available.
	 */
	@NotNull
	public static PrintService getPrintService( @Nullable final String printerName, @Nullable final DocFlavor docFlavor )
	throws PrinterException
	{
		PrintService result = null;

		final PrintService[] printServices = PrintServiceLookup.lookupPrintServices( docFlavor, null );
		final List<String> foundPrinters = new ArrayList<String>( printServices.length );
		for ( final PrintService availableService : printServices )
		{
			final PrinterName nameAttribute = availableService.getAttribute( PrinterName.class );
			final String name = ( nameAttribute != null ) ? nameAttribute.getValue() : availableService.getName();
			foundPrinters.add( name );

			if ( ( printerName == null ) || printerName.contains( name ) )
			{
				result = availableService;
				break;
			}
		}

		if ( result == null )
		{
			final String mimeType = ( docFlavor != null ) ? docFlavor.getMimeType() : null;

			if ( ( printerName == null ) || foundPrinters.isEmpty() )
			{
				throw new FatalPrinterException( ( mimeType != null ) ? "No printers available for '" + mimeType + "' documents" : "No printers available" );
			}
			else
			{
				final StringBuilder sb = new StringBuilder();
				sb.append( "Printer '" ).append( printerName ).append( "' not available" );

				if ( mimeType != null )
				{
					sb.append( " for '" ).append( mimeType ).append( "' documents" );
				}

				sb.append( ". Found " ).append( ( foundPrinters.size() == 1 ) ? "printer" : "printers" ).append( ": " );

				for ( int i = 0; i < foundPrinters.size(); i++ )
				{
					if ( i > 0 )
					{
						sb.append( ", " );
					}

					sb.append( foundPrinters.get( i ) );
				}
				sb.append( '.' );

				throw new FatalPrinterException( sb.toString() );
			}
		}

		return result;
	}

	/**
	 * Get map of print services by (printer) name. If specified, this will only
	 * return services that support the given document flavor.
	 *
	 * @param docFlavor Document flavor to get service for ({@code null} = any).
	 *
	 * @return Map {@link PrintService} by (printer) name.
	 */
	@NotNull
	public static Map<String, PrintService> getPrintServices( @Nullable final DocFlavor docFlavor )
	{
		final Map<String, PrintService> result = new TreeMap<String, PrintService>();

		for ( final PrintService printService : PrintServiceLookup.lookupPrintServices( docFlavor, null ) )
		{
			final PrinterName nameAttribute = printService.getAttribute( PrinterName.class );
			final String name = ( nameAttribute != null ) ? nameAttribute.getValue() : printService.getName();
			result.put( name, printService );
		}

		return result;
	}

	/**
	 * Print a document to a printer shared over the network using SMB (Windows
	 * file and printer sharing).
	 *
	 * @param host         Hostname or IP that the printer is shared from.
	 * @param share        Name of the shared printer.
	 * @param documentData Document data to print.
	 *
	 * @throws PrinterException if the document could not be printed.
	 */
	private static void printToSMB( final String host, final String share, final byte[] documentData )
	throws PrinterException
	{
		final String useNTSmbs = Config.getProperty( "jcifs.smb.client.useNTSmbs", "true" );
		try
		{
			try
			{
				Config.setProperty( "jcifs.smb.client.useNTSmbs", "false" );
				final String url = "smb://" + host + '/' + share + '/';
				final SmbFileOutputStream out = new SmbFileOutputStream( url );
				try
				{
					out.write( documentData );
					out.flush();
				}
				finally
				{
					out.close();
				}
			}
			finally
			{
				Config.setProperty( "jcifs.smb.client.useNTSmbs", useNTSmbs );
			}
		}
		catch ( IOException e )
		{
			throw new PrinterIOException( e );
		}
	}

	/**
	 * This class monitors a print job and offers functionality to wait unit the
	 * job is completed.
	 */
	private static class PrintJobMonitor
	implements PrintJobListener
	{
		/**
		 * Indicates whether the job is done.
		 */
		private boolean _done = false;

		/**
		 * Print exception that occurred.
		 */
		private PrinterException _printerException = null;

		/**
		 * Lock object.
		 */
		private final Object _lock = new Object();

		/**
		 * Create print job monitor for the given job.
		 *
		 * @param job Print job to monitor.
		 */
		private PrintJobMonitor( final DocPrintJob job )
		{
			job.addPrintJobListener( this );
		}

		/**
		 * Print exception.
		 *
		 * @throws PrinterException if printing failed.
		 */
		public void waitUntilDone()
		throws PrinterException
		{
			final Object lock = _lock;
			synchronized ( lock )
			{
				while ( !_done )
				{
					try
					{
						lock.wait( 10000L );
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
						break;
					}
				}
			}

			if ( _printerException != null )
			{
				throw _printerException;
			}
		}

		public void printDataTransferCompleted( final PrintJobEvent pje )
		{
		}

		public void printJobCompleted( final PrintJobEvent pje )
		{
			done();
		}

		public void printJobRequiresAttention( final PrintJobEvent pje )
		{
		}

		public void printJobCanceled( final PrintJobEvent pje )
		{
			_printerException = new PrinterAbortException( pje.toString() );
			done();
		}

		public void printJobFailed( final PrintJobEvent pje )
		{
			_printerException = new PrinterAbortException( pje.toString() );
			done();
		}

		public void printJobNoMoreEvents( final PrintJobEvent pje )
		{
			done();
		}

		/**
		 * Called to notify that the print job is done (success or failure).
		 */
		protected void done()
		{
			final Object lock = _lock;
			synchronized ( lock )
			{
				_done = true;
				lock.notifyAll();
			}
		}
	}
}
