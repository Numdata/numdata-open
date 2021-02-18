/**
 * Surrogate for 'java.util.Locale'.
 *
 * @author Gerrit Meinders
 */
export default class Locale
{
    /**
     * Constant for language.
     */
    static ENGLISH: Locale;

    /**
     * Constant for language.
     */
    static FRENCH: Locale;

    /**
     * Constant for language.
     */
    static GERMAN: Locale;

    /**
     * Constant for language.
     */
    static ITALIAN: Locale;

    /**
     * Constant for language.
     */
    static JAPANESE: Locale;

    /**
     * Constant for language.
     */
    static KOREAN: Locale;

    /**
     * Constant for language.
     */
    static CHINESE: Locale;

    /**
     * Constant for language.
     */
    static SIMPLIFIED_CHINESE: Locale;

    /**
     * Constant for language.
     */
    static TRADITIONAL_CHINESE: Locale;

    /**
     * Constant for country.
     */
    static FRANCE: Locale;

    /**
     * Constant for country.
     */
    static GERMANY: Locale;

    /**
     * Constant for country.
     */
    static ITALY: Locale;

    /**
     * Constant for country.
     */
    static JAPAN: Locale;

    /**
     * Constant for country.
     */
    static KOREA: Locale;

    /**
     * Constant for country.
     */
    static CHINA: Locale;

    /**
     * Constant for country.
     */
    static PRC: Locale;

    /**
     * Constant for country.
     */
    static TAIWAN: Locale;

    /**
     * Constant for country.
     */
    static UK: Locale;

    /**
     * Constant for country.
     */
    static US: Locale;

    /**
     * Constant for country.
     */
    static CANADA: Locale;

    /**
     * Constant for country.
     */
    static CANADA_FRENCH: Locale;

    /**
     * Constant for the root locale.
     */
    static ROOT: Locale;

    /**
     * Returns the default locale.
     *
     * @returns {Locale} Default locale.
     */
    static getDefault(): Locale;

    /**
     * @param [language] An ISO 639 alpha-2 language code.
     * @param [country] An ISO 3166 alpha-2 country code.
     * @param [variant] Any arbitrary value used to indicate a variation.
     */
    constructor( language: string, country?: string, variant?: string );

    language: string;

    country: string;

    variant: string;

    toString(): string;
}
