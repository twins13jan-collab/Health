package eg.gov.health.platform.common;
import java.util.*;
/** List pagination (completion batch 8). Lists keep their
 *  legacy shape when no paging parameters are given (full
 *  list + count); with ?size=&page= they return one page plus
 *  total/page metadata, so large real-world lists stop
 *  shipping whole tables to every client. */
public final class Paging {
  private Paging() {}
  public static <T> Map<String,Object> page(List<T> rows, String key, Integer page, Integer size) {
    int total = rows.size();
    if (size == null || size <= 0) return Map.of("ok", true, key, rows, "count", total);
    int p = page == null || page < 0 ? 0 : page;
    int from = Math.min(p * size, total);
    int to = Math.min(from + size, total);
    return Map.of("ok", true, key, rows.subList(from, to), "count", total,
      "page", p, "size", size, "pages", (total + size - 1) / size);
  }
}
