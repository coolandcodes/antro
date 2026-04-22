package com.codedev.antro.compiler.frontend.helpers;

import java.io.Reader;
import java.io.BufferedReader;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A utility class that wraps around a buffered reader to ...
 */
public class NamedBufferedReader extends BufferedReader {
    private final String fileName;

    public NamedBufferedReader(Reader in, String fileName) {
        super(in);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}