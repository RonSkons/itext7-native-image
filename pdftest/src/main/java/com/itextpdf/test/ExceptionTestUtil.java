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
package com.itextpdf.test;

/**
 * Class containing the exception messages.
 */
public final class ExceptionTestUtil {

    private static final String DOCTYPE_IS_DISALLOWED =
            "DOCTYPE is disallowed when the feature "
                    + "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true.";

    private static final String TEST_MESSAGE = "Test message";

    /**
     * Returns message about disallowed DOCTYPE.
     *
     * @return message for case when DOCTYPE is disallowed in XML
     */
    public static String getDoctypeIsDisallowedExceptionMessage() {
        return DOCTYPE_IS_DISALLOWED;
    }

    /**
     * Returns test message for case with XXE.
     *
     * @return message with text for testing
     */
    public static String getXxeTestMessage() {
        return TEST_MESSAGE;
    }
}
