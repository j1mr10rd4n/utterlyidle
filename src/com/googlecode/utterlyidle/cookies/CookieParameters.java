package com.googlecode.utterlyidle.cookies;

import com.googlecode.totallylazy.*;
import com.googlecode.utterlyidle.HeaderParameters;
import com.googlecode.utterlyidle.Parameters;
import com.googlecode.utterlyidle.Rfc2616;

import javax.ws.rs.core.HttpHeaders;

import static com.googlecode.totallylazy.Pair.pair;
import static com.googlecode.totallylazy.Predicates.not;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.empty;
import static com.googlecode.totallylazy.Strings.equalIgnoringCase;
import static com.googlecode.totallylazy.regex.Regex.regex;
import static javax.ws.rs.core.HttpHeaders.COOKIE;

public class CookieParameters extends Parameters<String, String> {
    private CookieParameters(final HeaderParameters headerParameters) {
        super(equalIgnoringCase());
        parseRequestCookies(sequence(headerParameters.getValues(COOKIE)).filter(not(empty())));
    }

    public static CookieParameters cookies(final HeaderParameters headerParameters) {
        return new CookieParameters(headerParameters);
    }

    private void parseRequestCookies(Sequence<String> headers) {
        for (String header : headers) {
            parseIntoPairs(header).map(unQuoteValue()).fold(this, Parameters.<String, String>pairIntoParameters());
        }
    }

    private Callable1<? super Pair<String, String>, Pair<String, String>> unQuoteValue() {
        return new Callable1<Pair<String, String>, Pair<String, String>>() {
            public Pair<String, String> call(Pair<String, String> pair) throws Exception {
                return pair(pair.first(), Rfc2616.toUnquotedString(pair.second()));
            }
        };
    }

    private Sequence<Pair<String, String>> parseIntoPairs(String header) {
        return regex("\\s*;\\s*").
                split(header).
                map(splitOnFirst("=")).
                filter(not(anAttribute()));
    }

    private static Predicate<? super Pair<String, String>> anAttribute() {
        return new Predicate<Pair<String, String>>() {
            public boolean matches(Pair<String, String> pair) {
                return pair.first().startsWith("$");
            }
        };
    }

    private static Callable1<? super String, Pair<String, String>> splitOnFirst(final String separator) {
        return new Callable1<String, Pair<String, String>>() {
            public Pair<String, String> call(String cookie) throws Exception {
                return pair(cookie.substring(0, cookie.indexOf(separator)), cookie.substring(cookie.indexOf(separator) + 1, cookie.length()));
            }
        };
    }


    public static String toHttpHeader(String name, Cookie cookie) {
        final String cookieValue = String.format("%s=%s; ", name, Rfc2616.toQuotedString(cookie.value()));
        final String attributes = sequence(cookie.attributes()).toString("; ");

        return cookieValue + attributes;
    }

}
