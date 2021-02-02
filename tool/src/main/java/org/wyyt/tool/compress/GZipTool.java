package org.wyyt.tool.compress;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * the common function of gzip compress
 * <p>
 *
 * @author Ning.Zhang(Pegasus)
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize       02/14/2021       Initialize   *
 * *****************************************************************
 */
@Slf4j
public final class GZipTool {

    private static final String SUFFIX = "H4sIAAA";

    public static String compress(final String value) throws Exception {
        if (ObjectUtils.isEmpty(value)) {
            return value;
        } else if (value.length() <= 125) {
            return value;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (final GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(value.getBytes());
        }
        return Base64.encodeBase64String(out.toByteArray());
    }

    public static String uncompress(String value) throws Exception {
        if (ObjectUtils.isEmpty(value)) {
            return value;
        } else if (!value.startsWith(SUFFIX)) {
            return value;
        }

        String result;
        byte[] compressed = Base64.decodeBase64(value);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteArrayInputStream in = new ByteArrayInputStream(compressed);
             final GZIPInputStream ginzip = new GZIPInputStream(in)) {

            byte[] buffer = new byte[1024];
            int offset;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            result = out.toString();
        }
        return result;
    }
}