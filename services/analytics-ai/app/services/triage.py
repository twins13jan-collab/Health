"""Rule-based NEWS2 triage *support* — advisory only, by design.

Catalog v6 guardrail: AI never makes an executive clinical or financial
decision alone. This module therefore:
  * computes a transparent NEWS2 score (every component is returned),
  * ALWAYS returns requires_human_review=True,
  * NEVER returns a final decision (final_decision is always None),
  * phrases its output as a *suggested* response for a clinician to accept
    or override.

NEWS2 (Royal College of Physicians, 2017) scoring tables for adults.
"""
from __future__ import annotations

from ..models import (
    Consciousness,
    News2Component,
    TriageSupportRequest,
    TriageSupportResponse,
)


def _score_rr(rr: int) -> int:
    if rr <= 8: return 3
    if rr <= 11: return 1
    if rr <= 20: return 0
    if rr <= 24: return 2
    return 3


def _score_spo2_scale1(spo2: float) -> int:
    if spo2 <= 91: return 3
    if spo2 <= 93: return 2
    if spo2 <= 95: return 1
    return 0


def _score_spo2_scale2(spo2: float, on_oxygen: bool) -> int:
    # Target range 88-92% for hypercapnic patients.
    if spo2 <= 83: return 3
    if spo2 <= 85: return 2
    if spo2 <= 87: return 1
    if spo2 <= 92: return 0
    if not on_oxygen: return 0  # >=93 on air
    if spo2 <= 94: return 1
    if spo2 <= 96: return 2
    return 3


def _score_sbp(sbp: int) -> int:
    if sbp <= 90: return 3
    if sbp <= 100: return 2
    if sbp <= 110: return 1
    if sbp <= 219: return 0
    return 3


def _score_pulse(pulse: int) -> int:
    if pulse <= 40: return 3
    if pulse <= 50: return 1
    if pulse <= 90: return 0
    if pulse <= 110: return 1
    if pulse <= 130: return 2
    return 3


def _score_temp(t: float) -> int:
    if t <= 35.0: return 3
    if t <= 36.0: return 1
    if t <= 38.0: return 0
    if t <= 39.0: return 1
    return 2


def score_news2(req: TriageSupportRequest) -> TriageSupportResponse:
    spo2_score = (
        _score_spo2_scale2(req.spo2_pct, req.on_supplemental_oxygen)
        if req.spo2_scale == 2
        else _score_spo2_scale1(req.spo2_pct)
    )
    components = [
        News2Component(parameter="Respiratory rate", parameter_ar="معدل التنفس",
                       value=f"{req.respiratory_rate}/min", score=_score_rr(req.respiratory_rate)),
        News2Component(parameter="SpO2", parameter_ar="تشبع الأكسجين",
                       value=f"{req.spo2_pct:g}% (scale {req.spo2_scale})", score=spo2_score),
        News2Component(parameter="Supplemental oxygen", parameter_ar="الأكسجين الإضافي",
                       value="oxygen" if req.on_supplemental_oxygen else "air",
                       score=2 if req.on_supplemental_oxygen else 0),
        News2Component(parameter="Systolic BP", parameter_ar="ضغط الدم الانقباضي",
                       value=f"{req.systolic_bp} mmHg", score=_score_sbp(req.systolic_bp)),
        News2Component(parameter="Pulse", parameter_ar="النبض",
                       value=f"{req.pulse}/min", score=_score_pulse(req.pulse)),
        News2Component(parameter="Consciousness (ACVPU)", parameter_ar="درجة الوعي",
                       value=req.consciousness.value,
                       score=0 if req.consciousness == Consciousness.alert else 3),
        News2Component(parameter="Temperature", parameter_ar="درجة الحرارة",
                       value=f"{req.temperature_c} °C", score=_score_temp(req.temperature_c)),
    ]
    total = sum(c.score for c in components)
    red_flags = [c.parameter for c in components if c.score == 3]

    # NEWS2 aggregate risk + the single-parameter-3 rule.
    if total >= 7:
        risk, risk_ar = "high", "مرتفع"
        resp = "Urgent review by a clinician competent in acute illness; consider continuous monitoring and critical-care referral."
        resp_ar = "مراجعة عاجلة من طبيب متمرس في الحالات الحادة؛ يُنصح بالمراقبة المستمرة والنظر في الإحالة للرعاية المركزة."
    elif total >= 5:
        risk, risk_ar = "medium", "متوسط"
        resp = "Urgent review by a ward-based clinician; reassess NEWS2 frequently (at least every 4-6 hours, sooner if deteriorating)."
        resp_ar = "مراجعة عاجلة من طبيب القسم؛ إعادة تقييم NEWS2 باستمرار (كل ٤-٦ ساعات على الأقل)."
    elif red_flags:
        risk, risk_ar = "low-medium", "منخفض-متوسط"
        resp = "Review by a registered clinician promptly: a single parameter scores 3 even though the aggregate is low."
        resp_ar = "مراجعة فورية من ممارس مسجل: توجد علامة واحدة بدرجة ٣ رغم انخفاض المجموع."
    else:
        risk, risk_ar = "low", "منخفض"
        resp = "Routine monitoring; minimum 12-hourly NEWS2 reassessment per local protocol."
        resp_ar = "مراقبة روتينية؛ إعادة تقييم NEWS2 كل ١٢ ساعة على الأقل وفق البروتوكول المحلي."

    return TriageSupportResponse(
        requires_human_review=True,  # hard guardrail — never False
        final_decision=None,         # hard guardrail — never a decision
        news2_total=total,
        components=components,
        risk_category=risk,
        risk_category_ar=risk_ar,
        suggested_response=resp,
        suggested_response_ar=resp_ar,
        red_flag_parameters=red_flags,
        disclaimer="Advisory only: this NEWS2 calculation supports — and never replaces — clinical judgement. A qualified clinician must review and decide.",
        disclaimer_ar="للاسترشاد فقط: حساب NEWS2 هذا يدعم الحكم السريري ولا يحل محله أبدًا. يجب أن يراجع طبيب مؤهل ويقرر.",
    )
