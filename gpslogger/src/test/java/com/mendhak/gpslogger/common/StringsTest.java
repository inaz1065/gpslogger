
package com.mendhak.gpslogger.common;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.test.suitebuilder.annotation.SmallTest;

import com.google.android.gms.location.DetectedActivity;
import com.mendhak.gpslogger.BuildConfig;
import com.mendhak.gpslogger.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.Null;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.mendhak.gpslogger.common.Strings.getBearingDescription;
import static com.mendhak.gpslogger.common.Strings.getDistanceDisplay;
import static com.mendhak.gpslogger.common.Strings.getSpeedDisplay;
import static com.mendhak.gpslogger.common.Strings.getTimeDisplay;
import static com.mendhak.gpslogger.common.Strings.toInt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class StringsTest {


    @Test
    public void HtmlDecode_WhenEntitiesPresent_EntitiesAreDecoded() {

        String actual = Strings.htmlDecode("Bert &amp; Ernie are here. They wish to &quot;talk&quot; to you.");
        String expected = "Bert & Ernie are here. They wish to \"talk\" to you.";
        assertThat("HTML Decode did not decode everything", actual, is(expected));

        actual = Strings.htmlDecode(null);
        expected = null;

        assertThat("HTML Decode should handle null input", actual, is(expected));

    }


    @Test
    public void GetIsoDateTime_DateObject_ConvertedToIso() {

        String actual = Strings.getIsoDateTime(new Date(1417726140000l));
        String expected = "2014-12-04T20:49:00.000Z";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }

    @Test
    public void GetIsoDateTime_HigherResolutionMilliseconds_ConvertedToIso() {

        String actual = Strings.getIsoDateTime(new Date(1417726140001l));
        String expected = "2014-12-04T20:49:00.001Z";
        assertThat("Conversion of date to ISO string", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Convert number to readable datetime simple.
     * Input : getReadableDateTime(new Date(1417726140001l))
     * Expected : "05 Dec 2014 05:49"
     */
    @Test
    public void GetReadableDateTime_ConvertedToSimple() {

        String actual = Strings.getReadableDateTime(new Date(1417726140001l));
        String expected = "05 Dec 2014 05:49";
        assertThat("Conversion of date to Simple string", actual, is(expected));
    }


    @Test
    public void CleanDescription_WhenAnnotationHasHtml_HtmlIsRemoved() {
        String content = "This is some annotation that will end up in an " +
                "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
                "and cause all sorts of mayhem. Either way, it won't \"work\"";

        String expected = "This is some annotation that will end up in an " +
                "XML file.  It will either bbreak/b or Bert &amp; Ernie will alert up" +
                "and cause all sorts of mayhem. Either way, it won't &quot;work&quot;";

        String actual = Strings.cleanDescriptionForXml(content);

        assertThat("Clean Description should remove characters", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Clean description which annotation has html for json .
     * Input : Strings.cleanDescriptionForJson(content)
     * String content = "This is some annotation that will end up in an " +
     * "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
     * "and cause all sorts of mayhem. \\Either way, it won't \"work\""
     * Expected : "This is some annotation that will end up in an " +
     * "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
     * "and cause all sorts of mayhem. Either way, it won't work"
     */
    @Test
    public void CleanDescriptionJson_WhenAnnotationHasHtml_HtmlIsRemoved() {
        String content = "This is some annotation that will end up in an " +
                "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
                "and cause all sorts of mayhem. \\Either way, it won't \"work\"";

        String expected = "This is some annotation that will end up in an " +
                "XML file.  It will either <b>break</b> or Bert & Ernie will alert up" +
                "and cause all sorts of mayhem. Either way, it won't work";

        String actual = Strings.cleanDescriptionForJson(content);

        assertThat("Clean Description should remove characters", actual, is(expected));
    }


    @Test
    public void GetFormattedCustomFileName_Serial_ReplacedWithBuildSerial() {
        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);

        String expected = "basename_" + Build.SERIAL;
        String actual = Strings.getFormattedCustomFileName("basename_%ser", gc, ph);
        assertThat("Static file name %SER should be replaced with Build Serial", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_Version_ReplacedWithBuildVersion() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);

        String expected = "basename_" + BuildConfig.VERSION_NAME;
        String actual = Strings.getFormattedCustomFileName("basename_%ver", gc, ph);
        assertThat("Static file name %VER should be replaced with Build Version", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_HOUR_ReplaceWithPaddedHour() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(4);

        String expected = "basename_04";
        String actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 4 AM should be replaced with 04", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(23);
        expected = "basename_23";
        actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 11PM should be relpaced with 23", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        expected = "basename_00";
        actual = Strings.getFormattedCustomFileName("basename_%HOUR", gc, ph);
        assertThat("%HOUR 0 should be relpaced with 00", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_MIN_ReplaceWithPaddedMinute() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(4);
        when(gc.get(Calendar.MINUTE)).thenReturn(7);

        String actual = Strings.getFormattedCustomFileName("basename_%HOUR%MIN", gc, ph);
        String expected = "basename_0407";
        assertThat(" %MIN 7 should be replaced with 07", actual, is(expected));

        when(gc.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        when(gc.get(Calendar.MINUTE)).thenReturn(0);

        actual = Strings.getFormattedCustomFileName("basename_%HOUR%MIN", gc, ph);
        expected = "basename_0000";
        assertThat(" %MIN 0 should be replaced with 00", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_YEARMONDAY_ReplaceWithYearMonthDay() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(gc.get(Calendar.YEAR)).thenReturn(2016);
        when(gc.get(Calendar.MONTH)).thenReturn(Calendar.FEBRUARY);
        when(gc.get(Calendar.DAY_OF_MONTH)).thenReturn(1);

        String actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        String expected = "basename_20160201";
        assertThat("Year 2016 Month February Day 1 should be replaced with 20160301", actual, is(expected));


        when(gc.get(Calendar.MONTH)).thenReturn(Calendar.DECEMBER);
        actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        expected = "basename_20161201";
        assertThat("December month should be replaced with 12", actual, is(expected));

        when(gc.get(Calendar.MONTH)).thenReturn(0);
        actual = Strings.getFormattedCustomFileName("basename_%YEAR%MONTH%DAY", gc, ph);
        expected = "basename_20160101";
        assertThat("Zero month should be replaced with 1", actual, is(expected));

    }

    @Test
    public void GetFormattedCustomFileName_PROFILE_ReplaceWithProfileName() {

        PreferenceHelper ph = mock(PreferenceHelper.class);
        Calendar gc = mock(Calendar.class);
        when(ph.getCurrentProfileName()).thenReturn("OOMPALOOMPA");

        String actual = Strings.getFormattedCustomFileName("basename_%PROFILE", gc, ph);
        String expected = "basename_OOMPALOOMPA";

        assertThat("Profile replaced with profile name", actual, is(expected));

    }

    @Test
    public void getFormattedCustomFileName_DAYNAME_ReplaceWithThreeLetterDayName() {
        PreferenceHelper ph = mock(PreferenceHelper.class);
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTimeInMillis(1495663380828l); //24 May 2017

        String actual = Strings.getFormattedCustomFileName("basename_%DAYNAME", greg, ph);
        String expected = "basename_thu";

        assertThat("Day name substituted in file name", actual, is(expected));
    }

    @Test
    public void getFormattedCustomFileName_MONTHNAME_ReplaceWithThreeLetterDayName() {
        PreferenceHelper ph = mock(PreferenceHelper.class);
        GregorianCalendar greg = new GregorianCalendar();
        greg.setTimeInMillis(1495663380828l); //Wed, 24 May 2017

        String actual = Strings.getFormattedCustomFileName("basename_%MONTHNAME", greg, ph);
        String expected = "basename_may";

        assertThat("Month name substituted in file name", actual, is(expected));
    }

    private Context GetDescriptiveTimeString_Context() {
        Context ctx = mock(Context.class);
        when(ctx.getString(R.string.time_onesecond)).thenReturn("1 second");
        when(ctx.getString(R.string.time_halfminute)).thenReturn("1/2 minute");
        when(ctx.getString(R.string.time_oneminute)).thenReturn("1 minute");
        when(ctx.getString(R.string.time_onehour)).thenReturn("1 hour");
        when(ctx.getString(R.string.time_quarterhour)).thenReturn("15 minutes");
        when(ctx.getString(R.string.time_halfhour)).thenReturn("½ hour");
        when(ctx.getString(R.string.time_oneandhalfhours)).thenReturn("1½ hours");
        when(ctx.getString(R.string.time_twoandhalfhours)).thenReturn("2½ hours");
        when(ctx.getString(R.string.time_hms_format)).thenReturn("%sh %sm %ss");

        return ctx;
    }

    @Test
    public void GetDescriptiveTimeString_ZeroSeconds_ReturnsEmptyString() {

        String actual = Strings.getDescriptiveDurationString(0, GetDescriptiveTimeString_Context());
        String expected = "";
        assertThat("0 seconds is empty string", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 1 seconds
     * Input : Strings.getDescriptiveDurationString(1,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "1 second"
     */
    @Test
    public void GetDescriptiveTimeString_OneSeconds_ReturnOneSecondString() {
        String actual = Strings.getDescriptiveDurationString(1, GetDescriptiveTimeString_Context());
        String expected = "1 second";
        assertThat("1 second returns one second", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 30 seconds
     * Input : Strings.getDescriptiveDurationString(30,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "1/2 minute"
     */
    @Test
    public void GetDescriptiveTimeString_ThirtySeconds_ReturnThirtySecondsString() {
        String actual = Strings.getDescriptiveDurationString(30, GetDescriptiveTimeString_Context());
        String expected = "1/2 minute";
        assertThat("1/2 minute returns thirty seconds", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 60 seconds
     * Input : Strings.getDescriptiveDurationString(60,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "1 minute"
     */
    @Test
    public void GetDescriptiveTimeString_SixtySeconds_ReturnSixtySecondsString() {
        String actual = Strings.getDescriptiveDurationString(60, GetDescriptiveTimeString_Context());
        String expected = "1 minute";
        assertThat("1 minute returns 60 seconds", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 15 minute
     * Input : Strings.getDescriptiveDurationString(900,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "15 minute"
     */
    @Test
    public void GetDescriptiveTimeString_QuarterHour_ReturnQuarterHourString() {
        String actual = Strings.getDescriptiveDurationString(900, GetDescriptiveTimeString_Context());
        String expected = "15 minutes";
        assertThat("15 minutes returns 900 seconds", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_1800Seconds_ReturnsHalfHourString() {
        String actual = Strings.getDescriptiveDurationString(1800, GetDescriptiveTimeString_Context());
        String expected = "½ hour";
        assertThat("1800 seconds returns half hour", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 3600 seconds
     * Input : Strings.getDescriptiveDurationString(3600,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "1 hour"
     */
    @Test
    public void GetDescriptiveTimeString_Hour_ReturnHourString() {
        String actual = Strings.getDescriptiveDurationString(3600, GetDescriptiveTimeString_Context());
        String expected = "1 hour";
        assertThat("1hour returns 3600 seconds", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 4800 seconds
     * Input : Strings.getDescriptiveDurationString(4800,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "1½ hours"
     */
    @Test
    public void GetDescriptiveTimeString_OneAndHalfHour_ReturnOneAndHalfHourString() {
        String actual = Strings.getDescriptiveDurationString(4800, GetDescriptiveTimeString_Context());
        String expected = "1½ hours";
        assertThat("1½ hours returns 4800 seconds", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get descriptive time string when 9000 seconds
     * Input : Strings.getDescriptiveDurationString(9000,GetDescriptiveTimeString_Context())
     * Context is already define upper this mention.
     * Expected : "2½ hours"
     */
    @Test
    public void GetDescriptiveTimeString_TwoAndHalfHour_ReturnTwoAndHalfHourString() {
        String actual = Strings.getDescriptiveDurationString(9000, GetDescriptiveTimeString_Context());
        String expected = "2½ hours";
        assertThat("2½ hours returns 9000 seconds", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_2700Seconds_Returns45minutesString() {
        String actual = Strings.getDescriptiveDurationString(2700, GetDescriptiveTimeString_Context());
        String expected = "0h 45m 0s";
        assertThat("2700 seconds returns 45 minutes", actual, is(expected));
    }

    @Test
    public void GetDescriptiveTimeString_9001Seconds_ReturnsCorrespondingHours() {
        String actual = Strings.getDescriptiveDurationString(9001, GetDescriptiveTimeString_Context());
        String expected = "2h 30m 1s";
        assertThat("9001 seconds returns correspnding time", actual, is(expected));
    }

    private Context GetBearingDescription_Context(float bearingDegrees) {
        Context context = mock(Context.class);
        String cardinal;
        if (bearingDegrees > 348.75f || bearingDegrees <= 11.25f) {
            cardinal = context.getString(R.string.direction_north);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly North");
        } else if (bearingDegrees > 11.25f && bearingDegrees <= 33.75f) {
            cardinal = context.getString(R.string.direction_northnortheast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly North By Northeast");
        } else if (bearingDegrees > 33.75f && bearingDegrees <= 56.25f) {
            cardinal = context.getString(R.string.direction_northeast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly Northeast");
        } else if (bearingDegrees > 56.25f && bearingDegrees <= 78.75f) {
            cardinal = context.getString(R.string.direction_eastnortheast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly East By Northeast");
        } else if (bearingDegrees > 78.75f && bearingDegrees <= 101.25f) {
            cardinal = context.getString(R.string.direction_east);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly East");
        } else if (bearingDegrees > 101.25f && bearingDegrees <= 123.75f) {
            cardinal = context.getString(R.string.direction_eastsoutheast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly East By Southeast");
        } else if (bearingDegrees > 123.75f && bearingDegrees <= 146.26f) {
            cardinal = context.getString(R.string.direction_southeast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly Southeast");
        } else if (bearingDegrees > 146.25f && bearingDegrees <= 168.75f) {
            cardinal = context.getString(R.string.direction_southsoutheast);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly South By Southeast");
        } else if (bearingDegrees > 168.75f && bearingDegrees <= 191.25f) {
            cardinal = context.getString(R.string.direction_south);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly South");
        } else if (bearingDegrees > 191.25f && bearingDegrees <= 213.75f) {
            cardinal = context.getString(R.string.direction_southsouthwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly South By Southwest");
        } else if (bearingDegrees > 213.75f && bearingDegrees <= 236.25f) {
            cardinal = context.getString(R.string.direction_southwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly Southwest");
        } else if (bearingDegrees > 236.25f && bearingDegrees <= 258.75f) {
            cardinal = context.getString(R.string.direction_westsouthwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly West By Southwest");
        } else if (bearingDegrees > 258.75f && bearingDegrees <= 281.25f) {
            cardinal = context.getString(R.string.direction_west);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly West");
        } else if (bearingDegrees > 281.25f && bearingDegrees <= 303.75f) {
            cardinal = context.getString(R.string.direction_westnorthwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly West By Northwest");
        } else if (bearingDegrees > 303.75f && bearingDegrees <= 326.25f) {
            cardinal = context.getString(R.string.direction_northwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly Northwest");
        } else if (bearingDegrees > 326.25f && bearingDegrees <= 348.75f) {
            cardinal = context.getString(R.string.direction_northnorthwest);
            when(context.getString(R.string.direction_roughly, cardinal)).thenReturn("Roughly North By Northwest");
        } else {
            cardinal = context.getString(R.string.unknown_direction);
            when(cardinal).thenReturn("Unknown Direction");
        }
        return context;
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in north direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(399f))
     * Context is already define upper this mention.
     * Expected : "Roughly North"
     */
    @Test
    public void getBearingDescription_ReturnsNorth() {
        Float bearingDegrees = 399f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly North";
        assertThat("North returns Roughly North", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in north by northeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(20f))
     * Context is already define upper this mention.
     * Expected : "Roughly North By Northeast"
     */
    @Test
    public void getBearingDescription_ReturnsNorthNortheast() {
        Float bearingDegrees = 20f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly North By Northeast";
        assertThat("North By Northeast returns Roughly North By Northeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in northeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(40f))
     * Context is already define upper this mention.
     * Expected : "Roughly Northeast"
     */
    @Test
    public void getBearingDescription_ReturnsNortheast() {
        Float bearingDegrees = 40f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly Northeast";
        assertThat("Northeast returns Roughly Northeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in east by northeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(60f))
     * Context is already define upper this mention.
     * Expected : "Roughly East By Northeast"
     */
    @Test
    public void getBearingDescription_ReturnsEastNortheast() {
        Float bearingDegrees = 60f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly East By Northeast";
        assertThat("East By Northeast returns Roughly East By Northeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in east direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(80f))
     * Context is already define upper this mention.
     * Expected : "Roughly East"
     */
    @Test
    public void getBearingDescription_ReturnsEast() {
        Float bearingDegrees = 80f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly East";
        assertThat("East returns Roughly East", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in east by southeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(102f))
     * Context is already define upper this mention.
     * Expected : "Roughly East By Southeast"
     */
    @Test
    public void getBearingDescription_ReturnsEastSouthEast() {
        Float bearingDegrees = 102f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly East By Southeast";
        assertThat("East By Southeast returns Roughly East By Southeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in southeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(124f))
     * Context is already define upper this mention.
     * Expected : "Roughly Southeast"
     */
    @Test
    public void getBearingDescription_ReturnsSoutheast() {
        Float bearingDegrees = 124f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly Southeast";
        assertThat("Southeast returns Roughly Southeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in south by southeast direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(147f))
     * Context is already define upper this mention.
     * Expected : "Roughly South By Southeast"
     */
    @Test
    public void getBearingDescription_ReturnsSouthSoutheast() {
        Float bearingDegrees = 147f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly South By Southeast";
        assertThat("South By Southeast returns Roughly South By Southeast", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in south direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(169f))
     * Context is already define upper this mention.
     * Expected : "Roughly South"
     */
    @Test
    public void getBearingDescription_ReturnsSouth() {
        Float bearingDegrees = 169f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly South";
        assertThat("South returns Roughly South", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in south by southwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(192f))
     * Context is already define upper this mention.
     * Expected : "Roughly South by Southwest"
     */
    @Test
    public void getBearingDescription_ReturnsSouthSouthwest() {
        Float bearingDegrees = 192f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly South By Southwest";
        assertThat("South By Southwest returns Roughly South By Southwest", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in southwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(214f))
     * Context is already define upper this mention.
     * Expected : "Roughly Southwest"
     */
    @Test
    public void getBearingDescription_ReturnsSouthwest() {
        Float bearingDegrees = 214f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly Southwest";
        assertThat("Southwest returns Roughly Southwest", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in west by southwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(237f))
     * Context is already define upper this mention.
     * Expected : "Roughly West by Southwest"
     */
    @Test
    public void getBearingDescription_ReturnsWestSouthwest() {
        Float bearingDegrees = 237f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly West By Southwest";
        assertThat("West By Southwest returns Roughly West By Southwest", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in west direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(259f))
     * Context is already define upper this mention.
     * Expected : "Roughly west"
     */
    @Test
    public void getBearingDescription_ReturnsWest() {
        Float bearingDegrees = 259f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly West";
        assertThat("West returns Roughly West", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in west by northwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(282f))
     * Context is already define upper this mention.
     * Expected : "Roughly West By Northwest"
     */
    @Test
    public void getBearingDescription_ReturnsWestNorthWest() {
        Float bearingDegrees = 282f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly West By Northwest";
        assertThat("West By Northwest returns Roughly West By Northwest", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in northwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(304f))
     * Context is already define upper this mention.
     * Expected : "Roughly Northwest"
     */
    @Test
    public void getBearingDescription_ReturnsNorthWest() {
        Float bearingDegrees = 304f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly Northwest";
        assertThat("Northwest returns Roughly Northwest", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get bearing description string when bearing degrees in North by northwest direction
     * Input : getBearingDescription(bearingDegrees,GetBearingDescription_Context(327f))
     * Context is already define upper this mention.
     * Expected : "Roughly North By Northwest"
     */
    @Test
    public void getBearingDescription_ReturnsNorthNorthWest() {
        Float bearingDegrees = 327f;
        String actual = getBearingDescription(bearingDegrees, GetBearingDescription_Context(bearingDegrees));
        String expected = "Roughly North By Northwest";
        assertThat("North By Northwest returns Roughly North By Northwest", actual, is(expected));
    }


    @Test
    public void SanitizeMarkdownForHelpView_WhenLocalLinkPresent_ReturnsCodeWebsite() {
        String md = "### [Open source libraries used](opensourcelibraries.html)\n" +
                "\n" +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n" +
                "This is [my screenshot](ss01.png)";
        String expected = "### [Open source libraries used](http://code.mendhak.com/gpslogger/opensourcelibraries.html)\n" +
                "\n" +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n" +
                "This is [my screenshot](http://code.mendhak.com/gpslogger/ss01.png)";
        assertThat("html link replaced with full URL", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenAbsoluteUrlLinkPresent_DoesNotReplace() {
        String md = "First line \r\n " +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        String expected = "First line \r\n " +
                "### [Donate Paypal](https://paypal.me/mendhak/)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";
        assertThat("Full URL not replaced", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenNull_ReturnsEmpty() {
        String md = null;

        String expected = "";

        assertThat("markdown returned is empty", Strings.getSanitizedMarkdownForFaqView(md), is(expected));
    }

    @Test
    public void SanitizeMarkdownForHelpView_WhenImagePresent_ImageRemoved() {
        String md = "First line \r\n " +
                "![badge](This is my badge)\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        String expected = "First line \r\n " +
                "\r\n " +
                "[Donate coffee grounds](https://example.com/) ";

        assertThat("Image in MD is removed", Strings.getSanitizedMarkdownForFaqView(md), is(expected));

    }


    @Test
    public void getDegreesMinutesSeconds_BasicConversion() {
        double lat = 11.812244d;
        String expected = "11° 48' 44.0784\" N";
        String actual = Strings.getDegreesMinutesSeconds(lat, true);

        assertThat("Degree Decimals converted to Degree Minute Seconds", actual, is(expected));
    }

    @Test
    public void getDegreesMinutesSeconds_NegativeIsSouth() {
        double lat = -16.44299d;
        String expected = "16° 26' 34.764\" S";
        String actual = Strings.getDegreesMinutesSeconds(lat, true);

        assertThat("Negative degree decimals converted to southerly degree minute second", actual, is(expected));
    }

    @Test
    public void getDegreesMinutesSeconds_Longitude_ReturnsEastWest() {
        double lon = 17.072754d;
        String expected = "17° 4' 21.9144\" E";
        String actual = Strings.getDegreesMinutesSeconds(lon, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));

        lon = -137.072754;
        expected = "137° 4' 21.9144\" W";
        actual = Strings.getDegreesMinutesSeconds(lon, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));
    }


    @Test
    public void getDegreesDecimalMinutes_BasicConversion() {
        double lat = 14.24231d;
        String expected = "14° 14.5386' N";
        String actual = Strings.getDegreesDecimalMinutes(lat, true);

        assertThat("Degree Decimals converted to Degree Decimal Minutes", actual, is(expected));
    }

    @Test
    public void getDegreesDecimalMinutes_NegativeIsSouth() {
        double lat = -54.81774d;
        String expected = "54° 49.0644' S";
        String actual = Strings.getDegreesDecimalMinutes(lat, true);

        assertThat("Negative Degree Decimals converted to southerly Degree Decimal Minutes", actual, is(expected));
    }

    @Test
    public void getDegreesDecimalMinutes_Longitude_ReturnsEastWest() {
        double lat = 101.898d;
        String expected = "101° 53.88' E";
        String actual = Strings.getDegreesDecimalMinutes(lat, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));

        lat = -111.111d;
        expected = "111° 6.66' W";
        actual = Strings.getDegreesDecimalMinutes(lat, false);

        assertThat("Longitude values have east west cardinality", actual, is(expected));
    }

    @Test
    public void getDecimalDegrees_BasicLatitude() {
        double lat = 59.28392417439d;
        String expected = "59" + getDecimalSeparator() + "283924";
        String actual = Strings.getDecimalDegrees(lat);

        assertThat("Decimal degrees formatted to 6 places", actual, is(expected));
    }


    @Test
    public void getDecimalDegrees_ShortLocation_NoPaddedZeros() {
        double lat = 59.28d;
        String expected = "59" + getDecimalSeparator() + "28";
        String actual = Strings.getDecimalDegrees(lat);

        assertThat("Decimal degrees no padded zero", actual, is(expected));
    }


    @Test
    public void getFormattedDegrees_DecimalDegrees_ReturnsDD() {
        double lat = 51.2828223838d;
        String expected = "51" + getDecimalSeparator() + "282822";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DECIMAL_DEGREES);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DD returns DD", actual, is(expected));
    }

    private char getDecimalSeparator() {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return symbols.getDecimalSeparator();
    }

    @Test
    public void getFormattedDegrees_DegreeMinuteSeconds_ReturnsDMS() {
        double lat = 51.2828223838d;
        String expected = "51° 16' 58.1606\" N";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DEGREES_MINUTES_SECONDS);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DMS returns DMS", actual, is(expected));

        double lon = -151.2828223838d;
        expected = "151° 16' 58.1606\" W";

        actual = Strings.getFormattedDegrees(lon, false, ph);
        assertThat("Preference DMS returns DMS", actual, is(expected));
    }

    @Test
    public void getFormattedDegrees_DegreesDecimalMinutes_ReturnsDDM() {
        double lat = 44.18923372d;
        String expected = "44° 11.354' N";

        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.getDisplayLatLongFormat()).thenReturn(PreferenceNames.DegreesDisplayFormat.DEGREES_DECIMAL_MINUTES);
        String actual = Strings.getFormattedDegrees(lat, true, ph);
        assertThat("Preference DDM returns DDM", actual, is(expected));

        double lon = -151.2828223838d;
        expected = "151° 16.9693' W";

        actual = Strings.getFormattedDegrees(lon, false, ph);
        assertThat("Preference DDM returns DDM", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get integer type number when input type is string
     * Input : toInt(1111,0)
     * Expected : "1111"
     */
    @Test
    public void toIntTest() {
        String str = "1111";
        int expected = 1111;
        int actual = toInt(str, 0);
        assertThat("Input 1111 to toInt returns 1111", actual, is(expected));
    }

    private Context getSpeedDisplay_Context() {
        Context ctx = mock(Context.class);
        when(ctx.getString(R.string.meters_per_second)).thenReturn("m/s");
        when(ctx.getString(R.string.miles_per_hour)).thenReturn("mi/h");
        when(ctx.getString(R.string.kilometers_per_hour)).thenReturn("km/h");
        return ctx;
    }

    /**
     * Added 18/03/29
     * Purpose : Get speed descriptive when input type is meter per second
     * Input :
     * case 1 : getSpeedDisplay(getSpeedDisplay_Context(),0.3,true);
     * case 2 : getSpeedDisplay(getSpeedDisplay_Context(),0.3,false);
     * case 3 : getSpeedDisplay(getSpeedDisplay_Context(),0.25,false);
     * Context is already define upper this mention.
     * Expected :
     * case 1 : 0.671mi/h
     * case 2 : 1.08km/h
     * case 3 : 0.25m/s
     */
    @Test
    public void getSpeedDisplayTest() {
        double metersPerSecondOverZeroPointTwentyEight = 0.3;
        double metersPerSecondBelowZeroPointTwentyEight = 0.25;
        boolean imperialTrue = true;
        boolean imperialFalse = false;
        /*
        case 1 Imperial is true
        may be return miles per hour
         */
        String actual1 = getSpeedDisplay(getSpeedDisplay_Context(), metersPerSecondOverZeroPointTwentyEight, imperialTrue);
        String expected1 = "0.671mi/h";
        assertThat("Miles per hour returns miles per hour", actual1, is(expected1));
        /*
        case 2 Imperial is false and Miles per second over 0.28
        may be returns Kilometers per hour
         */
        String actual2 = getSpeedDisplay(getSpeedDisplay_Context(), metersPerSecondOverZeroPointTwentyEight, imperialFalse);
        String expected2 = "1.08km/h";
        assertThat("Kilometers per hour returns Kilometers per hour", actual2, is(expected2));
        /*
        case 3 Imperial is false and Miles per second below 0.28
        may be returns Meters per second
         */
        String actual3 = getSpeedDisplay(getSpeedDisplay_Context(), metersPerSecondBelowZeroPointTwentyEight, imperialFalse);
        String expected3 = "0.25m/s";
        assertThat("Meters per second returns Meters per second", actual3, is(expected3));
    }

    private Context getDistanceDisplay_Context() {
        Context ctx = mock(Context.class);
        when(ctx.getString(R.string.meters)).thenReturn("meters");
        when(ctx.getString(R.string.feet)).thenReturn("feet");
        when(ctx.getString(R.string.miles)).thenReturn("mi");
        when(ctx.getString(R.string.kilometers)).thenReturn("km");
        return ctx;
    }

    /**
     * Added 18/03/29
     * Purpose : Get distance descriptive when input type is meters
     * Input :
     * case 1 : getDistanceDisplay(getDistanceDisplay_Context(),803,true,false);
     * case 2 : getDistanceDisplay(getDistanceDisplay_Context(),1001,true,true);
     * case 3 : getDistanceDisplay(getDistanceDisplay_Context(),1001,false,true);
     * case 4 : getDistanceDisplay(getDistanceDisplay_Context(),1001,false,false);
     * Context is already define upper this mention.
     * Expected :
     * case 1 : 2634.514feet
     * case 2 : 0.622mi
     * case 3 : 1.001km
     * case 4 : 1001meters
     */
    @Test
    public void getDistanceDisplayTest() {
        double metersOver1000 = 1001;
        double metersBelow804 = 803;
        boolean imperialTrue = true;
        boolean imperialFalse = false;
        boolean autoScaleTrue = true;
        boolean autoScaleFalse = false;
        /*
        case 1 Imperial is true And Autoscale false And Meters below 804
        may be return feet
         */
        String actual1 = getDistanceDisplay(getDistanceDisplay_Context(), metersBelow804, imperialTrue, autoScaleFalse);
        String expected1 = "2634.514feet";
        assertThat("Feet returns feet", actual1, is(expected1));
        /*
        case 2 Imperial is true And Autoscale true And Meters over 1000
        may be return miles
         */
        String actual2 = getDistanceDisplay(getDistanceDisplay_Context(), metersOver1000, imperialTrue, autoScaleTrue);
        String expected2 = "0.622mi";
        assertThat("Miles returns miles", actual2, is(expected2));
        /*
        case 3 Imperial is false And Autoscale true And Meters over 1000
        may be return miles
         */
        String actual3 = getDistanceDisplay(getDistanceDisplay_Context(), metersOver1000, imperialFalse, autoScaleTrue);
        String expected3 = "1.001km";
        assertThat("Kilometer returns Kilometer", actual3, is(expected3));
        /*
        case 4 Imperial is false And Autoscale false And Meters over 1000
        may be return miles
         */
        String actual4 = getDistanceDisplay(getDistanceDisplay_Context(), metersOver1000, imperialFalse, autoScaleFalse);
        String expected4 = "1001meters";
        assertThat("Meter returns Meter", actual4, is(expected4));

    }

    private Context getTimeDisplay_Context() {
        Context ctx = mock(Context.class);
        when(ctx.getString(R.string.seconds)).thenReturn("s");
        when(ctx.getString(R.string.minutes)).thenReturn("min");
        when(ctx.getString(R.string.hours)).thenReturn("hrs");
        return ctx;
    }

    /**
     * Added 18/03/29
     * Purpose : Get time descriptive when input type is milliseconds
     * Input :
     * case 1 : getTimeDisplay(getTimeDisplay_Context(),3600001);
     * case 2 : getTimeDisplay(getTimeDisplay_Context(),60001);
     * case 3 : getTimeDisplay(getTimeDisplay_Context(),59999);
     * Context is already define upper this mention.
     * Expected :
     * case 1 : 1hrs
     * case 2 : 1min
     * case 3 : 60s
     */
    @Test
    public void getTimeDisplayTest() {
        long msOver3600000 = 3600001;
        long msOver60000 = 60001;
        long msBelow60000 = 59999;
        /*
        case 1 Milli Second Over 3600000
         */
        String actual1 = getTimeDisplay(getTimeDisplay_Context(), msOver3600000);
        String expected1 = "1hrs";
        assertThat("Over 3600000 Milliseconds returns hrs", actual1, is(expected1));
        /*
        case 2 Millisecond over 60000
         */
        String actual2 = getTimeDisplay(getTimeDisplay_Context(), msOver60000);
        String expected2 = "1min";
        assertThat("Over 60000 Milliseconds returns min", actual2, is(expected2));
        /*
        case 3 Millisecond below 60000
         */
        String actual3 = getTimeDisplay(getTimeDisplay_Context(), msBelow60000);
        String expected3 = "60s";
        assertThat("Below 60000 Milliseconds returns sec", actual3, is(expected3));


    }

    @Test
    public void getFormattedFileName_BasicFileName_ReturnsAsIs() {
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("hello");
        PreferenceHelper ph = mock(PreferenceHelper.class);

        String expected = "hello";
        String actual = Strings.getFormattedFileName(sess, ph);

        assertThat("Basic file name unchanged", actual, is(expected));

        when(sess.getCurrentFileName()).thenReturn("");
        expected = "";
        actual = Strings.getFormattedFileName(sess, ph);
        assertThat("Empty file name returns empty string", actual, is(expected));
    }

    @Test
    public void getFormattedFileName_CreateCustomFileName_ReturnsFormatted() {
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("basename_%ver");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldCreateCustomFile()).thenReturn(true);

        String expected = "basename_" + BuildConfig.VERSION_NAME;
        String actual = Strings.getFormattedFileName(sess, ph);

        assertThat("Custom file name should do substitutions", actual, is(expected));


    }

    @Test
    public void getFormattedFileName_shouldPrefixSerialToFileName() {
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn("somefile");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldPrefixSerialToFileName()).thenReturn(true);

        String expected = Build.SERIAL + "_somefile";
        String actual = Strings.getFormattedFileName(sess, ph);

        assertThat("Build serial is prefixed to file name", actual, is(expected));

    }


    @Test
    public void getFormattedFileName_shouldPrefixSerialToFileName_SerialOnlyIncludedOnce() {
        Session sess = mock(Session.class);
        when(sess.getCurrentFileName()).thenReturn(Build.SERIAL + "___somefile");
        PreferenceHelper ph = mock(PreferenceHelper.class);
        when(ph.shouldPrefixSerialToFileName()).thenReturn(true);

        String expected = Build.SERIAL + "___somefile";
        String actual = Strings.getFormattedFileName(sess, ph);
        assertThat("Build serial only included once", actual, is(expected));

    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 0
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 0
     * Expected : "IN_VEHICLE"
     */
    @Test
    public void getDetectedActivityNameTest_In_Vehicle() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(0);
        String expected = "IN_VEHICLE";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("In Vehicle return In Vehicle", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 1
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 1
     * Expected : "ON_BICYCLE"
     */
    @Test
    public void getDetectedActivityNameTest_On_Bycycle() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(1);
        String expected = "ON_BICYCLE";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("On Bicycle return On Bycicle", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 2
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 2
     * Expected : "ON_FOOT"
     */
    @Test
    public void getDetectedActivityNameTest_On_Foot() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(2);
        String expected = "ON_FOOT";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("On Foot return On foot", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 3
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 3
     * Expected : "STILL"
     */
    @Test
    public void getDetectedActivityNameTest_Still() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(3);
        String expected = "STILL";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Still return Still", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 4
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 4
     * Expected : "UNKNOWN"
     */
    @Test
    public void getDetectedActivityNameTest_Unknown() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(4);
        String expected = "UNKNOWN";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Unknown return Unknown", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 5
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 5
     * Expected : "TILTING"
     */
    @Test
    public void getDetectedActivityNameTest_Tilting() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(5);
        String expected = "TILTING";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Tilting return Tilting", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 6
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 6
     * Expected : "UNKNOWN"
     */
    @Test
    public void getDetectedActivityNameTest_Case6() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(6);
        String expected = "UNKNOWN";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Case 6 return Unknown", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 7
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 7
     * Expected : "WALKING"
     */
    @Test
    public void getDetectedActivityNameTest_Walking() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(7);
        String expected = "WALKING";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Walking return Walking", actual, is(expected));
    }

    /**
     * Added 18/03/29
     * Purpose : Get detected activity name string when detective activity type is 8
     * Input : Strings.getDetectedActivityName(detectedActivity)
     * Activity type is defined 8
     * Expected : "RUNNING"
     */
    @Test
    public void getDetectedActivityNameTest_Running() {
        DetectedActivity detectedActivity = mock(DetectedActivity.class);
        when(detectedActivity.getType()).thenReturn(8);
        String expected = "RUNNING";
        String actual = Strings.getDetectedActivityName(detectedActivity);
        assertThat("Running return Running", actual, is(expected));
    }
}