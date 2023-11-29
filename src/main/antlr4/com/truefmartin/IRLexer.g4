lexer grammar IRLexer;


// Tag open and close
JS: '<script>' -> skip, mode(JavaScript);
JS_TYPE: '<script type="' [ A-Za-z0-9/\-+]+? '">' -> skip, mode(JavaScript);
TAG_START: '<'  [A-Za-z0-9]+? '>' ;
TAG_END: '</'  [A-Za-z0-9]+ '>' ;
TAG_END_EXLAM: '<' '!' [A-Za-z0-9]+ -> skip, mode(EXLAM) ;
//TAG_OPEN_EXPLAMATION: '<' '!' [[0-9-_'./\\~,`@#$%^&*():;{}A-Za-z"]+ '>' -> skip;

// The opening of a tag with attributes, e.g. <IMG someatributes=here..
TAG_START_OPEN: '<' [A-Za-z0-9]+ ' ' -> mode(IN_TAG);

COMMENT_START: '<!--' -> skip, mode(COMMENT);

URL: ( 'http://' | 'https://' )? 'www' ([A-Za-z0-9.\-_]+? ( '.' )+)+ (PLAIN_TEXT|[0-9-_'./\\~,`!@#$%^&*():;<>{}=])+?;
EMAIL: [A-Za-z0-9.\-_:]+? '@' [A-Za-z0-9.\-_]+ '.' [a-z][a-z][a-z]?;
FLOAT: INTEGER'.'INTEGER~'.' -> skip;
PUNCT: [-_'./\\~,`!@#$%^&*():;{}]+? -> skip;
INTEGER: [0-9,]+;
PLAIN_TEXT: [A-Za-z]+;
TEXT_WITH_PUNCTUATION: (PLAIN_TEXT|[0-9-_'./\\~,`!@#$%^&*():;<>{}])+?;
NEW_LINE: ('/r'? '\n') ;
//NEWLINE_NEWLINE: NEW_LINE NEW_LINE+ -> skip;
WS: (' '|'\t')+ -> skip;
OTHER: .+? -> skip;

mode JavaScript;
JS_END: '</script>' -> skip, mode(DEFAULT_MODE);
OTHER_JS: .+? -> skip;

mode EXLAM;
EXLAM_END: '>' -> skip, mode(DEFAULT_MODE);
EXLAM_CONTENT_SKIP: .+?  -> skip;


mode COMMENT;
COMMENT_END: '--' '!'? '>' -> skip, mode(DEFAULT_MODE);
COMMENT_CONTENT_SKIP: .+?  -> skip;

// The space indicated by XXX: <tagName XXX >
mode IN_TAG;
TAG_START_CLOSE: ('>'|'/>') -> mode(DEFAULT_MODE);
IN_TAG_URL: ('href'|'HREF') ' '? '="' (URL|EMAIL) '"';
CONTENT_START_IGNORE: ('content'|'alt')'="'[$@!*#]+?'"'-> skip;
CONTENT_START: ('content'|'alt')'="'TEXT_WITH_PUNCTUATION-> mode(CONTENT_MODE);
OTHERS: OTHER -> skip;

// Inside of a tag, any content or alt attribute, e.g. <IMG alt="XXX">
mode CONTENT_MODE;
CONTENT_CLOSE: '"' -> mode(IN_TAG);
CONTENT_FLOAT: FLOAT -> skip;
CONTENT_INTEGER: [0-9]?INTEGER?[0-9];
CONTENT_EMAIL: EMAIL;
CONTENT_TEXT: PLAIN_TEXT;
CONTENT_PUNCTUATION: PUNCT -> skip;
CONTENT_WS: ' ';
