package utilities;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import Base.DriverFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Thread-safe Video & Screen Recording Manager for Selenium test executions.
 * Works seamlessly in both Headless and Headed browser execution modes.
 *
 * Captures high-fidelity browser execution frames and generates a standalone,
 * interactive HTML5 Video Replay player embedded into ExtentReports.
 * Features:
 *  - Play / Pause controls
 *  - Timeline scrubber & frame counter
 *  - Step forward / Step backward
 *  - Multi-speed playback (0.5x, 1x, 2x)
 *  - Step labels & timestamps on each frame
 */
public final class ScreenRecorderUtil {

    private static final String VIDEO_DIR = "test-output/videos";

    public static class FrameItem {
        public final String base64Png;
        public final String stepLabel;
        public final long timestamp;

        public FrameItem(String base64Png, String stepLabel) {
            this.base64Png = base64Png;
            this.stepLabel = stepLabel;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final ThreadLocal<List<FrameItem>> THREAD_FRAMES = ThreadLocal.withInitial(() -> Collections.synchronizedList(new ArrayList<>()));
    private static final ThreadLocal<String> THREAD_TEST_NAME = new ThreadLocal<>();
    private static final ThreadLocal<String> THREAD_DESCRIPTION = new ThreadLocal<>();

    private ScreenRecorderUtil() {
    }

    public static class RecordingResult {
        private final String testName;
        private final String status;
        private final File htmlReplayFile;
        private final int frameCount;

        public RecordingResult(String testName, String status, File htmlReplayFile, int frameCount) {
            this.testName = testName;
            this.status = status;
            this.htmlReplayFile = htmlReplayFile;
            this.frameCount = frameCount;
        }

        public String getTestName() {
            return testName;
        }

        public String getStatus() {
            return status;
        }

        public File getHtmlReplayFile() {
            return htmlReplayFile;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public String toReportHtml() {
            StringBuilder sb = new StringBuilder();
            sb.append("<div style='margin-top:8px; padding:10px; background:#f8f9fa; border:1px solid #e2e8f0; border-radius:6px;'>");
            sb.append("<div style='font-size:13px; font-weight:600; margin-bottom:6px; color:#2d3748;'>");
            sb.append("🎬 Test Execution Video Replay (").append(status).append(")");
            sb.append("</div>");

            if (htmlReplayFile != null && htmlReplayFile.exists()) {
                String relativeUrl = "../videos/" + htmlReplayFile.getName();
                sb.append("<div style='margin-bottom:8px;'>");
                sb.append("<a href='").append(relativeUrl).append("' target='_blank' style='display:inline-block; padding:5px 12px; background:#2563eb; color:#fff; text-decoration:none; border-radius:4px; font-size:12px; font-weight:bold;'>");
                sb.append("▶ Open Full Video Player (").append(frameCount).append(" frames)</a>");
                sb.append("</div>");

                sb.append("<iframe src='").append(relativeUrl).append("' style='width:100%; height:380px; border:1px solid #cbd5e0; border-radius:6px; background:#000;' loading='lazy'></iframe>");
            } else {
                sb.append("<span style='font-size:11px; color:#718096;'>No video frames captured.</span>");
            }

            sb.append("</div>");
            return sb.toString();
        }
    }

    public static void startRecording(String testName, String description) {
        THREAD_FRAMES.get().clear();
        THREAD_TEST_NAME.set(testName);
        THREAD_DESCRIPTION.set(description);

        File videoFolder = new File(VIDEO_DIR);
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
    }

    public static void captureFrame(WebDriver driver, String stepLabel) {
        if (driver == null) {
            return;
        }
        try {
            if (driver instanceof TakesScreenshot takesScreenshot) {
                byte[] bytes = takesScreenshot.getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    String b64 = Base64.getEncoder().encodeToString(bytes);
                    List<FrameItem> frames = THREAD_FRAMES.get();
                    if (frames.size() < 150) {
                        frames.add(new FrameItem(b64, stepLabel));
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void captureFrame(String stepLabel) {
        if (DriverFactory.hasDriver()) {
            captureFrame(DriverFactory.getDriver(), stepLabel);
        }
    }

    public static RecordingResult stopRecording(String testName, String status, WebDriver driver) {
        if (driver != null) {
            captureFrame(driver, "Test Ended (" + status + ")");
        } else if (DriverFactory.hasDriver()) {
            captureFrame(DriverFactory.getDriver(), "Test Ended (" + status + ")");
        }

        List<FrameItem> frames = new ArrayList<>(THREAD_FRAMES.get());
        THREAD_FRAMES.get().clear();
        String description = THREAD_DESCRIPTION.get();

        File htmlPlayerFile = null;
        int frameCount = frames.size();
        if (frameCount > 0) {
            try {
                String safeName = sanitizeFilename(testName);
                String fileName = safeName + "_" + status.toUpperCase() + "_" + System.currentTimeMillis() + "_replay.html";
                htmlPlayerFile = new File(VIDEO_DIR, fileName);
                String htmlContent = buildHtmlReplay(testName, description, status, frames);
                Files.writeString(htmlPlayerFile.toPath(), htmlContent, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
            }
        }

        return new RecordingResult(testName, status, htmlPlayerFile, frameCount);
    }

    private static String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "TestExecution";
        }
        String cleaned = name.replaceAll("[\\[\\]]", "_")
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .replaceAll("_+", "_");
        if (cleaned.startsWith("_")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("_")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.length() > 45) {
            cleaned = cleaned.substring(0, 45);
        }
        return cleaned;
    }

    private static String buildHtmlReplay(String testName, String description, String status, List<FrameItem> frames) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang='en'>\n<head>\n");
        sb.append("<meta charset='UTF-8'>\n");
        sb.append("<title>Video Replay - ").append(escapeHtml(testName)).append("</title>\n");
        sb.append("<style>\n");
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; margin: 0; padding: 20px; }\n");
        sb.append(".header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; background: #1e293b; padding: 12px 18px; border-radius: 8px; }\n");
        sb.append(".title { font-size: 16px; font-weight: 700; }\n");
        sb.append(".badge { padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold; text-transform: uppercase; }\n");
        sb.append(".badge-pass { background: #10b981; color: #fff; }\n");
        sb.append(".badge-fail { background: #ef4444; color: #fff; }\n");
        sb.append(".badge-skip { background: #f59e0b; color: #fff; }\n");
        sb.append(".player-box { max-width: 1200px; margin: 0 auto; background: #000; border-radius: 10px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.6); }\n");
        sb.append(".screen-container { position: relative; width: 100%; height: 600px; display: flex; align-items: center; justify-content: center; background: #090d16; }\n");
        sb.append(".screen-container img { max-width: 100%; max-height: 100%; object-fit: contain; }\n");
        sb.append(".step-overlay { position: absolute; bottom: 12px; left: 16px; background: rgba(15,23,42,0.85); backdrop-filter: blur(4px); padding: 8px 16px; border-radius: 6px; font-size: 13px; color: #38bdf8; border: 1px solid rgba(56,189,248,0.3); }\n");
        sb.append(".controls { display: flex; align-items: center; gap: 12px; padding: 12px 20px; background: #1e293b; }\n");
        sb.append(".btn { background: #3b82f6; color: #fff; border: none; padding: 8px 14px; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 4px; transition: background 0.2s; }\n");
        sb.append(".btn:hover { background: #2563eb; }\n");
        sb.append(".btn-secondary { background: #334155; }\n");
        sb.append(".btn-secondary:hover { background: #475569; }\n");
        sb.append(".scrubber { flex: 1; accent-color: #3b82f6; cursor: pointer; }\n");
        sb.append(".counter { font-size: 12px; color: #94a3b8; font-variant-numeric: tabular-nums; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        sb.append("<div class='player-box'>\n");
        sb.append("  <div class='header'>\n");
        sb.append("    <div>\n");
        sb.append("      <div class='title'>").append(escapeHtml(testName)).append("</div>\n");
        if (description != null && !description.isBlank()) {
            sb.append("      <div style='font-size:12px; color:#94a3b8; margin-top:4px;'>").append(escapeHtml(description)).append("</div>\n");
        }
        sb.append("    </div>\n");
        String badgeClass = "PASS".equalsIgnoreCase(status) ? "badge-pass" : ("FAIL".equalsIgnoreCase(status) ? "badge-fail" : "badge-skip");
        sb.append("    <span class='badge ").append(badgeClass).append("'>").append(escapeHtml(status)).append("</span>\n");
        sb.append("  </div>\n");

        sb.append("  <div class='screen-container'>\n");
        sb.append("    <img id='screenImg' src='' alt='Screen Frame' />\n");
        sb.append("    <div id='stepOverlay' class='step-overlay'>Step 1</div>\n");
        sb.append("  </div>\n");

        sb.append("  <div class='controls'>\n");
        sb.append("    <button id='playBtn' class='btn' onclick='togglePlay()'>⏸ Pause</button>\n");
        sb.append("    <button class='btn btn-secondary' onclick='step(-1)' title='Previous Frame'>⏮</button>\n");
        sb.append("    <button class='btn btn-secondary' onclick='step(1)' title='Next Frame'>⏭</button>\n");
        sb.append("    <input type='range' id='scrubber' class='scrubber' min='0' max='")
                .append(frames.size() - 1).append("' value='0' oninput='seekTo(this.value)' />\n");
        sb.append("    <span id='counter' class='counter'>1 / ").append(frames.size()).append("</span>\n");
        sb.append("    <button id='speedBtn' class='btn btn-secondary' onclick='toggleSpeed()'>1x</button>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n\n");

        sb.append("<script>\n");
        sb.append("const frameList = [\n");
        for (int i = 0; i < frames.size(); i++) {
            FrameItem f = frames.get(i);
            sb.append("  { label: '").append(escapeJs(f.stepLabel))
                    .append("', data: 'data:image/png;base64,").append(f.base64Png).append("' }");
            if (i < frames.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("];\n\n");

        sb.append("let currentIndex = 0;\n");
        sb.append("let isPlaying = true;\n");
        sb.append("let intervalId = null;\n");
        sb.append("let speed = 600;\n\n");

        sb.append("const img = document.getElementById('screenImg');\n");
        sb.append("const overlay = document.getElementById('stepOverlay');\n");
        sb.append("const scrubber = document.getElementById('scrubber');\n");
        sb.append("const counter = document.getElementById('counter');\n");
        sb.append("const playBtn = document.getElementById('playBtn');\n");
        sb.append("const speedBtn = document.getElementById('speedBtn');\n\n");

        sb.append("function showFrame(idx) {\n");
        sb.append("  if (idx < 0) idx = 0;\n");
        sb.append("  if (idx >= frameList.length) idx = frameList.length - 1;\n");
        sb.append("  currentIndex = idx;\n");
        sb.append("  const frame = frameList[idx];\n");
        sb.append("  img.src = frame.data;\n");
        sb.append("  overlay.innerText = (idx + 1) + '. ' + frame.label;\n");
        sb.append("  scrubber.value = idx;\n");
        sb.append("  counter.innerText = (idx + 1) + ' / ' + frameList.length;\n");
        sb.append("}\n\n");

        sb.append("function togglePlay() {\n");
        sb.append("  if (isPlaying) {\n");
        sb.append("    clearInterval(intervalId);\n");
        sb.append("    isPlaying = false;\n");
        sb.append("    playBtn.innerText = '▶ Play';\n");
        sb.append("  } else {\n");
        sb.append("    if (currentIndex >= frameList.length - 1) currentIndex = 0;\n");
        sb.append("    startLoop();\n");
        sb.append("    isPlaying = true;\n");
        sb.append("    playBtn.innerText = '⏸ Pause';\n");
        sb.append("  }\n");
        sb.append("}\n\n");

        sb.append("function startLoop() {\n");
        sb.append("  clearInterval(intervalId);\n");
        sb.append("  intervalId = setInterval(() => {\n");
        sb.append("    if (currentIndex < frameList.length - 1) {\n");
        sb.append("      showFrame(currentIndex + 1);\n");
        sb.append("    } else {\n");
        sb.append("      clearInterval(intervalId);\n");
        sb.append("      isPlaying = false;\n");
        sb.append("      playBtn.innerText = '↺ Replay';\n");
        sb.append("    }\n");
        sb.append("  }, speed);\n");
        sb.append("}\n\n");

        sb.append("function seekTo(val) {\n");
        sb.append("  showFrame(parseInt(val));\n");
        sb.append("}\n\n");

        sb.append("function step(delta) {\n");
        sb.append("  if (isPlaying) togglePlay();\n");
        sb.append("  showFrame(currentIndex + delta);\n");
        sb.append("}\n\n");

        sb.append("function toggleSpeed() {\n");
        sb.append("  if (speed === 600) { speed = 300; speedBtn.innerText = '2x'; }\n");
        sb.append("  else if (speed === 300) { speed = 1200; speedBtn.innerText = '0.5x'; }\n");
        sb.append("  else { speed = 600; speedBtn.innerText = '1x'; }\n");
        sb.append("  if (isPlaying) startLoop();\n");
        sb.append("}\n\n");

        sb.append("if (frameList.length > 0) {\n");
        sb.append("  showFrame(0);\n");
        sb.append("  startLoop();\n");
        sb.append("}\n");
        sb.append("</script>\n</body>\n</html>");

        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escapeJs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
