package com.codedev.antro.compiler.frontend;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.Map;

import com.codedev.antro.compiler.frontend.lexer.Token;
import com.codedev.antro.compiler.frontend.lexer.TokenType;
import com.codedev.antro.compiler.frontend.lexer.LexemeQueue;

import com.codedev.antro.compiler.frontend.helpers.NoticeConsoleLogger;
import com.codedev.antro.compiler.frontend.contracts.concerns.LexisException;

/*
 * Antro Compiler Project
 * https://www.coolcodes.io/antro
 * Copyright (c) 2014-2026 Ifeora Okechukwu
 * Licensed under the MIT license. See 'LICENSE' for details.
 */

/**
 * The core logic for turning file/string bytes or contents of buffered reader
 * into a series of tokens.
 */
public class Tokenizer {

    /* ============================
       Input handling
       ============================ */

    private final BufferedReader reader; /* @TODO: Modify this to use `NameBufferedReader` instead in the future */
    private final boolean multiCharScanActive;

    private String buffer = "";
    private int bufferPos = 0;

    private int line = 1;
    private int column = 0;

    /* ============================
       Output
       ============================ */

    private final LexemeQueue tokenQueue;


    /* ============================
       Keywords
       ============================ */

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
        Map.entry("if", TokenType.IF),
        Map.entry("else", TokenType.ELSE),
        Map.entry("elif", TokenType.ELIF),
        Map.entry("for", TokenType.FOR),
        Map.entry("while", TokenType.WHILE),
        Map.entry("do", TokenType.DO),
        Map.entry("begin", TokenType.BEGIN),
        Map.entry("end", TokenType.END),
        Map.entry("def", TokenType.DEF),
        Map.entry("var", TokenType.VAR),
        Map.entry("retn", TokenType.RETURN),
        Map.entry("continue", TokenType.CONTINUE),
        Map.entry("break", TokenType.BREAK),
        Map.entry("switch", TokenType.SWITCH),
        Map.entry("case", TokenType.CASE),
        Map.entry("default", TokenType.DEFAULT),
        Map.entry("main", TokenType.MAIN),
        Map.entry("void", TokenType.VOID),
        Map.entry("true", TokenType.BOOLEAN),
        Map.entry("false", TokenType.BOOLEAN),
        Map.entry("null", TokenType.NULL),
        Map.entry("throw", TokenType.THROW),
        Map.entry("panic_on", TokenType.PANIC_ON),
        Map.entry("eject_on", TokenType.EJECT_ON),
        Map.entry("use", TokenType.USE),
        Map.entry("call", TokenType.CALL),
        Map.entry("new", TokenType.NEW),
        Map.entry("export", TokenType.EXPORT),
        Map.entry("require", TokenType.REQUIRE),
        Map.entry("defer", TokenType.DEFER),
        Map.entry("invariants", TokenType.INVARIANTS)
    );


    /* ============================
       Constructors
       ============================ */

    public Tokenizer(String source, LexemeQueue tokenQueue) {
        this.reader = new BufferedReader(new StringReader(source), 2000); /* @TODO: Modify this to use `NameBufferedReader` instead in the future */
        this.tokenQueue = tokenQueue;
        this.multiCharScanActive = false;
    }

    public Tokenizer(BufferedReader reader, LexemeQueue tokenQueue) {
        this.reader = reader; /* @TODO: Modify this to use `NameBufferedReader` instead in the future */
        this.tokenQueue = tokenQueue;
        this.multiCharScanActive = false;
    }

    /* ============================
       Public API
       ============================ */

    public final void tokenize() throws LexisException {
        try {
            while (true) {
                char c = advance();
                if (isAtEnd(c)) {
                  break;
                }
                scanToken(c);
            }
        } catch (Exception e) {
            LexisException lexisEx = new LexisException("lexical scan of source failed", e);
            throw lexisEx;
        }

        emit(new Token(TokenType.EOF, "\0", line, column));
    }

    private char readUnicodeEscape() throws IOException {
        int value = 0;
    
        for (int charCount = 0; charCount < 4; charCount++) {
            char c = advance();
            if (!isHexDigit(c)) {
                error("Invalid Unicode escape sequence");
            }
            value = (value << 4) + Character.digit(c, 16);
        }
    
        return (char) value;
    }


    /* ============================
       Core scanning
       ============================ */

    private void scanToken(char c) throws Exception {

        int startColumn = column;

        // Whitespace
        if (isWhitespace(c)) {
            if (multiCharScanActive) {
                multiCharScanActive = false;
            }
            return;
        }

        // Comment
        if (isCommentStart(c)) {
            multiCharScanActive = true;
            while (!isAtEnd(peek())) {
                if (isCommentEnd(c, peek())) {
                    break;
                }
                advance();
            }

            if (c == '/' && isAtEnd(peek())) {
                NoticeConsoleLogger.logMessage(
                    "TOKENIZER",
                    "unterminated comment found at the end of source on line: " + line
                );
            }
            multiCharScanActive = false;
            return;
        }

        // Strings
        if (c == '"' || c == '\'') {
            multiCharScanActive = true;
            readString(c, false, startColumn);
            multiCharScanActive = false;
            return;
        }

        // Formatted string
        if (c == 'f' && (peek() == '"' || peek() == '\'')) {
            char quote = advance();
            multiCharScanActive = true;
            readString(quote, true, startColumn);
            multiCharScanActive = false;
            return;
        }

        // Numbers
        if (isDigit(c) || (c == '-' && isDigit(peek()))) {
            multiCharScanActive = true;
            readNumber(c, startColumn);
            multiCharScanActive = false;
            return;
        }

        // Identifiers / keywords
        if (isIdentifierStart(c)) {
            readIdentifier(c, startColumn);
            return;
        }

        // Operators & punctuation
        switch (c) {
            case '+': emit(simple(c, match('+') ? TokenType.INCREMENT :
                                    match('=') ? TokenType.PLUS_ASSIGN : TokenType.PLUS)); break;
            case '-': emit(simple(c, match('>') ? TokenType.ARROW :
                                    match('-') ? TokenType.DECREMENT :
                                    match('=') ? TokenType.MINUS_ASSIGN : TokenType.MINUS)); break;
            case '*': emit(simple(c, match('=') ? TokenType.STAR_ASSIGN : TokenType.STAR)); break;
            case '/': emit(simple(c, match('=') ? TokenType.SLASH_ASSIGN : TokenType.SLASH)); break;
            case '%': emit(simple(c, match('=') ? TokenType.MOD_ASSIGN : TokenType.MODULO)); break;
            case '&': emit(simple(c, match('&') ? TokenType.LOGICAL_AND : TokenType.BIT_AND)); break;
            case '|': emit(simple(c, match('|') ? TokenType.LOGICAL_OR : TokenType.BIT_OR)); break;
            case '<': emit(simple(c, match('<') ? TokenType.SHIFT_LEFT :
                                    match('=') ? TokenType.LESS_EQUAL : TokenType.LESS)); break;
            case '>': emit(simple(c, match('>') ? TokenType.SHIFT_RIGHT :
                                    match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER)); break;
            case '!': emit(simple(c, match('=') ? TokenType.NOT_EQUAL : TokenType.LOGICAL_NOT)); break;
            case '=': emit(simple(c, TokenType.ASSIGN)); break;

            case '(': emit(simple(c, TokenType.LPAREN)); break;
            case ')': emit(simple(c, TokenType.RPAREN)); break;
            case '{': emit(simple(c, TokenType.LBRACE)); break;
            case '}': emit(simple(c, TokenType.RBRACE)); break;
            case '[': emit(simple(c, TokenType.LBRACKET)); break;
            case ']': emit(simple(c, TokenType.RBRACKET)); break;
            case ',': emit(simple(c, TokenType.COMMA)); break;
            case '.': emit(simple(c, TokenType.DOT)); break;
            case ':': emit(simple(c, TokenType.COLON)); break;
            case ';': emit(simple(c, TokenType.SEMICOLON)); break;
            case '@': emit(simple(c, TokenType.AT)); break;

            default: error("Unexpected character found: '" + c + "'"); break;
        }
    }

    /* ============================
       Readers
       ============================ */

    private void readIdentifier(char first, int col) throws Exception {
        multiCharScanActive = true;
        StringBuilder sb = new StringBuilder();

        if (first !== null) {
            sb.append(first);
        } else {
            NullPointerException npEx = new NullPointerException(
                "reading first character (as null) of <identifier> on line: " + line
            );
            error("invalid <identifier> found", npEx);
        }

        while (isIdentifierPart(peek())) sb.append(advance());

        String text = sb.toString();

        char currCharacter = peek();
        if (isWhitespace(currCharacter) || isCommentStart(currCharacter)) {
            multiCharScanActive = false;
        } else {
            NoticeConsoleLogger.logMessage(
                "TOKENIZER",
                "extra character: '"+currCharacter+"' found close to identifier=['"+text+"']"
            );
        }

        
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        emit(new Token(type, text, line, col));
    }

    private void readNumber(char first, int col) throws IOException {
        multiCharScanActive = true;
        StringBuilder sb = new StringBuilder().append(first);

        boolean isHex = false;
        boolean isFloat = false;

        if (first == '0' && peek() == 'x') {
            sb.append(advance());
            isHex = true;
            while (isHexDigit(peek())) sb.append(advance());
            
            char currCharacter = peek();
            if (isWhitespace(currCharacter) || isCommentStart(currCharacter)) {
                multiCharScanActive = false;
            }

            emit(new Token(TokenType.INT_LITERAL, sb.toString(), line, col));
            return;
        }

        while (isDigit(peek())) sb.append(advance());

        char currCharacter = peek();
        if (isWhitespace(currCharacter) || isCommentStart(currCharacter)) {
            multiCharScanActive = false;
        }

        if (currCharacter == '.') {
            isFloat = true;
            sb.append(advance());
            while (isDigit(peek())) sb.append(advance());

            if (peek() == 'e' || peek() == 'E') {
                sb.append(advance());
                if (peek() == '+' || peek() == '-') sb.append(advance());
                while (isDigit(peek())) sb.append(advance());
            }
        }

        emit(new Token(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL, sb.toString(), line, col));
    }


    private void readString(char quote, boolean formatted, int col) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(formatted ? "f" + quote : quote);
    
        while (peek() != quote) {
            char c = advance();
    
            if (isAtEnd(c)) {
              error("Unterminated string literal found");
            }
    
            if (c == '\\') {
                char esc = advance();
                switch (esc) {
    
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> sb.append('\r');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case '\\' -> sb.append('\\');
                    case '\'' -> sb.append('\'');
                    case '"'  -> sb.append('"');
    
                    case 'u' -> {
                        sb.append(readUnicodeEscape());
                    }
    
                    default -> error("Invalid escape sequence: \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
    
        sb.append(advance()); // closing quote
        emit(new Token(
            formatted ? TokenType.FORMATTED_STRING : TokenType.STRING,
            sb.toString(),
            line,
            col
        ));
    }

    /* ============================
       Helpers
       ============================ */

    /**
     * Consume the next character
     */
    private char advance() throws Exception {
        boolean READER_EOF = false;
        char c = " ";
        
        try {
            if (bufferPos >= buffer.length()) {
                buffer = reader.readLine();
                
                if (buffer == null) {
                    READER_EOF = true;
                } else {
                    buffer += "\n";
                }
                bufferPos = 0;
            }

            if (READER_EOF) {
                c = '\0';
            } else {
                c = buffer.charAt(bufferPos++);
                column++;
            }
        } catch (IOException ex) {
            Exception ex = new Exception(
                "could not advance to next character in stream"
            );
            ex.initCause(ex);
            throw ex;
        }
        
        if (isAtNewLine(c)) {
            line++;
            column = 0;
        }
        
        return c;
    }

    private char peek() throws Exception {
        char c = advance();
        bufferPos--;
        column--;
        return c;
    }

    private char peek(boolean setBufferPos_IncrByOne) throws Exception {
        if (!setBufferPos_IncrByOne) {
            return peek();
        }

        char c = advance();
        c = advance();
        bufferPos-=2;
        column-=2;
        return c;
    }

    private boolean match(char expected) throws Exception {
        if (peek() != expected) return false;
        advance();
        return true;
    }

    private boolean isAtEnd(char nextOnAdvance) {
        return nextOnAdvance == '\0';
    }

    private boolean isAtNewLine(char nextOnAdvance) {
        return nextOnAdvance == '\n';
    }

    private void emit(Token token) {
        try {
            tokenQueue.pushNextToken(token);
        } catch (InterruptedException ex) {
            NoticeConsoleLogger.logMessage(
                "TOKENIZER",
                "thread running interrupted with message: " + ex.getMessage()
            );
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    private Token simple(char c, TokenType type) {
        return new Token(type, String.valueOf(c), line, column);
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f' || c == '\b';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private boolean isCommentStart(char c) throws Exception {
        if (c == "#") {
            return true;
        }

        if (c == "/") {
            char characterAhead = peek(true);
            return characterAhead == "*";
        }

        return false;
    }

    private boolean isCommentEnd(char start, char curr) throws IOException {
        if (start == '#') {
            return isAtNewLine(curr);
        }
        
        if (start == "/") {
            char characterAhead = peek(true);
            return curr == "*" && characterAhead == "/";
        }

        NoticeConsoleLogger.logMessage(
            "TOKENIZER",
            "plausible unexpected character: '"+start+"' at the start of comment"
        );
        return false;
    }

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c) || c == '_';
    }

    private void error(String msg) throws Exception {
        throw new Exception("[Line " + line + ", Col " + column + "]; " + msg);
    }

    private void error(String msg, RuntimeException ex) throws Exception {
        throw new Exception("[Line " + line + ", Col " + column + "]; " + msg, ex);
    }
}
