package eg.gov.health.platform.ethicscommittees;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Medical committees & ethics (module 141). Committee
 *  formation and mandates; review of ethically conflicted
 *  cases; review of technologies, AI and research; conflicts
 *  of interest and recusal; and approval of high-risk
 *  protocols — all decided at quorum, by members whose
 *  interests are declared. */
@RestController @RequestMapping("/api/ethics-committees")
public class EthicsCommitteesController {
  private final CommitteeRepository committees; private final CommitteeMemberRepository members;
  private final EthicsCaseRepository cases; private final CommitteeDecisionRepository decisions; private final AuditService audit;
  public EthicsCommitteesController(CommitteeRepository committees, CommitteeMemberRepository members,
                                    EthicsCaseRepository cases, CommitteeDecisionRepository decisions, AuditService audit) {
    this.committees=committees; this.members=members; this.cases=cases; this.decisions=decisions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record CommitteeBody(String name, String mandate, Integer quorumRequired) {}
  @PostMapping("/committees")
  Map<String,Object> create(@RequestBody CommitteeBody r) {
    var p = principal();
    var c = committees.save(new Committee(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.mandate(), r.quorumRequired()));
    return Map.of("ok",true,"committee",c);
  }
  public record MemberBody(String memberRef, Boolean coiDeclared) {}
  @PostMapping("/committees/{id}/members")
  Map<String,Object> member(@PathVariable UUID id, @RequestBody MemberBody r) {
    var p = principal();
    committees.findById(id).orElseThrow(() -> BizException.notFound("committee_not_found","اللجنة غير موجودة"));
    var m = members.save(new CommitteeMember(UUID.fromString(p.facilityId()), p.governorate(), id, r.memberRef(), Boolean.TRUE.equals(r.coiDeclared())));
    return Map.of("ok",true,"member",m,"may_vote",EthicsRules.votingPermitted(Boolean.TRUE.equals(r.coiDeclared())));
  }
  public record CaseBody(String caseType, String summary) {}
  @PostMapping("/committees/{id}/cases")
  Map<String,Object> openCase(@PathVariable UUID id, @RequestBody CaseBody r) {
    var p = principal();
    committees.findById(id).orElseThrow(() -> BizException.notFound("committee_not_found","اللجنة غير موجودة"));
    var c = cases.save(new EthicsCase(UUID.fromString(p.facilityId()), p.governorate(), id, r.caseType(), r.summary()));
    audit.record("ethics.case.opened","ethics_case",c.getId().toString(),"{}");
    return Map.of("ok",true,"ethics_case",c);
  }
  public record DecisionBody(String decision, Integer votesPresent, String voterRefs, String recusedMembers) {}
  @PostMapping("/cases/{cid}/decide")
  Map<String,Object> decide(@PathVariable UUID cid, @RequestBody DecisionBody r) {
    var p = principal();
    var c = cases.findById(cid).orElseThrow(() -> BizException.notFound("case_not_found","الحالة غير موجودة"));
    var committee = committees.findById(c.getCommitteeId()).orElseThrow(() -> BizException.notFound("committee_not_found","اللجنة غير موجودة"));
    if (!EthicsRules.quorumMet(r.votesPresent(), committee.getQuorumRequired()))
      throw BizException.conflict("no_quorum","قرار لجنة بلا نصاب ليس قرارًا");
    var roster = members.findByCommitteeId(committee.getId());
    if (r.voterRefs() != null) {
      for (String ref : r.voterRefs().split(",")) {
        String trimmed = ref.trim();
        roster.stream().filter(m -> trimmed.equals(m.getMemberRef())).findFirst().ifPresent(m -> {
          if (!EthicsRules.votingPermitted(m.isCoiDeclared()))
            throw BizException.conflict("undeclared_coi","عضو بصوت ومصلحة غير معلنة يلوث القرار — التصويت للمصرحين فقط");
        });
      }
    }
    var d = decisions.save(new CommitteeDecision(UUID.fromString(p.facilityId()), p.governorate(), cid, r.decision(), r.votesPresent(), r.voterRefs(), r.recusedMembers()));
    c.decide(); cases.save(c);
    audit.record("ethics.case.decided","committee_decision",d.getId().toString(),"{}");
    return Map.of("ok",true,"decision_record",d);
  }
}
