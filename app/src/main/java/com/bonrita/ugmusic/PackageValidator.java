package com.bonrita.ugmusic;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.telecom.Call;

import com.bonrita.ugmusic.utils.LogHelper;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates that the calling package is authorized to browse a
 * {@link android.service.media.MediaBrowserService}.
 * <p>
 * The list of allowed signing certificates and their corresponding package names is defined in
 * res/xml/allowed_media_browser_callers.xml.
 * <p>
 * If you add a new valid caller to allowed_media_browser_callers.xml and you don't know
 * its signature, this class will print to logcat (INFO level) a message with the proper base64
 * version of the caller certificate that has not been validated. You can copy from logcat and
 * paste into allowed_media_browser_callers.xml. Spaces and newlines are ignored.
 */
public class PackageValidator {
    private static final String TAG = LogHelper.makeLogTag(PackageValidator.class);

    /**
     * Map allowed callers' certificate keys to the expected caller information.
     */
    private final Map<String, ArrayList<CallerInfo>> mValidCertificates;

    public PackageValidator(Context ctx) {
        mValidCertificates = readValidCertificates(ctx.getResources().getXml(
                R.xml.allowed_media_browser_callers
        ));
    }

    private Map<String, ArrayList<CallerInfo>> readValidCertificates(XmlResourceParser parser) {
        HashMap<String, ArrayList<CallerInfo>> validCertificates = new HashMap<>();

        try {
            int eventType = parser.next();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG &&
                        parser.getName().equals(("signing_certificate"))) {
                    String name = parser.getAttributeValue(null, "name");
                    String packageName = parser.getAttributeValue(null, "package");
                    boolean isRelease = parser.getAttributeBooleanValue(null, "release", false);
                    String certificate = parser.nextText().replaceAll("\\s|\\n", "");

                    CallerInfo info = new CallerInfo(name, packageName, isRelease);

                    ArrayList<CallerInfo> infos = validCertificates.get(certificate);
                    if (info == null) {
                        infos = new ArrayList<>();
                        validCertificates.put(certificate, infos);
                    }
                    LogHelper.v(TAG, "Adding allowed caller: ", info.name,
                            " package=", info.packageName, " release=", info.release,
                            " certificate=", certificate);
                    infos.add(info);
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException | IOException e) {
            LogHelper.e(TAG, e, "Could not read allowed callers from XML.");
        }

        return validCertificates;
    }

    private final static class CallerInfo {
        final String name;
        final String packageName;
        final boolean release;

        public CallerInfo(String name, String packageName, boolean release) {
            this.name = name;
            this.packageName = packageName;
            this.release = release;
        }
    }
}
