package ch.cern.cmms.eamlightejb.tools;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Tools {

    public static String getVariableValue(String variableName) {
        String valueFromEnv = System.getenv().get(variableName);
        if (valueFromEnv != null && !valueFromEnv.isEmpty()) {
            return valueFromEnv;
        }
        String val = System.getProperty(variableName);
        if (val != null && !val.isEmpty()) {
            return val;
        }
        switch (variableName) {
            case "EAMLIGHT_AUTHENTICATION_MODE": return "LOCAL";
            case "EAMLIGHT_ADMIN_USER": return "ADMIN";
            case "EAMLIGHT_DEFAULT_USER": return "ADMIN";
            case "EAMLIGHT_ADMIN_PASSWORD": return "admin";
            case "EAMLIGHT_INFOR_ORGANIZATION": return "*";
            case "EAMLIGHT_INFOR_TENANT": return "infor";
            default: return null;
        }
    }

    public static Integer getVariableIntegerValue(String variableName) {
        try {
            String value = Tools.getVariableValue(variableName);
            return Integer.parseInt(value);
        } catch(NumberFormatException e) {
            return null;
        }
    }

    public static SSLContext sslContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }

}
