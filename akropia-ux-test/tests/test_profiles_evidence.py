import contextlib
import hashlib
import io
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
import zipfile
from html.parser import HTMLParser

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from desktop import Desktop
from evidence import verify_play_evidence, package_results, validate_note
from profiles import PLAYERS, prompt_for
import run


class ProfilesEvidenceTest(unittest.TestCase):
    def setUp(self):
        temp = tempfile.TemporaryDirectory()
        self.addCleanup(temp.cleanup)
        self.root = Path(temp.name)
        (self.root / 'screenshots').mkdir()
        self.stem = 'a' * 32
        self.frame = self.stem + '.png'
        (self.root / 'screenshots' / self.frame).write_bytes(b'PNG test fixture')
        (self.root / 'report.md').write_text('Zpráva ' + self.stem)

    def note(self, **fields):
        return dict(kind='note', frame=self.frame, goal='Inspect', expected='UI',
                    observed='Observed state', source='load' + self.stem, **fields)

    def ledger(self, turns=0, outcome='time_window', remaining=0):
        notes = [{'kind': 'observe'}]
        notes += [self.note(category='progress', turn=t) for t in range(turns + 1)]
        notes += [self.note(category='coverage', screen='Map', status='visited'),
                  self.note(category='session', status=outcome, seconds_remaining=remaining,
                            actions_remaining=0)]
        self.write(notes)
        return notes

    def write(self, notes):
        (self.root / 'actions.jsonl').write_text('\n'.join(json.dumps(n) for n in notes))

    def test_profile_is_explicit_with_correct_defaults_and_override(self):
        base = ['play', '--container', 'owned', '--model', 'selected', '--reasoning', 'medium']
        with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
            run.parse(base)
        for player, seconds in PLAYERS.items():
            self.assertEqual(seconds, run.parse(base + ['--player', player]).seconds)
            self.assertEqual(60, run.parse(base + ['--player', player, '--seconds', '60']).seconds)
            prompt = prompt_for(player)
            self.assertIn('category="session"', prompt)
            self.assertNotIn('Turn 40/40', prompt)
        self.assertIn('Profile C', prompt_for('beginner'))
        self.assertNotIn('Profile A', prompt_for('beginner'))
        with self.assertRaises(ValueError):
            prompt_for('../player')

    def test_zero_turns_and_more_than_forty_are_valid_not_victory(self):
        for turns in (0, 3, 104):
            self.ledger(turns)
            result = verify_play_evidence(self.root)
            self.assertEqual(turns, result['observed_turns'])
            self.assertEqual('time_window', result['player_outcome'])
            self.assertFalse(result['ending_verified'])
        self.ledger(outcome='victory')
        self.assertFalse(verify_play_evidence(self.root)['ending_verified'])

    def test_missing_duplicate_or_out_of_order_turns_rejected(self):
        notes = self.ledger(3)
        for bad in (notes[:2] + notes[3:], notes[:2] + notes[1:], notes[:1] + list(reversed(notes[1:5])) + notes[5:]):
            self.write(bad)
            with self.assertRaisesRegex(ValueError, 'consecutive'):
                verify_play_evidence(self.root)

    def test_final_note_and_real_budget_exhaustion_required(self):
        notes = self.ledger(remaining=1000)
        with self.assertRaisesRegex(ValueError, 'prematurely'):
            verify_play_evidence(self.root)
        self.write(notes[:-1])
        with self.assertRaisesRegex(ValueError, 'final session'):
            verify_play_evidence(self.root)
        self.write(notes + [notes[-1]])
        with self.assertRaisesRegex(ValueError, 'final session'):
            verify_play_evidence(self.root)
        notes = self.ledger(outcome='action_limit')
        notes[-1]['actions_remaining'] = 26
        self.write(notes)
        with self.assertRaisesRegex(ValueError, 'Action limit'):
            verify_play_evidence(self.root)

    def test_missing_screenshots_and_report_citations_rejected(self):
        self.ledger()
        (self.root / 'report.md').write_text('Missing ' + 'b' * 32)
        with self.assertRaisesRegex(ValueError, 'Missing cited'):
            verify_play_evidence(self.root)
        (self.root / 'report.md').write_text('Report')
        (self.root / 'screenshots' / self.frame).unlink()
        with self.assertRaisesRegex(ValueError, 'screenshot is missing'):
            verify_play_evidence(self.root)

    def test_note_schema_and_finding_ids_survive_server_restart(self):
        desktop = Desktop(self.root)
        with patch.object(desktop, 'capture') as capture:
            with self.assertRaisesRegex(ValueError, 'Missing cited screenshot'):
                desktop.note('Goal', 'Expected', 'Observed', 'b' * 32,
                             'Finding', 'finding', 'positive', 'info')
            capture.assert_not_called()
        self.assertFalse((self.root / 'actions.jsonl').exists())
        for bad in (dict(category='other'), dict(category='finding'),
                    dict(category='progress', turn=True),
                    dict(category='coverage', screen='Map', status='victory')):
            with self.assertRaises(ValueError):
                validate_note(bad)
        for i in (1, 2):
            desktop = Desktop(self.root)
            with patch.object(desktop, 'capture', return_value=({'frame': self.frame}, b'')):
                result = desktop.note('Goal', 'Expected', 'Observed', self.stem,
                                      finding='Unclear', category='finding', classification='unclear', severity='medium')
            self.assertEqual(f'UX-{i:03}', result['finding_id'])

    def test_portable_report_escapes_text_preserves_save_and_excludes_auth(self):
        notes = self.ledger()
        notes[1]['observed'] = '<script>alert(1)</script>'
        self.write(notes)
        outcome = verify_play_evidence(self.root)
        outcome['status'] = 'completed'
        (self.root / 'session.json').write_text(json.dumps(outcome))
        secret = self.root / 'profile/.codex/auth.json'
        secret.parent.mkdir(parents=True)
        secret.write_text('SECRET')
        save = self.root / 'profile/.akropia/saves/original.akropia'
        save.parent.mkdir(parents=True)
        with zipfile.ZipFile(save, 'w') as z:
            z.writestr('metadata.json', json.dumps({'kind':'MANUAL','saveName':'Příliš žluťoučký'}))
            z.writestr('game.json', '{}')
        digest = hashlib.sha256(save.read_bytes()).hexdigest()
        package_results(self.root, outcome)
        page = (self.root / 'review.html').read_text()
        self.assertNotIn('<script>', page)
        self.assertIn('&lt;script&gt;', page)
        with zipfile.ZipFile(self.root / 'ux-results.zip') as z:
            self.assertIsNone(z.testzip())
            self.assertFalse(any('.codex' in name for name in z.namelist()))
            self.assertEqual(digest, hashlib.sha256(z.read('saves/save-001.akropia')).hexdigest())
            names = set(z.namelist())
        class Links(HTMLParser):
            def handle_starttag(inner, tag, attrs):
                for key, value in attrs:
                    if key in ('href', 'src'):
                        self.assertIn(value, names)
        Links().feed(page)

    def test_play_mounts_selected_prompt_and_records_its_hash(self):
        arguments = ['play', '--container', 'owned', '--image', 'sha256:tested',
                     '--player', 'beginner', '--model', 'selected', '--reasoning', 'medium']
        auth = self.root / 'auth.json'
        auth.write_text('secret')
        outputs = ['a' * 40, 'sha256:tested', json.dumps({
            'org.akropia.ux.protocol': '2', 'org.akropia.ux.revision': 'b' * 40,
            'org.akropia.ux.source-sha256': 'c' * 64}), json.dumps({'ExitCode': 0, 'OOMKilled': False})]
        with patch.object(run, 'ROOT', self.root), patch.object(run.signal, 'signal'), \
                patch.dict(run.os.environ, {'CODEX_HOME': str(self.root)}), \
                patch.object(run.subprocess, 'check_output', side_effect=outputs), \
                patch.object(run, 'command') as command, patch.object(run.subprocess, 'run'):
            self.assertEqual(0, run.main(arguments))
        manifest_path = next(self.root.glob('akropia-ux-test/runs/*/manifest.json'))
        manifest = json.loads(manifest_path.read_text())
        prompt = (manifest_path.parent / 'prompt.md').read_bytes()
        self.assertEqual('beginner', manifest['player'])
        self.assertEqual(hashlib.sha256(prompt).hexdigest(), manifest['prompt_sha256'])
        invocation = command.call_args_list[0].args
        self.assertIn(str(manifest_path.parent / 'prompt.md') + ':/task/player.md:ro', invocation)
        self.assertIn('UX_PLAYER=beginner', invocation)


if __name__ == '__main__':
    unittest.main()
