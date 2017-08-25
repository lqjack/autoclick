import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;

public class Cert {

    public static void main(String[] args) throws Exception {
        Cert cert = new Cert();
        cert.testConnectionTo("https://baidu.com");
    }

    public void testConnectionTo(String aURL) throws Exception {
        URL destinationURL = new URL(aURL);
        HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
        conn.connect();
        Certificate[] certs = conn.getServerCertificates();
        System.out.println("nb = " + certs.length);
        for (Certificate cert : certs) {
            System.out.println("Certificate is: " + cert);
            if (cert instanceof X509Certificate) {
                try {
                    ((X509Certificate) cert).checkValidity();
//                    FileOutputStream os = new FileOutputStream(cert + ".cert");
//                    i++;
//                    os.write(cert.getEncoded());
                } catch (CertificateExpiredException cee) {
                    System.out.println("Certificate is expired");
                }
            } else {
                System.err.println("Unknown certificate type: " + cert);
            }
        }
    }

}
