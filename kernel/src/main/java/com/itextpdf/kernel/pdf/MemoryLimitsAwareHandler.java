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
package com.itextpdf.kernel.pdf;

import com.itextpdf.kernel.exceptions.KernelExceptionMessageConstant;
import com.itextpdf.kernel.exceptions.MemoryLimitsAwareException;

import java.util.HashSet;

/**
 * A {@link MemoryLimitsAwareHandler} handles memory allocation and prevents decompressed
 * pdf streams from occupation of more space than allowed.
 *
 * <p>A configured MemoryLimitsAwareHandler can be set as a property of {@link ReaderProperties}
 * instance which is passed to {@link PdfReader}.
 *
 * @see ReaderProperties#setMemoryLimitsAwareHandler(MemoryLimitsAwareHandler)
 */
public class MemoryLimitsAwareHandler {


    private static final int SINGLE_SCALE_COEFFICIENT = 100;
    private static final int SUM_SCALE_COEFFICIENT = 500;

    private static final int MAX_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE = 50000000;
    private static final int MIN_LIMIT_FOR_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE = 500000;
    private static final int SINGLE_DECOMPRESSED_PDF_STREAM_MIN_SIZE = Integer.MAX_VALUE / 100;
    private static final long SUM_OF_DECOMPRESSED_PDF_STREAMS_MIN_SIZE = Integer.MAX_VALUE / 20;
    private static final long MAX_X_OBJECTS_SIZE_PER_PAGE = 1024L*1024L*1024L*3;

    private int maxSizeOfSingleDecompressedPdfStream;
    private long maxSizeOfDecompressedPdfStreamsSum;
    private int maxNumberOfElementsInXrefStructure;

    private long maxXObjectsSizePerPage;

    private long allMemoryUsedForDecompression = 0;
    private long memoryUsedForCurrentPdfStreamDecompression = 0;

    boolean considerCurrentPdfStream = false;

    /**
     * Creates a {@link MemoryLimitsAwareHandler} which will be used to handle decompression of pdf streams.
     * The max allowed memory limits will be generated by default.
     */
    public MemoryLimitsAwareHandler() {
        this(SINGLE_DECOMPRESSED_PDF_STREAM_MIN_SIZE, SUM_OF_DECOMPRESSED_PDF_STREAMS_MIN_SIZE,
                MAX_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE, MAX_X_OBJECTS_SIZE_PER_PAGE);
    }

    /**
     * Creates a {@link MemoryLimitsAwareHandler} which will be used to handle decompression of pdf streams.
     * The max allowed memory limits will be generated by default, based on the size of the document.
     *
     * @param documentSize the size of the document, which is going to be handled by iText.
     */
    public MemoryLimitsAwareHandler(long documentSize) {
        this((int) calculateDefaultParameter(documentSize, SINGLE_SCALE_COEFFICIENT,
                SINGLE_DECOMPRESSED_PDF_STREAM_MIN_SIZE), calculateDefaultParameter(documentSize, SUM_SCALE_COEFFICIENT,
                SUM_OF_DECOMPRESSED_PDF_STREAMS_MIN_SIZE), calculateMaxElementsInXref(documentSize), MAX_X_OBJECTS_SIZE_PER_PAGE);
    }

    private MemoryLimitsAwareHandler(int maxSizeOfSingleDecompressedPdfStream, long maxSizeOfDecompressedPdfStreamsSum,
            int maxNumberOfElementsInXrefStructure, long maxXObjectsSizePerPage) {
        this.maxSizeOfSingleDecompressedPdfStream = maxSizeOfSingleDecompressedPdfStream;
        this.maxSizeOfDecompressedPdfStreamsSum = maxSizeOfDecompressedPdfStreamsSum;
        this.maxNumberOfElementsInXrefStructure = maxNumberOfElementsInXrefStructure;
        this.maxXObjectsSizePerPage = maxXObjectsSizePerPage;
    }

    /**
     * Gets the maximum allowed size which can be occupied by a single decompressed pdf stream.
     *
     * @return the maximum allowed size which can be occupied by a single decompressed pdf stream.
     */
    public int getMaxSizeOfSingleDecompressedPdfStream() {
        return maxSizeOfSingleDecompressedPdfStream;
    }

    /**
     * Sets the maximum allowed size which can be occupied by a single decompressed pdf stream.
     * This value correlates with maximum heap size. This value should not exceed limit of the heap size.
     *
     * <p>iText will throw an exception if during decompression a pdf stream which was identified as
     * requiring memory limits awareness occupies more memory than allowed.
     *
     * @param maxSizeOfSingleDecompressedPdfStream the maximum allowed size which can be occupied by a single
     *                                             decompressed pdf stream.
     * @return this {@link MemoryLimitsAwareHandler} instance.
     * @see MemoryLimitsAwareHandler#isMemoryLimitsAwarenessRequiredOnDecompression(PdfArray)
     */
    public MemoryLimitsAwareHandler setMaxSizeOfSingleDecompressedPdfStream(int maxSizeOfSingleDecompressedPdfStream) {
        this.maxSizeOfSingleDecompressedPdfStream = maxSizeOfSingleDecompressedPdfStream;
        return this;
    }

    /**
     * Gets the maximum allowed size which can be occupied by all decompressed pdf streams.
     *
     * @return the maximum allowed size value which streams may occupy
     */
    public long getMaxSizeOfDecompressedPdfStreamsSum() {
        return maxSizeOfDecompressedPdfStreamsSum;
    }

    /**
     * Sets the maximum allowed size which can be occupied by all decompressed pdf streams.
     * This value can be limited by the maximum expected PDF file size when it's completely decompressed.
     * Setting this value correlates with the maximum processing time spent on document reading
     *
     * <p>iText will throw an exception if during decompression pdf streams which were identified as
     * requiring memory limits awareness occupy more memory than allowed.
     *
     * @param maxSizeOfDecompressedPdfStreamsSum he maximum allowed size which can be occupied by all decompressed pdf
     *                                           streams.
     * @return this {@link MemoryLimitsAwareHandler} instance.
     * @see MemoryLimitsAwareHandler#isMemoryLimitsAwarenessRequiredOnDecompression(PdfArray)
     */
    public MemoryLimitsAwareHandler setMaxSizeOfDecompressedPdfStreamsSum(long maxSizeOfDecompressedPdfStreamsSum) {
        this.maxSizeOfDecompressedPdfStreamsSum = maxSizeOfDecompressedPdfStreamsSum;
        return this;
    }

    /**
     * Performs a check if the {@link PdfStream} with provided setup of the filters requires
     * memory limits awareness during decompression.
     *
     * @param filters is an {@link PdfArray} of names of filters
     * @return true if PDF stream is suspicious and false otherwise
     */
    public boolean isMemoryLimitsAwarenessRequiredOnDecompression(PdfArray filters) {
        final HashSet<PdfName> filterSet = new HashSet<>();
        for (int index = 0; index < filters.size(); index++) {
            final PdfName filterName = filters.getAsName(index);
            if (!filterSet.add(filterName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets maximum number of elements in xref structure.
     *
     * @return maximum number of elements in xref structure.
     */
    public int getMaxNumberOfElementsInXrefStructure() {
        return maxNumberOfElementsInXrefStructure;
    }

    /**
     * Gets maximum page size.
     *
     * @return maximum page size.
     */
    public long getMaxXObjectsSizePerPage() {
        return maxXObjectsSizePerPage;
    }

    /**
     * Sets maximum page size.
     *
     * @param maxPageSize maximum page size.
     */
    public void setMaxXObjectsSizePerPage(long maxPageSize) {
        this.maxXObjectsSizePerPage = maxPageSize;
    }

    /**
     * Sets maximum number of elements in xref structure.
     *
     * @param maxNumberOfElementsInXrefStructure maximum number of elements in xref structure.
     */
    public void setMaxNumberOfElementsInXrefStructure(int maxNumberOfElementsInXrefStructure) {
        this.maxNumberOfElementsInXrefStructure = maxNumberOfElementsInXrefStructure;
    }

    /**
     * Performs a check of possible extension of xref structure.
     *
     * @param requestedCapacity capacity to which we need to expand xref array.
     */
    public void checkIfXrefStructureExceedsTheLimit(int requestedCapacity) {
        // Objects in xref structures are using 1-based indexes, so to store maxNumberOfElementsInXrefStructure
        // amount of elements we need maxNumberOfElementsInXrefStructure + 1 capacity.
        if (requestedCapacity - 1 > maxNumberOfElementsInXrefStructure) {
            throw new MemoryLimitsAwareException(KernelExceptionMessageConstant.XREF_STRUCTURE_SIZE_EXCEEDED_THE_LIMIT);
        }
    }

    public void checkIfPageSizeExceedsTheLimit(long totalXObjectsSize) {
        if (totalXObjectsSize > maxXObjectsSizePerPage) {
            throw new MemoryLimitsAwareException(KernelExceptionMessageConstant.TOTAL_XOBJECT_SIZE_ONE_PAGE_EXCEEDED_THE_LIMIT);
        }
    }

    /**
     * Calculate max number of elements allowed in xref table based on the size of the document, achieving max limit at 100MB.
     *
     * @param documentSizeInBytes document size in bytes.
     *
     * @return calculated limit.
     */
    protected static int calculateMaxElementsInXref(long documentSizeInBytes) {
        int maxDocSizeForMaxLimit = MAX_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE/MIN_LIMIT_FOR_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE;
        int documentSizeInMb = Math.max(1, Math.min((int) documentSizeInBytes / (1024 * 1024), maxDocSizeForMaxLimit));
        return documentSizeInMb * MIN_LIMIT_FOR_NUMBER_OF_ELEMENTS_IN_XREF_STRUCTURE;
    }

    /**
     * Considers the number of bytes which are occupied by the decompressed pdf stream.
     * If memory limits have not been faced, throws an exception.
     *
     * @param numOfOccupiedBytes the number of bytes which are occupied by the decompressed pdf stream.
     * @return this {@link MemoryLimitsAwareHandler} instance.
     * @see MemoryLimitsAwareException
     */
    MemoryLimitsAwareHandler considerBytesOccupiedByDecompressedPdfStream(long numOfOccupiedBytes) {
        if (considerCurrentPdfStream && memoryUsedForCurrentPdfStreamDecompression < numOfOccupiedBytes) {
            memoryUsedForCurrentPdfStreamDecompression = numOfOccupiedBytes;
            if (memoryUsedForCurrentPdfStreamDecompression > maxSizeOfSingleDecompressedPdfStream) {
                throw new MemoryLimitsAwareException(
                        KernelExceptionMessageConstant.DURING_DECOMPRESSION_SINGLE_STREAM_OCCUPIED_MORE_MEMORY_THAN_ALLOWED);
            }
        }
        return this;
    }

    /**
     * Begins handling of current pdf stream decompression.
     *
     * @return this {@link MemoryLimitsAwareHandler} instance.
     */
    MemoryLimitsAwareHandler beginDecompressedPdfStreamProcessing() {
        ensureCurrentStreamIsReset();
        considerCurrentPdfStream = true;
        return this;
    }

    /**
     * Ends handling of current pdf stream decompression.
     * If memory limits have not been faced, throws an exception.
     *
     * @return this {@link MemoryLimitsAwareHandler} instance.
     * @see MemoryLimitsAwareException
     */
    MemoryLimitsAwareHandler endDecompressedPdfStreamProcessing() {
        allMemoryUsedForDecompression += memoryUsedForCurrentPdfStreamDecompression;
        if (allMemoryUsedForDecompression > maxSizeOfDecompressedPdfStreamsSum) {
            throw new MemoryLimitsAwareException(
                    KernelExceptionMessageConstant.DURING_DECOMPRESSION_MULTIPLE_STREAMS_IN_SUM_OCCUPIED_MORE_MEMORY_THAN_ALLOWED);
        }
        ensureCurrentStreamIsReset();
        considerCurrentPdfStream = false;
        return this;
    }

    long getAllMemoryUsedForDecompression() {
        return allMemoryUsedForDecompression;
    }

    private static long calculateDefaultParameter(long documentSize, int scale, long min) {
        long result = documentSize * scale;
        if (result < min) {
            result = min;
        }
        if (result > min * scale) {
            result = min * scale;
        }
        return result;
    }

    private void ensureCurrentStreamIsReset() {
        memoryUsedForCurrentPdfStreamDecompression = 0;
    }
}
