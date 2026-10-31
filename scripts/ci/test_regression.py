from contextlib import redirect_stdout
import io
import json
import os
import zipfile
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import regression


class RegressionTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.expected = {'example.Tests': ['works', 'breaks']}
        self.run = {'id': 42, 'head_sha': 'a' * 40, 'head_branch': 'main',
                    'html_url': 'https://github.com/jajir/microcol/actions/runs/42',
                    'created_at': '2026-10-30T02:17:00Z', 'status': 'completed'}

    def report(self, folder=None, outcome='', counts='', second='breaks'):
        folder = folder or self.root
        folder.mkdir(parents=True, exist_ok=True)
        (folder / 'TEST-example.Tests.xml').write_text(
            f'<testsuite name="example.Tests" tests="2" {counts}>'
            '<testcase classname="example.Tests" name="works"/>'
            f'<testcase classname="example.Tests" name="{second}">{outcome}</testcase>'
            '</testsuite>')

    def test_pass_fail_error_and_skip_counts(self):
        for tag, count, key, state in (('', '', 'passed', 'passed'),
                                      ('failure', 'failures', 'failed', 'failed'),
                                      ('error', 'errors', 'errors', 'failed'),
                                      ('skipped', 'skipped', 'skipped', 'incomplete')):
            with self.subTest(tag=tag):
                self.report(outcome=f'<{tag} message="detail"/>' if tag else '',
                            counts=f'{count}="1"' if count else '')
                group = regression.collect_group(self.root, self.expected)
                self.assertEqual(state, group['state'])
                self.assertEqual(2 if not tag else 1, group[key])
                self.assertEqual(2 if not tag else 1, group['passed'])

    def test_missing_malformed_wrong_counts_and_missing_methods_are_not_passes(self):
        self.assertEqual('incomplete', regression.collect_group(self.root, self.expected)['state'])
        for content in ('broken XML', '<testsuite name="example.Tests" tests="2"/>'):
            (self.root / 'TEST-example.Tests.xml').write_text(content)
            self.assertEqual('incomplete', regression.collect_group(self.root, self.expected)['state'])
        self.report(counts='failures="1"')
        self.assertEqual('incomplete', regression.collect_group(self.root, self.expected)['state'])
        self.report(second='unexpected')
        self.assertEqual('incomplete', regression.collect_group(self.root, self.expected)['state'])

    def test_empty_group_displays_unknown_counts_not_zero(self):
        group = regression.collect_group(self.root, self.expected)
        markdown, badges = regression.render({'groups': {'ui': group}}, self.run, 'a' * 40)
        self.assertIn('incomplete', badges['ui'])
        self.assertNotIn('0 pass', badges['ui'])
        self.assertIn('| Full UI / support | — | — | — | — |', markdown)

    def test_duplicate_report_never_passes(self):
        self.report()
        nested = self.root / 'duplicate'
        self.report(nested)
        self.assertEqual('incomplete', regression.collect_group(self.root, self.expected)['state'])

    def test_interrupted_xml_preserves_other_completed_failures(self):
        self.report(outcome='<failure message="actual failure"/>', counts='failures="1"')
        (self.root / 'TEST-aaa-interrupted.xml').write_text('<testsuite')
        result = regression.collect_group(self.root, self.expected)
        self.assertEqual((1, 1, 'failed'), (result['passed'], result['failed'], result['state']))
        self.assertIn('actual failure', result['problems'][0]['message'])
        self.assertIn('reason', result)

    def test_two_groups_and_process_failure(self):
        root = self.root / 'run-one'
        root.mkdir()
        (root / 'manifest.json').write_text(json.dumps({
            'source': {'revision': 'a' * 40}, 'scope': 'full',
            'expected': {'microcol-game': self.expected, 'microcol-test': self.expected}}))
        (root / 'summary.json').write_text(json.dumps({'status': 'passed'}))
        for folder in ('model', 'ui-01'):
            self.report(root / folder / 'run-export' / 'surefire-reports')
            (root / folder / 'result.json').write_text(json.dumps({'status': 'passed'}))
        summary = regression.summarize(self.root, 'a' * 40)
        self.assertEqual(2, summary['groups']['model']['passed'])
        self.assertEqual(2, summary['groups']['ui']['passed'])
        (root / 'ui-01/result.json').unlink()
        self.assertEqual('incomplete', regression.summarize(self.root, 'a' * 40)['groups']['ui']['state'])
        with self.assertRaises(ValueError):
            regression.summarize(self.root, 'b' * 40)
        (root / 'manifest.json').unlink()
        self.assertEqual({}, regression.summarize(self.root, 'a' * 40)['groups'])

    def test_failed_report_is_red_and_escaped_even_with_newer_main(self):
        self.report(outcome='<failure message="&lt;script&gt;bad&lt;/script&gt;"/>', counts='failures="1"')
        summary = {'groups': {'ui': regression.collect_group(self.root, self.expected)}}
        markdown, badges = regression.render(summary, self.run, 'b' * 40)
        self.assertIn('b42318', badges['ui'])
        self.assertIn('1 pass / 1 fail / 0 skip', badges['ui'])
        self.assertIn('older main', badges['ui'])
        self.assertIn('Newer main is not covered', markdown)
        self.assertIn('&lt;script&gt;', markdown)
        self.assertNotIn('<script>', markdown)
        self.assertIn('actions/runs/42', markdown)
        self.assertIn('unknown', badges['model'])
        self.assertNotIn('0 pass', badges['model'])

    def test_pass_is_green_only_for_complete_current_run(self):
        self.report()
        summary = {'groups': {'model': regression.collect_group(self.root, self.expected)}}
        _, badges = regression.render(summary, self.run, 'a' * 40)
        self.assertIn('1a7f37', badges['model'])
        for current, job in (('b' * 40, None), ('a' * 40, {'status': 'in_progress'}),
                             ('a' * 40, {'status': 'completed', 'conclusion': 'cancelled'})):
            _, badges = regression.render(summary, self.run, current, job)
            self.assertIn('9a6700', badges['model'])
            self.assertNotIn('1a7f37', badges['model'])

    def test_latest_attempt_skips_only_explicit_no_change_runs(self):
        latest = dict(self.run, id=44)
        failed = dict(self.run, id=43)
        calls = [
            {'workflow_runs': [latest, failed, self.run]},
            {'jobs': [{'name': regression.JOB, 'conclusion': 'skipped'}]},
            {'jobs': [{'name': regression.JOB, 'conclusion': 'failure'}]},
        ]
        with patch.object(regression, 'api', side_effect=calls):
            run, job = regression.latest_run('jajir/microcol')
        self.assertEqual(43, run['id'])
        self.assertEqual('failure', job['conclusion'])
        with patch.object(regression, 'api', side_effect=[{'workflow_runs': [latest]}, {'jobs': []}]):
            self.assertEqual(latest, regression.latest_run('jajir/microcol')[0])
        with patch.object(regression, 'api', return_value={'workflow_runs': [latest]}):
            self.assertEqual((None, None), regression.latest_run('jajir/microcol', 44))

    def test_missing_artifact_and_api_failure_publish_unknown(self):
        with patch.object(regression, 'api', return_value={'artifacts': []}):
            result = regression.read_summary('jajir/microcol', self.run)
        self.assertEqual({}, result['groups'])
        with patch.object(regression, 'api', side_effect=ValueError('unavailable')):
            regression.publish('jajir/microcol', 'a' * 40, self.root)
        self.assertIn('unknown', (self.root / 'test-results-model.svg').read_text())
        self.assertIn('unavailable', (self.root / 'report.md').read_text())

    def test_manual_run_is_unconditional_and_scheduled_same_revision_skips(self):
        output = self.root / 'outputs'
        environment = {'GITHUB_EVENT_NAME': 'workflow_dispatch', 'GITHUB_SHA': 'a' * 40,
                       'GITHUB_OUTPUT': str(output), 'GITHUB_RUN_ID': '45'}
        with patch.dict(os.environ, environment), patch.object(regression, 'latest_run') as latest:
            with redirect_stdout(io.StringIO()):
                regression.plan('jajir/microcol')
            latest.assert_not_called()
        self.assertEqual('game_required=true\n', output.read_text())
        environment['GITHUB_EVENT_NAME'] = 'schedule'
        with patch.dict(os.environ, environment), patch.object(regression, 'latest_run', return_value=(self.run, None)):
            with redirect_stdout(io.StringIO()):
                regression.plan('jajir/microcol')
        self.assertTrue(output.read_text().endswith('game_required=false\n'))

    def test_artifact_identity_counts_and_rerun_attempt_are_validated(self):
        summary = {'revision': 'a' * 40, 'attempt': 2, 'groups': {
            'model': {'state': 'passed', 'passed': 2, 'failed': 0, 'errors': 0, 'skipped': 0}}}
        run = dict(self.run, run_attempt=2)

        def read(value):
            archive = io.BytesIO()
            with zipfile.ZipFile(archive, 'w') as output:
                output.writestr('summary.json', json.dumps(value))
            responses = [{'artifacts': [
                {'id': 9, 'name': 'test-summary-1', 'expired': False},
                {'id': 10, 'name': 'test-summary-2', 'expired': False}]}, archive.getvalue()]
            with patch.object(regression, 'api', side_effect=responses) as api:
                result = regression.read_summary('jajir/microcol', run)
                self.assertIn('/10/zip', api.call_args.args[0])
                return result
        self.assertEqual(2, read(summary)['groups']['model']['passed'])
        with self.assertRaises(ValueError):
            read([])
        with self.assertRaises(ValueError):
            read(dict(summary, groups=[]))
        with self.assertRaises(ValueError):
            read(dict(summary, attempt=1))
        with self.assertRaises(ValueError):
            read(dict(summary, revision='b' * 40))
        for value in (-1, '2', True):
            summary['groups']['model']['passed'] = value
            with self.assertRaises(ValueError):
                read(summary)
        summary['groups']['model'].update(passed=2, failed=1)
        with self.assertRaises(ValueError):
            read(summary)

    def test_workflows_keep_test_and_publication_gates_wired(self):
        workflow = (regression.ROOT / '.github/workflows/dev-builds.yml').read_text()
        self.assertNotIn('  push:', workflow)
        self.assertIn("cron: '17 2 * * *'", workflow)
        self.assertIn('run: python3 scripts/ci/regression.py plan', workflow)
        self.assertIn('name: Full regression tests', workflow)
        self.assertIn('timeout-minutes: 270', workflow)
        self.assertIn('bash scripts/ui-tests/verify.sh --workers 1 --timeout 14400 --total-timeout 15300', workflow)
        self.assertIn('publish_builds:', workflow)
        self.assertIn('default: false', workflow)
        self.assertEqual(3, workflow.count("(github.event_name == 'schedule' || inputs.publish_builds)"))
        self.assertIn('needs: [changes, checks, native, functional]', workflow)
        self.assertIn('name: test-summary-${{ github.run_attempt }}', workflow)
        self.assertIn('name: tests-functional-${{ github.run_attempt }}', workflow)
        docs = (regression.ROOT / '.github/workflows/docs.yml').read_text()
        self.assertIn('workflows: [Full regression tests]', docs)
        self.assertIn('types: [requested, completed]', docs)
        self.assertIn('python scripts/ci/regression.py publish', docs)
        self.assertIn('run: cp target/test-status/test-results-*.svg target/docs-site/', docs)

    def test_nightly_compares_whole_change_range(self):
        def git(*args):
            return subprocess.check_output(['git', *args], cwd=self.root, stderr=subprocess.PIPE).decode().strip()
        git('init', '-q')
        git('config', 'user.email', 'test@example.invalid')
        git('config', 'user.name', 'Test')
        (self.root / 'game.java').write_text('old')
        git('add', '.')
        git('commit', '-qm', 'base')
        base = git('rev-parse', 'HEAD')
        (self.root / 'docs').mkdir()
        (self.root / 'docs/page.md').write_text('first')
        git('add', '.')
        git('commit', '-qm', 'docs')
        docs = git('rev-parse', 'HEAD')
        self.assertFalse(regression.needs_regression(base, docs, self.root))
        self.assertFalse(regression.needs_regression(base, base, self.root))
        (self.root / 'game.java').write_text('changed')
        git('add', '.')
        git('commit', '-qm', 'game')
        (self.root / 'docs/page.md').write_text('last')
        git('add', '.')
        git('commit', '-qm', 'last docs')
        head = git('rev-parse', 'HEAD')
        self.assertTrue(regression.needs_regression(base, head, self.root))
        self.assertTrue(regression.needs_regression(None, head, self.root))
        self.assertTrue(regression.needs_regression('f' * 40, head, self.root))


if __name__ == '__main__':
    unittest.main()
