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
package com.itextpdf.kernel.utils.annotationsflattening;

import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;

/**
 * Interface for annotation flatten workers.
 * This interface is then used in {@link com.itextpdf.kernel.utils.PdfAnnotationFlattener}
 * to flatten annotations.
 */
public interface IAnnotationFlattener {

    /**
     * Flatten annotation.
     *
     * @param annotation annotation to flatten
     * @param page       page to flatten annotation on
     *
     * @return true if annotation was flattened, false otherwise
     */
    boolean flatten(PdfAnnotation annotation, PdfPage page);
}

