/** Duktape-Android modification: Hack in date parsing and timezone support for Android */
extern duk_int_t android__get_local_tzoffset(duk_double_t time);
extern duk_bool_t android__date_parse_string(duk_context* ctx, const char* str);
#define DUK_USE_DATE_GET_LOCAL_TZOFFSET android__get_local_tzoffset
#define DUK_USE_DATE_PARSE_STRING android__date_parse_string
/** End Duktape-Android. */