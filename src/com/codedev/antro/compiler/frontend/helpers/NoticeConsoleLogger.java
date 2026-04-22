package com.codedev.antro.compiler.frontend.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * A utility class for printing timestamped notices to the console.
 */
public class NoticeConsoleLogger {

    // Pre-define the formatter for efficiency (ISO-style timestamp)
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Prints a formatted message to the standard output.
     * * @param message The text to display in the console.
     */
    public static boolean logMessage(String prefix, String message) {
        String timestamp = LocalDateTime.now().format(formatter);

        if (prefix == null) return false;
        if (message == null) return false;
        
        // Using System.out.println ensures the output is flushed 
        // and thread-safe for simple console logging.
        System.out.println("[" + timestamp + "]:[NOTICE]:["+ prefix.upper() +"] >> " + message);
        return true;
    }
}