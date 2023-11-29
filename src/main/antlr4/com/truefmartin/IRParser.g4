parser grammar IRParser;


options {
    tokenVocab = IRLexer;
}

// The start and end of .html file. Everything is in 'document'
document
    : html+ EOF
    ;

// Each line inside of the .html file is an 'html'
html
    : tagStart NEW_LINE+
    | noTagStart NEW_LINE+
    | NEW_LINE+
    ;

// A line that starts with either <tag> or <tag 
tagStart
    : TAG_START+? internalTag*?  (outOfTag | TAG_END)*?
    | internalTag+? TAG_START*? (outOfTag | TAG_END)*?
    ;

// A line that starts with <tag> (closed html tag), we ignore TAG_START,
// and begin to care with outOfTag 
//tag
//    : (outOfTag | TAG_END)*?
//    ;

// A line that does not start with <tag> or <tag
noTagStart
    : outOfTag (outOfTag | TAG_START | TAG_END)+
    | TAG_END+
    ;

// Any content not directly inside of of a tag. Can be: <tag> XXX <tag>, or: XXX <tag>
outOfTag
    : outOfTagClean // No punctuation
    | outOfTagDirty // Ignored
    | handleInteger
    ;

outOfTagDirty
    : TEXT_WITH_PUNCUATION
    ;

// Is own parser rule so that we have a single method that can print the text 
// no matter what it is, without having to perform logic on type
outOfTagClean
    : PLAIN_TEXT
    | EMAIL
    | URL
    ;

// Tag that has an attribute, e.g <tag XXX 
// only contentText is cared about
internalTag
    : TAG_START_OPEN contentText? TAG_START_CLOSE
    ;

// CONTENT_START would be all of: ' content="XXX ' in a content/alt attribute, until whitespace
// IN_TAG_URL has no whitespace, and should be treated as a whole. Later remove: content=" 
contentText
    : CONTENT_START (contentOptions| CONTENT_WS)+ CONTENT_CLOSE
    | IN_TAG_URL
    ;

// CONTENT_TEXT and CONTENT_EMAIL can be printed as is, integer needs removal of ','
contentOptions
    : CONTENT_TEXT
    | handleInteger
    | CONTENT_EMAIL
    ;

// Seperate rule so that only integers traversal methods need to modify text
handleInteger
    : INTEGER
    | CONTENT_INTEGER
    ;
