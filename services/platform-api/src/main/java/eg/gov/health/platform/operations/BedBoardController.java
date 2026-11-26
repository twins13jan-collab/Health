package eg.gov.health.platform.operations;
import eg.gov.health.platform.clinical.EncounterRepository;
import eg.gov.health.platform.facility.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** National capacity board (cycle 225): aggregates only for national roles —
 *  counts and occupancy, never patient details of other facilities. */
@RestController @RequestMapping("/api/capacity")
public class BedBoardController {
  private final FacilityRepository facilities; private final EncounterRepository encounters;
  public BedBoardController(FacilityRepository facilities, EncounterRepository encounters) { this.facilities=facilities; this.encounters=encounters; }
  @GetMapping("/board")
  Map<String,Object> board(@RequestParam(required=false) String governorate) {
    var rows = governorate==null ? facilities.findAll() : facilities.findByGovernorate(governorate);
    var board = rows.stream().map(f -> {
      long open = encounters.findByFacilityIdOrderByArrivedAtDesc(f.getId()).stream().filter(e -> !"closed".equals(e.getStatus()) && !"completed".equals(e.getStatus())).count();
      int cap = f.getBedCapacity()==null?0:f.getBedCapacity();
      var m = new LinkedHashMap<String,Object>();
      m.put("facility_id", f.getId()); m.put("name_ar", f.getNameAr()); m.put("governorate", f.getGovernorate());
      m.put("bed_capacity", cap); m.put("open_encounters", open);
      m.put("occupancy_pct", cap==0? null : Math.round(1000.0*Math.min(open,cap)/cap)/10.0);
      return m;
    }).toList();
    return Map.of("ok",true,"board",board,"count",board.size());
  }
}
