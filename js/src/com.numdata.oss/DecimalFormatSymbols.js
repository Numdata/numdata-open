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

/**
 * Surrogate for 'java.text.DecimalFormatSymbols'.
 *
 * @author Gerrit Meinders
 */
export default class DecimalFormatSymbols
{
	/**
	 * Decimal separator character.
	 */
	decimalSeparator;

	/**
	 * @param {string} decimalSeparator
	 */
	constructor( decimalSeparator )
	{
		this.decimalSeparator = decimalSeparator;
	}

	/**
	 * Returns decimal format symbols for the given locale.
	 *
	 * @param {Locale} locale
	 *
	 * @returns {DecimalFormatSymbols}
	 */
	static getInstance( locale )
	{
		return new DecimalFormatSymbols( decimalSymbolComma.indexOf( locale.toString() ) === -1 ? '.' : ',' );
	}
}

// Java locales that use comma. All others use period.
const decimalSymbolComma = [
	'be', 'be_BY', 'bg', 'bg_BG', 'ca', 'ca_ES', 'cs', 'cs_CZ', 'da', 'da_DK',
	'de', 'de_AT', 'de_DE', 'de_LU', 'el', 'el_CY', 'el_GR', 'es',
	'es_AR', 'es_BO', 'es_CL', 'es_CO', 'es_EC', 'es_ES', 'es_PE', 'es_PY',
	'es_UY', 'es_VE', 'et', 'et_EE', 'fi', 'fi_FI', 'fr', 'fr_BE', 'fr_CA',
	'fr_FR', 'fr_LU', 'hr', 'hr_HR', 'hu', 'hu_HU', 'in', 'in_ID', 'is',
	'is_IS', 'it', 'it_IT', 'lt', 'lt_LT', 'lv', 'lv_LV', 'mk', 'mk_MK', 'nl',
	'nl_BE', 'nl_NL', 'no', 'no_NO', 'no_NO_NY', 'pl', 'pl_PL', 'pt', 'pt_BR',
	'pt_PT', 'ro', 'ro_RO', 'ru', 'ru_RU', 'sk', 'sk_SK', 'sl', 'sl_SI', 'sq',
	'sq_AL', 'sr', 'sr_BA', 'sr_CS', 'sr_ME', 'sr_RS', 'sv', 'sv_SE', 'tr',
	'tr_TR', 'uk', 'uk_UA', 'vi', 'vi_VN'
];
