package Base;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Finds (and if needed downloads) the msedgedriver.exe that matches the installed Microsoft Edge,
 * straight from Microsoft's current host {@code https://msedgedriver.microsoft.com/}.
 *
 * <p>Why not WebDriverManager: version 5.8 still uses msedgedriver.azureedge.net and an old blob
 * storage URL, both retired by Microsoft, so it can no longer download the Edge driver.
 *
 * <p>Cache: {@code ~/.cache/selenium/msedgedriver/win64/<version>/msedgedriver.exe} (downloaded once
 * per Edge version). Override completely with {@code -Dwebdriver.edge.driver=<path>}.
 */
final class EdgeDriverResolver {

    private static final String HOST = System.getProperty("edge.driver.host", "https://msedgedriver.microsoft.com/");

    private EdgeDriverResolver() {
    }

    /** Sets {@code webdriver.edge.driver} to a matching msedgedriver.exe and returns its path. */
    static synchronized String resolve() {
        String preset = System.getProperty("webdriver.edge.driver");
        if (preset != null && new File(preset).isFile()) {
            return preset;
        }
        try {
            String version = driverVersionFor(installedEdgeVersion());
            Path dir = Path.of(System.getProperty("user.home"), ".cache", "selenium", "msedgedriver", "win64", version);
            Path exe = dir.resolve("msedgedriver.exe");
            if (!Files.isRegularFile(exe)) {
                System.out.println("[BROWSER] Downloading Edge driver " + version + " from " + HOST);
                Files.createDirectories(dir);
                byte[] zip = get(HOST + version + "/edgedriver_win64.zip");
                try (ZipInputStream z = new ZipInputStream(new java.io.ByteArrayInputStream(zip))) {
                    for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
                        if (e.getName().endsWith("msedgedriver.exe")) {
                            Files.copy(z, exe, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
                if (!Files.isRegularFile(exe)) {
                    throw new IllegalStateException("msedgedriver.exe not found in the downloaded zip");
                }
            }
            System.setProperty("webdriver.edge.driver", exe.toString());
            return exe.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not get the Edge driver from " + HOST + ": " + e.getMessage(), e);
        }
    }

    /** Installed Edge version from the registry (e.g. 158.0.4370.33); null if not found. */
    private static String installedEdgeVersion() {
        for (String key : new String[] {"HKCU\\Software\\Microsoft\\Edge\\BLBeacon", "HKLM\\Software\\Microsoft\\Edge\\BLBeacon",
                "HKLM\\Software\\WOW6432Node\\Microsoft\\Edge\\BLBeacon"}) {
            try {
                Process p = new ProcessBuilder("reg", "query", key, "/v", "version").redirectErrorStream(true).start();
                String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                p.waitFor();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)").matcher(out);
                if (m.find()) {
                    return m.group(1);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** Driver version for that Edge (same major), or the latest stable if Edge's version is unknown. */
    private static String driverVersionFor(String edgeVersion) throws Exception {
        String file = edgeVersion == null ? "LATEST_STABLE" : "LATEST_RELEASE_" + edgeVersion.split("\\.")[0] + "_WINDOWS";
        byte[] raw = get(HOST + file);
        // Microsoft serves these files as UTF-16 with a byte-order mark.
        String text = (raw.length > 1 && (raw[0] == (byte) 0xFF || raw[0] == (byte) 0xFE))
                ? new String(raw, StandardCharsets.UTF_16) : new String(raw, StandardCharsets.UTF_8);
        String v = text.replaceAll("[^0-9.]", "");
        if (!v.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            throw new IllegalStateException("unexpected version '" + text.trim() + "' in " + file);
        }
        return v;
    }

    private static byte[] get(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30)).build();
        HttpResponse<InputStream> r = client.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(3)).build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (r.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + r.statusCode() + " for " + url);
        }
        try (InputStream in = r.body()) {
            return in.readAllBytes();
        }
    }
}
