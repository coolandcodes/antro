package com.codedev.antro.compiler.frontend;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import com.codedev.antro.compiler.frontend.Token;

public enum TokenType {

    // Special
    EOF,

    // Identifiers & literals
    IDENTIFIER,
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING,
    FORMATTED_STRING,
    BOOLEAN,
    NULL,

    // Keywords
    IF, ELSE, ELIF, FOR, WHILE, DO, BEGIN, END,
    DEF, VAR, RETURN, MAIN, VOID,
    SWITCH, CASE, DEFAULT,
    BREAK, CONTINUE,
    EXPORT, REQUIRE, DEFER,
    THROW, PANIC_ON, EJECT_ON,
    USE, CALL,
    NEW, INVARIANTS,

    // Types
    TYPE_INT, TYPE_FLT, TYPE_STR, TYPE_ARR, TYPE_BOOL, TYPE_NIL,

    // Operators
    PLUS, MINUS, STAR, SLASH, MODULO,
    INCREMENT, DECREMENT,
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, STAR_ASSIGN, SLASH_ASSIGN, MOD_ASSIGN,
    EQUAL, NOT_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT,
    BIT_AND, BIT_OR, SHIFT_LEFT, SHIFT_RIGHT,
    ARROW,

    // Delimiters
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    COMMA, DOT, COLON, SEMICOLON,
    AT
}

public class Tokennizer {

    /* ============================
       Input handling
       ============================ */

    private final BufferedReader reader;
    private final boolean fromString;

    private String buffer = "";
    private int bufferPos = 0;

    private int line = 1;
    private int column = 0;

    /* ============================
       Output
       ============================ */

    private final List<Token> tokens = new ArrayList<>();
    private final LinkedBlockingQueue<Token> tokenQueue = new LinkedBlockingQueue<>();

    /* ============================
       Lookahead support
       ============================ */

    private int tokenCursor = 0;

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

    public Tokenizer(String source) {
        this.reader = new BufferedReader(new java.io.StringReader(source));
        this.fromString = true;
    }

    public Tokenizer(BufferedReader reader) {
        this.reader = reader;
        this.fromString = false;
    }

    /* ============================
       Public API
       ============================ */

    public void tokenize() {
        try {
            while (true) {
                char c = advance();
                if (!isAtEnd(c)) {
                  if (reader.) {

                  }
                } else {
                  break;
                }
                scanToken(c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        emit(new Token(TokenType.EOF, "\0", line, column));
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public LinkedBlockingQueue<Token> getTokenQueue() {
        return tokenQueue;
    }

    public Token peek() {
        return tokenCursor < tokens.size() ? tokens.get(tokenCursor) : null;
    }

    public Token peekNext() {
        return (tokenCursor + 1) < tokens.size() ? tokens.get(tokenCursor + 1) : null;
    }

    public Token consume() {
        return tokens.get(tokenCursor++);
    }

    /* ============================
       Core scanning
       ============================ */

    private void scanToken(char c) throws IOException {

        int startColumn = column;

        // Whitespace
        if (isWhitespace(c)) return;

        // Comment
        if (c == '#') {
            while (peek() != '\n' && peek() != '\0') advance();
            return;
        }

        // Strings
        if (c == '"' || c == '\'') {
            readString(c, false, startColumn);
            return;
        }

        // Formatted string
        if (c == 'f' && (peek() == '"' || peek() == '\'')) {
            char quote = advance();
            readString(quote, true, startColumn);
            return;
        }

        // Numbers
        if (isDigit(c) || (c == '-' && isDigit(peek()))) {
            readNumber(c, startColumn);
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

            case '(' -> emit(simple(c, TokenType.LPAREN));
            case ')' -> emit(simple(c, TokenType.RPAREN));
            case '{' -> emit(simple(c, TokenType.LBRACE));
            case '}' -> emit(simple(c, TokenType.RBRACE));
            case '[' -> emit(simple(c, TokenType.LBRACKET));
            case ']' -> emit(simple(c, TokenType.RBRACKET));
            case ',' -> emit(simple(c, TokenType.COMMA));
            case '.' -> emit(simple(c, TokenType.DOT));
            case ':' -> emit(simple(c, TokenType.COLON));
            case ';' -> emit(simple(c, TokenType.SEMICOLON));
            case '@' -> emit(simple(c, TokenType.AT));

            default -> error("Unexpected character: " + c);
        }
    }

    /* ============================
       Readers
       ============================ */

    private void readIdentifier(char first, int col) throws IOException {
        StringBuilder sb = new StringBuilder().append(first);
        while (isIdentifierPart(peek())) sb.append(advance());

        String text = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        emit(new Token(type, text, line, col));
    }

    private void readNumber(char first, int col) throws IOException {
        StringBuilder sb = new StringBuilder().append(first);

        boolean isHex = false;
        boolean isFloat = false;

        if (first == '0' && peek() == 'x') {
            sb.append(advance());
            isHex = true;
            while (isHexDigit(peek())) sb.append(advance());
            emit(new Token(TokenType.INT_LITERAL, sb.toString(), line, col));
            return;
        }

        while (isDigit(peek())) sb.append(advance());

        if (peek() == '.') {
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

    private void readString(char quote, boolean formatted, int col) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(formatted ? "f" + quote : quote);

        while (peek() != quote) {
            if (peek() == '\0') error("Unterminated string");
            sb.append(advance());
        }

        sb.append(advance()); // closing quote
        emit(new Token(formatted ? TokenType.FORMATTED_STRING : TokenType.STRING, sb.toString(), line, col));
    }

    /* ============================
       Helpers
       ============================ */

    private char advance() throws IOException {
        if (bufferPos >= buffer.length()) {
            buffer = reader.readLine();
            if (buffer == null) {
              bufferPos = 0;
              return '\0';
            }
            buffer += "\n";
            bufferPos = 0;
        }
        char c = buffer.charAt(bufferPos++);
        column++;
        if (c == '\n') {
            line++;
            column = 0;
        }
        return c;
    }

    private char peek() throws IOException {
        char c = advance();
        bufferPos--;
        column--;
        return c;
    }

    private boolean match(char expected) throws IOException {
        if (peek() != expected) return false;
        advance();
        return true;
    }

    private boolean isAtEnd(char nextOnAdvance) {
        return nextOnAdvance == '\0';
    }

    private void emit(Token token) {
        tokens.add(token);
        tokenQueue.offer(token);
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

    private boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c) || c == '_';
    }

    private void error(String msg) {
        throw new RuntimeException("[Line " + line + ", Col " + column + "] " + msg);
    }
}
