/*
 * Copyright (c) 2006-2017, Numdata BV, The Netherlands.
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

import java.awt.print.*;
import java.text.*;
import java.util.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class is used for printer settings.
 *
 * @author G.B.M. Rupert
 */
public class PrinterSettings
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( PrinterSettings.class );

	/**
	 * Constant to identify 'method' setting.
	 */
	public static final String METHOD = "method";

	/**
	 * Constant to identify 'hostname' setting.
	 */
	public static final String HOSTNAME = "hostname";

	/**
	 * Constant to identify 'networkPort' setting.
	 */
	public static final String NETWORK_PORT = "networkPort";

	/**
	 * Constant to identify 'queueName' setting.
	 */
	public static final String QUEUE_NAME = "queueName";

	/**
	 * Constant to identify 'resolution' setting.
	 */
	public static final String RESOLUTION = "resolution";

	/**
	 * Constant to identify 'page width' setting.
	 */
	public static final String PAGE_WIDTH = "pageWidth";

	/**
	 * Constant to identify 'page height' setting.
	 */
	public static final String PAGE_HEIGHT = "pageHeight";

	/**
	 * Constant to identify 'page orientation' setting.
	 */
	public static final String PAGE_ORIENTATION = "pageOrientation";

	/**
	 * Constant to identify 'imageable x' setting.
	 */
	public static final String IMAGEABLE_X = "imageableX";

	/**
	 * Constant to identify 'imageable y' setting.
	 */
	public static final String IMAGEABLE_Y = "imageableY";

	/**
	 * Constant to identify 'imageable width' setting.
	 */
	public static final String IMAGEABLE_WIDTH = "imageablePageWidth";

	/**
	 * Constant to identify 'imageable height' setting.
	 */
	public static final String IMAGEABLE_HEIGHT = "imageablePageHeight";

	/**
	 * The default LPD port number.
	 */
	public static final int LPD_PORT = 515;

	/**
	 * Printing method.
	 */
	public enum Method
	{
		/**
		 * LPD connection.
		 */
		LPD,

		/**
		 * SMB connection.
		 */
		SMB,

		/**
		 * Java Print Service API.
		 */
		JPS,

		/**
		 * Socket connection.
		 */
		SOCKET,

		/**
		 * Into the void (do not print).
		 */
		VOID
	}

	/**
	 * Page orientation strings.
	 */
	public enum PageOrientation
	{
		/**
		 * The origin is at the bottom left of the paper with x running bottom to top
		 * and y running left to right.
		 *
		 * Note that this is not the Macintosh landscape but is the Window's and
		 * PostScript landscape.
		 */
		LANDSCAPE,


		/**
		 * The origin is at the top left of the paper with x running to the right and
		 * y running down the paper.
		 */
		PORTRAIT,

		/**
		 * The origin is at the top right of the paper with x running top to bottom
		 * and y running right to left.
		 *
		 * Note that this is the Macintosh landscape.
		 */
		REVERSE_LANDSCAPE
	}

	/**
	 * A millimeter expressed in points (PostScript). A PostScript point is defined
	 * as 1/72 of an inch.
	 */
	public static final double MILLIMETER_IN_POINTS = 72.0 / 25.4;

	/**
	 * Method to use.
	 */
	@NotNull
	private Method _method = Method.JPS;

	/**
	 * Host name or IP address of server.
	 */
	@Nullable
	private String _hostname = "localhost";

	/**
	 * Network port to use on host.
	 */
	private int _networkPort = LPD_PORT;

	/**
	 * Name of queue.
	 */
	private String _queueName = "lp";

	/**
	 * Print resolution.
	 */
	private double _resolution = 300.0;

	/**
	 * Specifies the printer's default page format.
	 */
	private PageFormat _pageFormat = null;

	/**
	 * Create default printer settings.
	 */
	public PrinterSettings()
	{
		setMediaSize( MediaSize.ISO.A4 );
		setMargins( new Insets2D.Double( 20, 10, 20, 10 ) );
	}

	/**
	 * Import settings from {@link Properties}.
	 *
	 * @param settings Settings to import.
	 */
	public void importSettings( final Properties settings )
	{
		setMethod( PropertyTools.getEnum( settings, METHOD, Method.JPS ) );
		setHostname( PropertyTools.getString( settings, HOSTNAME, "localhost" ) );
		setNetworkPort( PropertyTools.getInt( settings, NETWORK_PORT, LPD_PORT ) );
		setQueueName( PropertyTools.getString( settings, QUEUE_NAME, "lp" ) );
		setResolution( PropertyTools.getDouble( settings, RESOLUTION, 300.0 ) );

		if ( settings.containsKey( PAGE_WIDTH ) && settings.containsKey( PAGE_HEIGHT ) )
		{
			final double width = PropertyTools.getDouble( settings, PAGE_WIDTH, 210.0 );
			final double height = PropertyTools.getDouble( settings, PAGE_HEIGHT, 297.0 );
			setPageSize( width, height );

			final double imageableX = PropertyTools.getDouble( settings, IMAGEABLE_X, 5.0 );
			final double imageableY = PropertyTools.getDouble( settings, IMAGEABLE_Y, 10.0 );
			final double imageableWidth = PropertyTools.getDouble( settings, IMAGEABLE_WIDTH, width - 10.0 );
			final double imageableHeight = PropertyTools.getDouble( settings, IMAGEABLE_HEIGHT, height - 20.0 );
			setImageableArea( imageableX, imageableY, imageableWidth, imageableHeight );

			setPageOrientation( PropertyTools.getEnum( settings, PAGE_ORIENTATION, PageOrientation.PORTRAIT ) );
		}

		if ( LOG.isTraceEnabled() )
		{
			final NumberFormat nf = TextTools.getNumberFormat( Locale.US, 1, 1, false );

			LOG.trace( "Imported printer settings:" );
			LOG.trace( "  method           : " + getMethod() );
			LOG.trace( "  hostname         : " + getHostname() );
			LOG.trace( "  queue name       : " + getQueueName() );
			LOG.trace( "  resolution       : " + nf.format( getResolution() ) + " dpi" );
			LOG.trace( "  page size        : " + nf.format( getPageWidth() ) + " x " + nf.format( getPageHeight() ) + " mm" );
			LOG.trace( "  imageable size   : " + nf.format( getImageablePageWidth() ) + " x " + nf.format( getImageablePageHeight() ) + " mm" );
			LOG.trace( "  imageable X/Y    : " + nf.format( getImageableX() ) + " x " + nf.format( getImageableY() ) + " mm" );
		}
	}

	/**
	 * Export settings.
	 *
	 * @return Settings.
	 */
	public NestedProperties exportSettings()
	{
		final NestedProperties result = new NestedProperties();
		result.set( METHOD, _method );
		result.set( HOSTNAME, _hostname );
		result.set( NETWORK_PORT, _networkPort );
		result.set( QUEUE_NAME, _queueName );
		result.set( RESOLUTION, _resolution );

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat != null )
		{
			result.set( PAGE_WIDTH, getPageWidth() );
			result.set( PAGE_HEIGHT, getPageHeight() );
			result.set( IMAGEABLE_X, getImageableX() );
			result.set( IMAGEABLE_Y, getImageableY() );
			result.set( IMAGEABLE_WIDTH, getImageablePageWidth() );
			result.set( IMAGEABLE_HEIGHT, getImageablePageHeight() );
			result.set( PAGE_ORIENTATION, getPageOrientation() );
		}
		else
		{
			result.remove( PAGE_WIDTH );
			result.remove( PAGE_HEIGHT );
			result.remove( IMAGEABLE_X );
			result.remove( IMAGEABLE_Y );
			result.remove( IMAGEABLE_WIDTH );
			result.remove( IMAGEABLE_HEIGHT );
			result.remove( PAGE_ORIENTATION );
		}

		return result;
	}

	@NotNull
	public Method getMethod()
	{
		return _method;
	}

	public void setMethod( @NotNull final Method method )
	{
		_method = method;
	}

	@Nullable
	public String getHostname()
	{
		return _hostname;
	}

	public void setHostname( @Nullable final String hostname )
	{
		_hostname = hostname;
	}

	public int getNetworkPort()
	{
		return _networkPort;
	}

	public void setNetworkPort( final int networkPort )
	{
		_networkPort = networkPort;
	}

	public String getQueueName()
	{
		return _queueName;
	}

	public void setQueueName( final String queueName )
	{
		_queueName = queueName;
	}

	public double getResolution()
	{
		return _resolution;
	}

	public void setResolution( final double resolution )
	{
		_resolution = resolution;
	}

	/**
	 * Get page orientation.
	 *
	 * @return Page orientation ({@link PageFormat#LANDSCAPE}, {@link
	 *         PageFormat#PORTRAIT}, or {@link PageFormat#REVERSE_LANDSCAPE}).
	 */
	@NotNull
	public PageOrientation getPageOrientation()
	{
		final PageFormat pageFormat = _pageFormat;

		final PageOrientation orientation;
		final int o = pageFormat.getOrientation();

		if ( o == PageFormat.LANDSCAPE )
		{
			orientation = PageOrientation.LANDSCAPE;
		}
		else if ( o == PageFormat.PORTRAIT )
		{
			orientation = PageOrientation.PORTRAIT;
		}
		else
		{
			orientation = PageOrientation.REVERSE_LANDSCAPE;
		}

		return orientation;
	}

	/**
	 * Set page orientation.
	 *
	 * @param pageOrientation Page orientation to set.
	 *
	 * @throws IllegalArgumentException if an unknown orientation was requested
	 */
	public void setPageOrientation( @NotNull final PageOrientation pageOrientation )
	{
		final PageFormat pageFormat = getOrCreatePageFormat();

		final int orientation;

		if ( pageOrientation == PageOrientation.LANDSCAPE )
		{
			orientation = PageFormat.LANDSCAPE;
		}
		else if ( pageOrientation == PageOrientation.PORTRAIT )
		{
			orientation = PageFormat.PORTRAIT;
		}
		else
		{
			orientation = PageFormat.REVERSE_LANDSCAPE;
		}

		pageFormat.setOrientation( orientation );
	}

	/**
	 * Gets the width of the printer's default page format, in millimeters.
	 *
	 * @return Width of the printer's default page format, in millimeters.
	 */
	public double getPageWidth()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getWidth() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the width of the printer's default page format, in millimeters.
	 *
	 * @param width Width to be set, in millimeters, in millimeters.
	 */
	public void setPageWidth( final double width )
	{
		if ( !MathTools.almostEqual( width, getPageWidth() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setSize( width * MILLIMETER_IN_POINTS, paper.getHeight() );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Gets the height of the printer's default page format, in millimeters.
	 *
	 * @return Height of the printer's default page format, in millimeters.
	 */
	public double getPageHeight()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getHeight() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the height of the printer's default page format, in millimeters.
	 *
	 * @param height Height to be set, in millimeters, in millimeters.
	 */
	public void setPageHeight( final double height )
	{
		if ( !MathTools.almostEqual( height, getPageHeight() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setSize( paper.getWidth(), height * MILLIMETER_IN_POINTS );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Sets the width of the printer's default page format.
	 *
	 * @param width  Width to be set, in millimeters.
	 * @param height Height to be set, in millimeters.
	 */
	public void setPageSize( final double width, final double height )
	{
		final PageFormat pageFormat = getOrCreatePageFormat();
		final Paper paper = pageFormat.getPaper();
		paper.setSize( width * MILLIMETER_IN_POINTS, height * MILLIMETER_IN_POINTS );
		pageFormat.setPaper( paper );
	}

	/**
	 * Gets the X-coordinate of the top-left corner of the imageable area of the
	 * printer's default page format, in millimeters.
	 *
	 * @return X-coordinate of top-left corner of imageable area, in millimeters.
	 */
	public double getImageableX()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getImageableX() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the X-coordinate of the top-left corner of the imageable area of the
	 * printer's default page format, in millimeters.
	 *
	 * @param x X-coordinate of top-left corner of imageable area, in millimeters.
	 */
	public void setImageableX( final double x )
	{
		if ( !MathTools.almostEqual( x, getImageableX() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setImageableArea( x * MILLIMETER_IN_POINTS, paper.getImageableY(), paper.getImageableWidth(), paper.getImageableHeight() );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Gets the Y-coordinate of the top-left corner of the imageable area of the
	 * printer's default page format, in millimeters.
	 *
	 * @return Y-coordinate of top-left corner of imageable area, in millimeters.
	 */
	public double getImageableY()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getImageableY() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the Y-coordinate of the top-left corner of the imageable area of the
	 * printer's default page format, in millimeters.
	 *
	 * @param y Y-coordinate of top-left corner of imageable area, in millimeters.
	 */
	public void setImageableY( final double y )
	{
		if ( !MathTools.almostEqual( y, getImageableY() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setImageableArea( paper.getImageableX(), y * MILLIMETER_IN_POINTS, paper.getImageableWidth(), paper.getImageableHeight() );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Gets the width of the imageable area of the printer's default page format,
	 * in millimeters.
	 *
	 * @return Width of the imageable area, in millimeters.
	 */
	public double getImageablePageWidth()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getImageableWidth() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the width of the imageable area of the printer's default page format,
	 * in millimeters.
	 *
	 * @param width Width of the imageable area, in millimeters.
	 */
	public void setImageablePageWidth( final double width )
	{
		if ( !MathTools.almostEqual( width, getImageablePageWidth() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setImageableArea( paper.getImageableX(), paper.getImageableY(), width * MILLIMETER_IN_POINTS, paper.getImageableHeight() );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Gets the height of the imageable area of the printer's default page format,
	 * in millimeters.
	 *
	 * @return Height of the imageable area, in millimeters.
	 */
	public double getImageablePageHeight()
	{
		final double result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			result = 0.0;
		}
		else
		{
			final Paper paper = pageFormat.getPaper();
			result = paper.getImageableHeight() / MILLIMETER_IN_POINTS;
		}

		return result;
	}

	/**
	 * Sets the height of the imageable area of the printer's default page format,
	 * in millimeters.
	 *
	 * @param height Height of the imageable area, in millimeters.
	 */
	public void setImageablePageHeight( final double height )
	{
		if ( !MathTools.almostEqual( height, getImageablePageHeight() ) )
		{
			final PageFormat pageFormat = getOrCreatePageFormat();
			final Paper paper = pageFormat.getPaper();
			paper.setImageableArea( paper.getImageableX(), paper.getImageableY(), paper.getImageableWidth(), height * MILLIMETER_IN_POINTS );
			pageFormat.setPaper( paper );
		}
	}

	/**
	 * Sets the imageable area of the printer's default page format.
	 *
	 * @param x      X-coordinate of the top-left corner of the imageable area, in
	 *               millimeters.
	 * @param y      Y-coordinate of the top-left corner of the imageable area, in
	 *               millimeters.
	 * @param width  Width of the imageable area, in millimeters.
	 * @param height Height of the imageable area, in millimeters.
	 */
	public void setImageableArea( final double x, final double y, final double width, final double height )
	{
		final PageFormat pageFormat = getOrCreatePageFormat();

		final Paper paper = pageFormat.getPaper();
		paper.setImageableArea( x * MILLIMETER_IN_POINTS, y * MILLIMETER_IN_POINTS, width * MILLIMETER_IN_POINTS, height * MILLIMETER_IN_POINTS );
		pageFormat.setPaper( paper );

	}

	/**
	 * Get print margins.
	 *
	 * @return Print margins (in millimeters).
	 */
	public Insets2D getMargins()
	{
		final Insets2D result;

		final PageFormat pageFormat = _pageFormat;
		if ( pageFormat != null )
		{
			final double left = getImageableX();
			final double right = getPageWidth() - getImageablePageWidth() - left;
			final double top = getImageableY();
			final double bottom = getPageHeight() - getImageablePageHeight() - top;

			result = new Insets2D.Double( top, left, bottom, right );
		}
		else
		{
			result = new Insets2D.Double();
		}

		return result;
	}

	/**
	 * Get print margins.
	 *
	 * @param margins Print margins (in millimeters).
	 */
	public void setMargins( final Insets2D margins )
	{
		final double x = margins.getLeft();
		final double y = margins.getTop();
		final double width = getPageWidth() - x - margins.getRight();
		final double height = getPageHeight() - y - margins.getBottom();

		setImageableArea( x, y, width, height );
	}

	/**
	 * Convenience method to set media size while maintaining margins (the
	 * imageable area is adjusted).
	 *
	 * @param mediaSize Media size to set.
	 */
	public void setMediaSize( @NotNull final MediaSize mediaSize )
	{
		final PageFormat pageFormat = getOrCreatePageFormat();
		final Paper paper = pageFormat.getPaper();

		final double marginTop = paper.getImageableY();
		final double marginLeft = paper.getImageableX();
		final double marginBottom = paper.getHeight() - paper.getImageableHeight() - marginTop;
		final double marginRight = paper.getWidth() - paper.getImageableWidth() - marginLeft;
		final double paperWidth = mediaSize.getX( Size2DSyntax.MM ) * MILLIMETER_IN_POINTS;
		final double paperHeight = mediaSize.getY( Size2DSyntax.MM ) * MILLIMETER_IN_POINTS;

		paper.setSize( paperWidth, paperHeight );
		paper.setImageableArea( marginLeft, marginTop, paperWidth - marginLeft - marginRight, paperHeight - marginTop - marginBottom );

		pageFormat.setPaper( paper );
	}

	@Nullable
	public PageFormat getPageFormat()
	{
		return ( _pageFormat == null ) ? null : (PageFormat)_pageFormat.clone();
	}

	public void setPageFormat( @Nullable final PageFormat pageFormat )
	{
		_pageFormat = ( pageFormat == null ) ? null : (PageFormat)pageFormat.clone();
	}

	/**
	 * Internal method to get current {@link PageFormat} or create a new one.
	 *
	 * @return {@link PageFormat}.
	 */
	@NotNull
	private PageFormat getOrCreatePageFormat()
	{
		PageFormat pageFormat = _pageFormat;
		if ( pageFormat == null )
		{
			pageFormat = new PageFormat();
			_pageFormat = pageFormat;
		}
		return pageFormat;
	}
}
