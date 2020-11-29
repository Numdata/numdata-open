/*
 * Copyright (c) 2010-2019, Numdata BV, The Netherlands.
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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class contains functionality related to the {@link Locale} class.
 *
 * @author Peter S. Heijnen
 */
public class LocaleTools
{
	/**
	 * Returns the given locale in RFC 5646 syntax.
	 *
	 * @param locale Locale to be converted.
	 *
	 * @return Locale in RFC 5646 syntax.
	 */
	@NotNull
	public static String getRfc5646ByLocale( @NotNull final Locale locale )
	{
		return getRfc5646ByLocale( locale.toString() );
	}

	/**
	 * Returns the given locale in RFC 5646 syntax.
	 *
	 * @param locale Locale as formatted by {@link Locale#toString()}.
	 *
	 * @return Locale in RFC 5646 syntax.
	 */
	@NotNull
	public static String getRfc5646ByLocale( @NotNull final String locale )
	{
		return locale.replace( "_", "-" );
	}

	/**
	 * Returns a Java locale representing the given RFC 5646 language tag.
	 *
	 * @param language Language tag in RFC 5646 syntax.
	 *
	 * @return Locale for the given tag.
	 */
	@NotNull
	public static Locale getLocaleByRfc5646( @NotNull final String language )
	{
		final String locale = language.replace( "-", "_" );
		return parseLocale( locale );
	}

	/**
	 * Get {@link Locale} for Microsoft locale ID (LCID).
	 *
	 * @param lcid Microsoft locale ID (LCID)
	 *
	 * @return {@link Locale}; {@code null} if unknown.
	 */
	public static Locale getLocaleByLcid( final int lcid )
	{
		final LocaleTools localeTools = getInstance();
		return localeTools._lcidToLocale.get( lcid );
	}

	/**
	 * Get {@link Locale} for Microsoft locale using its three letter acronym
	 * (TLA).
	 *
	 * @param tla Microsoft locale's three letter acronym (TLA).
	 *
	 * @return {@link Locale}; {@code null} if unknown.
	 */
	@Nullable
	public static Locale getLocaleByTla( final String tla )
	{
		final LocaleTools localeTools = getInstance();
		final Integer lcid = ( tla != null ) ? localeTools._tlaToLcid.get( tla.toUpperCase() ) : null;
		return ( lcid != null ) ? localeTools._lcidToLocale.get( lcid ) : null;
	}

	/**
	 * Get Microsoft locale ID (LCID) for {@link Locale}.
	 *
	 * @param locale {@link Locale}.
	 *
	 * @return Microsoft locale ID (LCID); {@code -1} if unknown.
	 */
	public static int getLcidByLocale( final Locale locale )
	{
		final LocaleTools localeTools = getInstance();
		final Integer lcid = localeTools._localeToLcid.get( locale );
		return ( lcid != null ) ? lcid : -1;
	}

	/**
	 * Get Microsoft locale ID (LCID) for three letter acronym (TLA).
	 *
	 * @param tla Microsoft three letter acronym (TLA) for locale.
	 *
	 * @return Microsoft locale ID (LCID); {@code -1} if unknown.
	 */
	public static int getLcidByTla( final String tla )
	{
		final LocaleTools localeTools = getInstance();
		final Integer lcid = ( tla != null ) ? localeTools._tlaToLcid.get( tla.toUpperCase() ) : null;
		return ( lcid != null ) ? lcid : -1;
	}

	/**
	 * Get three letter acronym (TLA) for Microsoft locale ID (LCID).
	 *
	 * @param lcid Microsoft locale ID (LCID).
	 *
	 * @return Three letter acronym; {@code null} if unknown.
	 */
	public static String getTlaByLcid( final int lcid )
	{
		final LocaleTools localeTools = getInstance();
		return localeTools._lcidToTla.get( lcid );
	}

	/**
	 * Get Microsoft locale's three letter acronym (TLA) for {@link Locale}.
	 *
	 * @param locale {@link Locale}.
	 *
	 * @return Three letter acronym for Microsoft locale. {@code null} if
	 * unknown.
	 */
	@Nullable
	public static String getTlaByLocale( final Locale locale )
	{
		final LocaleTools localeTools = getInstance();
		final Integer lcid = localeTools._localeToLcid.get( locale );
		return ( lcid != null ) ? localeTools._lcidToTla.get( lcid ) : null;
	}

	/**
	 * Returns the locale specified by the given string.
	 *
	 * @param string Locale string to be parsed; see {@link Locale#toString()}.
	 *
	 * @return Locale for the given string.
	 */
	@NotNull
	public static Locale parseLocale( @Nullable final String string )
	{
		final Locale result;

		if ( ( string == null ) || string.isEmpty() )
		{
			result = Locale.ROOT;
		}
		else
		{
			final int u1 = string.indexOf( '_' );
			if ( u1 < 0 )
			{
				result = new Locale( string );
			}
			else
			{
				final int u2 = string.indexOf( '_', u1 + 1 );
				if ( u2 < 0 )
				{
					result = new Locale( string.substring( 0, u1 ), string.substring( u1 + 1 ) );
				}
				else
				{
					result = new Locale( string.substring( 0, u1 ), string.substring( u1 + 1, u2 ), string.substring( u2 + 1 ) );
				}
			}
		}

		return result;
	}

	/**
	 * Maps Microsoft locale ID (LCID) to {@link Locale}.
	 */
	private final Map<Integer, Locale> _lcidToLocale = new HashMap<Integer, Locale>();

	/**
	 * Maps {@link Locale} to Microsoft locale ID (LCID).
	 */
	private final Map<Locale, Integer> _localeToLcid = new HashMap<Locale, Integer>();

	/**
	 * Maps three letter acronym (TLA) to Microsoft locale ID (LCID).
	 */
	private final Map<String, Integer> _tlaToLcid = new HashMap<String, Integer>();

	/**
	 * Maps Microsoft locale ID (LCID) to three letter acronym (TLA).
	 */
	private final Map<Integer, String> _lcidToTla = new HashMap<Integer, String>();

	/**
	 * Singleton instance of this class.
	 */
	@SuppressWarnings( "StaticNonFinalField" )
	private static LocaleTools _instance = null;

	/**
	 * Get singleton instance of this class.
	 *
	 * @return Singleton instance.
	 */
	private static LocaleTools getInstance()
	{
		LocaleTools result = _instance;
		if ( result == null )
		{
			result = new LocaleTools();
		}
		return result;
	}

	/**
	 * Construct instance.
	 */
	@SuppressWarnings( "AssignmentToStaticFieldFromInstanceMethod" )
	private LocaleTools()
	{
		if ( _instance != null )
		{
			throw new AssertionError();
		}

		_instance = this;

		createLcidMapping( 0x0436, "AFK", new Locale( "af" ) ); /* Afrikaans */
		createLcidMapping( 0x041c, "SQI", new Locale( "sq" ) ); /* Albanian */
		createLcidMapping( 0x3801, "ARU", new Locale( "ar", "AE" ) ); /* Arabic - United Arab Emirates */
		createLcidMapping( 0x3c01, "ARH", new Locale( "ar", "BH" ) ); /* Arabic - Bahrain */
		createLcidMapping( 0x1401, "ARG", new Locale( "ar", "DZ" ) ); /* Arabic - Algeria */
		createLcidMapping( 0x0c01, "ARE", new Locale( "ar", "EG" ) ); /* Arabic - Egypt */
		createLcidMapping( 0x0801, "ARI", new Locale( "ar", "IQ" ) ); /* Arabic - Iraq */
		createLcidMapping( 0x2c01, "ARJ", new Locale( "ar", "JO" ) ); /* Arabic - Jordan */
		createLcidMapping( 0x3401, "ARK", new Locale( "ar", "KW" ) ); /* Arabic - Kuwait */
		createLcidMapping( 0x3001, "ARB", new Locale( "ar", "LB" ) ); /* Arabic - Lebanon */
		createLcidMapping( 0x1001, "ARL", new Locale( "ar", "LY" ) ); /* Arabic - Libya */
		createLcidMapping( 0x1801, "ARM", new Locale( "ar", "MA" ) ); /* Arabic - Morocco */
		createLcidMapping( 0x2001, "ARO", new Locale( "ar", "OM" ) ); /* Arabic - Oman */
		createLcidMapping( 0x4001, "ARQ", new Locale( "ar", "QA" ) ); /* Arabic - Qatar */
		createLcidMapping( 0x0401, "ARA", new Locale( "ar", "SA" ) ); /* Arabic - Saudi Arabia */
		createLcidMapping( 0x2801, "ARS", new Locale( "ar", "SY" ) ); /* Arabic - Syria */
		createLcidMapping( 0x1c01, "ART", new Locale( "ar", "TN" ) ); /* Arabic - Tunisia */
		createLcidMapping( 0x2401, "ARY", new Locale( "ar", "YE" ) ); /* Arabic - Yemen */
		createLcidMapping( 0x042b, "HYE", new Locale( "hy" ) ); /* Armenian */
		createLcidMapping( 0x042c, "AZE", new Locale( "az", "AZ" ) ); /* Azeri - Latin */
		createLcidMapping( 0x082c, "AZE", new Locale( "az", "AZ" ) ); /* Azeri - Cyrillic */
		createLcidMapping( 0x042d, "EUQ", new Locale( "eu" ) ); /* Basque */
		createLcidMapping( 0x0423, "BEL", new Locale( "be" ) ); /* Belarusian */
		createLcidMapping( 0x0402, "BGR", new Locale( "bg" ) ); /* Bulgarian */
		createLcidMapping( 0x0403, "CAT", new Locale( "ca" ) ); /* Catalan */
		createLcidMapping( 0x0804, "CHS", Locale.SIMPLIFIED_CHINESE ); /* Chinese - China */
		createLcidMapping( 0x0c04, "ZHH", new Locale( "zh", "HK" ) ); /* Chinese - Hong Kong SAR */
		createLcidMapping( 0x1404, "ZHM", new Locale( "zh", "MO" ) ); /* Chinese - Macau SAR */
		createLcidMapping( 0x1004, "ZHI", new Locale( "zh", "SG" ) ); /* Chinese - Singapore */
		createLcidMapping( 0x0404, "CHT", Locale.TRADITIONAL_CHINESE ); /* Chinese - Taiwan */
		createLcidMapping( 0x041a, "HRV", new Locale( "hr" ) ); /* Croatian */
		createLcidMapping( 0x0405, "CSY", new Locale( "cs" ) ); /* Czech */
		createLcidMapping( 0x0406, "DAN", new Locale( "da" ) ); /* Danish */
		createLcidMapping( 0x0413, "NLD", new Locale( "nl", "NL" ) ); /* Dutch - Netherlands */
		createLcidMapping( 0x0813, "NLB", new Locale( "nl", "BE" ) ); /* Dutch - Belgium */
		createLcidMapping( 0x0c09, "ENA", new Locale( "en", "AU" ) ); /* English - Australia */
		createLcidMapping( 0x2809, "ENL", new Locale( "en", "BZ" ) ); /* English - Belize */
		createLcidMapping( 0x1009, "ENC", Locale.CANADA ); /* English - Canada */
		createLcidMapping( 0x2409, "ENB", new Locale( "en", "CB" ) ); /* English - Caribbean */
		createLcidMapping( 0x1809, "ENI", new Locale( "en", "IE" ) ); /* English - Ireland */
		createLcidMapping( 0x2009, "ENJ", new Locale( "en", "JM" ) ); /* English - Jamaica */
		createLcidMapping( 0x1409, "ENZ", new Locale( "en", "NZ" ) ); /* English - New Zealand */
		createLcidMapping( 0x3409, "ENP", new Locale( "en", "PH" ) ); /* English - Phillippines */
		createLcidMapping( 0x1c09, "ENS", new Locale( "en", "ZA" ) ); /* English - Southern Africa */
		createLcidMapping( 0x2c09, "ENT", new Locale( "en", "TT" ) ); /* English - Trinidad */
		createLcidMapping( 0x0809, "ENG", Locale.UK ); /* English - Great Britain */
		createLcidMapping( 0x0409, "ENU", Locale.US ); /* English - United States */
		createLcidMapping( 0x0425, "ETI", new Locale( "et" ) ); /* Estonian */
		createLcidMapping( 0x0429, "FAR", new Locale( "fa" ) ); /* Farsi */
		createLcidMapping( 0x040b, "FIN", new Locale( "fi" ) ); /* Finnish */
		createLcidMapping( 0x0438, "FOS", new Locale( "fo" ) ); /* Faroese */
		createLcidMapping( 0x040c, "FRA", Locale.FRANCE ); /* French - France */
		createLcidMapping( 0x080c, "FRB", new Locale( "fr", "BE" ) ); /* French - Belgium */
		createLcidMapping( 0x0c0c, "FRC", Locale.CANADA_FRENCH ); /* French - Canada */
		createLcidMapping( 0x140c, "FRL", new Locale( "fr", "LU" ) ); /* French - Luxembourg */
		createLcidMapping( 0x100c, "FRS", new Locale( "fr", "CH" ) ); /* French - Switzerland */
		createLcidMapping( 0x0407, "DEU", Locale.GERMANY ); /* German - Germany */
		createLcidMapping( 0x0c07, "DEA", new Locale( "de", "AT" ) ); /* German - Austria */
		createLcidMapping( 0x1407, "DEC", new Locale( "de", "LI" ) ); /* German - Liechtenstein */
		createLcidMapping( 0x1007, "DEL", new Locale( "de", "LU" ) ); /* German - Luxembourg */
		createLcidMapping( 0x0807, "DES", new Locale( "de", "CH" ) ); /* German - Switzerland */
		createLcidMapping( 0x0408, "ELL", new Locale( "el" ) ); /* Greek */
		createLcidMapping( 0x040d, "HEB", new Locale( "he" ) ); /* Hebrew */
		createLcidMapping( 0x0439, "HIN", new Locale( "hi" ) ); /* Hindi */
		createLcidMapping( 0x040e, "HUN", new Locale( "hu" ) ); /* Hungarian */
		createLcidMapping( 0x040f, "ISL", new Locale( "is" ) ); /* Icelandic */
		createLcidMapping( 0x0421, "IND", new Locale( "id" ) ); /* Indonesian */
		createLcidMapping( 0x0410, "ITA", Locale.ITALY ); /* Italian - Italy */
		createLcidMapping( 0x0810, "ITS", new Locale( "it", "CH" ) ); /* Italian - Switzerland */
		createLcidMapping( 0x0411, "JPN", Locale.JAPANESE ); /* Japanese */
		createLcidMapping( 0x0412, "KOR", Locale.KOREAN ); /* Korean */
		createLcidMapping( 0x0426, "LVI", new Locale( "lv" ) ); /* Latvian */
		createLcidMapping( 0x0427, "LTH", new Locale( "lt" ) ); /* Lithuanian */
		createLcidMapping( 0x042f, "MKI", new Locale( "mk" ) ); /* F.Y.R.O. Macedonia */
		createLcidMapping( 0x043e, "MSL", new Locale( "ms", "MY" ) ); /* Malay - Malaysia */
		createLcidMapping( 0x083e, "MSB", new Locale( "ms", "BN" ) ); /* Malay - Brunei */
		createLcidMapping( 0x044e, "MAR", new Locale( "mr" ) ); /* Marathi */
		createLcidMapping( 0x0414, "NOR", new Locale( "no", "NO" ) ); /* Norwegian - Bokml */
		createLcidMapping( 0x0814, "NON", new Locale( "no", "NO" ) ); /* Norwegian - Nynorsk */
		createLcidMapping( 0x0415, "PLK", new Locale( "pl" ) ); /* Polish */
		createLcidMapping( 0x0816, "PTG", new Locale( "pt", "PT" ) ); /* Portuguese - Portugal */
		createLcidMapping( 0x0416, "PTB", new Locale( "pt", "BR" ) ); /* Portuguese - Brazil */
		createLcidMapping( 0x0418, "ROM", new Locale( "ro" ) ); /* Romanian - Romania */
		createLcidMapping( 0x0419, "RUS", new Locale( "ru" ) ); /* Russian */
		createLcidMapping( 0x044f, "SAN", new Locale( "sa" ) ); /* Sanskrit */
		createLcidMapping( 0x0c1a, "SRB", new Locale( "sr", "SP" ) ); /* Serbian - Cyrillic */
		createLcidMapping( 0x081a, "SRL", new Locale( "sr", "SP" ) ); /* Serbian - Latin */
		createLcidMapping( 0x0424, "SLV", new Locale( "sl" ) ); /* Slovenian */
		createLcidMapping( 0x041b, "SKY", new Locale( "sk" ) ); /* Slovak */
		createLcidMapping( 0x040a, "ESP", new Locale( "es", "ES" ) ); /* Spanish - Spain (Traditional) */
		createLcidMapping( 0x2c0a, "ESS", new Locale( "es", "AR" ) ); /* Spanish - Argentina */
		createLcidMapping( 0x400a, "ESB", new Locale( "es", "BO" ) ); /* Spanish - Bolivia */
		createLcidMapping( 0x340a, "ESL", new Locale( "es", "CL" ) ); /* Spanish - Chile */
		createLcidMapping( 0x240a, "ESO", new Locale( "es", "CO" ) ); /* Spanish - Colombia */
		createLcidMapping( 0x140a, "ESC", new Locale( "es", "CR" ) ); /* Spanish - Costa Rica */
		createLcidMapping( 0x1c0a, "ESD", new Locale( "es", "DO" ) ); /* Spanish - Dominican Republic */
		createLcidMapping( 0x300a, "ESF", new Locale( "es", "EC" ) ); /* Spanish - Ecuador */
		createLcidMapping( 0x100a, "ESG", new Locale( "es", "GT" ) ); /* Spanish - Guatemala */
		createLcidMapping( 0x480a, "ESH", new Locale( "es", "HN" ) ); /* Spanish - Honduras */
		createLcidMapping( 0x080a, "ESM", new Locale( "es", "MX" ) ); /* Spanish - Mexico */
		createLcidMapping( 0x4c0a, "ESI", new Locale( "es", "NI" ) ); /* Spanish - Nicaragua */
		createLcidMapping( 0x180a, "ESA", new Locale( "es", "PA" ) ); /* Spanish - Panama */
		createLcidMapping( 0x280a, "ESR", new Locale( "es", "PE" ) ); /* Spanish - Peru */
		createLcidMapping( 0x500a, "ESU", new Locale( "es", "PR" ) ); /* Spanish - Puerto Rico */
		createLcidMapping( 0x3c0a, "ESZ", new Locale( "es", "PY" ) ); /* Spanish - Paraguay */
		createLcidMapping( 0x440a, "ESE", new Locale( "es", "SV" ) ); /* Spanish - El Salvador */
		createLcidMapping( 0x380a, "ESY", new Locale( "es", "UY" ) ); /* Spanish - Uruguay */
		createLcidMapping( 0x200a, "ESV", new Locale( "es", "VE" ) ); /* Spanish - Venezuela */
		createLcidMapping( 0x0441, "SWK", new Locale( "sw" ) ); /* Swahili */
		createLcidMapping( 0x041d, "SVE", new Locale( "sv", "SE" ) ); /* Swedish - Sweden */
		createLcidMapping( 0x081d, "SVF", new Locale( "sv", "FI" ) ); /* Swedish - Finland */
		createLcidMapping( 0x0449, "TAM", new Locale( "ta" ) ); /* Tamil */
		createLcidMapping( 0x0444, "TTT", new Locale( "tt" ) ); /* Tatar */
		createLcidMapping( 0x041e, "THA", new Locale( "th" ) ); /* Thai */
		createLcidMapping( 0x041f, "TRK", new Locale( "tr" ) ); /* Turkish */
		createLcidMapping( 0x0422, "UKR", new Locale( "uk" ) ); /* Ukrainian */
		createLcidMapping( 0x0420, "URD", new Locale( "ur" ) ); /* Urdu */
		createLcidMapping( 0x0843, "UZB", new Locale( "uz", "UZ" ) ); /* Uzbek - Cyrillic */
		createLcidMapping( 0x0443, "UZB", new Locale( "uz", "UZ" ) ); /* Uzbek - Latin */
		createLcidMapping( 0x042a, "VIT", new Locale( "vi" ) ); /* Vietnamese */
	}

	/**
	 * Internal helper method to create LCID mapping entry.
	 *
	 * @param lcid   Micorosoft locale ID.
	 * @param tla    Three letter acronym.
	 * @param locale {@link Locale} instance.
	 */
	private void createLcidMapping( final int lcid, final String tla, final Locale locale )
	{
		final Integer lcidInstance = lcid;
		_lcidToLocale.put( lcidInstance, locale );
		_localeToLcid.put( locale, lcidInstance );
		_lcidToTla.put( lcidInstance, tla );
		_tlaToLcid.put( tla, lcidInstance );
	}

	/**
	 * Returns whether the given string is a 2-letter language code as defined
	 * by ISO 639.
	 *
	 * @param language String to be checked.
	 *
	 * @return {@code true} if the string is a 2-letter language code.
	 */
	public static boolean isIsoLanguage( final String language )
	{
		final List<String> isoLanguages = Arrays.asList( Locale.getISOLanguages() );
		return isoLanguages.contains( language );
	}

	/**
	 * Returns whether the given string is a 2-letter country code as defined by
	 * ISO 3166.
	 *
	 * @param country String to be checked.
	 *
	 * @return {@code true} if the string is a 2-letter country code.
	 */
	public static boolean isIsoCountry( final String country )
	{
		final List<String> isoCountries = Arrays.asList( Locale.getISOCountries() );
		return isoCountries.contains( country );
	}

	/**
	 * Returns the best matching locale from the given list.
	 *
	 * @param defaultResult    Result if there are no (partial) matches at all.
	 * @param availableLocales Available locales.
	 * @param language         Preferred language.
	 * @param country          Preferred country.
	 *
	 * @return Best matching locale.
	 */
	public static Locale getBestMatch( @Nullable final Locale defaultResult, @NotNull final Iterable<Locale> availableLocales, @Nullable final String language, @Nullable final String country )
	{
		return getBestMatch( defaultResult, availableLocales, new Locale( ( language != null ) ? language.trim() : "", ( country != null ) ? country.trim() : "", "" ) );
	}

	/**
	 * Returns the best matching locale from the given list.
	 *
	 * @param defaultResult    Result if there are no (partial) matches at all.
	 * @param availableLocales Available locales.
	 * @param preferredLocale  Preferred locale.
	 *
	 * @return Best matching locale or default result.
	 */
	@Contract( "!null,_,_ -> !null" )
	@Nullable
	public static Locale getBestMatch( @Nullable final Locale defaultResult, @NotNull final Iterable<Locale> availableLocales, @Nullable final Locale preferredLocale )
	{
		Locale result = defaultResult;

		if ( preferredLocale != null )
		{
			int resultPreference = 0;

			for ( final Locale locale : availableLocales )
			{
				final int preference = !locale.getLanguage().equals( preferredLocale.getLanguage() ) ? 0 :
				                       !locale.getCountry().equals( preferredLocale.getCountry() ) ? 1 :
				                       !locale.getVariant().equals( preferredLocale.getVariant() ) ? 2 : 3;
				if ( preference > resultPreference )
				{
					result = locale;
					resultPreference = preference;
				}
			}
		}

		return result;
	}
}
