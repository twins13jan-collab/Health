import json, re
from collections import Counter

def norm(s):
    if not s: return ""
    s = s.replace("ـ", "")
    s = re.sub(r"[إأآ]", "ا", s); s = re.sub(r"ا{2,}", "ا", s)
    s = s.replace("ى", "ي").replace("ة", "ه").replace("ؤ", "و").replace("ئ", "ي")
    return re.sub(r"\s+", " ", s).strip()

GOV_CANON = {
    "القاهره": "القاهرة", "الجيزه": "الجيزة", "الاسكندريه": "الإسكندرية",
    "القليوبيه": "القليوبية", "الشرقيه": "الشرقية", "بورسعيد": "بورسعيد",
    "السويس": "السويس", "الغربيه": "الغربية", "البحيره": "البحيرة",
    "الاسماعيليه": "الإسماعيلية", "الدقهليه": "الدقهلية", "المنوفيه": "المنوفية",
    "بني سويف": "بني سويف", "سوهاج": "سوهاج", "البحر الاحمر": "البحر الأحمر",
    "دمياط": "دمياط", "قنا": "قنا", "الاقصر": "الأقصر", "الفيوم": "الفيوم",
    "اسيوط": "أسيوط", "اسوان": "أسوان", "المنيا": "المنيا",
    "مرسي مطروح": "مطروح", "مطروح": "مطروح", "جنوب سيناء": "جنوب سيناء",
    "كفر الشيخ": "كفر الشيخ", "شمال سيناء": "شمال سيناء",
}
PRIVATE_UNIVERSITIES = ["مصر للعلوم", "فاروس", "المستقبل", "البريطانيه", "الالمانيه",
    "الفرنسيه", "الروسيه", "النهضه", "درايه", "هليوبوليس", "ميريت", "سفنكس",
    "الجامعه الحديثه", "الاهرام الكنديه", "الدلتا للعلوم", "جامعه سيناء"]

def classify(name):
    """(ownership, type, level, marker) or None. v2: token-exact
    markers, syndicate/teaching disambiguation, national institutes
    only by name, air-force + international medical centre as
    military, private universities excluded."""
    n = norm(name)
    toks = n.split(" ")
    def has_tok(t): return t in toks
    if any(p in n for p in PRIVATE_UNIVERSITIES):
        return None
    if "جامع" in n or "قصر العيني" in n or "الدمرداش" in n:
        return ("university", "university_hospital", "tertiary", "university")
    if "التامين الصحي" in n or "هيئه التامين" in n:
        return ("insurance", "hospital", "secondary", "insurance")
    if ("القوات المسلحه" in n or "العسكري" in n or "الجوي" in n
            or "الطبي العالمي" in n or ("الجلاء" in n and "مستشف" in n)):
        return ("military", "hospital", "tertiary", "military")
    if "الشرطه" in n or "وزاره الداخليه" in n:
        return ("police", "hospital", "secondary", "police")
    if "التعليمي" in n and "مستشف" in n and "نقاب" not in n:
        return ("moh", "hospital", "tertiary", "teaching")
    if ("المعهد القومي" in n or "معهد ناصر" in n or "المعهد التذكاري" in n
            or "معهد تيودور" in n or "المعهد القومى" in n):
        return ("moh", "specialized_center", "tertiary", "institute")
    if has_tok("العام"):
        return ("moh", "general_hospital", "secondary", "moh:general")
    if has_tok("المركزي"):
        return ("moh", "general_hospital", "secondary", "moh:central")
    for m in ["الحميات", "الصدر", "الرمد", "الجلديه", "الجذام", "النفسيه",
              "وزاره الصحه", "الامانه العامه", "امانه المراكز", "المؤسسه العلاجيه",
              "هيئه المستشفيات", "الحكومي"]:
        if m in n:
            return ("moh", "hospital", "secondary", "moh:" + m)
    return None

rows = json.load(open("/home/hatch/workspace/network_rows.json"))
hosp = [r for r in rows if norm(r.get("ptype", "")).startswith("المستشفيات")]
included, excluded, seen = [], [], set()
for r in hosp:
    gov = GOV_CANON.get(norm(r.get("gov", "")), r.get("gov", ""))
    key = (norm(r.get("name", "")), gov, norm(r.get("city", "")))
    if key in seen: continue
    seen.add(key)
    c = classify(r.get("name", ""))
    rec = {**r, "gov_canon": gov, "class": c}
    (included if c else excluded).append(rec)

print("hospital rows:", len(hosp), "| unique:", len(included) + len(excluded))
print("INCLUDED:", len(included), Counter(x["class"][0] for x in included), Counter(x["class"][1] for x in included))
for x in included: print(" +", x["gov_canon"], "|", x["city"], "|", x["name"], "|", x["class"][3])
print("EXCLUDED:", len(excluded))
json.dump({"included": included, "excluded": excluded},
          open("/home/hatch/workspace/network_classified.json", "w"), ensure_ascii=False)
