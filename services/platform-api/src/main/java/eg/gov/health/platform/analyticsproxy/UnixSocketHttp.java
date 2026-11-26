package eg.gov.health.platform.analyticsproxy;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import java.io.*; import java.nio.charset.StandardCharsets;
/** Minimal HTTP/1.1 over a Unix domain socket. The sandbox
 *  resets raw JVM TCP even on localhost (the same reason JDBC
 *  rides a Unix socket here), so the platform reaches the
 *  co-located analytics service through its socket — the same
 *  code path serves any co-located deployment; remote
 *  deployments use plain HTTP via app.analytics.url. */
public final class UnixSocketHttp {
  private UnixSocketHttp() {}
  public record Response(int status, String body) {}
  public static Response request(String socketPath, String method, String path, String jsonBody) throws Exception {
    var addr = AFUNIXSocketAddress.of(new File(socketPath));
    try (var sock = AFUNIXSocket.newInstance()) {
      sock.connect(addr, 4000);
      sock.setSoTimeout(15000);
      byte[] payload = jsonBody == null ? new byte[0] : jsonBody.getBytes(StandardCharsets.UTF_8);
      var head = new StringBuilder();
      head.append(method).append(' ').append(path).append(" HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n");
      if (jsonBody != null) head.append("Content-Type: application/json\r\nContent-Length: ").append(payload.length).append("\r\n");
      head.append("\r\n");
      var out = sock.getOutputStream();
      out.write(head.toString().getBytes(StandardCharsets.UTF_8));
      if (payload.length > 0) out.write(payload);
      out.flush();
      var raw = sock.getInputStream().readAllBytes();
      String all = new String(raw, StandardCharsets.UTF_8);
      int split = all.indexOf("\r\n\r\n");
      String headers = split >= 0 ? all.substring(0, split) : "";
      String body = split >= 0 ? all.substring(split + 4) : all;
      int status = 502;
      var firstLine = headers.split("\r\n")[0].split(" ");
      if (firstLine.length >= 2) { try { status = Integer.parseInt(firstLine[1]); } catch (Exception ignored) {} }
      if (headers.toLowerCase().contains("transfer-encoding: chunked")) body = dechunk(body);
      return new Response(status, body);
    }
  }
  private static String dechunk(String body) {
    var sb = new StringBuilder(); int i = 0;
    while (i < body.length()) {
      int eol = body.indexOf("\r\n", i);
      if (eol < 0) break;
      String sizeStr = body.substring(i, eol).trim();
      int size; try { size = Integer.parseInt(sizeStr, 16); } catch (Exception e) { break; }
      if (size == 0) break;
      sb.append(body, eol + 2, Math.min(eol + 2 + size, body.length()));
      i = eol + 2 + size + 2;
    }
    return sb.toString();
  }
}
