"""Structured screen observations and portable, local-only result packaging."""
import hashlib
import html
import json
from pathlib import Path
import re
import zipfile

CATEGORIES = ("progress", "finding", "coverage", "session")
CLASSIFICATIONS = ("bug", "unclear", "contradiction", "hypothesis", "tool_error", "positive")
SEVERITIES = ("high", "medium", "low", "info")
COVERAGE = ("tested", "visited", "unavailable", "not_reached")
OUTCOMES = ("time_window", "victory", "defeat", "blocked", "action_limit")


def validate_note(note):
    for key in ("goal", "expected", "observed", "source", "finding", "screen"):
        value = note.get(key, "")
        if not isinstance(value, str) or len(value) > 4000:
            raise ValueError("Note text must contain at most 4000 characters")
    category = note.get("category", "progress")
    if category not in CATEGORIES:
        raise ValueError("Unknown note category")
    turn = note.get("turn")
    if turn is not None and (type(turn) is not int or turn < 0 or category != "progress"):
        raise ValueError("Turn must be a nonnegative integer on a progress checkpoint")
    if category == "finding":
        if (not note.get("finding", "").strip() or note.get("classification") not in CLASSIFICATIONS
                or note.get("severity") not in SEVERITIES):
            raise ValueError("Finding needs text, classification and severity")
    elif note.get("classification") or note.get("severity"):
        raise ValueError("Classification and severity belong to findings")
    if category == "coverage":
        if not note.get("screen", "").strip() or note.get("status") not in COVERAGE:
            raise ValueError("Coverage needs a screen and coverage status")
    elif category == "session":
        if note.get("status") not in OUTCOMES:
            raise ValueError("Final session note needs a stopping outcome")
    elif note.get("status"):
        raise ValueError("Status belongs to coverage or the final session note")


def events_in(directory):
    path = Path(directory) / "actions.jsonl"
    return [json.loads(line) for line in path.read_text().splitlines()] if path.exists() else []


def cited_frames(text, available):
    # Players sometimes concatenate a word ending in hex letters with a frame ID.
    found = set()
    for token in re.findall(r"[0-9a-f]{32,}", text):
        matches = {token[i:i + 32] for i in range(len(token) - 31)} & available
        if not matches:
            raise ValueError("Missing cited screenshot: " + token)
        found.update(matches)
    return found


def verify_play_evidence(directory):
    directory = Path(directory)
    report = directory / "report.md"
    if not report.is_file() or not report.read_text().strip():
        raise RuntimeError("Codex finished without a report")
    events = events_in(directory)
    if not any(e.get("kind") == "observe" or e.get("status") == "delivered" for e in events):
        raise RuntimeError("Codex never observed the desktop")
    notes = [e for e in events if e.get("kind") == "note"]
    available = {p.stem for p in (directory / "screenshots").glob("*.png")}
    for n in notes:
        validate_note(n)
        frame = n.get("frame", "")
        if Path(frame).name != frame or not (directory / "screenshots" / frame).is_file():
            raise ValueError("Note screenshot is missing")
        if any(not n.get(k, "").strip() for k in ("goal", "expected", "observed", "source")):
            raise ValueError("Note must describe expectation, observation and source")
        cited_frames(n["source"], available)
    cited_frames(report.read_text(), available)
    finals = [n for n in notes if n.get("category") == "session"]
    if len(finals) != 1 or notes[-1] != finals[0]:
        raise ValueError("Exactly one final session note must close the ledger")
    final = finals[0]
    if final["status"] == "time_window" and final.get("seconds_remaining", 999999) > 120:
        raise ValueError("Time window ended prematurely")
    if final["status"] == "action_limit" and final.get("actions_remaining", 999999) > 25:
        raise ValueError("Action limit outcome without exhausted/reserved action budget")
    turns = [n["turn"] for n in notes if n.get("turn") is not None]
    if not turns or turns != list(range(turns[-1] + 1)):
        raise ValueError("Turn checkpoints must start at zero and be consecutive")
    findings = [n for n in notes if n.get("category") == "finding"]
    ids = [n.get("finding_id") for n in findings]
    if any(not i for i in ids) or len(ids) != len(set(ids)):
        raise ValueError("Findings need unique IDs")
    coverage = [n for n in notes if n.get("category") == "coverage"]
    if not coverage:
        raise ValueError("Record encountered coverage and gaps")
    return {"observed_turns": turns[-1], "player_outcome": final["status"],
            "ending_verified": False, "review_status": "pending",
            "findings": findings, "coverage": coverage, "final_note": final}


def package_results(directory, outcome):
    """Retain raw evidence; package only reports, referenced images and game saves."""
    directory = Path(directory)
    events = events_in(directory)
    notes = [e for e in events if e.get("kind") == "note"]
    manifest_path = directory / "manifest.json"
    manifest = json.loads(manifest_path.read_text()) if manifest_path.exists() else {}
    report_path = directory / "report.md"
    report = report_path.read_text() if report_path.exists() else "No player report; inspect session.json."
    exports = directory / "saves"
    exports.mkdir(exist_ok=True)
    saves = []
    for source in sorted((directory / "profile/.akropia").rglob("*.akropia")):
        if not source.is_file() or source.is_symlink():
            continue
        with zipfile.ZipFile(source) as archive:
            if archive.testzip() is not None:
                raise ValueError("Corrupt saved game archive")
            metadata = json.loads(archive.read("metadata.json"))
        target = exports / ("save-%03d.akropia" % (len(saves) + 1))
        data = source.read_bytes()
        target.write_bytes(data)
        saves.append({"file": str(target.relative_to(directory)), "metadata": metadata,
                      "sha256": hashlib.sha256(data).hexdigest()})
    summary = {"manifest": manifest, "session": outcome, "saves": saves,
               "findings": [n for n in notes if n.get("category") == "finding"],
               "coverage": [n for n in notes if n.get("category") == "coverage"]}
    (directory / "review.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n")
    esc = html.escape
    parts = ['<!doctype html><html lang="cs"><meta charset="utf-8"><meta name="viewport" content="width=device-width"><title>Akropia UX</title>',
             '<style>body{font:17px/1.6 system-ui;max-width:1100px;margin:40px auto;padding:20px}img{max-width:100%}pre{white-space:pre-wrap;overflow-wrap:anywhere;font:inherit}details{border-top:1px solid #bbb;padding:12px 0}</style>',
             '<h1>Akropia UX — ' + esc(str(manifest.get("player", "unknown"))) + '</h1>',
             '<p>Automatický export; nezávislá kontrola nálezů a výsledku hry zatím neproběhla.</p>',
             '<p>Revize image: ' + esc(str(manifest.get("image_revision", "unknown"))) + '</p>',
             '<p>Stav relace: ' + esc(str(outcome.get("status"))) + '; výsledek podle hráče: ' + esc(str(outcome.get("player_outcome", "unknown"))) + '</p>',
             '<p><a href="review.json">Strukturovaný report a metadata</a> · <a href="session.json">Stav relace</a></p>',
             '<h2>Uložené hry</h2><p>Ve hře použijte Načíst hru → Importovat soubor. Kompatibilita s jinými revizemi není ověřena.</p><ul>']
    for save in saves:
        parts.append('<li><a href="' + save['file'] + '">' + esc(str(save['metadata'].get('saveName', save['file']))) + '</a></li>')
    parts.append('</ul><h2>Zpráva hráče</h2><pre>' + esc(report) + '</pre><h2>Nálezy, pokrytí a časová osa</h2>')
    selected = set()
    available = {p.stem for p in (directory / "screenshots").glob("*.png")}
    # Incomplete runs still get a report; retain valid references without masking the original failure.
    for text in [report] + [n.get('source', '') for n in notes]:
        for token in re.findall(r'[0-9a-f]{32,}', text):
            selected.update({token[i:i + 32] for i in range(len(token) - 31)} & available)
    for i, n in enumerate(notes):
        title = ' · '.join(str(n[k]) for k in ('finding_id', 'category', 'goal') if n.get(k))
        parts.append('<details id="note-' + str(i) + '"><summary>' + esc(title) + '</summary>')
        for k in ('time', 'turn', 'classification', 'severity', 'screen', 'status', 'expected', 'observed', 'finding', 'source'):
            if n.get(k) is not None:
                parts.append('<p><strong>' + k + ':</strong> ' + esc(str(n[k])) + '</p>')
        name = n.get('frame', '')
        if Path(name).stem in available and Path(name).name == name:
            selected.add(Path(name).stem)
            parts.append('<img loading="lazy" src="screenshots/' + esc(name, quote=True) + '" alt="' + esc(title, quote=True) + '">')
        parts.append('</details>')
    parts.append('<h2>Další citované snímky</h2><ul>')
    for stem in sorted(selected):
        parts.append('<li><a href="screenshots/' + stem + '.png">' + stem + '</a></li>')
    parts.append('</ul></html>')
    (directory / 'review.html').write_text(''.join(parts))
    (directory / 'README.txt').write_text('Rozbalte celý ZIP a otevřete review.html. Výsledky vyžadují kontrolu; nejsou důkazem dohratelnosti.\n')
    paths = [directory / p for p in ('review.html', 'review.json', 'session.json', 'README.txt', 'manifest.json', 'prompt.md', 'report.md', 'actions.jsonl') if (directory / p).is_file()]
    paths += [directory / 'screenshots' / (s + '.png') for s in sorted(selected)]
    paths += [directory / s['file'] for s in saves]
    with zipfile.ZipFile(directory / 'ux-results.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in paths:
            archive.write(path, str(path.relative_to(directory)))
    with zipfile.ZipFile(directory / 'ux-results.zip') as archive:
        if archive.testzip() is not None:
            raise ValueError("Result ZIP failed integrity check")
