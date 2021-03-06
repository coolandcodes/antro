/*
 * Antro Compiler
 * https://www.coolcode.io/antro
 * Copyright (c) 2014-2018 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

package com.codedev.antro.compiler.tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A class to read the contents of an InputStream or Reader.
 *
 * @author Orlando Hill
 * @modifiedby Ifeora Okechukwu
 */
public final class Lexemizer {

    /** The initial capacity of the buffer list. */
    private static final int FILLED_INIT = 128; // 128KB

    /** The size for each buffer array. */
    private static final int BLOCK_SIZE = 1024; // 1KB

    /**
     * Empty constructor.
     */
    private Lexemizer(){

    }

    /**
     * Reads the contents of the given stream from its current position as characters.
     *
     * Treats IOExceptions from the input stream as if the end of input has
     * been reached.
     *
     * @param input The stream to read from.
     *
     * @return The contents of the stream.
     */
    public static char[] asArray(final InputStream input)
    {
        return Lexemizer.asArray(new InputStreamReader(input));
    }

    /**
     * Reads the contents of the given stream from its current position as bytes.
     *
     * Treats IOExceptions from the input stream as if the end of input has
     * been reached.
     *
     * @param input The stream to read from.
     *
     * @return The contents of the stream.
     */
    public static byte[] asArray(final FileInputStream input){

        return Lexemizer.asBytesArray(new FileInputStreamReader(input));
    }

    /**
     *
     *
     *
     *
     * @param
     *
     * @return
     */
    public static byte[] asBytesArray(final InputStreamReader){

        byte[] result = new byte[BLOCK_SIZE];

        return result;
    }

    /**
     * Reads the contents of the given reader from its current position.
     *
     * Treats IOExceptions from the reader as if the end of input has been
     * reached.
     *
     * @param reader The reader to read from.
     *
     * @return The contents of the reader.
     */
    public static char[] asArray(final Reader reader)
    {
        final ArrayList<char[]> filled = new ArrayList<char[]>(FILLED_INIT);
        char[] current = new char[BLOCK_SIZE];
        int currentIndex = 0;
        int resultSize = 0;

        try
        {
            int in = reader.read();

            // While still able to read
            while (in != -1)
            {
                // If we have filled our current block
                if (currentIndex == BLOCK_SIZE)
                {
                    // Make a new block
                    filled.add(current);
                    current = new char[BLOCK_SIZE];
                    currentIndex = 0;
                }

                // Put our input into the current block
                current[currentIndex] = (char) in;
                currentIndex++;
                resultSize++;

                // Read again from the input
                in = reader.read();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        final char[] result = new char[resultSize];
        int resultIndex = 0;

        // Copy the filled blocks
        for (char[] block : filled)
        {
            System.arraycopy(block, 0, result, resultIndex, BLOCK_SIZE);
            resultIndex += BLOCK_SIZE;
        }

        // Copy the left over
        System.arraycopy(current, 0, result, resultIndex, currentIndex);

        return result;
    }
}