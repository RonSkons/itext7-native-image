/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
    Authors: Apryse Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.commons.utils;

import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class DateTimeUtilTest extends ExtendedITextTest {

    private static final double ZERO_DELTA = 1e-6;
    private static final double ONE_SECOND_DELTA = 1000.0;

    @Test
    public void getCurrentTest() {
        long nowEpoch = new Date().getTime();
        long nowDateTimeUtilEpoch = DateTimeUtil.getCurrentTimeDate().getTime();
        Assert.assertEquals(nowEpoch, nowDateTimeUtilEpoch, ONE_SECOND_DELTA);
    }

    @Test
    public void isInPastTest() {
        Date date = new Date(1);
        Assert.assertTrue(DateTimeUtil.isInPast(date));
    }

    @Test
    public void parseDateAndGetUtcMillisFromEpochTest() {
        Date date = DateTimeUtil.parseWithDefaultPattern("2020-05-05");
        Calendar parsedDate = DateTimeUtil.getCalendar(date);

        double millisFromEpochTo2020_05_05 = DateTimeUtil.getUtcMillisFromEpoch(parsedDate);

        long offset = DateTimeUtil.getCurrentTimeZoneOffset(date);

        Assert.assertEquals(1588636800000d - offset, millisFromEpochTo2020_05_05, ZERO_DELTA);
    }
    
    @Test
    public void addMillisToDateTest() {
        Date almostCurrentTime = new Date(new Date().getTime() - 2000);
        long twoSeconds = 2000;
        Assert.assertEquals(new Date().getTime(),
                DateTimeUtil.addMillisToDate(almostCurrentTime, twoSeconds).getTime(), ONE_SECOND_DELTA);
    }

    @Test
    public void compareUtcMillisFromEpochWithNullParamAndCurrentTimeTest() {
        double getUtcMillisFromEpochWithNullParam = DateTimeUtil.getUtcMillisFromEpoch(null);
        double millisFromEpochToCurrentTime = DateTimeUtil.getUtcMillisFromEpoch(DateTimeUtil.getCurrentTimeCalendar());

        Assert.assertEquals(millisFromEpochToCurrentTime, getUtcMillisFromEpochWithNullParam, ONE_SECOND_DELTA);
    }

    @Test
    public void parseDateAndGetRelativeTimeTest() {
        Date date = DateTimeUtil.parseWithDefaultPattern("2020-05-05");
        double relativeTime = DateTimeUtil.getRelativeTime(date);

        long offset = DateTimeUtil.getCurrentTimeZoneOffset(date);

        Assert.assertEquals(1588636800000d - offset, relativeTime, ZERO_DELTA);
    }

    @Test
    public void addYearPositiveValueTest () {
        GregorianCalendar originalDate = new GregorianCalendar(2000 + 1900, 1, 1);
        originalDate.getTime();

        Date newDate = DateTimeUtil.addYearsToDate(originalDate.getTime(), 5);

        Assert.assertEquals(2005, newDate.getYear());
    }

    @Test
    public void addYearNegativeValueTest () {
        GregorianCalendar originalDate = new GregorianCalendar(2000 + 1900, 1, 1);
        originalDate.getTime();

        Date newDate = DateTimeUtil.addYearsToDate(originalDate.getTime(), -3);

        Assert.assertEquals(1997, newDate.getYear());
    }
}
