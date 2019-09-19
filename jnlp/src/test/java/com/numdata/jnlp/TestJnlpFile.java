/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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
package com.numdata.jnlp;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;

import com.numdata.jnlp.JnlpFile.*;
import com.numdata.jnlp.JnlpInformation.*;
import com.numdata.oss.io.*;
import org.junit.*;
import org.w3c.dom.*;

/**
 * Unit test for {@link JnlpFile} API.
 *
 * @author Peter S. Heijnen
 */
public class TestJnlpFile
{
	/**
	 * Test writing of almost empty JNLP files.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testBasic()
	throws Exception
	{
		final JnlpFile jnlpFile = new JnlpFile();
		jnlpFile.addInformation();
		jnlpFile.createApplication();

		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter( System.out, "UTF-8" );
		jnlpFile.write( xmlStreamWriter );
		xmlStreamWriter.close();
	}

	/**
	 * Test writing of complex JNLP files.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testComplex()
	throws Exception
	{
		final JnlpFile jnlpFile = new JnlpFile();
		jnlpFile.setUpdateCheck( UpdateCheck.ALWAYS );
		jnlpFile.setUpdatePolicy( UpdatePolicy.ALWAYS );
		jnlpFile.setSpec( "1.6+" );
		jnlpFile.setApplicationPermissions( true );
		jnlpFile.setHref( "http://org.test:1234/jnlp/app.jnlp" );
		jnlpFile.setCodebase( "http://org.test:1234/jnlp/codebase/" );

		final JnlpInformation defaultInformation = jnlpFile.addInformation();
		defaultInformation.setTitle( "Example Application" );
		defaultInformation.setVendor( "Numdata BV" );
		defaultInformation.setHomepage( "http://www.numdata.com/" );
		defaultInformation.setDescription( KindOfDescription.DEFAULT, "This application is used for unit testing." );
		defaultInformation.setDescription( KindOfDescription.SHORT, "This is not a real application; if you think it is real, please schedule a counseling session." );

		final JnlpInformation dutchInformation = jnlpFile.addInformation();
		dutchInformation.addLocale( new Locale( "nl" ) );
		dutchInformation.setDescription( KindOfDescription.DEFAULT, "Deze applicatie wordt gebruikt voor unit testen." );

		final JnlpResources resources = jnlpFile.addResources();

		final JnlpJreElement java6 = resources.addJava();
		java6.addVersion( "1.6+" );
		java6.setInitialHeapSize( "64m" );
		java6.setMaxHeapSize( "256m" );
		java6.addJavaVmArg( "-server" );

		final JnlpJreElement legacyJava = resources.addJava();
		legacyJava.addVersion( "1.3+" );
		legacyJava.setInitialHeapSize( "64m" );
		legacyJava.setMaxHeapSize( "256m" );
		legacyJava.addJavaVmArg( "-server" );
		final JnlpResources legacyResources = legacyJava.addResources();
		final JnlpJar legacyJar = legacyResources.addJar();
		legacyJar.setHref( "legacy-support.jar" );

		final JnlpJar mainJar = resources.addJar();
		mainJar.setMain( Boolean.TRUE );
		mainJar.setHref( "classes/MyApp.jar" );
		mainJar.setVersion( "1.4.0_04 1.4&1.4.1_02+" );

		final JnlpJar libJar1 = resources.addJar();
		libJar1.setHref( "libs/lib1.jar" );
		libJar1.setLazyDownload( Boolean.TRUE );
		libJar1.setPart( "lib1" );

		resources.addPackage( "test.org.Lib1Class", "lib1", false );
		resources.addPackage( "test.org.lib1.*", "lib1", true );
		resources.addPackage( "test.org.support.lib1.*", "lib1", false );

		final JnlpJar libJar2 = resources.addJar();
		libJar2.setHref( "libs/lib2.jar" );
		libJar2.setLazyDownload( Boolean.TRUE );
		libJar2.setVersion( "2.1+" );
		libJar2.setPart( "lib2" );

		resources.addPackage( "test.org.Lib2Class", "lib2", false );
		resources.addPackage( "test.org.lib2.*", "lib2", true );
		resources.addPackage( "test.org.support.lib2.*", "lib2", false );

		final JnlpExtension extension = resources.addExtension();
		extension.setName( "myExtension" );
		extension.setHref( "myExtension.jnlp" );
		extension.setVersion( "1.0" );

		final JnlpExtDownload extDownload1 = extension.addExtDownload();
		extDownload1.setExtPart( "base" );
		extDownload1.setPart( "lib1" );
		extDownload1.setLazyDownload( true );

		final JnlpExtDownload extDownload2 = extension.addExtDownload();
		extDownload2.setExtPart( "base" );
		extDownload2.setPart( "lib2" );
		extDownload2.setLazyDownload( true );

		final JnlpExtDownload extDownload3 = extension.addExtDownload();
		extDownload3.setExtPart( "function1" );
		extDownload3.setPart( "lib1" );
		extDownload3.setLazyDownload( true );

		final JnlpExtDownload extDownload4 = extension.addExtDownload();
		extDownload4.setExtPart( "function2" );
		extDownload4.setPart( "lib2" );
		extDownload4.setLazyDownload( true );

		final JnlpResources resourcesWin32 = jnlpFile.addResources();
		resourcesWin32.addOs( "Windows" );
		resourcesWin32.addArch( "x86" );
		final JnlpNativelib nativelibWin32 = resourcesWin32.addNativelib();
		nativelibWin32.setHref( "native-win32.jar" );

		final JnlpResources resourcesWin64 = jnlpFile.addResources();
		resourcesWin64.addOs( "Windows" );
		resourcesWin64.addArch( "amd64" );
		resourcesWin64.addArch( "x86_64" );
		final JnlpNativelib nativelibWin64 = resourcesWin64.addNativelib();
		nativelibWin64.setHref( "natives-win64.jar" );

		final JnlpApplicationDesc applicationDesc = jnlpFile.createApplication();
		applicationDesc.setMainClass( "test.org.Example" );
		applicationDesc.addArgument( "arg1" );
		applicationDesc.addArgument( "arg2" );

		printFormatted( jnlpFile );
	}

	/**
	 * Test writing of JNLP file for JOGL.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testJogl()
	throws Exception
	{
		final JnlpFile jnlpFile = new JnlpFile();
		jnlpFile.setHref( "http://org.test:1234/jnlp/app.jnlp" );
		jnlpFile.setCodebase( "http://org.test:1234/jnlp/codebase/" );
		final JnlpInformation information = jnlpFile.addInformation();
		information.setTitle( "Java Binding to the OpenGL API" );
		information.setVendor( "Sun Microsystems, Inc." );
		information.setHomepage( "http://jogl.dev.java.net/" );
		information.setDescription( KindOfDescription.DEFAULT, "Java Binding to the OpenGL API - JSR-231 Current Build" );
		information.setDescription( KindOfDescription.SHORT, "Java programming language binding to the OpenGL 3D graphics API. (Current build of JSR-231 APIs)" );
		information.setOfflineAllowed( true );
		jnlpFile.setAllPermissions( true );

		final JnlpResources resources = jnlpFile.addResources();
		final JnlpJar joglJar = resources.addJar();
		joglJar.setHref( "jogl.jar" );
		final JnlpExtension gluegenRtExtension = resources.addExtension();
		gluegenRtExtension.setName( "gluegen-rt" );
		gluegenRtExtension.setHref( jnlpFile.getCodebase() + "gluegen-rt.jnlp" );

		final JnlpResources resourcesWin32 = jnlpFile.addResources();
		resourcesWin32.addOs( "Windows" );
		resourcesWin32.addArch( "x86" );
		final JnlpNativelib nativelibWin32 = resourcesWin32.addNativelib();
		nativelibWin32.setHref( "jogl-natives-windows-i586.jar" );

		final JnlpResources resourcesWin64 = jnlpFile.addResources();
		resourcesWin64.addOs( "Windows" );
		resourcesWin64.addArch( "amd64" );
		resourcesWin64.addArch( "x86_64" );
		final JnlpNativelib nativelibWin64 = resourcesWin64.addNativelib();
		nativelibWin64.setHref( "jogl-natives-windows-amd64.jar" );

		jnlpFile.createComponent();

		printFormatted( jnlpFile );
	}

	/**
	 * Create nicely formatted JNLP file output.
	 *
	 * @param jnlpFile JNLP file to write.
	 *
	 * @throws Exception if the file could not be written.
	 */
	private static void printFormatted( final JnlpFile jnlpFile )
	throws Exception
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter( baos, "UTF-8" );
		jnlpFile.write( xmlStreamWriter );
		xmlStreamWriter.close();

		final ByteArrayInputStream in = new ByteArrayInputStream( baos.toByteArray() );

		final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		final Document document = documentBuilder.parse( in );
		final Element documentElement = document.getDocumentElement();

		XMLTools.prettyPrint( documentElement, System.out );
	}
}
